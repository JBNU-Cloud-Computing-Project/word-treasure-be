package cloudcomputing.wordtreasure.api.game.response;

import cloudcomputing.wordtreasure.model.game.dto.CurrentGameInfo;
import cloudcomputing.wordtreasure.model.game.dto.GameProgressDto;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;

public record CurrentGameResponse(
        Long dailyWordId,
        LocalDate gameDate,
        String status,
        String remainingTime,
        String difficulty,
        boolean hasStarted,
        Long gameSessionId,
        @JsonInclude(JsonInclude.Include.NON_NULL)  // null이면 JSON에서 제외
        GameProgressDto progress  // 게임 시작 전이면 null
) {
    public static CurrentGameResponse from(CurrentGameInfo info) {
        return new CurrentGameResponse(
                info.dailyWordId(),
                info.gameDate(),
                info.status() != null ? info.status().name() : null,
                info.remainingTime(),
                info.difficulty().name(),
                info.hasStarted(),
                info.gameSessionId(),
                info.progress()
        );
    }
}
