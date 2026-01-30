package unicon.Achiva.domain.push.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import unicon.Achiva.domain.push.entity.LinkToken;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface LinkTokenRepository extends JpaRepository<LinkToken, UUID> {

    /**
     * JWT ID(jti)로 링크 토큰을 조회합니다.
     * @param jti JWT ID
     * @return LinkToken (존재하지 않으면 Optional.empty())
     */
    Optional<LinkToken> findByJti(String jti);

    /**
     * 만료된 토큰들을 삭제합니다 (배치 작업용).
     * @param dateTime 기준 시각 (이 시각 이전에 만료된 토큰들을 삭제)
     */
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
