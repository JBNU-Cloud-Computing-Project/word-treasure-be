package cloudcomputing.wordtreasure.model.game.repository;

import cloudcomputing.wordtreasure.model.game.entity.Attempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {
    /**
     * 특정 게임 세션의 모든 시도 조회 (시간순)
     */
    @Query("SELECT a FROM Attempt a WHERE a.gameSession.id = :gameSessionId ORDER BY a.attemptNumber ASC")
    List<Attempt> findByGameSessionIdOrderByAttemptNumber(@Param("gameSessionId") Long gameSessionId);

    /**
     * 특정 게임 세션의 시도 개수 조회
     */
    @Query("SELECT COUNT(a) FROM Attempt a WHERE a.gameSession.id = :gameSessionId")
    Long countByGameSessionId(@Param("gameSessionId") Long gameSessionId);
}
