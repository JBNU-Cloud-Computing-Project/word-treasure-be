package cloudcomputing.wordtreasure.model.member.service;

import cloudcomputing.wordtreasure.model.game.entity.Attempt;
import cloudcomputing.wordtreasure.model.game.entity.GameSession;
import cloudcomputing.wordtreasure.model.game.entity.GameStatus;
import cloudcomputing.wordtreasure.model.game.repository.AttemptRepository;
import cloudcomputing.wordtreasure.model.game.repository.GameSessionRepository;
import cloudcomputing.wordtreasure.model.member.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BestRecordsService {

    private final GameSessionRepository gameSessionRepository;
    private final AttemptRepository attemptRepository;

    /**
     * 최고 기록 조회
     *
     * @param memberId 회원 ID
     * @return 최고 기록
     */
    public BestRecords getBestRecords(Long memberId) {
        log.info("최고 기록 조회 - memberId: {}", memberId);

        // 모든 성공한 게임 세션 조회
        List<GameSession> successSessions = gameSessionRepository
                .findByMemberIdAndStatus(memberId, GameStatus.SUCCESS);

        if (successSessions.isEmpty()) {
            // 성공한 게임이 없으면 null 반환
            return new BestRecords(null, null, null, null);
        }

        // 1. 최고 순위
        BestRankInfo bestRank = findBestRank(successSessions);

        // 2. 가장 빠른 정답
        FastestSolveInfo fastestSolve = findFastestSolve(successSessions);

        // 3. 최장 연속 기록
        LongestStreakInfo longestStreak = findLongestStreak(memberId);

        // 4. 첫 시도 최고 유사도
        HighestFirstTryInfo highestFirstTry = findHighestFirstTry(memberId);

        return new BestRecords(bestRank, fastestSolve, longestStreak, highestFirstTry);
    }

    /**
     * 최고 순위 찾기
     */
    private BestRankInfo findBestRank(List<GameSession> sessions) {
        return sessions.stream()
                .filter(s -> s.getFinalRank() != null)
                .min(Comparator.comparing(GameSession::getFinalRank))
                .map(s -> new BestRankInfo(
                        s.getFinalRank(),
                        s.getDailyWord().getGameDate(),
                        s.getDailyWord().getWord()
                ))
                .orElse(null);
    }

    /**
     * 가장 빠른 정답 찾기 (시도 횟수 기준)
     */
    private FastestSolveInfo findFastestSolve(List<GameSession> sessions) {
        return sessions.stream()
                .min(Comparator.comparing(GameSession::getAttemptCount))
                .map(s -> {
                    String time = calculateCompletionTime(s);
                    return new FastestSolveInfo(
                            time,
                            s.getDailyWord().getGameDate(),
                            s.getDailyWord().getWord(),
                            s.getAttemptCount()
                    );
                })
                .orElse(null);
    }

    /**
     * 최장 연속 기록 찾기
     */
    private LongestStreakInfo findLongestStreak(Long memberId) {
        List<GameSession> allSessions = gameSessionRepository
                .findByMemberIdOrderByStartedAt(memberId);

        if (allSessions.isEmpty()) {
            return null;
        }

        int maxStreak = 0;
        int currentStreak = 0;
        LocalDate streakStart = null;
        LocalDate streakEnd = null;
        LocalDate maxStreakStart = null;
        LocalDate maxStreakEnd = null;

        LocalDate previousDate = null;

        for (GameSession session : allSessions) {
            LocalDate currentDate = session.getStartedAt().toLocalDate();

            if (session.getStatus() == GameStatus.SUCCESS) {
                if (previousDate == null || currentDate.equals(previousDate.plusDays(1))) {
                    // 연속 기록 시작 또는 계속
                    if (currentStreak == 0) {
                        streakStart = currentDate;
                    }
                    currentStreak++;
                    streakEnd = currentDate;
                } else {
                    // 연속 끊김
                    if (currentStreak > maxStreak) {
                        maxStreak = currentStreak;
                        maxStreakStart = streakStart;
                        maxStreakEnd = streakEnd;
                    }
                    currentStreak = 1;
                    streakStart = currentDate;
                    streakEnd = currentDate;
                }
                previousDate = currentDate;
            } else {
                // 실패 시 연속 끊김
                if (currentStreak > maxStreak) {
                    maxStreak = currentStreak;
                    maxStreakStart = streakStart;
                    maxStreakEnd = streakEnd;
                }
                currentStreak = 0;
                previousDate = null;
            }
        }

        // 마지막 연속 기록 확인
        if (currentStreak > maxStreak) {
            maxStreak = currentStreak;
            maxStreakStart = streakStart;
            maxStreakEnd = streakEnd;
        }

        if (maxStreak == 0) {
            return null;
        }

        return new LongestStreakInfo(maxStreak, maxStreakStart, maxStreakEnd);
    }

    /**
     * 첫 시도 최고 유사도 찾기
     */
    private HighestFirstTryInfo findHighestFirstTry(Long memberId) {
        List<GameSession> allSessions = gameSessionRepository
                .findByMemberId(memberId);

        Optional<GameSession> bestSession = Optional.empty();
        BigDecimal maxSimilarity = BigDecimal.ZERO;

        for (GameSession session : allSessions) {
            // 첫 번째 시도 조회
            List<Attempt> firstAttempt = attemptRepository
                    .findByGameSessionIdAndAttemptNumber(session.getId(), 1);

            if (!firstAttempt.isEmpty()) {
                Attempt attempt = firstAttempt.get(0);
                if (attempt.getSimilarityScore().compareTo(maxSimilarity) > 0) {
                    maxSimilarity = attempt.getSimilarityScore();
                    bestSession = Optional.of(session);
                }
            }
        }

        return bestSession.map(s -> {
            Attempt firstAttempt = attemptRepository
                    .findByGameSessionIdAndAttemptNumber(s.getId(), 1).get(0);

            return new HighestFirstTryInfo(
                    firstAttempt.getSimilarityScore(),
                    s.getDailyWord().getGameDate(),
                    s.getDailyWord().getWord(),
                    firstAttempt.getUserInput()
            );
        }).orElse(null);
    }

    /**
     * 완료 시간 계산
     */
    private String calculateCompletionTime(GameSession session) {
        if (session.getCompletedAt() == null || session.getStartedAt() == null) {
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
}
