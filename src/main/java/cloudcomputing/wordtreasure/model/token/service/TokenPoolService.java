package cloudcomputing.wordtreasure.model.token.service;

import cloudcomputing.wordtreasure.model.game.repository.DailyTokenPoolRepository;
import cloudcomputing.wordtreasure.model.token.entity.DailyTokenPool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 토큰 풀 관리 서비스
 * - 일일 토큰 적립 및 배분 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenPoolService {


    private final DailyTokenPoolRepository poolRepository;

    /**
     * 오늘의 토큰 풀에 금액 추가
     * - 시도 비용, 힌트 비용 적립 시 호출
     */
    @Transactional
    public void addToTodayPool(Long amount) {
        if (amount == null || amount <= 0) {
            log.warn("잘못된 적립 금액 - amount: {}", amount);
            return;
        }

        LocalDate today = LocalDate.now();
        DailyTokenPool pool = poolRepository.findByGameDate(today)
                .orElseGet(() -> createNewPool(today));

        pool.addToPool(amount);
        poolRepository.save(pool);

        log.debug("토큰 풀 적립 완료 - date: {}, amount: {}, totalPool: {}",
                today, amount, pool.getTotalPool());
    }

    /**
     * 특정 날짜의 토큰 풀 조회
     */
    @Transactional(readOnly = true)
    public DailyTokenPool getPoolByDate(LocalDate date) {
        return poolRepository.findByGameDate(date).orElse(null);
    }

    /**
     * 새로운 토큰 풀 생성
     * - 전날 이월분을 확인하여 초기화
     */
    @Transactional
    public DailyTokenPool createNewPool(LocalDate date) {
        // 전날 이월금 조회
        LocalDate yesterday = date.minusDays(1);
        DailyTokenPool yesterdayPool = poolRepository.findByGameDate(yesterday).orElse(null);

        Long carryOver = 0L;
        if (yesterdayPool != null && yesterdayPool.getIsDistributed()) {
            carryOver = yesterdayPool.getNextDayCarryOver();
        }

        DailyTokenPool newPool = new DailyTokenPool(date, carryOver);
        DailyTokenPool saved = poolRepository.save(newPool);

        log.info("새로운 토큰 풀 생성 - date: {}, carryOver: {}", date, carryOver);

        return saved;
    }

    /**
     * 미배분된 풀 목록 조회 (배치 처리용)
     */
    @Transactional(readOnly = true)
    public java.util.List<DailyTokenPool> getUndistributedPools() {
        return poolRepository.findUndistributedPools(LocalDate.now());
    }
}
