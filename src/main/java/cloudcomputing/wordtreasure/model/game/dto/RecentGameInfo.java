package cloudcomputing.wordtreasure.model.game.dto;

import cloudcomputing.wordtreasure.model.game.entity.GameSession;

import java.time.LocalDate;

public record RecentGameInfo(
        LocalDate gameDate,
        String word,
        cloudcomputing.wordtreasure.model.game.entity.GameStatus status,
        Integer attemptCount,
        java.math.BigDecimal highestSimilarity
) {
    public static RecentGameInfo from(GameSession session) {
        return new RecentGameInfo(
                session.getDailyWord().getGameDate(),
                session.getDailyWord().getWord(),
                session.getStatus(),
                session.getAttemptCount(),
                session.getHighestSimilarity()
        );
    }
}
