package cloudcomputing.wordtreasure.model.game.service;

import cloudcomputing.wordtreasure.model.game.dto.LeaderboardEntry;
import cloudcomputing.wordtreasure.model.game.dto.MyLeaderboardRanking;
import cloudcomputing.wordtreasure.model.game.entity.GameSession;
import cloudcomputing.wordtreasure.model.game.repository.GameSessionRepository;
import cloudcomputing.wordtreasure.model.game.repository.LeaderboardProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaderboardService {

    private final GameSessionRepository gameSessionRepository;

    /**
     * 일간 리더보드 조회
     * 시도횟수 + 완료시간 순으로 정렬 (실시간)
     */
    public List<LeaderboardEntry> getDailyLeaderboard(LocalDate date, int page, int size) {
        log.info("일간 리더보드 조회 - date: {}, page: {}, size: {}", date, page, size);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<GameSession> sessionPage = gameSessionRepository
                .findDailyLeaderboard(startOfDay, endOfDay, pageable);

        AtomicInteger rank = new AtomicInteger((page - 1) * size + 1);

        return sessionPage.getContent().stream()
                .map(session -> {
                    String completionTime;
                    if (session.getCompletedAt() != null) {
                        completionTime = formatDuration(
                                Duration.between(session.getStartedAt(), session.getCompletedAt())
                        );
                    } else {
                        // 미완료자는 진행 시간 표시
                        completionTime = formatDuration(
                                Duration.between(session.getStartedAt(), session.getUpdatedAt())
                        );
                    }

                    // ✅ 최고 유사율 사용 (BigDecimal → double 변환)
                    double finalScore = session.getHighestSimilarity() != null
                            ? session.getHighestSimilarity().doubleValue()
                            : 0.0;

                    return LeaderboardEntry.forDaily(
                            rank.getAndIncrement(),
                            session.getMember().getNickName(),
                            session.getAttemptCount(),
                            completionTime,
                            finalScore,
                            session.getTokensEarned()
                    );
                })
                .toList();
    }

    /**
     * 주간 리더보드 조회 (캐싱)
     * 누적 토큰 기준으로 정렬
     */
    @Cacheable(value = "weeklyLeaderboard", key = "#startDate + '-' + #endDate + '-' + #page", unless = "#result.isEmpty()")
    public List<LeaderboardEntry> getWeeklyLeaderboard(LocalDate startDate, LocalDate endDate, int page, int size) {
        log.info("주간 리더보드 조회 - start: {}, end: {}, page: {}, size: {}", startDate, endDate, page, size);

        return getPeriodLeaderboard(startDate, endDate, page, size);
    }

    /**
     * 월간 리더보드 조회 (캐싱)
     */
    @Cacheable(value = "monthlyLeaderboard", key = "#year + '-' + #month + '-' + #page", unless = "#result.isEmpty()")
    public List<LeaderboardEntry> getMonthlyLeaderboard(int year, int month, int page, int size) {
        log.info("월간 리더보드 조회 - year: {}, month: {}, page: {}, size: {}", year, month, page, size);

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        return getPeriodLeaderboard(startDate, endDate, page, size);
    }

    /**
     * 전체 기간 리더보드 조회 (캐싱)
     */
    @Cacheable(value = "allTimeLeaderboard", key = "#page", unless = "#result.isEmpty()")
    public List<LeaderboardEntry> getAllTimeLeaderboard(int page, int size) {
        log.info("전체 리더보드 조회 - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<LeaderboardProjection> projectionPage = gameSessionRepository.findAllTimeLeaderboard(pageable);

        AtomicInteger rank = new AtomicInteger((page - 1) * size + 1);

        return projectionPage.getContent().stream()
                .map(proj -> {
                    String avgTime = proj.getAvgCompletionSeconds() != null
                            ? formatDuration(Duration.ofSeconds(proj.getAvgCompletionSeconds().longValue()))
                            : "00:00:00";

                    double avgScore = proj.getSuccessfulGames() > 0
                            ? (proj.getSuccessfulGames() * 100.0 / proj.getTotalGames())
                            : 0.0;

                    return LeaderboardEntry.forPeriod(
                            rank.getAndIncrement(),
                            proj.getNickname(),
                            proj.getTotalGames().intValue(),
                            proj.getSuccessfulGames().intValue(),
                            avgTime,
                            avgScore,
                            proj.getTokensEarned().intValue()
                    );
                })
                .toList();
    }

    /**
     * 내 일간 순위 조회
     */
    public MyLeaderboardRanking getMyDailyRanking(LocalDate date, Long memberId) {
        log.info("내 일간 순위 조회 - memberId: {}, date: {}", memberId, date);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        Optional<GameSession> sessionOpt = gameSessionRepository
                .findDailySessionByMemberId(memberId, startOfDay, endOfDay);

        if (sessionOpt.isEmpty()) {
            return null;  // 참여하지 않음
        }

        GameSession session = sessionOpt.get();

        long betterCount = gameSessionRepository.findDailyLeaderboard(startOfDay, endOfDay, Pageable.unpaged())
                .stream()
                .filter(gs -> {
                    BigDecimal mySimil = session.getHighestSimilarity();
                    BigDecimal otherSimil = gs.getHighestSimilarity();

                    // null 처리
                    if (mySimil == null) mySimil = BigDecimal.ZERO;
                    if (otherSimil == null) otherSimil = BigDecimal.ZERO;

                    // 1순위: 유사율 비교
                    int similCompare = otherSimil.compareTo(mySimil);
                    if (similCompare != 0) {
                        return similCompare > 0;  // 상대방 유사율이 더 높으면 나보다 앞섬
                    }

                    // 2순위: 시도횟수 비교 (적을수록 좋음)
                    int attemptCompare = Integer.compare(gs.getAttemptCount(), session.getAttemptCount());
                    if (attemptCompare != 0) {
                        return attemptCompare < 0;  // 상대방 시도횟수가 적으면 나보다 앞섬
                    }

                    // 3순위: 완료시간 비교
                    LocalDateTime myTime = session.getCompletedAt() != null
                            ? session.getCompletedAt()
                            : session.getUpdatedAt();
                    LocalDateTime otherTime = gs.getCompletedAt() != null
                            ? gs.getCompletedAt()
                            : gs.getUpdatedAt();

                    return otherTime.isBefore(myTime);  // 상대방이 더 빠르면 나보다 앞섬
                })
                .count();

        int myRank = (int) betterCount + 1;

        String completionTime;
        if (session.getCompletedAt() != null) {
            completionTime = formatDuration(
                    Duration.between(session.getStartedAt(), session.getCompletedAt())
            );
        } else {
            completionTime = formatDuration(
                    Duration.between(session.getStartedAt(), session.getUpdatedAt())
            );
        }

        double finalScore = session.getHighestSimilarity() != null
                ? session.getHighestSimilarity().doubleValue()
                : 0.0;

        return MyLeaderboardRanking.forDaily(
                myRank,
                session.getMember().getNickName(),
                session.getAttemptCount(),
                completionTime,
                finalScore,
                session.getTokensEarned()
        );
    }

    /**
     * 내 주간 순위 조회
     */
    public MyLeaderboardRanking getMyWeeklyRanking(LocalDate startDate, LocalDate endDate, Long memberId) {
        log.info("내 주간 순위 조회 - memberId: {}, start: {}, end: {}", memberId, startDate, endDate);

        return getMyPeriodRanking(startDate, endDate, memberId);
    }

    /**
     * 내 월간 순위 조회
     */
    public MyLeaderboardRanking getMyMonthlyRanking(int year, int month, Long memberId) {
        log.info("내 월간 순위 조회 - memberId: {}, year: {}, month: {}", memberId, year, month);

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        return getMyPeriodRanking(startDate, endDate, memberId);
    }

    /**
     * 내 전체 순위 조회
     */
    public MyLeaderboardRanking getMyAllTimeRanking(Long memberId) {
        log.info("내 전체 순위 조회 - memberId: {}", memberId);

        Optional<LeaderboardProjection> statsOpt = gameSessionRepository.findAllTimeStatsByMemberId(memberId);

        if (statsOpt.isEmpty()) {
            return null;  // 참여 기록 없음
        }

        LeaderboardProjection stats = statsOpt.get();

        // 내 순위 계산
        long betterCount = gameSessionRepository.findAllTimeLeaderboard(Pageable.unpaged())
                .stream()
                .filter(proj -> {
                    // 1순위: 토큰 비교
                    int tokenCompare = Long.compare(proj.getTokensEarned(), stats.getTokensEarned());
                    if (tokenCompare != 0) {
                        return tokenCompare > 0;
                    }

                    // 2순위: 총 시도횟수 비교
                    int attemptCompare = Long.compare(proj.getTotalAttempts(), stats.getTotalAttempts());
                    if (attemptCompare != 0) {
                        return attemptCompare < 0;
                    }

                    // 3순위: 평균 완료시간 비교
                    Double myAvg = stats.getAvgCompletionSeconds();
                    Double otherAvg = proj.getAvgCompletionSeconds();

                    if (myAvg == null) return true;
                    if (otherAvg == null) return false;

                    return otherAvg < myAvg;
                })
                .count();

        int myRank = (int) betterCount + 1;

        String avgTime = stats.getAvgCompletionSeconds() != null
                ? formatDuration(Duration.ofSeconds(stats.getAvgCompletionSeconds().longValue()))
                : "00:00:00";

        double avgScore = stats.getSuccessfulGames() > 0
                ? (stats.getSuccessfulGames() * 100.0 / stats.getTotalGames())
                : 0.0;

        return MyLeaderboardRanking.forPeriod(
                myRank,
                stats.getNickname(),
                stats.getTotalGames().intValue(),
                stats.getSuccessfulGames().intValue(),
                avgTime,
                avgScore,
                stats.getTokensEarned().intValue()
        );
    }

    /**
     * 전체 참여자 수 조회 (기간별)
     */
    public long getTotalParticipants(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return gameSessionRepository.countAllParticipants();
        } else if (startDate.equals(endDate)) {
            LocalDateTime startOfDay = startDate.atStartOfDay();
            LocalDateTime endOfDay = startDate.plusDays(1).atStartOfDay();
            return gameSessionRepository.countDailyParticipants(startOfDay, endOfDay);
        } else {
            // 기간별 (그대로 유지)
            return gameSessionRepository.countPeriodParticipants(startDate, endDate);
        }
    }

    /**
     * 기간별 리더보드 조회 (공통 로직)
     */
    private List<LeaderboardEntry> getPeriodLeaderboard(LocalDate startDate, LocalDate endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<LeaderboardProjection> projectionPage = gameSessionRepository.findPeriodLeaderboard(
                startDate, endDate, pageable
        );

        AtomicInteger rank = new AtomicInteger((page - 1) * size + 1);

        return projectionPage.getContent().stream()
                .map(proj -> {
                    String avgTime = proj.getAvgCompletionSeconds() != null
                            ? formatDuration(Duration.ofSeconds(proj.getAvgCompletionSeconds().longValue()))
                            : "00:00:00";

                    double avgScore = proj.getSuccessfulGames() > 0
                            ? (proj.getSuccessfulGames() * 100.0 / proj.getTotalGames())
                            : 0.0;

                    return LeaderboardEntry.forPeriod(
                            rank.getAndIncrement(),
                            proj.getNickname(),
                            proj.getTotalGames().intValue(),
                            proj.getSuccessfulGames().intValue(),
                            avgTime,
                            avgScore,
                            proj.getTokensEarned().intValue()
                    );
                })
                .toList();
    }

    /**
     * 내 기간별 순위 조회 (공통 로직)
     */
    private MyLeaderboardRanking getMyPeriodRanking(LocalDate startDate, LocalDate endDate, Long memberId) {
        Optional<LeaderboardProjection> statsOpt = gameSessionRepository.findPeriodStatsByMemberId(
                memberId, startDate, endDate
        );

        if (statsOpt.isEmpty()) {
            return null;  // 해당 기간 참여 없음
        }

        LeaderboardProjection stats = statsOpt.get();

        // 내 순위 계산
        long betterCount = gameSessionRepository.findPeriodLeaderboard(startDate, endDate, Pageable.unpaged())
                .stream()
                .filter(proj -> {
                    // 1순위: 토큰 비교 (많을수록 좋음)
                    int tokenCompare = Long.compare(proj.getTokensEarned(), stats.getTokensEarned());
                    if (tokenCompare != 0) {
                        return tokenCompare > 0;  // 상대방 토큰이 더 많으면 나보다 앞섬
                    }

                    // 2순위: 총 시도횟수 비교 (적을수록 좋음)
                    int attemptCompare = Long.compare(proj.getTotalAttempts(), stats.getTotalAttempts());
                    if (attemptCompare != 0) {
                        return attemptCompare < 0;  // 상대방 시도가 적으면 나보다 앞섬
                    }

                    // 3순위: 평균 완료시간 비교 (빠를수록 좋음)
                    Double myAvg = stats.getAvgCompletionSeconds();
                    Double otherAvg = proj.getAvgCompletionSeconds();

                    if (myAvg == null) return true;  // 내가 성공 기록 없으면 뒤로
                    if (otherAvg == null) return false;  // 상대방이 성공 기록 없으면 내가 앞섬

                    return otherAvg < myAvg;  // 상대방이 더 빠르면 나보다 앞섬
                })
                .count();

        int myRank = (int) betterCount + 1;

        String avgTime = stats.getAvgCompletionSeconds() != null
                ? formatDuration(Duration.ofSeconds(stats.getAvgCompletionSeconds().longValue()))
                : "00:00:00";

        double avgScore = stats.getSuccessfulGames() > 0
                ? (stats.getSuccessfulGames() * 100.0 / stats.getTotalGames())
                : 0.0;

        return MyLeaderboardRanking.forPeriod(
                myRank,
                stats.getNickname(),
                stats.getTotalGames().intValue(),
                stats.getSuccessfulGames().intValue(),
                avgTime,
                avgScore,
                stats.getTokensEarned().intValue()
        );
    }

    /**
     * Duration을 HH:MM:SS 형식으로 변환
     */
    private String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
}
