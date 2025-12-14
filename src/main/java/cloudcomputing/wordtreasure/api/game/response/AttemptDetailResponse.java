package cloudcomputing.wordtreasure.api.game.response;

import cloudcomputing.wordtreasure.model.game.dto.AttemptInfo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AttemptDetailResponse(
        Integer attemptNumber,
        String userInput,
        BigDecimal similarityScore,
        String hint,
        LocalDateTime createdAt
) {
    public static AttemptDetailResponse from(AttemptInfo info) {
        return new AttemptDetailResponse(
                info.attemptNumber(),
                info.userInput(),
                info.similarityScore(),
                info.hint(),
                info.createdAt()
        );
    }
}
