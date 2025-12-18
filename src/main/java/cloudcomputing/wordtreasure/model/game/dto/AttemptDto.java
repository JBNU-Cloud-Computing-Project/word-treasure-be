package cloudcomputing.wordtreasure.model.game.dto;

import cloudcomputing.wordtreasure.model.game.entity.Attempt;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AttemptDto(
        Long attemptId,
        Integer attemptNumber,
        String userInput,
        BigDecimal similarityScore,
        String hint,
        Integer tokenCost,
        LocalDateTime createdAt
) {
    public static AttemptDto from(Attempt attempt) {
        return new AttemptDto(
                attempt.getId(),
                attempt.getAttemptNumber(),
                attempt.getUserInput(),
                attempt.getSimilarityScore(),
                attempt.getHintProvided(),
                attempt.getTokensSpent(),
                attempt.getCreatedAt()
        );
    }
}
