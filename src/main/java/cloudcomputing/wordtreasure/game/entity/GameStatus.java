package cloudcomputing.wordtreasure.game.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GameStatus {
    PLAYING("진행 중"),
    SUCCESS("성공"),
    FAIL("실패");

    private final String description;

    public boolean isCompleted() {
        return this == SUCCESS || this == FAIL;
    }

    public boolean isSuccess() {
        return this == SUCCESS;
    }
}
