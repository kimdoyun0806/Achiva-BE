package unicon.Achiva.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import unicon.Achiva.global.security.login.filter.UserInitializedAuthorizationManager;
import unicon.Achiva.global.security.login.handler.UserNotInitializedDeniedHandler;

import java.util.HashSet;
import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * SecurityFilterChain
     * Cognito OIDC JWT 검증을 표준 Resource Server로 활성화하고,
     * 기존 인가 규칙은 그대로 유지한다.
     */
    @Bean
    public SecurityFilterChain filterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http,
                                           JwtDecoder jwtDecoder,
                                           Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthConverter,
                                           UserInitializedAuthorizationManager initManager,
                                           UserNotInitializedDeniedHandler deniedHandler) throws Exception {

        http
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(h -> h.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .authorizeHttpRequests(auth -> auth
                        // 정적/유틸
                        .requestMatchers("/", "/css/**", "/images/**", "/js/**", "/favicon.ico").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        // 공개 인증 API (명시적 나열)
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/check-email",
                                "/api/auth/check-nickname",
                                "/api/auth/send-verification-code",
                                "/api/auth/verify-code"
                        ).permitAll()
                        // 회원가입은 JWT 필요 (authenticated, but Member 존재 불필요)
                        .requestMatchers("/api/auth/register").authenticated()
                        // 푸시 관련 API
                        .requestMatchers("/api/push/link-intent").authenticated()  // Cognito JWT 필요
                        .requestMatchers("/api/push/register").permitAll()         // linkToken 자체 검증
                        .requestMatchers("/api/push/send").access(initManager)     // Member 존재 확인 필수
                        // 유저 초기화 확인용
                        .requestMatchers("/api/auth/isinit").authenticated()
                        // CORS preflight 안정성
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 그 외 보호
                        .anyRequest().access(initManager)
                )
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthConverter)
                        )
                ).exceptionHandling(ex -> ex
                        .accessDeniedHandler(deniedHandler)
                );

        return http.build();
    }

    /**
     * jwtDecoder
     * Cognito Issuer 기반으로 NimbusJwtDecoder를 생성하고,
     * 기본 검증(서명/만료/발급자)에 선택적으로 audience 검증을 추가한다.
     */
    @Bean
    public JwtDecoder jwtDecoder(@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuer,
                                 @Value("${app.security.expected-client-id:}") String expectedClientId) {
        log.info("JWT Decoder 초기화 시작 - issuer: {}", issuer);

        NimbusJwtDecoder decoder = JwtDecoders.fromIssuerLocation(issuer);
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);

        if (expectedClientId == null || expectedClientId.isBlank()) {
            log.info("[JWT] expectedClientId가 비어 있음: audience 검증은 수행하지 않음");
            decoder.setJwtValidator(withIssuer);
        } else {
            log.info("[JWT] expectedClientId 설정됨: audience 검증 추가 - {}", expectedClientId);
            OAuth2TokenValidator<Jwt> withClientId = new JwtClaimValidator<>("client_id", clientId -> {
                if (clientId instanceof String c) return c.equals(expectedClientId);
                if (clientId instanceof java.util.Collection<?> c) return c.contains(expectedClientId);
                log.info("[JWT] JWT client_id 형식이 예기치 않음: {}", clientId);
                return false;
            });
            decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, withClientId));
        }

        log.info("[JWT] JWT Decoder 초기화 완료");
        return decoder;
    }

    /**
     * jwtAuthConverter
     * scope(claim) 및 cognito:groups(claim)를 Spring Security 권한으로 변환한다.
     * - scope -> SCOPE_xxx
     * - cognito:groups -> ROLE_<GROUP>
     */
    @Bean
    public Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthConverter() {
        JwtGrantedAuthoritiesConverter scopes = new JwtGrantedAuthoritiesConverter();
        scopes.setAuthorityPrefix("SCOPE_");

        return jwt -> {
            var authorities = new HashSet<>(scopes.convert(jwt));
            var groups = jwt.getClaimAsStringList("cognito:groups");
            if (groups != null) {
                groups.forEach(g ->
                        authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + g))
                );
            }
            String principalName = resolveUsername(jwt);
            return new JwtAuthenticationToken(jwt, authorities, principalName);
        };
    }

    /**
     * resolveUsername
     * Principal 표시용 사용자명을 email -> cognito:username -> sub 순으로 결정한다.
     */
    private String resolveUsername(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        if (email != null) return email;
        String username = jwt.getClaimAsString("cognito:username");
        if (username != null) return username;
        return jwt.getSubject();
    }

    /**
     * corsConfigurationSource
     * CORS 설정을 적용한다. 운영 환경에 맞는 Origin 화이트리스트를 권장한다.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // 운영 환경에 맞게 구체적으로 나열
        config.setAllowedOrigins(List.of(
                "https://www.achiva.kr/",
                "https://iworkouttoday.com",
                "https://container-service-1.wffkggdq3jc9m.ap-northeast-2.cs.amazonlightsail.com",
                "https://achiva-fe-git-develop-achiva.vercel.app",  // Vercel 개발 환경
                "http://localhost:8081",
                "http://localhost:3000",
                "http://localhost:8080" // 개발용
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
