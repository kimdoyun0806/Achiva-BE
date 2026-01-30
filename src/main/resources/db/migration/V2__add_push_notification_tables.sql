-- ============================================================================
-- Achiva Database Migration - Push Notification Tables
-- Version: 2
-- Description: Add PushToken and LinkToken tables for push notification system
-- ============================================================================

-- ============================================================================
-- Table: push_token
-- Description: Stores Expo push tokens for user devices
-- ============================================================================
CREATE TABLE IF NOT EXISTS push_token (
    id BINARY(16) NOT NULL,
    member_id BINARY(16) NOT NULL,
    expo_push_token VARCHAR(200) NOT NULL,
    device_info VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_used_at DATETIME(6),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    CONSTRAINT uk_push_token_member_token UNIQUE (member_id, expo_push_token),
    INDEX idx_push_token_member_id (member_id),
    INDEX idx_push_token_is_active (is_active),
    INDEX idx_push_token_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Table: link_token
-- Description: Stores JWT tokens for secure deep linking
-- ============================================================================
CREATE TABLE IF NOT EXISTS link_token (
    id BINARY(16) NOT NULL,
    jti VARCHAR(36) NOT NULL,
    member_id BINARY(16) NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    expires_at DATETIME(6) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    CONSTRAINT uk_link_token_jti UNIQUE (jti),
    INDEX idx_link_token_jti (jti),
    INDEX idx_link_token_expires_at (expires_at),
    INDEX idx_link_token_member_id (member_id),
    INDEX idx_link_token_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
