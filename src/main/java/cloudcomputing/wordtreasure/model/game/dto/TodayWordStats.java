package cloudcomputing.wordtreasure.model.game.dto;

import java.math.BigDecimal;

public record TodayWordStats(
        Long dailyWordId,
        String word,
        String difficulty,
        Integer totalParticipants,
        Integer successfulParticipants,
        BigDecimal successRate,
        BigDecimal averageAttempts
) {
}
