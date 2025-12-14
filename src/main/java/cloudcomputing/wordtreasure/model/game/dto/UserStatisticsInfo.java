package cloudcomputing.wordtreasure.model.game.dto;

import cloudcomputing.wordtreasure.model.member.entity.MemberStatistics;

import java.time.LocalDate;

public record UserStatisticsInfo(
        Integer totalGames,
        Integer successfulGames,
        Integer failedGames,
        java.math.BigDecimal successRate,
        java.math.BigDecimal averageAttempts,
        Integer bestRank,
        Long fastestSolveTimeSeconds,
        Integer longestStreak,
        Integer currentStreak,
        LocalDate lastPlayDate,
        Integer currentTokens
) {
    public static UserStatisticsInfo from(MemberStatistics stats, Integer currentTokens) {
        return new UserStatisticsInfo(
                stats.getTotalGames(),
                stats.getSuccessfulGames(),
                stats.getFailedGames(),
                stats.getSuccessRate(),
                stats.getAverageScore(),
                stats.getBestRank(),
                stats.getFastestSolveTimeSeconds(),
                stats.getLongestStreak(),
                stats.getCurrentStreak(),
                stats.getLastPlayDate(),
                currentTokens
        );
    }
}
