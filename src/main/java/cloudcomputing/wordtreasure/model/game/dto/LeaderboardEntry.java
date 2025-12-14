package cloudcomputing.wordtreasure.model.game.dto;

public record LeaderboardEntry(
        Integer rank,
        String nickname,
        Integer totalGames,
        Integer successfulGames,
        Double successRate,
        String averageCompletionTime,
        Double averagerScore,
        Integer tokensEarned,
        Integer attemptCount,
        String completionTime,
        Double finalScore
) {
    // 일간 순위용 생성자
    public static LeaderboardEntry forDaily(
            Integer rank,
            String nickname,
            Integer attemptCount,
            String completionTime,
            Double finalScore,
            Integer tokensEarned
    ) {
        return new LeaderboardEntry(
                rank, nickname, null, null, null, null, null,
                tokensEarned, attemptCount, completionTime, finalScore
        );
    }

    // 주간/월간/전체 순위용 생성자
    public static LeaderboardEntry forPeriod(
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

        return new LeaderboardEntry(
                rank, nickname, totalGames, successfulGames, successRate,
                averageCompletionTime, averageScore, tokensEarned,
                null, null, null
        );
    }
}
