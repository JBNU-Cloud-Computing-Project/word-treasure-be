package cloudcomputing.wordtreasure.model.game.service;

import cloudcomputing.wordtreasure.model.game.dto.AttemptResult;
import cloudcomputing.wordtreasure.model.game.dto.GameStartResult;
import cloudcomputing.wordtreasure.model.game.dto.HintResult;
import cloudcomputing.wordtreasure.model.game.entity.*;
import cloudcomputing.wordtreasure.model.game.repository.AttemptRepository;
import cloudcomputing.wordtreasure.model.game.repository.DailyWordRepository;
import cloudcomputing.wordtreasure.model.game.repository.ExtraHintRepository;
import cloudcomputing.wordtreasure.model.game.repository.GameSessionRepository;
import cloudcomputing.wordtreasure.model.member.entity.Member;
import cloudcomputing.wordtreasure.model.member.entity.MemberStatistics;
import cloudcomputing.wordtreasure.model.member.repositroy.MemberRepository;
import cloudcomputing.wordtreasure.model.member.repositroy.MemberStatisticsRepository;
import cloudcomputing.wordtreasure.model.token.entity.TransactionType;
import cloudcomputing.wordtreasure.model.token.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GamePlayService {

    private final MemberRepository memberRepository;
    private final DailyWordRepository dailyWordRepository;
    private final GameSessionRepository gameSessionRepository;
    private final AttemptRepository attemptRepository;
    private final GameConfigService gameConfigService;
    private final TokenService tokenService;
    private final SimilarityCalculator similarityCalculator;
    private final ExtraHintRepository extraHintRepository;
    private final RankingService rankingService;
    private final MemberStatisticsRepository memberStatisticsRepository;

    // 동시성 제어를 위한 Lock Map
    private final ConcurrentHashMap<String, Object> gameLocks = new ConcurrentHashMap<>();

    private static void setField(Object target, String fieldName, Object value) {
        try {
            var f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("필드 설정 실패: " + fieldName, e);
        }
    }

    /**
     * 게임 시작 - 동시성 제어
     */
    @Transactional
    public GameStartResult startGame(Long memberId, Long dailyWordId) {
        // 1. 회원별 + 단어별 고유 키 생성
        String lockKey = String.format("game_start_%d_%d", memberId, dailyWordId);

        // 2. 해당 키에 대한 Lock 객체 획득 (없으면 생성)
        Object lock = gameLocks.computeIfAbsent(lockKey, k -> new Object());

        // 3. synchronized 블록으로 동시성 제어
        synchronized (lock) {
            return startGameInternal(memberId, dailyWordId);
        }
    }

    /**
     * 실제 게임 시작 로직 (동기화 블록 내에서 실행)
     */
    private GameStartResult startGameInternal(Long memberId, Long dailyWordId) {
        log.info("게임 시작 요청 - memberId: {}, dailyWordId: {}", memberId, dailyWordId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        DailyWord dailyWord = dailyWordRepository.findById(dailyWordId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 일일 단어입니다."));

        if (!dailyWord.getGameDate().equals(LocalDate.now())) {
            throw new IllegalStateException("오늘의 단어가 아닙니다.");
        }

        // 기존 세션 조회 (synchronized가 보호하므로 일반 조회로 충분)
        var existing = gameSessionRepository.findByMemberIdAndDailyWordId(memberId, dailyWordId);

        if (existing.isPresent()) {
            GameSession s = existing.get();
            log.info("이미 게임 세션이 존재하여 기존 세션 반환 - gameSessionId: {}", s.getId());

            int maxAttempts = gameConfigService.getIntValue(GameConfigKey.MAX_ATTEMPTS);
            int attemptCost = gameConfigService.getIntValue(GameConfigKey.ATTEMPT_COST_TOKENS);
            int hintCost = gameConfigService.getIntValue(GameConfigKey.HINT_COST_TOKENS);

            return new GameStartResult(
                    s.getId(), dailyWordId, dailyWord.getGameDate(),
                    maxAttempts, attemptCost, hintCost, member.getCurrentTokens(), s.getStartedAt()
            );
        }

        // 새 세션 생성
        GameSession session = new GameSession(
                null, member, dailyWord, GameStatus.PLAYING,
                0, BigDecimal.ZERO, LocalDateTime.now(), null, null, 0, 0, null, null
        );

        GameSession savedSession = gameSessionRepository.save(session);
        dailyWord.incrementParticipants();

        log.info("게임 시작 완료 - gameSessionId: {}", savedSession.getId());

        int maxAttempts = gameConfigService.getIntValue(GameConfigKey.MAX_ATTEMPTS);
        int attemptCost = gameConfigService.getIntValue(GameConfigKey.ATTEMPT_COST_TOKENS);
        int hintCost = gameConfigService.getIntValue(GameConfigKey.HINT_COST_TOKENS);

        return new GameStartResult(
                savedSession.getId(), dailyWordId, dailyWord.getGameDate(),
                maxAttempts, attemptCost, hintCost, member.getCurrentTokens(), savedSession.getStartedAt()
        );
    }

    /**
     * 단어 시도 제출
     */
    @Transactional
    public AttemptResult submitAttempt(Long gameSessionId, String userInput) {
        log.info("시도 제출 - gameSessionId: {}, userInput: {}", gameSessionId, userInput);

        GameSession session = gameSessionRepository.findById(gameSessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게임 세션입니다."));

        if (session.getStatus() != GameStatus.PLAYING) {
            throw new IllegalStateException("이미 종료된 게임입니다.");
        }

        validateNotExpired(session);

        int maxAttempts = gameConfigService.getIntValue(GameConfigKey.MAX_ATTEMPTS);
        if (session.getAttemptCount() >= maxAttempts) {
            throw new IllegalStateException("최대 시도 횟수를 초과했습니다.");
        }

        int attemptCost = gameConfigService.getIntValue(GameConfigKey.ATTEMPT_COST_TOKENS);
        tokenService.deductTokens(
                session.getMember().getMemberId(), attemptCost,
                TransactionType.ATTEMPT_COST,
                String.format("%d번째 시도", session.getAttemptCount() + 1), session
        );

        String answer = session.getDailyWord().getWord();
        BigDecimal similarity = similarityCalculator.calculateSimilarity(userInput, answer);
        String hint = similarityCalculator.generateHint(userInput, answer, similarity);

        Attempt attempt = new Attempt(
                null, session, session.getAttemptCount() + 1,
                userInput, similarity, hint, attemptCost, LocalDateTime.now()
        );
        // 시도 저장
        Attempt savedAttempt = attemptRepository.save(attempt);

        // 세션 상태 갱신 (시도 수, 최고 유사도, 토큰 소비 합계)
        updateSession(session, similarity, attemptCost);

        // 실시간 순위 업데이트 (매 시도마다)
        try {
            rankingService.updateRanking(
                    session.getDailyWord().getId(),
                    session.getMember().getMemberId(),
                    session.getHighestSimilarity().doubleValue(),
                    session.getAttemptCount()
            );
        } catch (Exception e) {
            // 순위 업데이트 실패가 게임 진행을 막지 않도록
            log.warn("실시간 순위 업데이트 실패 - gameSessionId: {}", gameSessionId, e);
        }
        boolean isCorrect = similarity.compareTo(BigDecimal.valueOf(100)) == 0;

        if (isCorrect) {
            completeGame(session, true);
        } else if (session.getAttemptCount() >= maxAttempts) {
            completeGame(session, false);
        }

        Member member = memberRepository.findById(session.getMember().getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        log.info("시도 제출 완료 - attemptId: {}, attemptNumber: {}, similarity: {}, isCorrect: {}",
                savedAttempt.getId(), savedAttempt.getAttemptNumber(), similarity, isCorrect);

        return new AttemptResult(
                savedAttempt.getId(), savedAttempt.getAttemptNumber(), userInput, similarity,
                isCorrect, hint, attemptCost, member.getCurrentTokens(),
                session.getAttemptCount(), session.getHighestSimilarity(),
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

        GameSession session = gameSessionRepository.findById(gameSessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게임 세션입니다."));

        if (session.getStatus() != GameStatus.PLAYING) {
            throw new IllegalStateException("이미 종료된 게임입니다.");
        }

        validateNotExpired(session);

        int hintCost = gameConfigService.getIntValue(GameConfigKey.HINT_COST_TOKENS);

        tokenService.deductTokens(
                session.getMember().getMemberId(), hintCost,
                TransactionType.HINT_COST, "추가 힌트 요청", session
        );

        String hintText = generateExtraHint(session);

        ExtraHint extraHint = new ExtraHint(
                null, session, hintText, hintCost, LocalDateTime.now()
        );

        ExtraHint savedHint = extraHintRepository.save(extraHint);
        updateSessionTokensSpent(session, hintCost);

        Member member = memberRepository.findById(session.getMember().getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        log.info("힌트 요청 완료 - hintId: {}, hintText: {}", savedHint.getId(), hintText);

        return new HintResult(savedHint.getId(), hintText, hintCost, member.getCurrentTokens());
    }

    /**
     * 게임 세션 업데이트
     */
    private void updateSession(GameSession session, BigDecimal similarity, int tokensSpent) {
        try {
            var field = GameSession.class.getDeclaredField("attemptCount");
            field.setAccessible(true);
            field.set(session, session.getAttemptCount() + 1);

            if (session.getHighestSimilarity() == null ||
                    similarity.compareTo(session.getHighestSimilarity()) > 0) {
                var similarityField = GameSession.class.getDeclaredField("highestSimilarity");
                similarityField.setAccessible(true);
                similarityField.set(session, similarity);
            }

            var tokensSpentField = GameSession.class.getDeclaredField("tokensSpent");
            tokensSpentField.setAccessible(true);
            tokensSpentField.set(session, session.getTokensSpent() + tokensSpent);

        } catch (Exception e) {
            log.error("게임 세션 업데이트 실패", e);
            throw new RuntimeException("게임 세션 업데이트 중 오류 발생", e);
        }
    }

    private void updateSessionTokensSpent(GameSession session, int tokensSpent) {
        try {
            var tokensSpentField = GameSession.class.getDeclaredField("tokensSpent");
            tokensSpentField.setAccessible(true);
            tokensSpentField.set(session, session.getTokensSpent() + tokensSpent);
        } catch (Exception e) {
            log.error("토큰 소비 누적 실패", e);
            throw new RuntimeException("토큰 소비 누적 중 오류 발생", e);
        }
    }

    /**
     * 게임 완료 처리
     */
    private void completeGame(GameSession session, boolean isSuccess) {
        try {
            // 1. 게임 상태 업데이트
            var statusField = GameSession.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(session, isSuccess ? GameStatus.SUCCESS : GameStatus.FAIL);

            var completedAtField = GameSession.class.getDeclaredField("completedAt");
            completedAtField.setAccessible(true);
            completedAtField.set(session, LocalDateTime.now());

            if (isSuccess) {
                // 2-A. 성공 시: 최종 점수 계산 및 순위 확정
                session.getDailyWord().recordSuccess();

                double finalScore = rankingService.calculateRankingScore(
                        session.getHighestSimilarity().doubleValue(),
                        session.getAttemptCount()
                );

                //Redis에 최종 점수 등록 (100% 유사율로 재등록)
                rankingService.addToRanking(
                        session.getDailyWord().getId(),
                        session.getMember().getMemberId(),
                        finalScore
                );

                // 3. Redis에서 최종 순위 조회
                Integer rank = rankingService.getMemberRank(
                        session.getDailyWord().getId(),
                        session.getMember().getMemberId()
                );

                if (rank != null) {
                    var finalRankField = GameSession.class.getDeclaredField("finalRank");
                    finalRankField.setAccessible(true);
                    finalRankField.set(session, rank);
                }

                // 4. 보상 토큰 계산 및 지급
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

                log.info("게임 성공 완료 - gameSessionId: {}, rank: {}, reward: {}, finalScore: {}",
                        session.getId(), rank, reward, finalScore);

            } else {
                // 2-B. 실패 시: 최종 상태를 Redis에 반영
                session.getDailyWord().recordFailure();

                // 실패해도 현재 최고 유사율로 최종 점수 등록
                double finalScore = rankingService.calculateRankingScore(
                        session.getHighestSimilarity() != null
                                ? session.getHighestSimilarity().doubleValue()
                                : 0.0,
                        session.getAttemptCount()
                );

                // Redis에 최종 점수 등록
                rankingService.addToRanking(
                        session.getDailyWord().getId(),
                        session.getMember().getMemberId(),
                        finalScore
                );

                log.info("게임 실패 완료 - gameSessionId: {}, highestSimilarity: {}, attempts: {}, finalScore: {}",
                        session.getId(),
                        session.getHighestSimilarity(),
                        session.getAttemptCount(),
                        finalScore);
            }

            // 5. 회원 통계 업데이트 (성공/실패 공통)
            updateMemberStatistics(session, isSuccess);
        } catch (Exception e) {
            log.error("게임 완료 처리 실패", e);
            throw new RuntimeException("게임 완료 처리 중 오류 발생", e);
        }
    }

    /**
     * 회원 통계 갱신 (없으면 생성)
     */
    private void updateMemberStatistics(GameSession session, boolean isSuccess) {
        Long memberId = session.getMember().getMemberId();
        LocalDate today = session.getDailyWord().getGameDate();

        MemberStatistics stats = memberStatisticsRepository
                .findByMemberId(memberId)
                .orElseGet(() -> {
                    // 기본값으로 생성
                    // 주의: @MapsId 구조에서 신규 저장 시 id를 null로 두어 persist 경로를 타도록 해야 함
                    MemberStatistics created = new MemberStatistics(
                            null, // id는 null → persist
                            session.getMember(),
                            0, 0, 0,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            null, null,
                            0, 0,
                            null
                    );
                    return memberStatisticsRepository.save(created);
                });

        try {
            // 누적 게임 수
            int prevTotal = stats.getTotalGames();
            int newTotal = prevTotal + 1;

            // 성공/실패 누적
            int prevSuccess = stats.getSuccessfulGames();
            int prevFail = stats.getFailedGames();
            int newSuccess = prevSuccess + (isSuccess ? 1 : 0);
            int newFail = prevFail + (isSuccess ? 0 : 1);

            // 평균 시도 수(averageScore 필드를 평균 시도로 사용)
            int attempts = session.getAttemptCount();
            BigDecimal prevAvg = stats.getAverageScore() == null ? BigDecimal.ZERO : stats.getAverageScore();
            BigDecimal totalAttemptsSoFar = prevAvg.multiply(BigDecimal.valueOf(prevTotal));
            BigDecimal newAvg = totalAttemptsSoFar
                    .add(BigDecimal.valueOf(attempts))
                    .divide(BigDecimal.valueOf(newTotal), 2, RoundingMode.HALF_UP);

            // 성공률 (0~100)
            BigDecimal successRate = BigDecimal.valueOf(newSuccess)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(newTotal), 2, RoundingMode.HALF_UP);

            // 최고 순위(bestRank): 낮을수록 좋음
            Integer bestRank = stats.getBestRank();
            if (isSuccess && session.getFinalRank() != null) {
                bestRank = (bestRank == null) ? session.getFinalRank() : Math.min(bestRank, session.getFinalRank());
            }

            // 최단 시간(fastestSolveTimeSeconds)
            Long fastest = stats.getFastestSolveTimeSeconds();
            if (isSuccess && session.getCompletedAt() != null && session.getStartedAt() != null) {
                long sec = Duration.between(session.getStartedAt(), session.getCompletedAt()).getSeconds();
                fastest = (fastest == null) ? sec : Math.min(fastest, sec);
            }

            // 스트릭(성공 연속 일수로 간주)
            int currentStreak = stats.getCurrentStreak() == null ? 0 : stats.getCurrentStreak();
            int longestStreak = stats.getLongestStreak() == null ? 0 : stats.getLongestStreak();
            LocalDate lastPlayDate = stats.getLastPlayDate();
            if (isSuccess) {
                if (lastPlayDate != null && lastPlayDate.equals(today.minusDays(1))) {
                    currentStreak = currentStreak + 1;
                } else {
                    currentStreak = 1;
                }
                longestStreak = Math.max(longestStreak, currentStreak);
            } else {
                currentStreak = 0; // 실패 시 스트릭 끊김
            }

            // 리플렉션으로 필드 세팅 (엔티티에 setter가 없음)
            setField(stats, "totalGames", newTotal);
            setField(stats, "successfulGames", newSuccess);
            setField(stats, "failedGames", newFail);
            setField(stats, "successRate", successRate);
            setField(stats, "averageScore", newAvg);
            setField(stats, "bestRank", bestRank);
            setField(stats, "fastestSolveTimeSeconds", fastest);
            setField(stats, "longestStreak", longestStreak);
            setField(stats, "currentStreak", currentStreak);
            setField(stats, "lastPlayDate", today);

            memberStatisticsRepository.save(stats);
            log.info("회원 통계 갱신 완료 - memberId: {}, total: {}, success: {}, fail: {}, avgAttempts: {}, successRate: {}",
                    memberId, newTotal, newSuccess, newFail, newAvg, successRate);
        } catch (Exception e) {
            log.error("회원 통계 갱신 실패 - memberId: {}", memberId, e);
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
     */
    private String generateExtraHint(GameSession session) {
        String answer = session.getDailyWord().getWord();
        String description = session.getDailyWord().getDescription();
        int attemptCount = session.getAttemptCount();

        if (attemptCount < 3) {
            return String.format("이 단어는 '%s'에 관련된 단어입니다.",
                    description.substring(0, Math.min(10, description.length())));
        } else if (attemptCount < 6) {
            return String.format("정답은 총 %d글자입니다.", answer.length());
        } else {
            String firstChar = answer.substring(0, 1);
            String lastChar = answer.substring(answer.length() - 1);
            return String.format("첫 글자는 '%s'이고, 마지막 글자는 '%s'입니다.",
                    firstChar, lastChar);
        }
    }

    /**
     * 자정 기준 만료 체크
     */
    private void validateNotExpired(GameSession session) {
        LocalDate today = LocalDate.now();
        LocalDate sessionDate = session.getDailyWord().getGameDate();

        if (!today.equals(sessionDate)) {
            throw new IllegalStateException("게임이 만료되었습니다. (자정 이후에는 진행할 수 없습니다.)");
        }
    }
}