package cloudcomputing.wordtreasure.model.game.repository;

public interface LeaderboardProjection {
    Long getMemberId();

    String getNickname();

    Long getTotalGames();

    Long getSuccessfulGames();

    Long getTokensEarned();

    Double getAvgCompletionSeconds();  // 평균 완료 시간 (초)
}
