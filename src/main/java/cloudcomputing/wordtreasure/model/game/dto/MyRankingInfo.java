package cloudcomputing.wordtreasure.model.game.dto;

public record MyRankingInfo(
        Integer rank,          // null이면 순위 없음 (진행중 or 실패)
        String nickname,
        String status          // "진행중", "성공", "실패"
) {
}
