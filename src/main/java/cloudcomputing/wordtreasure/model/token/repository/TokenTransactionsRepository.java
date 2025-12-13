package cloudcomputing.wordtreasure.model.token.repository;

import cloudcomputing.wordtreasure.model.token.entity.TokenTransaction;
import cloudcomputing.wordtreasure.model.token.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TokenTransactionsRepository extends JpaRepository<TokenTransaction, Long> {

    /**
     * 특정 회원의 토큰 거래 내역 조회 (최신순)
     */
    @Query("SELECT t FROM TokenTransaction t WHERE t.member.memberId = :memberId ORDER BY t.createdAt DESC")
    List<TokenTransaction> findByMemberIdOrderByCreatedAtDesc(@Param("memberId") Long memberId);

    /**
     * 특정 회원의 특정 타입 거래 내역 조회
     */
    @Query("SELECT t FROM TokenTransaction t WHERE t.member.memberId = :memberId AND t.transactionType = :type ORDER BY t.createdAt DESC")
    List<TokenTransaction> findByMemberIdAndTransactionType(
            @Param("memberId") Long memberId,
            @Param("type") TransactionType transactionType
    );

    /**
     * 특정 게임 세션의 토큰 거래 내역 조회
     */
    @Query("SELECT t FROM TokenTransaction t WHERE t.relatedGameSession.id = :gameSessionId")
    List<TokenTransaction> findByGameSessionId(@Param("gameSessionId") Long gameSessionId);

    /**
     * 특정 기간의 토큰 거래 내역 조회
     */
    @Query("SELECT t FROM TokenTransaction t WHERE t.member.memberId = :memberId AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    List<TokenTransaction> findByMemberIdAndDateRange(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
