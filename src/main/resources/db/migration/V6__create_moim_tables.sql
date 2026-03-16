-- ============================================================================
-- Table: moim
-- Description: Group/Meeting entities matching the Moim domain
-- ============================================================================
CREATE TABLE IF NOT EXISTS moim (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    max_member INT NOT NULL,
    is_private BOOLEAN NOT NULL DEFAULT FALSE,
    password VARCHAR(255),
    is_official BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    INDEX idx_moim_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================================
-- Table: moim_member
-- Description: Junction table mapping members to a moim with roles
-- ============================================================================
CREATE TABLE IF NOT EXISTS moim_member (
    id BIGINT NOT NULL AUTO_INCREMENT,
    moim_id BIGINT NOT NULL,
    member_id BINARY(16) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    INDEX idx_moim_member_moim_id (moim_id),
    INDEX idx_moim_member_member_id (member_id),
    INDEX idx_moim_member_is_deleted (is_deleted),
    CONSTRAINT fk_moim_member_moim
        FOREIGN KEY (moim_id) REFERENCES moim(id) ON DELETE CASCADE,
    CONSTRAINT fk_moim_member_member
        FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================================
-- Table: moim_categories
-- Description: ElementCollection table for Moim categories
-- ============================================================================
CREATE TABLE IF NOT EXISTS moim_categories (
    moim_id BIGINT NOT NULL,
    categories VARCHAR(50),
    CONSTRAINT fk_moim_categories_moim
        FOREIGN KEY (moim_id) REFERENCES moim(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
