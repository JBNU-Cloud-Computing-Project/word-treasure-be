package cloudcomputing.wordtreasure.model.game.dto;

import cloudcomputing.wordtreasure.model.game.entity.ExtraHint;

import java.time.LocalDateTime;

public record ExtraHintInfo(
        String hintText,
        Integer tokensSpent,
        LocalDateTime createdAt
) {
    public static ExtraHintInfo from(ExtraHint hint) {
        return new ExtraHintInfo(
                hint.getHintText(),
                hint.getTokensSpent(),
                hint.getCreatedAt()
        );
    }
}
