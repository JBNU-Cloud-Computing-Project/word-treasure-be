package cloudcomputing.wordtreasure.model.member.entity;

import cloudcomputing.wordtreasure.common.audit.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "members", indexes = {
        @Index(name = "idx_member_email", columnList = "email"),
        @Index(name = "idx_member_nickname", columnList = "nickName")
})
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(nullable = false, unique = true, length = 50)
    private String nickName;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private Integer currentTokens;

    @Column(nullable = false)
    private Long totalTokensEarned;

    private LocalDateTime lastLoginAt;

    @Builder
    public Member(Long memberId, String nickName, String email, String passwordHash, LocalDateTime lastLoginAt) {
        this.memberId = memberId;
        this.nickName = nickName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.currentTokens = 0;
        this.totalTokensEarned = 0L;
        this.lastLoginAt = lastLoginAt = LocalDateTime.now();
    }

    public void addTokens(int amount) {
        validatePositiveAmount(amount);
        this.currentTokens += amount;
        this.totalTokensEarned += amount;
    }

    public void deductTokens(int amount) {
        validatePositiveAmount(amount);
        validateSufficientBalance(amount);
        this.currentTokens -= amount;
    }

    public boolean hasEnoughTokens(int amount) {
        return this.currentTokens >= amount;
    }

    private void validatePositiveAmount(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("토큰 금액은 양수여야 합니다.");
        }
    }

    private void validateSufficientBalance(int amount) {
        if (this.currentTokens < amount) {
            throw new IllegalArgumentException(
                    String.format("토큰 잔액이 부족합니다. (현재: %d, 필요: %d)",
                            this.currentTokens, amount)
            );
        }
    }

    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }
}
