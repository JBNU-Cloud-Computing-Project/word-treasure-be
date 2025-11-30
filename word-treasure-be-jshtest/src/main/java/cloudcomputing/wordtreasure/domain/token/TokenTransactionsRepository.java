package cloudcomputing.wordtreasure.domain.token;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenTransactionsRepository extends JpaRepository<TokenTransactions, Long> {
}