package cloudcomputing.wordtreasure.model.game.dto;

public record AllDifficultyStats(
        DifficultyStats easy,
        DifficultyStats medium,
        DifficultyStats hard
) {
    public static AllDifficultyStats of(
            DifficultyStats easy,
            DifficultyStats medium,
            DifficultyStats hard
    ) {
        return new AllDifficultyStats(easy, medium, hard);
    }
}
