CREATE TABLE IF NOT EXISTS organization (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    password VARCHAR(255),
    active BIT(1) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    is_deleted BIT(1) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_organization_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE member
    ADD COLUMN organization_id BIGINT NOT NULL,
    ADD KEY idx_member_organization_id (organization_id),
    ADD CONSTRAINT fk_member_organization FOREIGN KEY (organization_id) REFERENCES organization (id);

ALTER TABLE moim
    ADD COLUMN organization_id BIGINT NOT NULL,
    ADD KEY idx_moim_organization_id (organization_id),
    ADD CONSTRAINT fk_moim_organization FOREIGN KEY (organization_id) REFERENCES organization (id);
