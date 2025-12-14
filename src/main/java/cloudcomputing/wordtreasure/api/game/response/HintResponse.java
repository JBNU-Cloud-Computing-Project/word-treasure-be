package cloudcomputing.wordtreasure.api.game.response;

import cloudcomputing.wordtreasure.model.game.dto.HintResult;

public record HintResponse(
        Long hintId,
        String hintText,
        Integer tokensSpent,
        Integer remainingTokens
) {
    public static HintResponse from(HintResult result) {
        return new HintResponse(
                result.hintId(),
                result.hintText(),
                result.tokensSpent(),
                result.remainingTokens()
        );
    }
}
