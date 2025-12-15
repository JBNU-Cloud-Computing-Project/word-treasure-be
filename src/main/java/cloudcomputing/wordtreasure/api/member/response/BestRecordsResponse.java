package cloudcomputing.wordtreasure.api.member.response;

import cloudcomputing.wordtreasure.model.member.dto.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BestRecordsResponse(
        BestRankResponse bestRank,
        FastestSolveResponse fastestSolve,
        LongestStreakResponse longestStreak,
        HighestFirstTryResponse highestSimilarityOnFirstTry
) {
    public static BestRecordsResponse from(BestRecords records) {
        return new BestRecordsResponse(
                records.bestRank() != null
                        ? BestRankResponse.from(records.bestRank())
                        : null,
                records.fastestSolve() != null
                        ? FastestSolveResponse.from(records.fastestSolve())
                        : null,
                records.longestStreak() != null
                        ? LongestStreakResponse.from(records.longestStreak())
                        : null,
                records.highestSimilarityOnFirstTry() != null
                        ? HighestFirstTryResponse.from(records.highestSimilarityOnFirstTry())
                        : null
        );
    }

    /**
     * 최고 순위
     */
    record BestRankResponse(
            Integer rank,
            LocalDate gameDate,
            String word
    ) {
        static BestRankResponse from(BestRankInfo info) {
            return new BestRankResponse(
                    info.rank(),
                    info.gameDate(),
                    info.word()
            );
        }
    }

    /**
     * 가장 빠른 정답
     */
    record FastestSolveResponse(
            String time,
            LocalDate gameDate,
            String word,
            Integer attemptCount
    ) {
        static FastestSolveResponse from(FastestSolveInfo info) {
            return new FastestSolveResponse(
                    info.time(),
                    info.gameDate(),
                    info.word(),
                    info.attemptCount()
            );
        }
    }

    /**
     * 최장 연속
     */
    record LongestStreakResponse(
            Integer streakDays,
            LocalDate startDate,
            LocalDate endDate
    ) {
        static LongestStreakResponse from(LongestStreakInfo info) {
            return new LongestStreakResponse(
                    info.streakDays(),
                    info.startDate(),
                    info.endDate()
            );
        }
    }

    /**
     * 첫 시도 최고 유사도
     */
    record HighestFirstTryResponse(
            BigDecimal similarity,
            LocalDate gameDate,
            String word,
            String userInput
    ) {
        static HighestFirstTryResponse from(HighestFirstTryInfo info) {
            return new HighestFirstTryResponse(
                    info.similarity(),
                    info.gameDate(),
                    info.word(),
                    info.userInput()
            );
        }
    }
}
