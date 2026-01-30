package unicon.Achiva.domain.push.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import unicon.Achiva.global.common.UuidBaseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
    name = "link_token",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_link_token_jti", columnNames = "jti")
    },
    indexes = {
        @Index(name = "idx_link_token_jti", columnList = "jti"),
        @Index(name = "idx_link_token_expires_at", columnList = "expires_at")
    }
)
public class LinkToken extends UuidBaseEntity {

    @Column(name = "jti", length = 36, nullable = false, unique = true)
    private String jti;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Builder.Default
    @Column(name = "used", nullable = false)
    private boolean used = false;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    // Business methods
    public void markAsUsed() {
        this.used = true;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
}
