package cloudcomputing.wordtreasure.model.game.dto;

public record RankingEntry(
        Integer rank,
        Long memberId,
        Double score
) {
}
