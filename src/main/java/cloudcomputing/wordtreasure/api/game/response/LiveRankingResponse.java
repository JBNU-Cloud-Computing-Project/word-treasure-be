package cloudcomputing.wordtreasure.api.game.response;

import cloudcomputing.wordtreasure.model.game.dto.LiveRankingEntry;
import cloudcomputing.wordtreasure.model.game.dto.MyRankingInfo;

import java.util.List;
import java.util.stream.Collectors;

public record LiveRankingResponse(
        List<RankingItemResponse> rankings,
        MyRankResponse myRank,
        Long totalParticipants
) {
    public static LiveRankingResponse of(
            List<LiveRankingEntry> rankings,
            MyRankingInfo myRank,
            long totalParticipants
    ) {
        List<RankingItemResponse> rankingItems = rankings.stream()
                .map(RankingItemResponse::from)
                .collect(Collectors.toList());

        MyRankResponse myRankResponse = myRank != null
                ? MyRankResponse.from(myRank)
                : null;

        return new LiveRankingResponse(
                rankingItems,
                myRankResponse,
                totalParticipants
        );
    }
}
