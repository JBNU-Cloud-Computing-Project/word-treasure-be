package cloudcomputing.wordtreasure.model.game.dto;

import cloudcomputing.wordtreasure.model.game.entity.GameStatus;

import java.math.BigDecimal;
import java.util.List;

public record GameSessionDetail(
        Long gameSessionId,
        GameStatus status,
        Integer attemptCount,
        BigDecimal highestSimilarity,
        Integer remainingAttempts,
        Integer currentTokens,
        List<AttemptInfo> attempts,
        List<ExtraHintInfo> extraHints
) {
}
