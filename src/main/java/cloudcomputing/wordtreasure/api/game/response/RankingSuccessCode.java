package cloudcomputing.wordtreasure.api.game.response;

import cloudcomputing.wordtreasure.common.controller.response.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum RankingSuccessCode implements SuccessCode {
    LIVE_RANKING_INFO("RK-S0001", "실시간 순위를 조회했습니다"),
    DAILY_RANKING_INFO("RK-S0002", "일간 순위를 조회했습니다"),
    WEEKLY_RANKING_INFO("RK-S0003", "주간 순위를 조회했습니다"),
    MONTHLY_RANKING_INFO("RK-S0004", "월간 순위를 조회했습니다"),
    ALL_TIME_RANKING_INFO("RK-S0005", "전체 순위를 조회했습니다");

    private final String value;
    private final String message;
}
