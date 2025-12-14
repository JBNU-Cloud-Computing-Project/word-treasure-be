package cloudcomputing.wordtreasure.api.game.response;

import cloudcomputing.wordtreasure.model.game.dto.GameResult;
import cloudcomputing.wordtreasure.model.game.service.GameResultService;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public record GameResultResponse(
        Long gameSessionId,
        String status,
        DailyWordResponse dailyWord,
        Integer attemptCount,
        Integer finalRank,
        BigDecimal highestSimilarity,
        String completionTime,
        Integer tokensSpent,
        Integer tokensEarned,
        Integer netTokens,
        List<AttemptDetailResponse> attempts,
        List<AttemptDetailResponse> closestAttempts
) {
    public static GameResultResponse from(GameResult result) {
        return new GameResultResponse(
                result.gameSessionId(),
                result.status().name(),
                DailyWordResponse.from(result.dailyWord()),
                result.attemptCount(),
                result.finalRank(),
                result.highestSimilarity(),
                result.completionTime(),
                result.tokensSpent(),
                result.tokensEarned(),
                result.netTokens(),
                result.attempts().stream()
                        .map(AttemptDetailResponse::from)
                        .collect(Collectors.toList()),
                result.closestAttempts() != null
                        ? result.closestAttempts().stream()
                        .map(AttemptDetailResponse::from)
                        .collect(Collectors.toList())
                        : null
        );
    }

    record AttemptDetailResponse(
            Integer attemptNumber,
            String userInput,
            BigDecimal similarityScore
    ) {
        static AttemptDetailResponse from(GameResultService.AttemptInfo info) {
            return new AttemptDetailResponse(
                    info.attemptNumber(),
                    info.userInput(),
                    info.similarityScore()
            );
        }
    }
}

