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

import java.time.Duration;
import java.time.LocalDate;
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

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<GameSession> sessionPage = gameSessionRepository.findDailyLeaderboard(date, pageable);

        AtomicInteger rank = new AtomicInteger((page - 1) * size + 1);

        return sessionPage.getContent().stream()
                .map(session -> {
                    String completionTime = formatDuration(
                            Duration.between(session.getStartedAt(), session.getCompletedAt())
                    );

                    return LeaderboardEntry.forDaily(
                            rank.getAndIncrement(),
                            session.getMember().getNickName(),
                            session.getAttemptCount(),
                            completionTime,
                            100.0,  // 성공한 경우 항상 100%
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

        Optional<GameSession> sessionOpt = gameSessionRepository.findDailySessionByMemberId(memberId, date);

        if (sessionOpt.isEmpty()) {
            return null;  // 참여하지 않음
        }

        GameSession session = sessionOpt.get();

        // 내 순위 계산 - 나보다 앞선 사람 수 + 1
        long betterCount = gameSessionRepository.findDailyLeaderboard(date, Pageable.unpaged())
                .stream()
                .filter(gs ->
                        gs.getAttemptCount() < session.getAttemptCount() ||
                                (gs.getAttemptCount().equals(session.getAttemptCount()) &&
                                        gs.getCompletedAt().isBefore(session.getCompletedAt()))
                )
                .count();

        int myRank = (int) betterCount + 1;

        String completionTime = formatDuration(
                Duration.between(session.getStartedAt(), session.getCompletedAt())
        );

        return MyLeaderboardRanking.forDaily(
                myRank,
                session.getMember().getNickName(),
                session.getAttemptCount(),
                completionTime,
                100.0,
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

        // 내 순위 계산 - 나보다 토큰이 많은 사람 수 + 1
        long betterCount = gameSessionRepository.findAllTimeLeaderboard(Pageable.unpaged())
                .stream()
                .filter(proj -> proj.getTokensEarned() > stats.getTokensEarned())
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
            // 전체 기간
            return gameSessionRepository.countAllParticipants();
        } else if (startDate.equals(endDate)) {
            // 일간
            return gameSessionRepository.countDailySuccessful(startDate);
        } else {
            // 기간별
            return gameSessionRepository.countPeriodParticipants(startDate, endDate);
        }
    }

    // ========== Private Helper Methods ==========

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

        // 내 순위 계산 - 나보다 토큰이 많은 사람 수 + 1
        long betterCount = gameSessionRepository.findPeriodLeaderboard(startDate, endDate, Pageable.unpaged())
                .stream()
                .filter(proj -> proj.getTokensEarned() > stats.getTokensEarned())
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
