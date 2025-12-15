package cloudcomputing.wordtreasure.model.game.dto;

import java.math.BigDecimal;

public record DifficultyStats(
        String difficulty,
        BigDecimal successRate,
        Integer totalGames,
        Integer successfulGames
) {
    public static DifficultyStats of(
            String difficulty,
            BigDecimal successRate,
            Integer totalGames,
            Integer successfulGames
    ) {
        return new DifficultyStats(difficulty, successRate, totalGames, successfulGames);
    }
}
