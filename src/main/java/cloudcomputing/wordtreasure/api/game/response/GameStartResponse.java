package cloudcomputing.wordtreasure.api.game.response;

import cloudcomputing.wordtreasure.model.game.dto.GameStartResult;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record GameStartResponse(
        Long gameSessionId,
        Long dailyWordId,
        LocalDate gameDate,
        Integer maxAttempts,
        Integer attemptCost,
        Integer hintCost,
        Integer currentTokens,
        LocalDateTime startedAt
) {
    public static GameStartResponse from(GameStartResult result) {
        return new GameStartResponse(
                result.gameSessionId(),
                result.dailyWordId(),
                result.gameDate(),
                result.maxAttempts(),
                result.attemptCost(),
                result.hintCost(),
                result.currentTokens(),
                result.startedAt()
        );
    }
}
