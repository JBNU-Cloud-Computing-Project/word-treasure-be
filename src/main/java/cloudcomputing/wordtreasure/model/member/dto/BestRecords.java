package cloudcomputing.wordtreasure.model.member.dto;

public record BestRecords(
        BestRankInfo bestRank,
        FastestSolveInfo fastestSolve,
        LongestStreakInfo longestStreak,
        HighestFirstTryInfo highestSimilarityOnFirstTry
) {
}