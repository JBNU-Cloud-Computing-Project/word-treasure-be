package cloudcomputing.wordtreasure.api.member.response;

import cloudcomputing.wordtreasure.model.game.dto.RecentGameInfo;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecentGameResponse(
        LocalDate gameDate,
        String word,
        String status,
        Integer attemptCount,
        BigDecimal highestSimilarity
) {
    public static RecentGameResponse from(RecentGameInfo info) {
        return new RecentGameResponse(
                info.gameDate(),
                info.word(),
                info.status().name(),
                info.attemptCount(),
                info.highestSimilarity()
        );
    }
}
