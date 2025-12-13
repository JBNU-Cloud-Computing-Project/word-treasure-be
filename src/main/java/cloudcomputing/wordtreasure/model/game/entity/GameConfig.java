package cloudcomputing.wordtreasure.model.game.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "game_config")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class GameConfig {
    /**
     * 설정 키 (Primary Key)
     * 예: "daily_free_tokens", "max_attempts"
     */
    @Id
    @Column(name = "config_key", length = 50, nullable = false)
    private String key;

    @Column(name = "config_value", nullable = false)
    private String value;

    @Column(name = "description")
    private String description;

    /**
     * GameConfigKey를 사용한 생성자
     */
    public static GameConfig of(GameConfigKey configKey) {
        return new GameConfig(
                configKey.getKey(),
                configKey.getDefaultValue(),
                configKey.getDescription()
        );
    }

    /**
     * GameConfigKey를 사용한 생성자 (커스텀 값)
     */
    public static GameConfig of(GameConfigKey configKey, String customValue) {
        return new GameConfig(
                configKey.getKey(),
                customValue,
                configKey.getDescription()
        );
    }

    /**
     * 값 업데이트
     */
    public void updateValue(String newValue) {
        this.value = newValue;
    }

    /**
     * int 타입으로 값 반환
     */
    public int getValueAsInt() {
        try {
            return Integer.parseInt(this.value);
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    String.format("설정값을 int로 변환할 수 없습니다. key: %s, value: %s", key, value));
        }
    }

    /**
     * boolean 타입으로 값 반환
     */
    public boolean getValueAsBoolean() {
        return Boolean.parseBoolean(this.value);
    }
}
