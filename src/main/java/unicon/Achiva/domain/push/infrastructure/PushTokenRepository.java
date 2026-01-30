package unicon.Achiva.domain.push.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import unicon.Achiva.domain.push.entity.PushToken;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PushTokenRepository extends JpaRepository<PushToken, UUID> {

    /**
     * 특정 회원의 특정 Expo 푸시 토큰을 조회합니다.
     * @param memberId 회원 ID
     * @param expoPushToken Expo 푸시 토큰
     * @return PushToken (존재하지 않으면 Optional.empty())
     */
    Optional<PushToken> findByMemberIdAndExpoPushToken(UUID memberId, String expoPushToken);

    /**
     * 특정 회원의 활성화된 모든 푸시 토큰을 조회합니다.
     * @param memberId 회원 ID
     * @return 활성화된 PushToken 리스트
     */
    List<PushToken> findAllByMemberIdAndIsActiveTrue(UUID memberId);

    /**
     * 푸시 알림이 활성화된 회원들의 활성 토큰을 조회합니다.
     * Member 테이블의 pushEnabled가 true인 경우만 조회합니다.
     * @return 활성화된 PushToken 리스트
     */
    @Query("SELECT pt FROM PushToken pt JOIN Member m ON pt.memberId = m.id WHERE m.pushEnabled = true AND pt.isActive = true AND pt.isDeleted = false")
    List<PushToken> findAllActiveByPushEnabled();
}
