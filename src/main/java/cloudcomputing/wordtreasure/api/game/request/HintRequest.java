package cloudcomputing.wordtreasure.api.game.request;

import jakarta.validation.constraints.NotNull;

public record HintRequest(
        @NotNull(message = "게임 세션 ID는 필수입니다.")
        Long gameSessionId
) {
}
