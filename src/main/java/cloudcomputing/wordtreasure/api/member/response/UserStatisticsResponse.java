package cloudcomputing.wordtreasure.api.member.response;

import cloudcomputing.wordtreasure.model.game.dto.UserStatisticsInfo;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UserStatisticsResponse(
        Integer totalGames,
        Integer successfulGames,
        Integer failedGames,
        BigDecimal successRate,
        BigDecimal averageAttempts,
        Integer bestRank,
        String fastestSolveTime,
        Integer longestStreak,
        Integer currentStreak,
        LocalDate lastPlayDate,
        Integer currentTokens
) {
    public static UserStatisticsResponse from(UserStatisticsInfo info) {
        // fastestSolveTimeSeconds를 HH:MM:SS 형식으로 변환
        String fastestTime = null;
        if (info.fastestSolveTimeSeconds() != null) {
            long seconds = info.fastestSolveTimeSeconds();
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            long secs = seconds % 60;
            fastestTime = String.format("%02d:%02d:%02d", hours, minutes, secs);
        }

        return new UserStatisticsResponse(
                info.totalGames(),
                info.successfulGames(),
                info.failedGames(),
                info.successRate(),
                info.averageAttempts(),
                info.bestRank(),
                fastestTime,
                info.longestStreak(),
                info.currentStreak(),
                info.lastPlayDate(),
                info.currentTokens()
        );
    }
}
