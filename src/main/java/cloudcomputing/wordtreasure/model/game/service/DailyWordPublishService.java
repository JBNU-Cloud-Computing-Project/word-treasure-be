package cloudcomputing.wordtreasure.model.game.service;

import cloudcomputing.wordtreasure.model.game.entity.DailyWord;
import cloudcomputing.wordtreasure.model.game.entity.WordPool;
import cloudcomputing.wordtreasure.model.game.repository.DailyWordRepository;
import cloudcomputing.wordtreasure.model.game.repository.WordPoolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyWordPublishService {

    private final WordSelectionService wordSelectionService;
    private final DailyWordRepository dailyWordRepository;
    private final WordPoolRepository wordPoolRepository;

    /**
     * 자동 출제: 특정 날짜의 일일 단어 출제
     *
     * @param targetDate 출제 예정일
     * @return 출제된 DailyWord
     * @throws IllegalStateException 이미 출제된 날짜거나 선택 가능한 단어가 없는 경우
     */
    @Transactional
    public DailyWord publishWordForDate(LocalDate targetDate) {
        log.info("=== 일일 단어 자동 출제 시작 ===");
        log.info("출제 예정일: {}", targetDate);

        // 1. 이미 출제되었는지 확인
        if (dailyWordRepository.existsByGameDate(targetDate)) {
            throw new IllegalStateException(
                    String.format("해당 날짜에 이미 단어가 출제되었습니다: %s", targetDate));
        }

        // 2. 단어 선정
        WordPool selectedWord = wordSelectionService.selectWordForDate(targetDate);
        if (selectedWord == null) {
            throw new IllegalStateException(
                    "출제 가능한 단어가 없습니다. word_pool 데이터를 확인하세요.");
        }

        // 3. DailyWord 생성 및 저장
        DailyWord dailyWord = DailyWord.fromWordPool(selectedWord, targetDate);
        DailyWord saved = dailyWordRepository.save(dailyWord);

        // 4. WordPool의 사용 통계 업데이트
        selectedWord.markAsUsed(targetDate);
        wordPoolRepository.save(selectedWord);

        log.info("일일 단어 출제 완료: word={}, difficulty={}, date={}",
                saved.getWord(), saved.getDifficulty(), saved.getGameDate());
        log.info("=== 일일 단어 자동 출제 종료 ===");

        return saved;
    }

    /**
     * 수동 출제: 특정 단어를 지정하여 출제 (테스트용)
     *
     * @param targetDate     출제 예정일
     * @param wordPoolId     출제할 단어 ID
     * @param forceOverwrite 기존 단어가 있어도 덮어쓸지 여부
     * @return 출제된 DailyWord
     */
    @Transactional
    public DailyWord publishWordManually(LocalDate targetDate, Long wordPoolId, boolean forceOverwrite) {
        log.info("=== 일일 단어 수동 출제 시작 ===");
        log.info("출제 예정일: {}, wordPoolId: {}, forceOverwrite: {}",
                targetDate, wordPoolId, forceOverwrite);

        // 1. 기존 단어 확인 및 처리
        boolean alreadyExists = dailyWordRepository.existsByGameDate(targetDate);
        if (alreadyExists) {
            if (!forceOverwrite) {
                throw new IllegalStateException(
                        String.format("해당 날짜에 이미 단어가 출제되었습니다: %s (forceOverwrite=true로 덮어쓸 수 있습니다)", targetDate));
            }

            // 기존 단어 삭제
            dailyWordRepository.findByGameDate(targetDate)
                    .ifPresent(existing -> {
                        log.info("기존 단어 삭제: word={}, date={}", existing.getWord(), existing.getGameDate());
                        dailyWordRepository.delete(existing);
                    });
        }

        // 2. 지정된 단어 조회
        WordPool selectedWord = wordSelectionService.selectWordById(wordPoolId);

        // 3. DailyWord 생성 및 저장
        DailyWord dailyWord = DailyWord.fromWordPool(selectedWord, targetDate);
        DailyWord saved = dailyWordRepository.save(dailyWord);

        // 4. WordPool의 사용 통계 업데이트
        selectedWord.markAsUsed(targetDate);
        wordPoolRepository.save(selectedWord);

        log.info("일일 단어 수동 출제 완료: word={}, difficulty={}, date={}",
                saved.getWord(), saved.getDifficulty(), saved.getGameDate());
        log.info("=== 일일 단어 수동 출제 종료 ===");

        return saved;
    }

    /**
     * 오늘의 단어 자동 출제 (스케줄러에서 호출)
     */
    @Transactional
    public DailyWord publishTodayWord() {
        return publishWordForDate(LocalDate.now());
    }

    /**
     * 특정 날짜의 출제 여부 확인
     */
    public boolean isPublishedForDate(LocalDate date) {
        return dailyWordRepository.existsByGameDate(date);
    }

    /**
     * 특정 날짜의 출제된 단어 조회
     */
    public DailyWord getPublishedWordForDate(LocalDate date) {
        return dailyWordRepository.findByGameDate(date)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("해당 날짜에 출제된 단어가 없습니다: %s", date)));
    }
}
