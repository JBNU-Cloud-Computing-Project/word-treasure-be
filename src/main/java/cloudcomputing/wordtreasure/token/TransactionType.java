package cloudcomputing.wordtreasure.token;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionType {
    // 획득 타입
    SIGNUP_BONUS("회원가입 보너스", true),
    DAILY_BONUS("일일 로그인 보너스", true),
    GAME_REWARD("게임 보상", true),

    // 소비 타입
    ATTEMPT_COST("시도 비용", false),
    HINT_COST("힌트 비용", false);

    private final String description;
    private final boolean isEarning;  // true: 획득, false: 소비

    /**
     * 획득 타입인지 확인
     */
    public boolean isEarn() {
        return isEarning;
    }

    /**
     * 소비 타입인지 확인
     */
    public boolean isSpend() {
        return !isEarning;
    }

    /**
     * 자동 설명 생성 템플릿
     */
    public String generateDescription(Object... params) {
        return switch (this) {
            case SIGNUP_BONUS -> "회원가입 축하 보너스";
            case DAILY_BONUS -> String.format("일일 로그인 보너스 (%s)", params[0]);
            case GAME_REWARD -> String.format("%s등 달성 보상", params[0]);
            case ATTEMPT_COST -> String.format("%s번째 시도", params[0]);
            case HINT_COST -> "추가 힌트 요청";
        };
    }
}