package cloudcomputing.wordtreasure.model.token.service;

import cloudcomputing.wordtreasure.model.game.entity.GameSession;
import cloudcomputing.wordtreasure.model.member.entity.Member;
import cloudcomputing.wordtreasure.model.member.repositroy.MemberRepository;
import cloudcomputing.wordtreasure.model.token.entity.TokenTransaction;
import cloudcomputing.wordtreasure.model.token.entity.TransactionType;
import cloudcomputing.wordtreasure.model.token.exception.InsufficientTokenException;
import cloudcomputing.wordtreasure.model.token.repository.TokenTransactionsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TokenService {
    private final MemberRepository memberRepository;
    private final TokenTransactionsRepository transactionsRepository;

    /**
     * 토큰 지급
     *
     * @param memberId           회원 ID
     * @param amount             지급할 토큰 양 (양수)
     * @param transactionType    거래 유형
     * @param description        거래 설명
     * @param relatedGameSession 관련 게임 세션 (선택)
     * @throws IllegalArgumentException memberId가 존재하지 않거나, amount가 0 이하이거나, 필수 gameSession이 없을 때
     */
    @Transactional
    public void addTokens(
            Long memberId,
            int amount,
            TransactionType transactionType,
            String description,
            GameSession relatedGameSession
    ) {
        log.info("토큰 지급 시작 - memberId: {}, amount: {}, type: {}",
                memberId, amount, transactionType);

        // 1. 검증
        validateAmount(amount);
        validateTransactionType(transactionType, relatedGameSession, true);

        // 2. 비관적 락으로 회원 조회
        Member member = findMemberWithLock(memberId);

        // 3. 회원 엔티티에서 토큰 추가
        member.addTokens(amount);

        // 4. 거래 내역 기록
        TokenTransaction transaction = TokenTransaction.createEarnTransaction(
                member,
                transactionType,
                amount,
                member.getCurrentTokens(),
                relatedGameSession,
                description
        );

        TokenTransaction savedTransaction = transactionsRepository.save(transaction);

        log.info("토큰 지급 완료 - transactionId: {}, 현재 잔액: {}",
                savedTransaction.getId(), member.getCurrentTokens());

    }

    /**
     * 토큰 차감
     *
     * @param memberId           회원 ID
     * @param amount             차감할 토큰 양 (양수)
     * @param transactionType    거래 유형
     * @param description        거래 설명
     * @param relatedGameSession 관련 게임 세션 (선택)
     * @return 생성된 거래 내역
     * @throws InsufficientTokenException 토큰 잔액 부족 시
     * @throws IllegalArgumentException   memberId가 존재하지 않거나, amount가 0 이하이거나, 필수 gameSession이 없을 때
     */
    @Transactional
    public TokenTransaction deductTokens(
            Long memberId,
            int amount,
            TransactionType transactionType,
            String description,
            GameSession relatedGameSession
    ) {
        log.info("토큰 차감 시작 - memberId: {}, amount: {}, type: {}",
                memberId, amount, transactionType);

        // 1. 검증
        validateAmount(amount);
        validateTransactionType(transactionType, relatedGameSession, false);

        // 2. 비관적 락으로 회원 조회
        Member member = findMemberWithLock(memberId);

        // 3. 잔액 확인
        if (!member.hasEnoughTokens(amount)) {
            log.warn("토큰 잔액 부족 - memberId: {}, 현재: {}, 필요: {}",
                    memberId, member.getCurrentTokens(), amount);
            throw new InsufficientTokenException(member.getCurrentTokens(), amount);
        }

        // 4. 회원 엔티티에서 토큰 차감
        member.deductTokens(amount);

        // 5. 거래 내역 기록
        TokenTransaction transaction = TokenTransaction.createSpendTransaction(
                member,
                transactionType,
                amount,
                member.getCurrentTokens(),
                relatedGameSession,
                description
        );

        TokenTransaction savedTransaction = transactionsRepository.save(transaction);

        log.info("토큰 차감 완료 - transactionId: {}, 현재 잔액: {}",
                savedTransaction.getId(), member.getCurrentTokens());

        return savedTransaction;
    }

    /**
     * 회원의 현재 토큰 잔액 조회
     *
     * @param memberId 회원 ID
     * @return 현재 토큰 잔액
     */
    public int getBalance(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("존재하지 않는 회원입니다. memberId: %d", memberId)));

        return member.getCurrentTokens();
    }

    /**
     * 토큰 차감 가능 여부 확인
     *
     * @param memberId 회원 ID
     * @param amount   필요한 토큰 양
     * @return 차감 가능 여부
     */
    public boolean canDeduct(Long memberId, int amount) {
        try {
            int balance = getBalance(memberId);
            return balance >= amount;
        } catch (Exception e) {
            log.error("토큰 차감 가능 여부 확인 실패 - memberId: {}", memberId, e);
            return false;
        }
    }

    // ========== Private 검증 메서드 ==========

    private Member findMemberWithLock(Long memberId) {
        return memberRepository.findByIdWithLock(memberId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("존재하지 않는 회원입니다. memberId: %d", memberId)));
    }

    private void validateAmount(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("토큰 금액은 양수여야 합니다.");
        }
    }

    private void validateTransactionType(
            TransactionType transactionType,
            GameSession relatedGameSession,
            boolean isEarn
    ) {
        // 획득/소비 타입 검증
        if (isEarn && !transactionType.isEarn()) {
            throw new IllegalArgumentException(
                    String.format("%s는 토큰 지급 타입이 아닙니다.", transactionType));
        }
        if (!isEarn && !transactionType.isSpend()) {
            throw new IllegalArgumentException(
                    String.format("%s는 토큰 차감 타입이 아닙니다.", transactionType));
        }

        // 게임 세션 필수 여부 검증
        if (transactionType.isGameSessionRequired() && relatedGameSession == null) {
            throw new IllegalArgumentException(
                    String.format("%s 타입은 게임 세션 정보가 필수입니다.", transactionType));
        }
    }
}
