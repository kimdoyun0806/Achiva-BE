package unicon.Achiva.domain.push.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LinkTokenResponse {
    private String linkToken;   // JWT 형식의 linkToken
    private int expiresIn;      // 유효 기간 (초 단위, 예: 300)
}
