package cloudcomputing.wordtreasure.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 설정
 * Python FastAPI 서비스 호출용
 */
@Configuration
public class RestTemplateConfig {

    @Value("${python.service.connect-timeout:5000}")
    private int connectTimeout;

    @Value("${python.service.read-timeout:10000}")
    private int readTimeout;

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // 연결 타임아웃 (Python 서버 연결 대기 시간)
        factory.setConnectTimeout(connectTimeout);

        // 읽기 타임아웃 (응답 대기 시간)
        factory.setReadTimeout(readTimeout);

        return new RestTemplate(factory);
    }
}
