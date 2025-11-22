package cloudcomputing.wordtreasure.member.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ParticipationLevel {
    PARTICIPATED(1, "게임 참여"),
    MEDIUM_SCORE(2, "50% 이상 유사도"),
    HIGH_SCORE(3, "80% 이상 유사도"),
    SUCCESS(4, "정답 성공");
    
    private final int value;
    private final String description;
}
