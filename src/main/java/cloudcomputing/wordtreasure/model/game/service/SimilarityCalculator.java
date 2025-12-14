package cloudcomputing.wordtreasure.model.game.service;

import java.math.BigDecimal;

/**
 * 유사도 계산 인터페이스
 * <p>
 * 구현체:
 * 1. TemporarySimilarityCalculator (현재) - 임시 로직
 * 2. PythonFastApiSimilarityCalculator (미래) - Python FastAPI 연동
 */
public interface SimilarityCalculator {
    /**
     * 사용자 입력과 정답 단어의 유사도 계산
     *
     * @param userInput 사용자가 입력한 단어/문장
     * @param answer    정답 단어
     * @return 유사도 (0.00 ~ 100.00)
     */
    BigDecimal calculateSimilarity(String userInput, String answer);

    /**
     * 유사도 기반 힌트 생성
     *
     * @param userInput  사용자가 입력한 단어/문장
     * @param answer     정답 단어
     * @param similarity 계산된 유사도
     * @return 힌트 문구
     */
    String generateHint(String userInput, String answer, BigDecimal similarity);
}
