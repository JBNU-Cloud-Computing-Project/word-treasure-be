package cloudcomputing.wordtreasure.model.member.entity;

import cloudcomputing.wordtreasure.common.audit.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "member_statistics")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
public class MemberStatistics extends BaseTimeEntity {
    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @MapsId
    private Member member;

    @Column(nullable = false)
    private Integer totalGames = 0;

    @Column(nullable = false)
    private Integer successfulGames = 0;

    @Column(nullable = false)
    private Integer failedGames = 0;

    @Column(precision = 5, scale = 2, nullable = false)
    private BigDecimal successRate = BigDecimal.ZERO;

    @Column(precision = 4, scale = 2, nullable = false)
    private BigDecimal averageScore = BigDecimal.ZERO;

    private Integer bestRank;

    private Long fastestSolveTimeSeconds;

    @Column(nullable = false)
    private Integer longestStreak = 0;

    @Column(nullable = false)
    private Integer currentStreak = 0;

    private LocalDate lastPlayDate;
}
