package cloudcomputing.wordtreasure.application.token;

import cloudcomputing.wordtreasure.domain.member.Members;
import cloudcomputing.wordtreasure.domain.member.MembersRepository;
import cloudcomputing.wordtreasure.domain.token.TokenTransactions;
import cloudcomputing.wordtreasure.domain.token.TokenTransactionsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final MembersRepository membersRepository;
    private final TokenTransactionsRepository transactionsRepository;

    // 토큰 지급 (+)
    public void addTokens(Long memberId, int amount, TokenTransactions.TransactionType type, String description) {

        Members member = membersRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원 없음"));

        int newBalance = member.getCurrentTokens() + amount;

        member.setCurrentTokens(newBalance);
        member.setTotalTokensEarned(member.getTotalTokensEarned() + Math.max(amount, 0));
        membersRepository.save(member);

        TokenTransactions tx = TokenTransactions.builder()
                .memberId(memberId)
                .amount(amount)
                .balanceAfter(newBalance)
                .transactionType(type)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();

        transactionsRepository.save(tx);
    }

    // 토큰 차감 (-)
    public void useTokens(Long memberId, int amount, TokenTransactions.TransactionType type, String description) {

        Members member = membersRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원 없음"));

        if (member.getCurrentTokens() < amount) {
            throw new RuntimeException("토큰 부족");
        }

        int newBalance = member.getCurrentTokens() - amount;

        member.setCurrentTokens(newBalance);
        membersRepository.save(member);

        TokenTransactions tx = TokenTransactions.builder()
                .memberId(memberId)
                .amount(-amount)
                .balanceAfter(newBalance)
                .transactionType(type)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();

        transactionsRepository.save(tx);
    }
}
