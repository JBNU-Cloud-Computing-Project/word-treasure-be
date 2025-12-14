package cloudcomputing.wordtreasure.model.game.dto;

import java.time.LocalDate;

public record CurrentGameInfo(
        Long dailyWordId,
        LocalDate gameDate,
        cloudcomputing.wordtreasure.model.game.entity.GameStatus status,
        String remainingTime,
        cloudcomputing.wordtreasure.model.game.entity.Difficulty difficulty,
        boolean hasStarted,
        Long gameSessionId
) {
}
