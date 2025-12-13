package cloudcomputing.wordtreasure.model.game.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GameConfigKey {
    // 토큰 관련
    SIGNUP_BONUS_TOKENS("signup_bonus_tokens", "회원가입 보너스 토큰", "10"),
    DAILY_BONUS_TOKENS("daily_bonus_tokens", "일일 로그인 보너스 토큰", "5"),
    ATTEMPT_COST_TOKENS("attempt_cost_tokens", "시도당 소비 토큰", "1"),
    HINT_COST_TOKENS("hint_cost_tokens", "힌트당 소비 토큰", "2"),

    // 게임 규칙
    MAX_ATTEMPTS("max_attempts", "최대 시도 횟수", "10"),
    DAILY_FREE_TOKENS("daily_free_tokens", "일일 무료 지급 토큰", "10"),

    // 순위 보상
    RANK_1_REWARD("rank_1_reward", "1등 보상 토큰", "50"),
    RANK_2_REWARD("rank_2_reward", "2등 보상 토큰", "40"),
    RANK_3_REWARD("rank_3_reward", "3등 보상 토큰", "35"),
    RANK_DEFAULT_REWARD("rank_default_reward", "기본 정답 보상 토큰", "20"),
    ;

    private final String key;
    private final String description;
    private final String defaultValue;

    /**
     * key로 GameConfigKey 찾기
     */
    public static GameConfigKey fromKey(String key) {
        for (GameConfigKey configKey : values()) {
            if (configKey.key.equals(key)) {
                return configKey;
            }
        }
        throw new IllegalArgumentException("존재하지 않는 설정 키입니다: " + key);
    }

    /**
     * 기본값을 int로 파싱
     */
    public int getDefaultValueAsInt() {
        return Integer.parseInt(defaultValue);
    }
}
