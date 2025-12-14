package cloudcomputing.wordtreasure.api.game.response;

import cloudcomputing.wordtreasure.model.game.dto.GameSessionDetail;

import java.math.BigDecimal;
import java.util.List;

public record GameSessionResponse(
        Long gameSessionId,
        String status,
        Integer attemptCount,
        BigDecimal highestSimilarity,
        Integer remainingAttempts,
        Integer currentTokens,
        List<AttemptDetailResponse> attempts,
        List<ExtraHintResponse> extraHints
) {
    public static GameSessionResponse from(GameSessionDetail detail) {
        return new GameSessionResponse(
                detail.gameSessionId(),
                detail.status().name(),
                detail.attemptCount(),
                detail.highestSimilarity(),
                detail.remainingAttempts(),
                detail.currentTokens(),
                detail.attempts().stream()
                        .map(AttemptDetailResponse::from)
                        .toList(),
                detail.extraHints().stream()
                        .map(ExtraHintResponse::from)
                        .toList()
        );
    }
}
