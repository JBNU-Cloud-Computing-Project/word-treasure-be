package cloudcomputing.wordtreasure.api.game.response;

import cloudcomputing.wordtreasure.model.game.dto.LeaderboardEntry;
import cloudcomputing.wordtreasure.model.game.dto.LeaderboardPeriod;
import cloudcomputing.wordtreasure.model.game.dto.MyLeaderboardRanking;
import cloudcomputing.wordtreasure.model.game.dto.PaginationInfo;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public record LeaderboardResponse(
        @JsonInclude(JsonInclude.Include.NON_NULL)
        LeaderboardPeriod period,
        List<LeaderboardEntry> rankings,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        MyLeaderboardRanking myRanking,
        PaginationInfo pagination
) {
    public static LeaderboardResponse of(
            LeaderboardPeriod period,
            List<LeaderboardEntry> rankings,
            MyLeaderboardRanking myRanking,
            PaginationInfo pagination
    ) {
        return new LeaderboardResponse(period, rankings, myRanking, pagination);
    }
}
