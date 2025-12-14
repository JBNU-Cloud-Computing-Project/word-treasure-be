package cloudcomputing.wordtreasure.api.game.request;

import jakarta.validation.constraints.NotNull;

public record GameStartRequest(
        @NotNull(message = "일일 단어 ID는 필수입니다.")
        Long dailyWordId
) {
}
