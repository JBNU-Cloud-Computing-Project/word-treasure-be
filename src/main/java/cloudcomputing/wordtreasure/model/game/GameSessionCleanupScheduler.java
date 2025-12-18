package cloudcomputing.wordtreasure.model.game;

import cloudcomputing.wordtreasure.model.game.entity.GameSession;
import cloudcomputing.wordtreasure.model.game.entity.GameStatus;
import cloudcomputing.wordtreasure.model.game.repository.GameSessionRepository;
import cloudcomputing.wordtreasure.model.game.service.RankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameSessionCleanupScheduler {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final GameSessionRepository gameSessionRepository;
    private final RankingService rankingService;

    /**
     * 매일 00:00:00에 전날 PLAYING 세션을 FAIL로 확정하고,
     * 실패/만료도 랭킹에 포함하므로 Redis에 최종 점수도 확정 등록한다.
     * <p>
     * completedAt은 "엄밀히 자정 종료"로 맞추기 위해 00:00:00으로 고정한다.
     */
    @Transactional
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void finalizeYesterdaySessions() {
        LocalDate kstToday = LocalDate.now(KST);
        LocalDate yesterday = kstToday.minusDays(1);

        // "엄밀히 자정 종료" (KST 자정 00:00:00)
        LocalDateTime midnight = kstToday.atStartOfDay();

        List<GameSession> playingSessions =
                gameSessionRepository.findByGameDateAndStatusWithFetch(yesterday, GameStatus.PLAYING);

        int processed = 0;

        for (GameSession session : playingSessions) {
            // 1) DB 상태 확정: PLAYING -> FAIL, completedAt = 자정 고정
            try {
                var statusField = GameSession.class.getDeclaredField("status");
                statusField.setAccessible(true);
                statusField.set(session, GameStatus.FAIL);

                var completedAtField = GameSession.class.getDeclaredField("completedAt");
                completedAtField.setAccessible(true);
                completedAtField.set(session, midnight);
            } catch (Exception e) {
                log.error("자정 세션 상태 확정 실패 - gameSessionId: {}", session.getId(), e);
                continue;
            }

            // 2) Redis 랭킹 최종 점수 확정 등록 (실패/만료 포함)
            double finalScore = rankingService.calculateRankingScore(
                    session.getHighestSimilarity() != null ? session.getHighestSimilarity().doubleValue() : 0.0,
                    session.getAttemptCount()
            );

            try {
                rankingService.addToRanking(
                        session.getDailyWord().getId(),
                        session.getMember().getMemberId(),
                        finalScore
                );
            } catch (Exception e) {
                // 랭킹 실패가 DB 정리를 막으면 더 큰 장애가 되므로 경고만 남기고 진행
                log.warn("자정 랭킹 확정 등록 실패 - gameSessionId: {}", session.getId(), e);
            }

            processed++;
        }

        if (!playingSessions.isEmpty()) {
            gameSessionRepository.saveAll(playingSessions);
        }

        log.info("자정 세션 정리 완료 - gameDate: {}, processed: {}, total: {}, completedAtFixed: {}",
                yesterday, processed, playingSessions.size(), midnight);
    }
}
