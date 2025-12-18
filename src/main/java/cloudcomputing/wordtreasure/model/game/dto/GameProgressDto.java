package cloudcomputing.wordtreasure.model.game.dto;

import java.util.List;

public record GameProgressDto(
        List<AttemptDto> attempts,        // 모든 시도 기록
        List<HintDto> hints,              // 모든 힌트 기록
        Integer maxAttempts,              // 최대 시도 횟수
        Integer remainingAttempts,        // 남은 시도 횟수
        Integer totalTokensUsed           // 지금까지 사용한 총 토큰
) {
    /**
     * 게임 세션으로부터 진행 상태 생성
     */
    public static GameProgressDto from(
            List<AttemptDto> attempts,
            List<HintDto> hints,
            Integer maxAttempts,
            Integer currentAttemptCount,
            Integer totalTokensUsed
    ) {
        return new GameProgressDto(
                attempts,
                hints,
                maxAttempts,
                maxAttempts - currentAttemptCount,
                totalTokensUsed
        );
    }
}
