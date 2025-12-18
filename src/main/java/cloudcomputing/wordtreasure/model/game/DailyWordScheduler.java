package cloudcomputing.wordtreasure.model.game;

import cloudcomputing.wordtreasure.model.game.entity.DailyWord;
import cloudcomputing.wordtreasure.model.game.service.DailyWordPublishService;
import cloudcomputing.wordtreasure.model.token.service.TokenRewardDistributor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyWordScheduler {

    private final DailyWordPublishService dailyWordPublishService;
    private final TokenRewardDistributor tokenRewardDistributor;

    /**
     * 매일 자정에 실행되는 메인 스케줄러
     * 1. 어제 토큰 풀 배분
     * 2. 오늘의 단어 출제
     * <p>
     * cron 표현식: "초 분 시 일 월 요일"
     * "0 0 0 * * *" = 매일 00:00:00
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void midnightScheduler() {
        log.info("╔════════════════════════════════════════╗");
        log.info("║       자정 스케줄러 실행 시작           ║");
        log.info("╚════════════════════════════════════════╝");

        LocalDate yesterday = LocalDate.now().minusDays(1);

        try {
            log.info("어제 토큰 풀 배분 시작...");
            tokenRewardDistributor.distributeRewards(yesterday);
            log.info("어제 토큰 풀 배분 완료!");
        } catch (Exception e) {
            log.error("토큰 풀 배분 실패 (계속 진행)", e);
            // 배분 실패해도 단어 출제는 계속 진행
        }

        try {
            log.info("오늘의 단어 출제 시작...");
            DailyWord published = dailyWordPublishService.publishTodayWord();

            log.info("✅ 일일 단어 출제 성공!");
            log.info("   - 단어: {}", published.getWord());
            log.info("   - 난이도: {}", published.getDifficulty());
            log.info("   - 출제일: {}", published.getGameDate());

        } catch (IllegalStateException e) {
            log.warn("일일 단어 출제 실패: {}", e.getMessage());

        } catch (Exception e) {
            log.error("일일 단어 출제 중 예외 발생", e);
        }

        log.info("╔════════════════════════════════════════╗");
        log.info("║       자정 스케줄러 실행 완료           ║");
        log.info("╚════════════════════════════════════════╝");
    }

    /**
     * 테스트용: 앱 시작 직후 1회 실행 + 이후 5분 간격(종료 후 5분)으로 실행
     * 단어 출제만 수행하고 토큰 풀 배분은 실행하지 않음
     * 실제 배포 시에는 이 메서드를 제거하거나 주석 처리하세요
     */
    @Scheduled(initialDelay = 0L, fixedDelay = 5 * 60 * 1000L)
    public void midnightSchedulerForTest() {
        log.info("🧪 [테스트] 앱 시작 직후 1회 + 이후 5분 간격으로 단어 출제 시도");

        // 테스트 환경에서는 단어 출제만 수행
        publishDailyWord();
    }

    /**
     * 단어 출제 로직만 분리
     */
    private void publishDailyWord() {
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
}
