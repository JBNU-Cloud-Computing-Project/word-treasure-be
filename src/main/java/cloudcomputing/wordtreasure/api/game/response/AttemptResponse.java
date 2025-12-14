package cloudcomputing.wordtreasure.api.game.response;

import cloudcomputing.wordtreasure.model.game.dto.AttemptResult;

import java.math.BigDecimal;

public record AttemptResponse(
        Long attemptId,
        Integer attemptNumber,
        String userInput,
        BigDecimal similarityScore,
        boolean isCorrect,
        String hint,
        Integer tokensSpent,
        Integer remainingTokens,
        Integer attemptCount,
        BigDecimal highestSimilarity,
        Integer rank,
        Integer tokensEarned,
        String completionTime
) {
    public static AttemptResponse from(AttemptResult result) {
        return new AttemptResponse(
                result.attemptId(),
                result.attemptNumber(),
                result.userInput(),
                result.similarityScore(),
                result.isCorrect(),
                result.hint(),
                result.tokensSpent(),
                result.remainingTokens(),
                result.attemptCount(),
                result.highestSimilarity(),
                result.rank(),
                result.tokensEarned(),
                result.completionTime()
        );
    }
}
