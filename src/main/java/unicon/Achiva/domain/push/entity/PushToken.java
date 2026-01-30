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
    name = "push_token",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_push_token_member_token",
            columnNames = {"member_id", "expo_push_token"}
        )
    },
    indexes = {
        @Index(name = "idx_push_token_member_id", columnList = "member_id"),
        @Index(name = "idx_push_token_is_active", columnList = "is_active")
    }
)
public class PushToken extends UuidBaseEntity {

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "expo_push_token", length = 200, nullable = false)
    private String expoPushToken;

    @Column(name = "device_info", length = 255)
    private String deviceInfo;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    // Business methods
    public void deactivate() {
        this.isActive = false;
    }

    public void reactivate() {
        this.isActive = true;
    }

    public void updateLastUsedAt() {
        this.lastUsedAt = LocalDateTime.now();
    }

    public void updateDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
}
