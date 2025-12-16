package cloudcomputing.wordtreasure.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 스케줄링 기능 활성화 설정
 *
 * @EnableScheduling 어노테이션으로 Spring의 스케줄링 기능을 활성화합니다.
 * 이 설정이 있어야 @Scheduled 어노테이션이 동작합니다.
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {
    // 스케줄링 관련 추가 설정이 필요하면 여기에 작성
}