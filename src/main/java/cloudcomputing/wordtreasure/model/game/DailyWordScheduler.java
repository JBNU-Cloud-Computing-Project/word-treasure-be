package cloudcomputing.wordtreasure.model.game;

import cloudcomputing.wordtreasure.model.game.entity.DailyWord;
import cloudcomputing.wordtreasure.model.game.service.DailyWordPublishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyWordScheduler {

    private final DailyWordPublishService dailyWordPublishService;

    /**
     * 매일 자정에 오늘의 단어 출제
     * <p>
     * cron 표현식: "초 분 시 일 월 요일"
     * "0 0 0 * * *" = 매일 00:00:00
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void publishDailyWord() {
        log.info("╔════════════════════════════════════════╗");
        log.info("║  일일 단어 자동 출제 스케줄러 실행    ║");
        log.info("╚════════════════════════════════════════╝");

        try {
            DailyWord published = dailyWordPublishService.publishTodayWord();

            log.info("✅ 일일 단어 출제 성공!");
            log.info("   - 단어: {}", published.getWord());
            log.info("   - 난이도: {}", published.getDifficulty());
            log.info("   - 출제일: {}", published.getGameDate());

        } catch (IllegalStateException e) {
            log.warn("⚠️  일일 단어 출제 실패: {}", e.getMessage());

        } catch (Exception e) {
            log.error("❌ 일일 단어 출제 중 예외 발생", e);
        }
    }

    /**
     * 테스트용: 앱 시작 직후 1회 실행 + 이후 5분 간격(종료 후 5분)으로 실행
     * 실제 배포 시에는 이 메서드를 제거하거나 주석 처리하세요
     */
    @Scheduled(initialDelay = 0L, fixedDelay = 5 * 60 * 1000L)
    public void publishDailyWordForTest() {
        log.info("🧪 [테스트] 앱 시작 직후 1회 + 이후 5분 간격으로 단어 출제 시도");
        publishDailyWord();
    }
}
