package cloudcomputing.wordtreasure.api.game.response;

import cloudcomputing.wordtreasure.model.game.dto.LiveRankingEntry;

public record RankingItemResponse(
        Integer rank,
        String nickname,
        Integer attemptCount,
        String completionTime
) {
    public static RankingItemResponse from(LiveRankingEntry entry) {
        return new RankingItemResponse(
                entry.rank(),
                entry.nickname(),
                entry.attemptCount(),
                entry.completionTime()
        );
    }
}
