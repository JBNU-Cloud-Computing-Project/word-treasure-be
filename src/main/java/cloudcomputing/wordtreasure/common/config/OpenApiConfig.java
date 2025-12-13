package cloudcomputing.wordtreasure.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "워드트레저 API 문서",
                description = "워드트레저 서비스의 REST API에 대한 스펙을 제공합니다. 모든 설명과 예시는 한글로 제공됩니다.",
                version = "v1",
                contact = @Contact(name = "워드트레저 팀", email = "support@wordtreasure.example"),
                license = @License(name = "MIT License")
        ),
        servers = {
                @Server(description = "로컬 서버", url = "http://localhost:8080"),
                @Server(description = "개발 서버", url = "https://dev.wordtreasure.example")
        }
)
public class OpenApiConfig {
}
