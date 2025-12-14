package cloudcomputing.wordtreasure.model.game.dto;

public record HintResult(
        Long hintId,
        String hintText,
        Integer tokensSpent,
        Integer remainingTokens
) {
}
