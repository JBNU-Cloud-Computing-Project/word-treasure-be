package cloudcomputing.wordtreasure.model.game.repository;

import cloudcomputing.wordtreasure.model.game.entity.ExtraHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExtraHintRepository extends JpaRepository<ExtraHint, Long> {
    /**
     * 특정 게임 세션의 모든 추가 힌트 조회 (시간순)
     */
    @Query("SELECT eh FROM ExtraHint eh WHERE eh.gameSession.id = :gameSessionId ORDER BY eh.createdAt ASC")
    List<ExtraHint> findByGameSessionIdOrderByCreatedAt(@Param("gameSessionId") Long gameSessionId);

    /**
     * 특정 게임 세션의 힌트 개수 조회
     */
    @Query("SELECT COUNT(eh) FROM ExtraHint eh WHERE eh.gameSession.id = :gameSessionId")
    Long countByGameSessionId(@Param("gameSessionId") Long gameSessionId);
}
