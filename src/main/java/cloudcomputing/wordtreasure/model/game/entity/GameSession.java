package cloudcomputing.wordtreasure.model.game.entity;

import cloudcomputing.wordtreasure.common.audit.BaseTimeEntity;
import cloudcomputing.wordtreasure.model.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "game_sessions",
        indexes = {
                @Index(name = "idx_game_daily_word", columnList = "daily_word_id"),
                @Index(name = "idx_game_status", columnList = "status"),
                @Index(name = "idx_game_completed_at", columnList = "completed_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_member_daily_word",
                        columnNames = {"member_id", "daily_word_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
public class GameSession extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "member_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_word_id", nullable = false)
    private DailyWord dailyWord;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GameStatus status = GameStatus.PLAYING;

    @Column(nullable = false)
    private Integer attemptCount = 0;

    @Column(precision = 5, scale = 2)
    private BigDecimal highestSimilarity;

    @Column(nullable = false)
    private LocalDateTime startedAt = LocalDateTime.now();

    private LocalDateTime completedAt;

    private Integer finalRank;

    @Column(nullable = false)
    private Integer tokensSpent = 0;

    @Column(nullable = false)
    private Integer tokensEarned = 0;

    // 양방향 관계 설정
    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attempt> attempts = new ArrayList<>();

    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExtraHint> extraHints = new ArrayList<>();
}
