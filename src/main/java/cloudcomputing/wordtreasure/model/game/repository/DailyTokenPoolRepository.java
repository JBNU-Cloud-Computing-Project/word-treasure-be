package cloudcomputing.wordtreasure.model.game.repository;

import cloudcomputing.wordtreasure.model.token.entity.DailyTokenPool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyTokenPoolRepository extends JpaRepository<DailyTokenPool, Long> {
    /**
     * 특정 날짜의 토큰 풀 조회
     */
    Optional<DailyTokenPool> findByGameDate(LocalDate gameDate);

    /**
     * 배분되지 않은 풀 조회 (배치 처리용)
     */
    @Query("SELECT dtp FROM DailyTokenPool dtp " +
            "WHERE dtp.isDistributed = false " +
            "AND dtp.gameDate < :today " +
            "ORDER BY dtp.gameDate ASC")
    java.util.List<DailyTokenPool> findUndistributedPools(@Param("today") LocalDate today);

    /**
     * 특정 날짜 이후의 풀 목록 조회 (통계용)
     */
    @Query("SELECT dtp FROM DailyTokenPool dtp " +
            "WHERE dtp.gameDate >= :startDate " +
            "ORDER BY dtp.gameDate DESC")
    java.util.List<DailyTokenPool> findPoolsSince(@Param("startDate") LocalDate startDate);

    /**
     * 전체 이월 금액 합계 조회
     */
    @Query("SELECT COALESCE(SUM(dtp.nextDayCarryOver), 0) FROM DailyTokenPool dtp " +
            "WHERE dtp.isDistributed = true")
    Long getTotalCarryOverAmount();
}
