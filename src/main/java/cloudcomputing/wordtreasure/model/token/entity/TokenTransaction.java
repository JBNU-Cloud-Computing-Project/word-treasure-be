package cloudcomputing.wordtreasure.model.token.entity;

import cloudcomputing.wordtreasure.model.game.entity.GameSession;
import cloudcomputing.wordtreasure.model.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
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
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TokenTransaction {
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

    @Builder
    public TokenTransaction(Long id, Member member, TransactionType transactionType, Integer amount, Integer balanceAfter, GameSession relatedGameSession, String description, LocalDateTime createdAt) {
        this.id = id;
        this.member = member;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.relatedGameSession = relatedGameSession;
        this.description = description;
        this.createdAt = createdAt;
    }

    /**
     * 토큰 획득 거래 생성 (amount는 양수로 저장)
     */
    public static TokenTransaction createEarnTransaction(
            Member member,
            TransactionType transactionType,
            int amount,
            int balanceAfter,
            GameSession relatedGameSession,
            String description
    ) {
        validatePositiveAmount(amount);

        return new TokenTransaction(
                null,
                member,
                transactionType,
                amount,  // 양수로 저장
                balanceAfter,
                relatedGameSession,
                description,
                LocalDateTime.now()
        );
    }

    /**
     * 토큰 소비 거래 생성 (amount는 음수로 저장)
     */
    public static TokenTransaction createSpendTransaction(
            Member member,
            TransactionType transactionType,
            int amount,
            int balanceAfter,
            GameSession relatedGameSession,
            String description
    ) {
        validatePositiveAmount(amount);

        return new TokenTransaction(
                null,
                member,
                transactionType,
                -amount,  // 음수로 저장
                balanceAfter,
                relatedGameSession,
                description,
                LocalDateTime.now()
        );
    }

    private static void validatePositiveAmount(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("토큰 금액은 양수여야 합니다.");
        }
    }

    /**
     * 획득 거래인지 확인
     */
    public boolean isEarn() {
        return amount > 0;
    }

    /**
     * 소비 거래인지 확인
     */
    public boolean isSpend() {
        return amount < 0;
    }
}
