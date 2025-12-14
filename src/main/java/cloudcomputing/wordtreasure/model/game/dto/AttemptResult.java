package cloudcomputing.wordtreasure.model.game.dto;

import java.math.BigDecimal;

/**
 * 시도 결과
 */
public record AttemptResult(
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
}
