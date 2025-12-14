package cloudcomputing.wordtreasure.api.game.response;

import cloudcomputing.wordtreasure.model.game.dto.CurrentGameInfo;

import java.time.LocalDate;

public record CurrentGameResponse(
        Long dailyWordId,
        LocalDate gameDate,
        String status,
        String remainingTime,
        String difficulty,
        boolean hasStarted,
        Long gameSessionId
) {
    public static CurrentGameResponse from(CurrentGameInfo info) {
        return new CurrentGameResponse(
                info.dailyWordId(),
                info.gameDate(),
                info.status() != null ? info.status().name() : null,
                info.remainingTime(),
                info.difficulty().name(),
                info.hasStarted(),
                info.gameSessionId()
        );
    }
}
