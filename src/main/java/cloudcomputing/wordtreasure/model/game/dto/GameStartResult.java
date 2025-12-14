package cloudcomputing.wordtreasure.model.game.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record GameStartResult(
        Long gameSessionId,
        Long dailyWordId,
        LocalDate gameDate,
        Integer maxAttempts,
        Integer attemptCost,
        Integer hintCost,
        Integer currentTokens,
        LocalDateTime startedAt
) {
}
