package unicon.Achiva.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        servers = {
                @Server(url = "https://container-service-1.wffkggdq3jc9m.ap-northeast-2.cs.amazonlightsail.com", description = "Production Server"),
//                @Server(url = "https://api.achiva.kr", description = "old Production Server"),
                @Server(url = "http://localhost:8080", description = "Local Serever")
        }
)
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("accessToken", new SecurityScheme()
                                .name("Authorization")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList("accessToken"))
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title("Achiva")
                .description("Achiva API 명세서. Organization 목록 조회를 제외한 모든 조회/수정은 로그인한 사용자의 organization 범위 내에서만 동작합니다. 명세서 페이지 로그아웃은 {baseurl}/logout 입력(테스트 환경과 별도임)")
                .version("1.0.0");
    }
}
