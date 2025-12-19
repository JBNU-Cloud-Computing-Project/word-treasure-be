package cloudcomputing.wordtreasure.model.token.service;

import cloudcomputing.wordtreasure.model.game.entity.GameSession;
import cloudcomputing.wordtreasure.model.game.repository.DailyTokenPoolRepository;
import cloudcomputing.wordtreasure.model.game.repository.GameSessionRepository;
import cloudcomputing.wordtreasure.model.member.entity.Member;
import cloudcomputing.wordtreasure.model.member.repositroy.MemberRepository;
import cloudcomputing.wordtreasure.model.token.entity.DailyTokenPool;
import cloudcomputing.wordtreasure.model.token.entity.TokenTransaction;
import cloudcomputing.wordtreasure.model.token.entity.TransactionType;
import cloudcomputing.wordtreasure.model.token.repository.TokenTransactionsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 토큰 풀 배분 서비스
 * - 자정에 전날의 토큰 풀을 순위별로 배분
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenRewardDistributor {


    private final DailyTokenPoolRepository poolRepository;
    private final GameSessionRepository gameSessionRepository;
    private final MemberRepository memberRepository;
    private final TokenTransactionsRepository transactionRepository;

    /**
     * 특정 날짜의 토큰 풀 배분
     * - 자정 스케줄러에서 호출됨
     */
    @Transactional
    public void distributeRewards(LocalDate targetDate) {
        log.info("========== 토큰 풀 배분 시작 - date: {} ==========", targetDate);

        // 1. 해당 날짜의 풀 조회
        DailyTokenPool pool = poolRepository.findByGameDate(targetDate).orElse(null);

        if (pool == null) {
            log.info("토큰 풀이 존재하지 않음 - date: {}", targetDate);
            return;
        }

        if (pool.getIsDistributed()) {
            log.warn("이미 배분 완료된 풀 - date: {}", targetDate);
            return;
        }

        if (pool.getTotalPool() == 0) {
            log.info("배분할 토큰이 없음 - date: {}", targetDate);
            pool.markAsDistributed(0L, 0L, 0);
            poolRepository.save(pool);
            return;
        }

        // 2. 해당 날짜의 성공한 게임 세션 조회 (순위순)
        List<GameSession> successSessions = gameSessionRepository
                .findSuccessSessionsByDateOrderByRank(targetDate);

        if (successSessions.isEmpty()) {
            log.info("참여자가 없어 전액 이월 - date: {}, amount: {}", targetDate, pool.getTotalPool());
            carryOverToNextDay(pool, pool.getTotalPool());
            pool.markAsDistributed(0L, pool.getTotalPool(), 0);
            poolRepository.save(pool);
            return;
        }

        // 3. 참여자 수에 따른 배분 비율 계산
        int participantCount = Math.min(successSessions.size(), 10); // 최대 10위까지만
        Map<Integer, Double> ratios = calculateDistributionRatios(participantCount);

        log.info("배분 시작 - 총 풀: {}, 참여자: {}, 배분 대상: {}",
                pool.getTotalPool(), successSessions.size(), participantCount);

        // 4. 순위별 배분
        long totalDistributed = 0L;

        for (int i = 0; i < participantCount; i++) {
            GameSession session = successSessions.get(i);
            int rank = i + 1;
            double ratio = ratios.get(rank);

            // 소수점 버림
            long reward = (long) (pool.getTotalPool() * ratio);

            if (reward > 0) {
                // 회원에게 토큰 지급
                Member member = session.getMember();
                member.addTokens((int) reward);
                memberRepository.save(member);

                // 트랜잭션 기록
                createRewardTransaction(member, session, reward, rank, targetDate);

                totalDistributed += reward;

                log.info("순위별 보상 지급 - rank: {}, memberId: {}, reward: {}, ratio: {}%",
                        rank, member.getMemberId(), reward, ratio * 100);
            }
        }

        // 5. 나머지 이월
        long remainder = pool.getTotalPool() - totalDistributed;
        if (remainder > 0) {
            carryOverToNextDay(pool, remainder);
            log.info("잔액 이월 - amount: {}", remainder);
        }

        // 6. 배분 완료 표시
        pool.markAsDistributed(totalDistributed, remainder, participantCount);
        poolRepository.save(pool);

        log.info("========== 토큰 풀 배분 완료 - 배분액: {}, 이월액: {} ==========",
                totalDistributed, remainder);
    }

    /**
     * 참여자 수에 따른 배분 비율 계산
     */
    private Map<Integer, Double> calculateDistributionRatios(int participantCount) {
        Map<Integer, Double> ratios = new HashMap<>();

        if (participantCount == 1) {
            ratios.put(1, 1.0); // 100%
        } else if (participantCount == 2) {
            ratios.put(1, 0.7);  // 70%
            ratios.put(2, 0.3);  // 30%
        } else if (participantCount == 3) {
            ratios.put(1, 0.5);  // 50%
            ratios.put(2, 0.3);  // 30%
            ratios.put(3, 0.2);  // 20%
        } else {
            // 4명 이상
            ratios.put(1, 0.4);  // 40%
            ratios.put(2, 0.3);  // 30%
            ratios.put(3, 0.2);  // 20%

            // 4~10위: 10%를 동등 분배
            int remainingRanks = Math.min(participantCount, 10) - 3;
            double perRank = 0.1 / remainingRanks;

            for (int i = 4; i <= Math.min(participantCount, 10); i++) {
                ratios.put(i, perRank);
            }
        }

        return ratios;
    }

    /**
     * 다음날로 이월
     */
    private void carryOverToNextDay(DailyTokenPool yesterdayPool, long amount) {
        LocalDate nextDay = yesterdayPool.getGameDate().plusDays(1);

        DailyTokenPool nextDayPool = poolRepository.findByGameDate(nextDay)
                .orElseGet(() -> {
                    DailyTokenPool newPool = new DailyTokenPool(nextDay, 0L);
                    return poolRepository.save(newPool);
                });

        nextDayPool.setCarryOver(nextDayPool.getCarryOver() + amount);
        nextDayPool.setTotalPool(nextDayPool.getTotalPool() + amount);
        poolRepository.save(nextDayPool);

        log.debug("다음날 풀로 이월 - nextDay: {}, amount: {}", nextDay, amount);
    }

    /**
     * 순위 보상 트랜잭션 생성
     */
    private void createRewardTransaction(
            Member member,
            GameSession gameSession,
            long amount,
            int rank,
            LocalDate gameDate
    ) {
        String description = String.format(
                "%s 순위 보상 (Rank #%d)",
                gameDate,
                rank
        );

        TokenTransaction transaction = TokenTransaction.createEarnTransaction(
                member,
                TransactionType.DAILY_RANKING_REWARD,
                (int) amount,
                member.getCurrentTokens(),
                gameSession,
                description
        );

        transactionRepository.save(transaction);
    }
}
