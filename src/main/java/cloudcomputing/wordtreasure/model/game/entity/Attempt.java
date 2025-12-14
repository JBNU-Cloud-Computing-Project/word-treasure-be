package cloudcomputing.wordtreasure.model.game.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "attempts",
        indexes = {
                @Index(name = "idx_attempt_session", columnList = "game_session_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_session_attempt_number",
                        columnNames = {"game_session_id", "attempt_number"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Attempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id", nullable = false)
    private GameSession gameSession;

    @Column(nullable = false)
    private Integer attemptNumber;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String userInput;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal similarityScore;

    @Column(columnDefinition = "TEXT")
    private String hintProvided;

    @Column(nullable = false)
    private Integer tokensSpent;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
