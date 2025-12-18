package cloudcomputing.wordtreasure.model.game.dto;

import cloudcomputing.wordtreasure.model.game.entity.ExtraHint;

import java.time.LocalDateTime;

public record HintDto(
        Long hintId,
        String hintType,           // 힌트 타입 (추후 확장 대비)
        String hintContent,        // 힌트 내용
        Integer tokenCost,         // 소비한 토큰
        LocalDateTime requestedAt  // 요청 시간
) {
    public static HintDto from(ExtraHint hint) {
        return new HintDto(
                hint.getId(),
                "EXTRA",  // 현재는 추가 힌트만 존재
                hint.getHintText(),
                hint.getTokensSpent(),
                hint.getCreatedAt()
        );
    }
}
