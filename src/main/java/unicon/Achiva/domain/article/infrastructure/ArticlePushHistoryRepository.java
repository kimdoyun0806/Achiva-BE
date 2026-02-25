package unicon.Achiva.domain.article.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import unicon.Achiva.domain.article.entity.ArticlePushHistory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public interface ArticlePushHistoryRepository extends JpaRepository<ArticlePushHistory, Long> {

    /**
     * 특정 작성자가 특정 수신자에게 오늘 이미 푸시를 보냈는지 확인
     */
    boolean existsByAuthorIdAndReceiverIdAndPushDate(UUID authorId, UUID receiverId, LocalDate pushDate);

    /**
     * 오래된 푸시 이력 삭제 (예: 30일 이상 된 데이터)
     * 주기적으로 실행하여 DB 용량 관리
     */
    @Modifying
    @Query("DELETE FROM ArticlePushHistory h WHERE h.createdAt < :cutoffDate")
    int deleteOldHistory(@Param("cutoffDate") LocalDateTime cutoffDate);
}
