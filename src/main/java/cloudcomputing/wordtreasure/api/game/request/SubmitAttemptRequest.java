package cloudcomputing.wordtreasure.api.game.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubmitAttemptRequest(
        @NotNull(message = "게임 세션 ID는 필수입니다.")
        Long gameSessionId,

        @NotBlank(message = "입력값은 필수입니다.")
        String userInput
) {
}
