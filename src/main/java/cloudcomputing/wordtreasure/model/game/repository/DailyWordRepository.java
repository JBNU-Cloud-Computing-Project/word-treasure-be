package cloudcomputing.wordtreasure.model.game.repository;

import cloudcomputing.wordtreasure.model.game.entity.DailyWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DailyWordRepository extends JpaRepository<DailyWord, Long> {
    /**
     * 특정 날짜의 일일 단어 조회
     */
    Optional<DailyWord> findByGameDate(LocalDate gameDate);

    /**
     * 오늘의 일일 단어 조회
     */
    /*@Query(value = "SELECT * FROM daily_words WHERE game_date = :date", nativeQuery = true)
    Optional<DailyWord> findTodayWord(@Param("date") LocalDate date);*/
    @Query("SELECT dw FROM DailyWord dw WHERE dw.gameDate = :date")
    Optional<DailyWord> findTodayWord(@Param("date") LocalDate date);

    @Query("SELECT dw FROM DailyWord dw " +
            "WHERE DATE(dw.createdAt) = :date " +
            "ORDER BY dw.createdAt DESC")
    Optional<DailyWord> findByCreatedDate(@Param("date") LocalDate date);

    // DailyWordRepository
    @Query("SELECT dw FROM DailyWord dw " +
            "WHERE dw.createdAt >= :startOfDay " +
            "ORDER BY dw.createdAt DESC " +
            "LIMIT 1")
    Optional<DailyWord> findTodayWordByCreatedAt(@Param("startOfDay") LocalDateTime startOfDay);

    /**
     * 날짜 범위로 일일 단어 목록 조회
     */
    @Query("SELECT dw FROM DailyWord dw WHERE dw.gameDate BETWEEN :startDate AND :endDate ORDER BY dw.gameDate DESC")
    List<DailyWord> findByGameDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 특정 날짜에 일일 단어가 존재하는지 확인
     */
    boolean existsByGameDate(LocalDate gameDate);
}
