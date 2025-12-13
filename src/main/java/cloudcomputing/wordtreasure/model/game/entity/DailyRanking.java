package cloudcomputing.wordtreasure.model.game.entity;

import cloudcomputing.wordtreasure.common.audit.BaseTimeEntity;
import cloudcomputing.wordtreasure.model.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "daily_rankings",
        indexes = {
                @Index(name = "idx_daily_ranking_daily_word", columnList = "daily_word_id"),
                @Index(name = "idx_daily_ranking_member_id", columnList = "member_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_daily_word_rank",
                        columnNames = {"daily_word_id", "member_id"}
                ),
                @UniqueConstraint(
                        name = "uk_daily_word_member",
                        columnNames = {"daily_word_id", "member_id"}
                )
        }
)
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
public class DailyRanking extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_word_id", nullable = false)
    private DailyWord dailyWord;

    @Column(nullable = false)
    private Integer rank;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(precision = 5, scale = 2, nullable = false)
    private BigDecimal similarityScore;

    @Column(nullable = false)
    private Integer attemptCount;

    private Long completionTimeSeconds;

    private Integer tokensEarned;
}
