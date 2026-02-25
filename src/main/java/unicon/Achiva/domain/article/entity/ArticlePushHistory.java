package unicon.Achiva.domain.article.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import unicon.Achiva.global.common.LongBaseEntity;

import java.time.LocalDate;
import java.util.UUID;

/**
 * 게시글 푸시 알림 발송 이력
 * 하루 1회 발송 정책을 위한 테이블
 */
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    name = "article_push_history",
    indexes = {
        @Index(name = "idx_author_receiver_date", columnList = "author_id, receiver_id, push_date")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_author_receiver_date", columnNames = {"author_id", "receiver_id", "push_date"})
    }
)
public class ArticlePushHistory extends LongBaseEntity {

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(name = "receiver_id", nullable = false)
    private UUID receiverId;

    @Column(name = "push_date", nullable = false)
    private LocalDate pushDate;

    @Column(name = "article_id", nullable = false)
    private UUID articleId;
}
