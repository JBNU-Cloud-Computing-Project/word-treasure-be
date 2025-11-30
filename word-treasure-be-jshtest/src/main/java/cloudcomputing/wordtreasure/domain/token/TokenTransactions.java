package cloudcomputing.wordtreasure.domain.token;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "token_transactions")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenTransactions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id")
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type")
    private TransactionType transactionType;

    private int amount;  // +획득 / -차감

    @Column(name = "balance_after")
    private int balanceAfter;

    @Column(name = "related_game_session_id")
    private Long relatedGameSessionId;

    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum TransactionType {
        SIGNUP_BONUS,
        DAILY_BONUS,
        GAME_REWARD,
        ATTEMPT_COST,
        HINT_COST
    }
}
