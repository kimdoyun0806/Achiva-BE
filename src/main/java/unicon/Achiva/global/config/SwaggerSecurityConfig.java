package unicon.Achiva.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Swagger UI와 OpenAPI 문서를 OIDC 세션으로 보호하고,
 * cognito:groups 기반 ROLE 매핑을 통해 ROLE_DOCS 보유자만 접근을 허용한다.
 */
@Configuration
//@EnableWebSecurity(debug = true)
public class SwaggerSecurityConfig {

    /**
     * Swagger 전용 보안 체인.
     * ROLE_DOCS 권한이 있는 사용자만 /swagger-ui/**, /v3/api-docs/** 접근 가능하도록 제한한다.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain swaggerOidcChain(HttpSecurity http,
                                                OAuth2UserService<OidcUserRequest, OidcUser> docsOidcUserService,
                                                AuthenticationSuccessHandler successHandler) throws Exception {
        http
                // Swagger와 OAuth2 관련 경로를 함께 매칭
                .securityMatcher("/swagger-ui/**", "/v3/api-docs/**", "/login/**", "/oauth2/**", "/api/debug/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(a -> a
                                .requestMatchers("/login/**", "/oauth2/**", "/error").permitAll()
                                .requestMatchers("/api/debug/token").hasRole("DOCS")
                                .anyRequest().hasRole("DOCS")
//                                .anyRequest().permitAll()
                )
                .oauth2Login(o -> o
                        .userInfoEndpoint(u -> u.oidcUserService(docsOidcUserService))
                        .successHandler(successHandler)
                );
        return http.build();
    }

    /**
     * 로그인 성공 시 토큰 로그 출력 (개발자 디버깅용)
     */
    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return (request, response, authentication) -> {
            var principal = (OidcUser) authentication.getPrincipal();
            System.out.println("✅ Swagger 로그인 성공");
//            System.out.println("Access Token: " + principal.getIdToken().getTokenValue());
            System.out.println("Email: " + principal.getEmail());
            response.sendRedirect("/swagger-ui/index.html");
        };
    }

    /**
     * OIDC 사용자 정보를 로드한 뒤, cognito:groups에 따라 ROLE_DOCS 권한을 부여한다.
     * groups에 "docs" 또는 "Admin"이 포함되면 ROLE_DOCS를 추가한다.
     */
    @Bean
    public OAuth2UserService<OidcUserRequest, OidcUser> docsOidcUserService() {
        return (OidcUserRequest req) -> {
            OidcUserService delegate = new OidcUserService();
            OidcUser user = delegate.loadUser(req);

            Set<GrantedAuthority> authorities = new HashSet<>(user.getAuthorities());
            Object rawGroups = user.getClaims().get("cognito:groups");
            if (rawGroups instanceof Collection<?> c) {
                for (Object g : c) {
                    if (Objects.equals(String.valueOf(g), "docs") || Objects.equals(String.valueOf(g), "Admin")) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_DOCS"));
                        break;
                    }
                }
            }

            return new DefaultOidcUser(
                    authorities,
                    user.getIdToken(),
                    user.getUserInfo(),
                    "email"
            );
        };
    }
}