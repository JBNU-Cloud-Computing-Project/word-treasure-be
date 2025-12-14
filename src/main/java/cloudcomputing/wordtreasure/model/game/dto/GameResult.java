package cloudcomputing.wordtreasure.model.game.dto;

import cloudcomputing.wordtreasure.model.game.entity.GameStatus;
import cloudcomputing.wordtreasure.model.game.service.GameResultService;

import java.math.BigDecimal;
import java.util.List;

public record GameResult(
        Long gameSessionId,
        GameStatus status,
        DailyWordInfo dailyWord,
        Integer attemptCount,
        Integer finalRank,
        BigDecimal highestSimilarity,
        String completionTime,
        Integer tokensSpent,
        Integer tokensEarned,
        Integer netTokens,
        List<GameResultService.AttemptInfo> attempts,
        List<GameResultService.AttemptInfo> closestAttempts  // 실패 시만 사용
) {
}
