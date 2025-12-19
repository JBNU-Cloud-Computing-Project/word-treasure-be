package cloudcomputing.wordtreasure.model.token.entity;

import cloudcomputing.wordtreasure.common.audit.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 일일 토큰 풀 관리 엔티티
 * - 매일 사용자들이 시도/힌트에 사용한 토큰을 풀로 적립
 * - 자정에 순위별로 배분하고 남은 금액은 다음날로 이월
 */
@Entity
@Table(
        name = "daily_token_pools",
        indexes = {
                @Index(name = "idx_pool_game_date", columnList = "game_date")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_game_date",
                        columnNames = {"game_date"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
public class DailyTokenPool extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 게임 날짜 (해당 풀이 적용되는 날짜)
     */
    @Column(name = "game_date", nullable = false, unique = true)
    private LocalDate gameDate;

    /**
     * 총 풀 금액 (전날 이월분 + 당일 적립분)
     */
    @Column(name = "total_pool", nullable = false)
    private Long totalPool = 0L;

    /**
     * 전날에서 이월된 금액
     */
    @Column(name = "carry_over", nullable = false)
    private Long carryOver = 0L;

    /**
     * 당일 사용자들이 적립한 금액 (시도 + 힌트 비용)
     */
    @Column(name = "daily_accumulated", nullable = false)
    private Long dailyAccumulated = 0L;

    /**
     * 실제 배분된 금액 (배분 완료 후 계산됨)
     */
    @Column(name = "distributed", nullable = false)
    private Long distributed = 0L;

    /**
     * 다음날로 이월될 금액 (소수점 버림으로 발생한 나머지)
     */
    @Column(name = "next_day_carry_over", nullable = false)
    private Long nextDayCarryOver = 0L;

    /**
     * 배분 완료 여부
     */
    @Column(name = "is_distributed", nullable = false)
    private Boolean isDistributed = false;

    /**
     * 배분 완료 시각
     */
    @Column(name = "distributed_at")
    private java.time.LocalDateTime distributedAt;

    /**
     * 참여자 수 (배분 시 기록)
     */
    @Column(name = "participant_count")
    private Integer participantCount;

    /**
     * 생성자 (새로운 풀 생성 시)
     */
    public DailyTokenPool(LocalDate gameDate, Long carryOver) {
        this.gameDate = gameDate;
        this.carryOver = carryOver;
        this.totalPool = carryOver;
        this.dailyAccumulated = 0L;
        this.distributed = 0L;
        this.nextDayCarryOver = 0L;
        this.isDistributed = false;
    }

    /**
     * 토큰 적립
     */
    public void addToPool(Long amount) {
        this.dailyAccumulated += amount;
        this.totalPool += amount;
    }

    /**
     * 배분 완료 처리
     */
    public void markAsDistributed(Long distributedAmount, Long remainder, Integer participants) {
        this.distributed = distributedAmount;
        this.nextDayCarryOver = remainder;
        this.participantCount = participants;
        this.isDistributed = true;
        this.distributedAt = java.time.LocalDateTime.now();
    }
}
