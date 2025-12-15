package cloudcomputing.wordtreasure.api.game.response;

import cloudcomputing.wordtreasure.model.game.dto.TodayWordStats;

import java.math.BigDecimal;

public record TodayWordStatsResponse(
        Long dailyWordId,
        String word,
        String difficulty,
        Integer totalParticipants,
        Integer successfulParticipants,
        BigDecimal successRate,
        BigDecimal averageAttempts
) {
    public static TodayWordStatsResponse from(TodayWordStats stats) {
        if (stats == null) {
            return null;
        }

        return new TodayWordStatsResponse(
                stats.dailyWordId(),
                stats.word(),
                stats.difficulty(),
                stats.totalParticipants(),
                stats.successfulParticipants(),
                stats.successRate(),
                stats.averageAttempts()
        );
    }
}
