package cloudcomputing.wordtreasure.model.game.dto;

import cloudcomputing.wordtreasure.model.game.entity.Attempt;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AttemptInfo(
        Integer attemptNumber,
        String userInput,
        BigDecimal similarityScore,
        String hint,
        LocalDateTime createdAt
) {
    public static AttemptInfo from(Attempt attempt) {
        return new AttemptInfo(
                attempt.getAttemptNumber(),
                attempt.getUserInput(),
                attempt.getSimilarityScore(),
                attempt.getHintProvided(),
                attempt.getCreatedAt()
        );
    }
}
