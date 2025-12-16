package cloudcomputing.wordtreasure.model.game.repository;

import cloudcomputing.wordtreasure.model.game.entity.Difficulty;
import cloudcomputing.wordtreasure.model.game.entity.WordPool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WordPoolRepository extends JpaRepository<WordPool, Long> {

    /**
     * 단어로 조회
     */
    Optional<WordPool> findByWord(String word);

    /**
     * 활성화된 단어만 조회
     */
    List<WordPool> findByIsActiveTrue();

    /**
     * 난이도와 활성화 상태로 필터링
     */
    List<WordPool> findByDifficultyAndIsActiveTrue(Difficulty difficulty);

    /**
     * 특정 날짜 이후로 사용되지 않은 활성화된 단어 조회
     * (또는 한 번도 사용되지 않은 단어)
     */
    @Query("SELECT w FROM WordPool w WHERE w.isActive = true " +
            "AND (w.lastUsedDate IS NULL OR w.lastUsedDate < :cutoffDate) " +
            "ORDER BY w.usageCount ASC, w.lastUsedDate ASC NULLS FIRST")
    List<WordPool> findUnusedWordsSince(@Param("cutoffDate") LocalDate cutoffDate);

    /**
     * 특정 난이도의 사용되지 않은 단어 조회
     */
    @Query("SELECT w FROM WordPool w WHERE w.isActive = true " +
            "AND w.difficulty = :difficulty " +
            "AND (w.lastUsedDate IS NULL OR w.lastUsedDate < :cutoffDate) " +
            "ORDER BY w.usageCount ASC, w.lastUsedDate ASC NULLS FIRST")
    List<WordPool> findUnusedWordsByDifficulty(
            @Param("difficulty") Difficulty difficulty,
            @Param("cutoffDate") LocalDate cutoffDate
    );

    /**
     * 카테고리별 활성화된 단어 조회
     */
    List<WordPool> findByCategoryAndIsActiveTrue(String category);

    /**
     * 단어가 존재하는지 확인
     */
    boolean existsByWord(String word);
}
