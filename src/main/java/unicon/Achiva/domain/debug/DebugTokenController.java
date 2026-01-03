package unicon.Achiva.domain.debug;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DebugTokenController {

    /**
     * 로그인된 사용자의 토큰 및 클레임 정보를 반환한다.
     * Swagger 디버깅용 — 운영 환경에서는 비활성화 권장.
     */
    @GetMapping("/api/debug/token")
    @Operation(
            summary = "로그인된 DOCS 유저의 ID Token 및 클레임 조회",
            description = """
                    Swagger UI에서 Cognito OIDC로 로그인한 사용자의 정보를 확인합니다.
                    이 엔드포인트는 개발·디버깅용이며, 외부 토큰이나 익명 접근은 허용되지 않습니다.
                    """
    )
    public Map<String, Object> getTokenInfo(
            @AuthenticationPrincipal OidcUser user,
            @RegisteredOAuth2AuthorizedClient("cognito") OAuth2AuthorizedClient authorizedClient
    ) {
        if (user == null || authorizedClient == null) {
            return Map.of("error", "Not authenticated");
        }

        return Map.of(
                "email", user.getEmail(),
                "idToken", user.getIdToken().getTokenValue(),
                "accessToken", authorizedClient.getAccessToken().getTokenValue(),
                "claims", user.getClaims()
        );
    }
}
