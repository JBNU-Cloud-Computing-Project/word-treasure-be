package cloudcomputing.wordtreasure.token;

import cloudcomputing.wordtreasure.game.entity.GameSession;
import cloudcomputing.wordtreasure.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "token_transactions",
        indexes = {
                @Index(name = "idx_transaction_member", columnList = "member_id"),
                @Index(name = "idx_transaction_created", columnList = "created_at"),
                @Index(name = "idx_transaction_type", columnList = "transaction_type")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TokenTransactions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;

    /**
     * 거래 금액
     * - 획득: 양수 (예: +20)
     * - 소비: 음수 (예: -5)
     */
    @Column(nullable = false)
    private Integer amount;

    /**
     * 거래 후 잔액
     * Member.currentTokens와 항상 일치해야 함
     */
    @Column(nullable = false)
    private Integer balanceAfter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_game_session_id")
    private GameSession relatedGameSession;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
