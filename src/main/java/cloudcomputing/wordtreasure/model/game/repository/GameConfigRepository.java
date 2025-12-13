package cloudcomputing.wordtreasure.model.game.repository;

import cloudcomputing.wordtreasure.model.game.entity.GameConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameConfigRepository extends JpaRepository<GameConfig, Long> {
    boolean existsByKey(String key);

    Optional<GameConfig> findByKey(String key);
}
