package cloudcomputing.wordtreasure.model.game.service;

import cloudcomputing.wordtreasure.model.game.dto.AttemptResult;
import cloudcomputing.wordtreasure.model.game.dto.GameStartResult;
import cloudcomputing.wordtreasure.model.game.dto.HintResult;
import cloudcomputing.wordtreasure.model.game.entity.*;
import cloudcomputing.wordtreasure.model.game.repository.DailyWordRepository;
import cloudcomputing.wordtreasure.model.game.repository.GameSessionRepository;
import cloudcomputing.wordtreasure.model.member.entity.Member;
import cloudcomputing.wordtreasure.model.member.repositroy.MemberRepository;
import cloudcomputing.wordtreasure.model.token.entity.TransactionType;
import cloudcomputing.wordtreasure.model.token.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GamePlayService {

    private final MemberRepository memberRepository;
    private final DailyWordRepository dailyWordRepository;
    private final GameSessionRepository gameSessionRepository;
    private final GameConfigService gameConfigService;
    private final TokenService tokenService;
    private final SimilarityCalculator similarityCalculator;

    /**
     * 게임 시작
     */
    @Transactional
    public GameStartResult startGame(Long memberId, Long dailyWordId) {
        log.info("게임 시작 요청 - memberId: {}, dailyWordId: {}", memberId, dailyWordId);

        // 1. 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 2. 일일 단어 조회
        DailyWord dailyWord = dailyWordRepository.findById(dailyWordId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 일일 단어입니다."));

        // 3. 오늘 날짜가 맞는지 확인
        if (!dailyWord.getGameDate().equals(LocalDate.now())) {
            throw new IllegalStateException("오늘의 단어가 아닙니다.");
        }

        // 4. 이미 게임 세션이 있는지 확인
        if (gameSessionRepository.findByMemberIdAndDailyWordId(memberId, dailyWordId).isPresent()) {
            throw new IllegalStateException("이미 오늘의 게임에 참여했습니다.");
        }

        // 5. 게임 세션 생성
        GameSession session = new GameSession(
                null,
                member,
                dailyWord,
                GameStatus.PLAYING,
                0,
                BigDecimal.ZERO,
                LocalDateTime.now(),
                null,
                null,
                0,
                0,
                null,
                null
        );

        GameSession savedSession = gameSessionRepository.save(session);

        // 6. 일일 단어 참여자 수 증가
        dailyWord.incrementParticipants();

        log.info("게임 시작 완료 - gameSessionId: {}", savedSession.getId());

        // 7. 게임 설정 정보 조회
        int maxAttempts = gameConfigService.getIntValue(GameConfigKey.MAX_ATTEMPTS);
        int attemptCost = gameConfigService.getIntValue(GameConfigKey.ATTEMPT_COST_TOKENS);
        int hintCost = gameConfigService.getIntValue(GameConfigKey.HINT_COST_TOKENS);

        return new GameStartResult(
                savedSession.getId(),
                dailyWordId,
                dailyWord.getGameDate(),
                maxAttempts,
                attemptCost,
                hintCost,
                member.getCurrentTokens(),
                savedSession.getStartedAt()
        );
    }

    /**
     * 단어 시도 제출
     */
    @Transactional
    public AttemptResult submitAttempt(Long gameSessionId, String userInput) {
        log.info("시도 제출 - gameSessionId: {}, userInput: {}", gameSessionId, userInput);

        // 1. 게임 세션 조회
        GameSession session = gameSessionRepository.findById(gameSessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게임 세션입니다."));

        // 2. 게임 진행 중인지 확인
        if (session.getStatus() != GameStatus.PLAYING) {
            throw new IllegalStateException("이미 종료된 게임입니다.");
        }

        // 3. 최대 시도 횟수 확인
        int maxAttempts = gameConfigService.getIntValue(GameConfigKey.MAX_ATTEMPTS);
        if (session.getAttemptCount() >= maxAttempts) {
            throw new IllegalStateException("최대 시도 횟수를 초과했습니다.");
        }

        // 4. 토큰 차감
        int attemptCost = gameConfigService.getIntValue(GameConfigKey.ATTEMPT_COST_TOKENS);
        tokenService.deductTokens(
                session.getMember().getMemberId(),
                attemptCost,
                TransactionType.ATTEMPT_COST,
                String.format("%d번째 시도", session.getAttemptCount() + 1),
                session
        );

        // 5. 유사도 계산
        String answer = session.getDailyWord().getWord();
        BigDecimal similarity = similarityCalculator.calculateSimilarity(userInput, answer);
        String hint = similarityCalculator.generateHint(userInput, answer, similarity);

        // 6. 시도 기록 생성
        Attempt attempt = new Attempt(
                null,
                session,
                session.getAttemptCount() + 1,
                userInput,
                similarity,
                hint,
                attemptCost,
                LocalDateTime.now()
        );

        // 7. 세션 업데이트
        updateSession(session, similarity, attemptCost);

        // 8. 정답 여부 확인
        boolean isCorrect = similarity.compareTo(BigDecimal.valueOf(100)) == 0;

        if (isCorrect) {
            completeGame(session, true);
        } else if (session.getAttemptCount() >= maxAttempts) {
            completeGame(session, false);
        }

        // 9. 회원의 현재 토큰 조회
        Member member = memberRepository.findById(session.getMember().getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        log.info("시도 제출 완료 - attemptNumber: {}, similarity: {}, isCorrect: {}",
                attempt.getAttemptNumber(), similarity, isCorrect);

        return new AttemptResult(
                attempt.getId(),
                attempt.getAttemptNumber(),
                userInput,
                similarity,
                isCorrect,
                hint,
                attemptCost,
                member.getCurrentTokens(),
                session.getAttemptCount(),
                session.getHighestSimilarity(),
                isCorrect ? session.getFinalRank() : null,
                isCorrect ? session.getTokensEarned() : null,
                isCorrect ? calculateCompletionTime(session) : null
        );
    }

    /**
     * 추가 힌트 요청
     */
    @Transactional
    public HintResult requestHint(Long gameSessionId) {
        log.info("힌트 요청 - gameSessionId: {}", gameSessionId);

        // 1. 게임 세션 조회
        GameSession session = gameSessionRepository.findById(gameSessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게임 세션입니다."));

        // 2. 게임 진행 중인지 확인
        if (session.getStatus() != GameStatus.PLAYING) {
            throw new IllegalStateException("이미 종료된 게임입니다.");
        }

        // 3. 힌트 비용 조회
        int hintCost = gameConfigService.getIntValue(GameConfigKey.HINT_COST_TOKENS);

        // 4. 토큰 차감
        tokenService.deductTokens(
                session.getMember().getMemberId(),
                hintCost,
                TransactionType.HINT_COST,
                "추가 힌트 요청",
                session
        );

        // 5. 힌트 생성
        String hintText = generateExtraHint(session);

        // 6. 힌트 저장
        ExtraHint extraHint = new ExtraHint(
                null,
                session,
                hintText,
                hintCost,
                LocalDateTime.now()
        );

        ExtraHint savedHint = extraHintRepository.save(extraHint);

        // 7. 세션의 토큰 소비 누적
        updateSessionTokensSpent(session, hintCost);

        // 8. 회원의 현재 토큰 조회
        Member member = memberRepository.findById(session.getMember().getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        log.info("힌트 요청 완료 - hintId: {}, hintText: {}", savedHint.getId(), hintText);

        return new HintResult(
                savedHint.getId(),
                hintText,
                hintCost,
                member.getCurrentTokens()
        );
    }

    // ========== Private 메서드 ==========

    /**
     * 게임 세션 업데이트
     */
    private void updateSession(GameSession session, BigDecimal similarity, int tokensSpent) {
        // 시도 횟수 증가 - 리플렉션으로 직접 업데이트 (임시)
        try {
            var field = GameSession.class.getDeclaredField("attemptCount");
            field.setAccessible(true);
            field.set(session, session.getAttemptCount() + 1);

            // 최고 유사도 업데이트
            if (session.getHighestSimilarity() == null ||
                    similarity.compareTo(session.getHighestSimilarity()) > 0) {
                var similarityField = GameSession.class.getDeclaredField("highestSimilarity");
                similarityField.setAccessible(true);
                similarityField.set(session, similarity);
            }

            // 토큰 소비 누적
            var tokensSpentField = GameSession.class.getDeclaredField("tokensSpent");
            tokensSpentField.setAccessible(true);
            tokensSpentField.set(session, session.getTokensSpent() + tokensSpent);

        } catch (Exception e) {
            log.error("게임 세션 업데이트 실패", e);
            throw new RuntimeException("게임 세션 업데이트 중 오류 발생", e);
        }
    }

    /**
     * 게임 완료 처리
     */
    private void completeGame(GameSession session, boolean isSuccess) {
        try {
            // 상태 업데이트
            var statusField = GameSession.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(session, isSuccess ? GameStatus.SUCCESS : GameStatus.FAIL);

            // 완료 시간
            var completedAtField = GameSession.class.getDeclaredField("completedAt");
            completedAtField.setAccessible(true);
            completedAtField.set(session, LocalDateTime.now());

            if (isSuccess) {
                // 일일 단어 통계 업데이트
                session.getDailyWord().recordSuccess();

                // TODO: Redis에 순위 등록 (다음 단계에서 구현)
                // 일단 finalRank는 null로 유지

                // 보상 토큰 지급
                int reward = gameConfigService.getIntValue(GameConfigKey.RANK_DEFAULT_REWARD);
                tokenService.addTokens(
                        session.getMember().getMemberId(),
                        reward,
                        TransactionType.GAME_REWARD,
                        "게임 정답 보상",
                        session
                );

                var tokensEarnedField = GameSession.class.getDeclaredField("tokensEarned");
                tokensEarnedField.setAccessible(true);
                tokensEarnedField.set(session, reward);

            } else {
                // 실패 처리
                session.getDailyWord().recordFailure();
            }

            log.info("게임 완료 - gameSessionId: {}, status: {}",
                    session.getId(), session.getStatus());

        } catch (Exception e) {
            log.error("게임 완료 처리 실패", e);
            throw new RuntimeException("게임 완료 처리 중 오류 발생", e);
        }
    }

    /**
     * 완료 시간 계산 (HH:MM:SS)
     */
    private String calculateCompletionTime(GameSession session) {
        if (session.getCompletedAt() == null) {
            return null;
        }

        long seconds = java.time.Duration.between(
                session.getStartedAt(),
                session.getCompletedAt()
        ).getSeconds();

        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    /**
     * 추가 힌트 생성
     * <p>
     * 현재: 간단한 힌트 생성 로직
     * 향후: 더 정교한 힌트 생성 로직으로 교체 가능
     */
    private String generateExtraHint(GameSession session) {
        String answer = session.getDailyWord().getWord();
        String description = session.getDailyWord().getDescription();
        Difficulty difficulty = session.getDailyWord().getDifficulty();
        int attemptCount = session.getAttemptCount();

        // 시도 횟수에 따라 힌트 난이도 조절
        if (attemptCount < 3) {
            // 초반: 카테고리 힌트
            return String.format("이 단어는 '%s'에 관련된 단어입니다.",
                    description.substring(0, Math.min(10, description.length())));
        } else if (attemptCount < 6) {
            // 중반: 글자 수 힌트
            return String.format("정답은 총 %d글자입니다.", answer.length());
        } else {
            // 후반: 구체적인 힌트
            String firstChar = answer.substring(0, 1);
            String lastChar = answer.substring(answer.length() - 1);
            return String.format("첫 글자는 '%s'이고, 마지막 글자는 '%s'입니다.", firstChar, lastChar);
        }
    }
}
