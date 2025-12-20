package cloudcomputing.wordtreasure.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 1. 인증 정보 포함 허용 (쿠키, Authorization 헤더 등)
        config.setAllowCredentials(true);

        // 2. 허용할 Origin 설정
        // 로컬 개발: React 개발 서버
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOrigin("http://localhost:5174"); // Vite 포트 충돌 시 대비

        // 프로덕션: Vercel 배포 주소 (배포 후 추가)
        config.addAllowedOrigin("https://word-treasure-fe.vercel.app");

        // 3. 허용할 HTTP 메서드
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("PATCH");
        config.addAllowedMethod("OPTIONS");

        // 4. 허용할 헤더
        config.addAllowedHeader("*");

        // 5. 노출할 헤더 (프론트엔드에서 읽을 수 있는 헤더)
        config.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Set-Cookie"
        ));

        // 6. preflight 요청 캐싱 시간 (초)
        config.setMaxAge(3600L);

        // 모든 경로에 대해 적용
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
