package cloudcomputing.wordtreasure.game.entity;

import cloudcomputing.wordtreasure.common.audit.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "game_config")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class GameConfig extends BaseTimeEntity {
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

    public void updateValue(String newValue) {
        this.value = newValue;
    }
}
