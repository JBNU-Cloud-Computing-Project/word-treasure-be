package cloudcomputing.wordtreasure.model.game.service.similarity;

import cloudcomputing.wordtreasure.model.game.service.SimilarityCalculator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Python FastAPI 유사도 계산 서비스 연동 구현체
 */
@Slf4j
@Component
@Profile("prod")
@Primary
@RequiredArgsConstructor
public class PythonFastApiSimilarityCalculator implements SimilarityCalculator {

    private final RestTemplate restTemplate;

    @Value("${python.service.url:http://localhost:8000}")
    private String pythonServiceUrl;

    @Override
    public BigDecimal calculateSimilarity(String userInput, String answer) {
        log.debug("Python 서비스 유사도 계산 요청 - userInput: {}, answer: {}", userInput, answer);

        try {
            SimilarityResponseDto response = callPythonService(userInput, answer);

            log.debug("Python 서비스 응답 - 유사도: {}%, 처리 시간: {}ms",
                    response.getSimilarityScore(), response.getProcessingTimeMs());

            return BigDecimal.valueOf(response.getSimilarityScore());

        } catch (RestClientException e) {
            log.error("Python 서비스 호출 실패: {}", e.getMessage(), e);
            throw new RuntimeException("유사도 계산 서비스 연결 실패. Python 서버가 실행 중인지 확인하세요.", e);
        } catch (Exception e) {
            log.error("Python 서비스 처리 중 오류: {}", e.getMessage(), e);
            throw new RuntimeException("유사도 계산 중 오류 발생", e);
        }
    }

    @Override
    public String generateHint(String userInput, String answer, BigDecimal similarity) {
        log.debug("Python 서비스 힌트 생성 요청 - userInput: {}, answer: {}", userInput, answer);

        try {
            // Python 서비스 호출 (이미 힌트가 포함됨)
            SimilarityResponseDto response = callPythonService(userInput, answer);

            log.debug("Python 서비스 힌트: {}", response.getHint());

            return response.getHint();

        } catch (RestClientException e) {
            log.error("Python 서비스 호출 실패: {}", e.getMessage(), e);
            throw new RuntimeException("힌트 생성 서비스 연결 실패. Python 서버가 실행 중인지 확인하세요.", e);
        } catch (Exception e) {
            log.error("Python 서비스 힌트 생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException("힌트 생성 중 오류 발생", e);
        }
    }

    /**
     * Python FastAPI 서비스 호출 (유사도 + 힌트 한 번에)
     */
    private SimilarityResponseDto callPythonService(String userInput, String answer) {
        String url = pythonServiceUrl + "/api/similarity/calculate";

        // 요청 생성
        SimilarityRequestDto request = new SimilarityRequestDto(userInput, answer);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<SimilarityRequestDto> entity = new HttpEntity<>(request, headers);

        // Python 서비스 호출
        ResponseEntity<SimilarityResponseDto> response = restTemplate.
                postForEntity(
                        url,
                        entity,
                        SimilarityResponseDto.class
                );

        if (response.getBody() == null) {
            throw new RuntimeException("Python 서비스로부터 응답을 받지 못했습니다.");
        }

        return response.getBody();
    }

    /**
     * 요청 DTO
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class SimilarityRequestDto {
        @JsonProperty("user_input")
        private String userInput;
        @JsonProperty("answer")
        private String answer;
    }

    /**
     * 응답 DTO
     */
    @Data
    @NoArgsConstructor
    private static class SimilarityResponseDto {
        @JsonProperty("similarity_score")
        private Double similarityScore;
        @JsonProperty("hint")
        private String hint;
        @JsonProperty("category_match")
        private Boolean categoryMatch;
        @JsonProperty("breakdown")
        private Map<String, Double> breakdown;
        @JsonProperty("processing_time_ms")
        private Double processingTimeMs;
    }
}
