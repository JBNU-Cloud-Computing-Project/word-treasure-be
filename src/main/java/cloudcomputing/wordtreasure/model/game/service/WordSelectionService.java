package cloudcomputing.wordtreasure.model.game.service;

import cloudcomputing.wordtreasure.model.game.entity.Difficulty;
import cloudcomputing.wordtreasure.model.game.entity.WordPool;
import cloudcomputing.wordtreasure.model.game.repository.WordPoolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WordSelectionService {

    // 난이도별 선정 확률
    private static final Map<Difficulty, Integer> DIFFICULTY_WEIGHTS = Map.of(
            Difficulty.EASY, 30,
            Difficulty.MEDIUM, 50,
            Difficulty.HARD, 20
    );
    // 최근 사용 제외 기간 (일)
    private static final int UNUSED_DAYS_THRESHOLD = 30;
    private final WordPoolRepository wordPoolRepository;

    /**
     * 일일 단어 선정 메인 로직
     *
     * @param targetDate 출제 예정일
     * @return 선정된 WordPool, 없으면 null
     */
    public WordPool selectWordForDate(LocalDate targetDate) {
        log.info("일일 단어 선정 시작: targetDate={}", targetDate);

        // 1. 난이도 결정 (확률 기반)
        Difficulty selectedDifficulty = selectDifficultyByWeight();
        log.info("선정된 난이도: {}", selectedDifficulty);

        // 2. 해당 난이도의 후보 단어 목록 조회
        LocalDate cutoffDate = targetDate.minusDays(UNUSED_DAYS_THRESHOLD);
        List<WordPool> candidates = wordPoolRepository.findUnusedWordsByDifficulty(
                selectedDifficulty,
                cutoffDate
        );

        log.info("후보 단어 개수: {} (난이도: {}, 기준일: {})",
                candidates.size(), selectedDifficulty, cutoffDate);

        // 3. 후보가 없으면 다른 난이도에서 선택 시도
        if (candidates.isEmpty()) {
            log.warn("후보 단어가 없어 다른 난이도에서 선택 시도");
            candidates = findAlternativeCandidates(cutoffDate);
        }

        // 4. 그래도 없으면 null 반환
        if (candidates.isEmpty()) {
            log.error("선택 가능한 단어가 없습니다. word_pool 데이터를 확인하세요.");
            return null;
        }

        // 5. 후보 중에서 랜덤 선택 (상위 10개 중에서)
        WordPool selected = selectRandomFromTopCandidates(candidates, 10);

        log.info("선정 완료: word={}, difficulty={}, usageCount={}",
                selected.getWord(), selected.getDifficulty(), selected.getUsageCount());

        return selected;
    }

    /**
     * 가중치 기반 난이도 선택
     * EASY: 30%, MEDIUM: 50%, HARD: 20%
     */
    private Difficulty selectDifficultyByWeight() {
        Random random = new Random();
        int randomValue = random.nextInt(100); // 0~99

        if (randomValue < 30) {
            return Difficulty.EASY;
        } else if (randomValue < 80) { // 30 + 50
            return Difficulty.MEDIUM;
        } else {
            return Difficulty.HARD;
        }
    }

    /**
     * 대체 후보 찾기 (모든 난이도에서 검색)
     */
    private List<WordPool> findAlternativeCandidates(LocalDate cutoffDate) {
        List<WordPool> allCandidates = wordPoolRepository.findUnusedWordsSince(cutoffDate);

        if (allCandidates.isEmpty()) {
            log.warn("기준일 {} 이후 사용되지 않은 단어가 없어, 모든 활성 단어에서 선택", cutoffDate);
            allCandidates = wordPoolRepository.findByIsActiveTrue();
        }

        return allCandidates;
    }

    /**
     * 상위 N개 후보 중에서 랜덤 선택
     * <p>
     * 이유: 완전 랜덤보다는 사용 횟수가 적은 단어를 우선하되,
     * 어느 정도 랜덤성을 보장하여 예측 불가능하게 함
     */
    private WordPool selectRandomFromTopCandidates(List<WordPool> candidates, int topN) {
        int candidateSize = Math.min(candidates.size(), topN);
        List<WordPool> topCandidates = candidates.subList(0, candidateSize);

        Random random = new Random();
        return topCandidates.get(random.nextInt(topCandidates.size()));
    }

    /**
     * 특정 단어 ID로 선정 (테스트 또는 수동 출제용)
     */
    public WordPool selectWordById(Long wordPoolId) {
        return wordPoolRepository.findById(wordPoolId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "해당 ID의 단어를 찾을 수 없습니다: " + wordPoolId));
    }

    /**
     * 난이도 분포 통계 조회 (모니터링용)
     */
    public Map<Difficulty, Long> getDifficultyDistribution() {
        List<WordPool> allWords = wordPoolRepository.findByIsActiveTrue();

        Map<Difficulty, Long> distribution = new HashMap<>();
        for (Difficulty difficulty : Difficulty.values()) {
            long count = allWords.stream()
                    .filter(w -> w.getDifficulty() == difficulty)
                    .count();
            distribution.put(difficulty, count);
        }

        return distribution;
    }
}
