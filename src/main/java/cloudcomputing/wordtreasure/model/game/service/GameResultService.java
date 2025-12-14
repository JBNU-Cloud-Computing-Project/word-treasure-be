package cloudcomputing.wordtreasure.model.game.service;

import cloudcomputing.wordtreasure.model.game.dto.DailyWordInfo;
import cloudcomputing.wordtreasure.model.game.dto.GameResult;
import cloudcomputing.wordtreasure.model.game.entity.Attempt;
import cloudcomputing.wordtreasure.model.game.entity.DailyWord;
import cloudcomputing.wordtreasure.model.game.entity.GameSession;
import cloudcomputing.wordtreasure.model.game.entity.GameStatus;
import cloudcomputing.wordtreasure.model.game.repository.AttemptRepository;
import cloudcomputing.wordtreasure.model.game.repository.GameSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameResultService {
    private final GameSessionRepository gameSessionRepository;
    private final AttemptRepository attemptRepository;

    /**
     * 게임 결과 조회
     *
     * @param gameSessionId 게임 세션 ID
     * @param memberId      요청 회원 ID (권한 확인용)
     * @return 게임 결과
     */
    public GameResult getGameResult(Long gameSessionId, Long memberId) {
        log.info("게임 결과 조회 - gameSessionId: {}, memberId: {}", gameSessionId, memberId);

        // 1. 게임 세션 조회
        GameSession session = gameSessionRepository.findById(gameSessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게임 세션입니다."));

        // 2. 권한 확인 (본인의 게임만 조회 가능)
        if (!session.getMember().getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("본인의 게임 결과만 조회할 수 있습니다.");
        }

        // 3. 게임 종료 확인
        if (session.getStatus() == GameStatus.PLAYING) {
            throw new IllegalStateException("아직 진행 중인 게임입니다.");
        }

        // 4. 시도 목록 조회
        List<Attempt> attempts = attemptRepository.findByGameSessionIdOrderByAttemptNumber(gameSessionId);

        // 5. 일일 단어 정보
        DailyWord dailyWord = session.getDailyWord();

        // 6. 성공 여부에 따라 다른 정보 구성
        if (session.getStatus() == GameStatus.SUCCESS) {
            return buildSuccessResult(session, dailyWord, attempts);
        } else {
            return buildFailResult(session, dailyWord, attempts);
        }
    }

    /**
     * 성공 결과 구성
     */
    private GameResult buildSuccessResult(
            GameSession session,
            DailyWord dailyWord,
            List<Attempt> attempts
    ) {
        // 완료 시간 계산
        String completionTime = calculateCompletionTime(session);

        // 시도별 정보
        List<AttemptInfo> attemptInfos = attempts.stream()
                .map(a -> new AttemptInfo(
                        a.getAttemptNumber(),
                        a.getUserInput(),
                        a.getSimilarityScore()
                ))
                .collect(Collectors.toList());

        return new GameResult(
                session.getId(),
                GameStatus.SUCCESS,
                new DailyWordInfo(
                        dailyWord.getWord(),
                        dailyWord.getDescription()
                ),
                session.getAttemptCount(),
                session.getFinalRank(),
                session.getHighestSimilarity(),
                completionTime,
                session.getTokensSpent(),
                session.getTokensEarned(),
                session.getTokensEarned() - session.getTokensSpent(),
                attemptInfos,
                null  // 성공 시 closestAttempts 불필요
        );
    }

    /**
     * 실패 결과 구성
     */
    private GameResult buildFailResult(
            GameSession session,
            DailyWord dailyWord,
            List<Attempt> attempts
    ) {
        // 시도별 정보
        List<AttemptInfo> attemptInfos = attempts.stream()
                .map(a -> new AttemptInfo(
                        a.getAttemptNumber(),
                        a.getUserInput(),
                        a.getSimilarityScore()
                ))
                .collect(Collectors.toList());

        // 가장 근접했던 시도 3개 (유사도 높은 순)
        List<AttemptInfo> closestAttempts = attempts.stream()
                .sorted(Comparator.comparing(Attempt::getSimilarityScore).reversed())
                .limit(3)
                .map(a -> new AttemptInfo(
                        a.getAttemptNumber(),
                        a.getUserInput(),
                        a.getSimilarityScore()
                ))
                .collect(Collectors.toList());

        return new GameResult(
                session.getId(),
                GameStatus.FAIL,
                new DailyWordInfo(
                        dailyWord.getWord(),
                        dailyWord.getDescription()
                ),
                session.getAttemptCount(),
                null,  // 실패 시 순위 없음
                session.getHighestSimilarity(),
                null,  // 실패 시 완료 시간 없음
                session.getTokensSpent(),
                session.getTokensEarned(),
                session.getTokensEarned() - session.getTokensSpent(),
                attemptInfos,
                closestAttempts
        );
    }

    /**
     * 완료 시간 계산 (HH:MM:SS)
     */
    private String calculateCompletionTime(GameSession session) {
        if (session.getCompletedAt() == null || session.getStartedAt() == null) {
            return null;
        }

        long seconds = Duration.between(
                session.getStartedAt(),
                session.getCompletedAt()
        ).getSeconds();

        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    /**
     * 시도 정보
     */
    public record AttemptInfo(
            Integer attemptNumber,
            String userInput,
            BigDecimal similarityScore
    ) {
    }
}
