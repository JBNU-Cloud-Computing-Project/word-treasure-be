package cloudcomputing.wordtreasure.model.game;

import cloudcomputing.wordtreasure.model.game.entity.DailyWord;
import cloudcomputing.wordtreasure.model.game.service.DailyWordPublishService;
import cloudcomputing.wordtreasure.model.token.service.TokenRewardDistributor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyWordScheduler {

    private final DailyWordPublishService dailyWordPublishService;
    private final TokenRewardDistributor tokenRewardDistributor;

    /**
     * 앱 시작 시 누락된 작업 복구
     * - 어제 토큰 배분이 안 됐으면 배분
     * - 오늘 단어가 출제 안 됐으면 출제
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeOnStartup() {
        log.info("╔════════════════════════════════════════╗");
        log.info("║    앱 시작 - 누락된 작업 확인 시작     ║");
        log.info("╚════════════════════════════════════════╝");

        ZoneId seoulZone = ZoneId.of("Asia/Seoul");
        LocalDate yesterday = LocalDate.now(seoulZone).minusDays(1);
        LocalDate today = LocalDate.now(seoulZone);

        // 1. 어제 토큰 배분 확인 및 실행
        try {
            log.info("어제({}) 토큰 배분 상태 확인 중...", yesterday);
            tokenRewardDistributor.distributeRewards(yesterday);
            log.info("✅ 어제 토큰 배분 처리 완료");
        } catch (Exception e) {
            log.error("❌ 앱 시작 시 토큰 배분 실패 (계속 진행)", e);
        }

        // 2. 오늘 단어 출제 확인 및 실행
        try {
            log.info("오늘({}) 단어 출제 상태 확인 중...", today);
            DailyWord published = dailyWordPublishService.publishTodayWord(today);

            log.info("✅ 오늘 단어 출제 완료!");
            log.info("   - 단어: {}", published.getWord());
            log.info("   - 난이도: {}", published.getDifficulty());
            log.info("   - 출제일: {}", published.getGameDate());

        } catch (IllegalStateException e) {
            log.info("ℹ️  오늘 단어 이미 출제됨: {}", e.getMessage());
        } catch (Exception e) {
            log.error("❌ 앱 시작 시 단어 출제 실패", e);
        }

        log.info("╔════════════════════════════════════════╗");
        log.info("║    앱 시작 - 누락된 작업 확인 완료     ║");
        log.info("╚════════════════════════════════════════╝");
    }

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
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate yesterday = LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1);

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
            DailyWord published = dailyWordPublishService.publishTodayWord(today);

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
}
