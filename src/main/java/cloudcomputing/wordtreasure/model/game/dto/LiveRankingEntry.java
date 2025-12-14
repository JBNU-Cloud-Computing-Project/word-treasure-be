package cloudcomputing.wordtreasure.model.game.dto;

public record LiveRankingEntry(
        Integer rank,
        String nickname,
        Integer attemptCount,
        String completionTime
) {
}
