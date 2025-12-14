package cloudcomputing.wordtreasure.model.game.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 임시 유사도 계산 구현체
 * <p>
 * 현재: 간단한 문자열 비교 로직
 * 향후: Python FastAPI 서비스로 교체 예정
 * <p>
 * 교체 방법:
 * 1. PythonFastApiSimilarityCalculator 구현
 * 2. @Primary 또는 @Qualifier로 전환
 * 3. 또는 이 클래스에 @Profile("local") 추가
 */
@Slf4j
@Component
public class TemporarySimilarityCalculator implements SimilarityCalculator {
    @Override
    public BigDecimal calculateSimilarity(String userInput, String answer) {
        log.debug("임시 유사도 계산 - userInput: {}, answer: {}", userInput, answer);

        String normalizedInput = normalize(userInput);
        String normalizedAnswer = normalize(answer);

        // 정답이면 100%
        if (normalizedInput.equals(normalizedAnswer)) {
            return BigDecimal.valueOf(100.00);
        }

        // 1. 포함 관계 체크
        double containScore = calculateContainmentScore(normalizedInput, normalizedAnswer);

        // 2. 편집 거리 기반 점수
        double editScore = calculateEditDistanceScore(normalizedInput, normalizedAnswer);

        // 3. 자모 유사도 (한글 특화)
        double jamoScore = calculateJamoSimilarity(normalizedInput, normalizedAnswer);

        // 가중 평균
        double finalScore = (containScore * 0.4) + (editScore * 0.3) + (jamoScore * 0.3);

        // 0~100 범위로 제한
        finalScore = Math.max(0, Math.min(100, finalScore));

        return BigDecimal.valueOf(finalScore).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String generateHint(String userInput, String answer, BigDecimal similarity) {
        double score = similarity.doubleValue();

        if (score >= 95.0) {
            return "거의 정답이에요! 더 정확한 표현이 있어요.";
        } else if (score >= 80.0) {
            return "아주 가까워요! 조금만 더 생각해보세요.";
        } else if (score >= 60.0) {
            return "비슷한 방향이에요. 더 구체적으로 표현해보세요.";
        } else if (score >= 40.0) {
            return "관련이 있지만 정확하지 않아요.";
        } else if (score >= 20.0) {
            return "방향이 조금 다른 것 같아요.";
        } else {
            return "전혀 다른 방향이에요. 다시 생각해보세요.";
        }
    }

    // ========== Private 메서드 ==========

    /**
     * 정규화: 공백 제거, 소문자 변환
     */
    private String normalize(String text) {
        return text.replaceAll("\\s+", "").toLowerCase();
    }

    /**
     * 포함 관계 점수 (부분 일치)
     */
    private double calculateContainmentScore(String input, String answer) {
        if (input.contains(answer) || answer.contains(input)) {
            int longer = Math.max(input.length(), answer.length());
            int shorter = Math.min(input.length(), answer.length());
            return (shorter / (double) longer) * 100;
        }
        return 0;
    }

    /**
     * 편집 거리 기반 점수 (Levenshtein Distance)
     */
    private double calculateEditDistanceScore(String input, String answer) {
        int distance = levenshteinDistance(input, answer);
        int maxLen = Math.max(input.length(), answer.length());

        if (maxLen == 0) return 100.0;

        double similarity = (1.0 - (distance / (double) maxLen)) * 100;
        return Math.max(0, similarity);
    }

    /**
     * Levenshtein Distance 계산
     */
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[s1.length()][s2.length()];
    }

    /**
     * 자모 유사도 (한글 특화)
     * 간단한 버전: 첫 글자와 마지막 글자 일치 여부
     */
    private double calculateJamoSimilarity(String input, String answer) {
        if (input.isEmpty() || answer.isEmpty()) {
            return 0;
        }

        double score = 0;

        // 첫 글자 일치
        if (input.charAt(0) == answer.charAt(0)) {
            score += 50;
        }

        // 마지막 글자 일치
        if (input.charAt(input.length() - 1) == answer.charAt(answer.length() - 1)) {
            score += 50;
        }

        return score;
    }
}
