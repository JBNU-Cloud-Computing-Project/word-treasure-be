package cloudcomputing.wordtreasure.model.game.dto;

import cloudcomputing.wordtreasure.model.game.entity.Difficulty;
import cloudcomputing.wordtreasure.model.game.entity.GameStatus;

import java.time.LocalDate;

public record CurrentGameInfo(
        Long dailyWordId,
        LocalDate gameDate,
        cloudcomputing.wordtreasure.model.game.entity.GameStatus status,
        String remainingTime,
        cloudcomputing.wordtreasure.model.game.entity.Difficulty difficulty,
        boolean hasStarted,
        Long gameSessionId,
        GameProgressDto progress
) {
    /**
     * 게임 시작 전 상태 생성
     */
    public static CurrentGameInfo withoutProgress(
            Long dailyWordId,
            LocalDate gameDate,
            String remainingTime,
            Difficulty difficulty
    ) {
        return new CurrentGameInfo(
                dailyWordId,
                gameDate,
                null,
                remainingTime,
                difficulty,
                false,
                null,
                null  // 진행 상태 없음
        );
    }

    /**
     * 게임 진행 중 상태 생성
     */
    public static CurrentGameInfo withProgress(
            Long dailyWordId,
            LocalDate gameDate,
            GameStatus status,
            String remainingTime,
            Difficulty difficulty,
            Long gameSessionId,
            GameProgressDto progress
    ) {
        return new CurrentGameInfo(
                dailyWordId,
                gameDate,
                status,
                remainingTime,
                difficulty,
                true,
                gameSessionId,
                progress
        );
    }
}
