package cloudcomputing.wordtreasure.model.token.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionType {
    // 획득 타입
    SIGNUP_BONUS("회원가입 보너스", true, false),
    DAILY_BONUS("일일 로그인 보너스", true, false),
    GAME_REWARD("게임 보상", true, true),
    DAILY_RANKING_REWARD("일간 순위 비용", true, true),
    // 소비 타입
    ATTEMPT_COST("시도 비용", false, true),
    HINT_COST("힌트 비용", false, true),
    ;


    private final String description;
    private final boolean isEarning;  // true: 획득, false: 소비
    private final boolean requiresGameSession;  // 게임 세션 ID 필수 여부

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
     * 게임 세션 ID가 필수인지 확인
     */
    public boolean isGameSessionRequired() {
        return requiresGameSession;
    }

    /**
     * 자동 설명 생성 템플릿
     */
    public String generateDescription(Object... params) {
        return switch (this) {
            case SIGNUP_BONUS -> "회원가입 축하 보너스";
            case DAILY_BONUS -> String.format("일일 로그인 보너스 (%s)", params[0]);
            case GAME_REWARD -> String.format("%s등 달성 보상", params[0]);
            case DAILY_RANKING_REWARD -> String.format("%s 순위 보상 (Rank #%s)", params[0], params[1]);
            case ATTEMPT_COST -> String.format("%s번째 시도", params[0]);
            case HINT_COST -> "추가 힌트 요청";
        };
    }
}