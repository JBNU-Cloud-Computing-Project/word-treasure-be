package cloudcomputing.wordtreasure.api.game.response;

import cloudcomputing.wordtreasure.model.game.dto.ExtraHintInfo;

import java.time.LocalDateTime;

record ExtraHintResponse(
        String hintText,
        Integer tokensSpent,
        LocalDateTime createdAt
) {
    public static ExtraHintResponse from(ExtraHintInfo info) {
        return new ExtraHintResponse(
                info.hintText(),
                info.tokensSpent(),
                info.createdAt()
        );
    }
}