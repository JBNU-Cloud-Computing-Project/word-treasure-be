package cloudcomputing.wordtreasure.game.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "extra_hints",
        indexes = {
                @Index(name = "idx_hint_session", columnList = "game_session_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ExtraHint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id", nullable = false)
    private GameSession gameSession;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String hintText;

    @Column(nullable = false)
    private Integer tokensSpent;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
