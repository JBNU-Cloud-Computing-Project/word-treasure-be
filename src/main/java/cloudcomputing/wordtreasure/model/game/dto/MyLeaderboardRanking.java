package cloudcomputing.wordtreasure.model.game.dto;

public record MyLeaderboardRanking(
        Integer rank,
        String nickname,
        Integer totalGames,
        Integer successfulGames,
        Double successRate,
        String averageCompletionTime,
        Double averageScore,
        Integer tokensEarned,
        Integer attemptCount,  // 일간용
        String completionTime, // 일간용
        Double finalScore // 일간용
) {
    // 일간 순위용
    public static MyLeaderboardRanking forDaily(
            Integer rank,
            String nickname,
            Integer attemptCount,
            String completionTime,
            Double finalScore,
            Integer tokensEarned
    ) {
        return new MyLeaderboardRanking(
                rank, nickname, null, null, null, null, null,
                tokensEarned, attemptCount, completionTime, finalScore
        );
    }

    // 주간/월간/전체용
    public static MyLeaderboardRanking forPeriod(
            Integer rank,
            String nickname,
            Integer totalGames,
            Integer successfulGames,
            String averageCompletionTime,
            Double averageScore,
            Integer tokensEarned
    ) {
        Double successRate = totalGames > 0
                ? (successfulGames * 100.0 / totalGames)
                : 0.0;

        return new MyLeaderboardRanking(
                rank, nickname, totalGames, successfulGames, successRate,
                averageCompletionTime, averageScore, tokensEarned,
                null, null, null
        );
    }
}
