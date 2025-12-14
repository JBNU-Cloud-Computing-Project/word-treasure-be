package cloudcomputing.wordtreasure.model.game.entity;

import cloudcomputing.wordtreasure.common.audit.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Entity
@Table(
        name = "daily_words",
        indexes = {
                @Index(name = "idx_daily_word_date", columnList = "game_date")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class DailyWord extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String word;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Difficulty difficulty;

    @Column(nullable = false, unique = true)
    private LocalDate gameDate;

    // 통계 필드 (비정규화 - 성능 최적화)
    @Column(nullable = false)
    private Integer totalParticipants = 0;

    @Column(nullable = false)
    private Integer successfulAttempts = 0;

    @Column(precision = 5, scale = 2)
    private BigDecimal successRate;

    @Column(nullable = false)
    private Integer totalTokenPool = 0;


    public void incrementParticipants() {
        this.totalParticipants++;
    }

    /**
     * 게임 성공 시 통계 업데이트
     */
    public void recordSuccess() {
        this.successfulAttempts++;
        updateSuccessRate();
    }

    /**
     * 게임 실패 시 통계 업데이트 (참여자 수는 이미 증가했으므로 성공률만 재계산)
     */
    public void recordFailure() {
        updateSuccessRate();
    }

    /**
     * 성공률 재계산
     */
    private void updateSuccessRate() {
        if (totalParticipants > 0) {
            BigDecimal success = BigDecimal.valueOf(successfulAttempts);
            BigDecimal total = BigDecimal.valueOf(totalParticipants);

            // 성공률 = (성공 / 전체) * 100
            this.successRate = success
                    .divide(total, 2, RoundingMode.HALF_UP) // scale = 2 유지
                    .multiply(BigDecimal.valueOf(100));
        } else {
            this.successRate = BigDecimal.ZERO;
        }
    }
}
