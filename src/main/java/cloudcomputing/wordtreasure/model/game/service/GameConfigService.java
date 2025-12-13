package cloudcomputing.wordtreasure.model.game.service;

import cloudcomputing.wordtreasure.model.game.entity.GameConfig;
import cloudcomputing.wordtreasure.model.game.entity.GameConfigKey;
import cloudcomputing.wordtreasure.model.game.repository.GameConfigRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameConfigService {

    private final GameConfigRepository gameConfigRepository;

    /**
     * 애플리케이션 시작 시 기본 설정값 초기화
     */
    @PostConstruct
    @Transactional
    public void initializeDefaultConfigs() {
        log.info("게임 설정 초기화 시작");

        for (GameConfigKey configKey : GameConfigKey.values()) {
            if (!gameConfigRepository.existsByKey(configKey.getKey())) {
                GameConfig config = GameConfig.of(configKey);
                gameConfigRepository.save(config);
                log.info("기본 설정 생성 - key: {}, value: {}",
                        configKey.getKey(), configKey.getDefaultValue());
            }
        }

        log.info("게임 설정 초기화 완료");
    }

    /**
     * 설정값 조회 (int)
     */
    public int getIntValue(GameConfigKey configKey) {
        return gameConfigRepository.findByKey(configKey.getKey())
                .map(GameConfig::getValueAsInt)
                .orElseGet(() -> {
                    log.warn("설정을 찾을 수 없어 기본값 사용 - key: {}", configKey.getKey());
                    return configKey.getDefaultValueAsInt();
                });
    }

    /**
     * 설정값 조회 (String)
     */
    public String getStringValue(GameConfigKey configKey) {
        return gameConfigRepository.findByKey(configKey.getKey())
                .map(GameConfig::getValue)
                .orElseGet(() -> {
                    log.warn("설정을 찾을 수 없어 기본값 사용 - key: {}", configKey.getKey());
                    return configKey.getDefaultValue();
                });
    }

    /**
     * 설정값 업데이트
     */
    @Transactional
    public void updateValue(GameConfigKey configKey, String newValue) {
        GameConfig config = gameConfigRepository.findByKey(configKey.getKey())
                .orElseGet(() -> {
                    log.info("설정이 없어 새로 생성 - key: {}", configKey.getKey());
                    return GameConfig.of(configKey);
                });

        config.updateValue(newValue);
        gameConfigRepository.save(config);

        log.info("설정 업데이트 - key: {}, newValue: {}", configKey.getKey(), newValue);
    }

    /**
     * 설정값 업데이트 (int)
     */
    @Transactional
    public void updateValue(GameConfigKey configKey, int newValue) {
        updateValue(configKey, String.valueOf(newValue));
    }
}
