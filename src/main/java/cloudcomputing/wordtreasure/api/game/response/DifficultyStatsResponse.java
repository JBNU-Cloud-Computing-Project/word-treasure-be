package cloudcomputing.wordtreasure.api.game.response;

import cloudcomputing.wordtreasure.model.game.dto.AllDifficultyStats;
import cloudcomputing.wordtreasure.model.game.dto.DifficultyStats;

import java.math.BigDecimal;

public record DifficultyStatsResponse(
        DifficultyItem easy,
        DifficultyItem medium,
        DifficultyItem hard
) {
    public static DifficultyStatsResponse from(AllDifficultyStats stats) {
        if (stats == null) {
            return null;
        }

        return new DifficultyStatsResponse(
                DifficultyItem.from(stats.easy()),
                DifficultyItem.from(stats.medium()),
                DifficultyItem.from(stats.hard())
        );
    }

    public record DifficultyItem(
            String difficulty,
            BigDecimal successRate,
            Integer totalGames,
            Integer successfulGames
    ) {
        public static DifficultyItem from(DifficultyStats stats) {
            return new DifficultyItem(
                    stats.difficulty(),
                    stats.successRate(),
                    stats.totalGames(),
                    stats.successfulGames()
            );
        }
    }
}
