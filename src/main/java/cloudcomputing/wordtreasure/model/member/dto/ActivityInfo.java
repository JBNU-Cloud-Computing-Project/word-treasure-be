package cloudcomputing.wordtreasure.model.member.dto;

import cloudcomputing.wordtreasure.model.game.entity.GameStatus;

import java.time.LocalDate;

public record ActivityInfo(
        LocalDate activityDate,
        Integer participationLevel,
        Long gameSessionId,
        GameStatus status
) {
}