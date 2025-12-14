package cloudcomputing.wordtreasure.api.game.response;

import cloudcomputing.wordtreasure.model.game.dto.MyRankingInfo;

public record MyRankResponse(
        Integer rank,
        String nickname,
        String status
) {
    public static MyRankResponse from(MyRankingInfo info) {
        return new MyRankResponse(
                info.rank(),
                info.nickname(),
                info.status()
        );
    }
}
