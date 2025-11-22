package cloudcomputing.wordtreasure.game.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Difficulty {
    EASY("쉬움", 1),
    MEDIUM("중급", 2),
    HARD("어려움", 3);

    private final String description;
    private final int level;

    public static Difficulty fromScore(int difficultyScore) {
        if (difficultyScore >= 70) {
            return HARD;
        } else if (difficultyScore >= 40) {
            return MEDIUM;
        } else {
            return EASY;
        }
    }

    /**
     * 평균 성공률로 난이도 재계산
     *
     * @param successRate 실제 게임 성공률 (0-100)
     * @return 조정된 난이도
     */
    public static Difficulty fromSuccessRate(double successRate) {
        if (successRate < 30.0) {
            return HARD;
        } else if (successRate < 70.0) {
            return MEDIUM;
        } else {
            return EASY;
        }
    }

    /**
     * 난이도 비교
     */
    public boolean isHarderThan(Difficulty other) {
        return this.level > other.level;
    }
}
