package cloudcomputing.wordtreasure.api.game.response;

import cloudcomputing.wordtreasure.common.controller.response.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum RankingSuccessCode implements SuccessCode {
    LIVE_RANKING_INFO("RK-S0001", "실시간 순위를 조회했습니다");

    private final String value;
    private final String message;
}
