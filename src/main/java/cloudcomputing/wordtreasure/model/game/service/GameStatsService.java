package cloudcomputing.wordtreasure.model.game.service;

import cloudcomputing.wordtreasure.model.game.dto.AllDifficultyStats;
import cloudcomputing.wordtreasure.model.game.dto.DifficultyStats;
import cloudcomputing.wordtreasure.model.game.dto.TodayWordStats;
import cloudcomputing.wordtreasure.model.game.entity.DailyWord;
import cloudcomputing.wordtreasure.model.game.entity.Difficulty;
import cloudcomputing.wordtreasure.model.game.repository.DailyWordRepository;
import cloudcomputing.wordtreasure.model.game.repository.GameSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameStatsService {

    private final DailyWordRepository dailyWordRepository;
    private final GameSessionRepository gameSessionRepository;

    /**
     * 오늘의 단어 통계 조회
     */
    @Cacheable(value = "todayWordStats", unless = "#result == null")
    public TodayWordStats getTodayWordStats() {
        log.info("오늘의 단어 통계 조회");

        // 1. 오늘의 단어 조회
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        DailyWord todayWord = dailyWordRepository.findTodayWord(today)
                .orElse(null);

        if (todayWord == null) {
            log.warn("오늘의 단어가 없습니다");
            return null;
        }

        // 2. 평균 시도 횟수 계산
        Double avgAttemptsRaw = gameSessionRepository.calculateAverageAttempts(
                todayWord.getGameDate()
        );

        BigDecimal avgAttempts = avgAttemptsRaw == null
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(avgAttemptsRaw).setScale(2, RoundingMode.HALF_UP);

        return new TodayWordStats(
                todayWord.getId(),
                todayWord.getWord(),
                todayWord.getDifficulty().getDescription(),
                todayWord.getTotalParticipants(),
                todayWord.getSuccessfulAttempts(),
                todayWord.getSuccessRate() != null ? todayWord.getSuccessRate() : BigDecimal.ZERO,
                avgAttempts
        );
    }

    /**
     * 난이도별 통계 조회 (전체 기간)
     */
    @Cacheable(value = "difficultyStats", unless = "#result == null")
    public AllDifficultyStats getAllDifficultyStats() {
        log.info("난이도별 통계 조회");

        // 1. 모든 일일 단어 조회
        List<DailyWord> allWords = dailyWordRepository.findAll();

        // 2. 난이도별 집계
        Map<Difficulty, StatAccumulator> statsMap = new HashMap<>();
        statsMap.put(Difficulty.EASY, new StatAccumulator());
        statsMap.put(Difficulty.MEDIUM, new StatAccumulator());
        statsMap.put(Difficulty.HARD, new StatAccumulator());

        for (DailyWord word : allWords) {
            Difficulty difficulty = word.getDifficulty();
            StatAccumulator accumulator = statsMap.get(difficulty);

            accumulator.totalGames += word.getTotalParticipants();
            accumulator.successfulGames += word.getSuccessfulAttempts();
        }

        // 3. 성공률 계산 및 DTO 생성
        DifficultyStats easy = createDifficultyStats(Difficulty.EASY, statsMap.get(Difficulty.EASY));
        DifficultyStats medium = createDifficultyStats(Difficulty.MEDIUM, statsMap.get(Difficulty.MEDIUM));
        DifficultyStats hard = createDifficultyStats(Difficulty.HARD, statsMap.get(Difficulty.HARD));

        return AllDifficultyStats.of(easy, medium, hard);
    }

    /**
     * 난이도 통계 DTO 생성
     */
    private DifficultyStats createDifficultyStats(Difficulty difficulty, StatAccumulator accumulator) {
        BigDecimal successRate = BigDecimal.ZERO;

        if (accumulator.totalGames > 0) {
            successRate = BigDecimal.valueOf(accumulator.successfulGames)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(accumulator.totalGames), 2, RoundingMode.HALF_UP);
        }

        return DifficultyStats.of(
                difficulty.getDescription(),
                successRate,
                accumulator.totalGames,
                accumulator.successfulGames
        );
    }

    /**
     * 통계 누적 클래스
     */
    private static class StatAccumulator {
        int totalGames = 0;
        int successfulGames = 0;
    }
}
