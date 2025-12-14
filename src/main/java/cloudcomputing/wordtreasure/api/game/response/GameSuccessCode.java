package cloudcomputing.wordtreasure.api.game.response;

import cloudcomputing.wordtreasure.common.controller.response.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum GameSuccessCode implements SuccessCode {
    CURRENT_GAME_INFO("GM-S0001", "현재 게임 상태를 조회했습니다"),
    GAME_STARTED("GM-S0002", "게임을 시작했습니다"),
    ATTEMPT_SUBMITTED("GM-S0003", "시도를 제출했습니다"),
    HINT_PROVIDED("GM-S0004", "힌트를 제공했습니다"),
    SESSION_INFO("GM-S0005", "게임 세션 정보를 조회했습니다"),
    GAME_RESULT_INFO("GM-S0006", "게임 결과를 조회했습니다"),
    SHARE_TEXT("GM-S0007", "공유 텍스트를 생성했습니다"),
    ;

    private final String value;
    private final String message;
}
