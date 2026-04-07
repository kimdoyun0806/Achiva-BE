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

INSERT INTO organization (name, description, password, active, created_at, updated_at, is_deleted)
SELECT
    'Legacy Organization',
    'Backfilled organization for pre-organization data',
    NULL,
    b'1',
    NOW(6),
    NOW(6),
    b'0'
WHERE NOT EXISTS (
    SELECT 1
    FROM organization
    WHERE name = 'Legacy Organization'
);

SET @legacy_organization_id := (
    SELECT id
    FROM organization
    WHERE name = 'Legacy Organization'
    LIMIT 1
);

ALTER TABLE member
    ADD COLUMN organization_id BIGINT NULL;

UPDATE member
SET organization_id = @legacy_organization_id
WHERE organization_id IS NULL;

ALTER TABLE member
    MODIFY COLUMN organization_id BIGINT NOT NULL,
    ADD KEY idx_member_organization_id (organization_id),
    ADD CONSTRAINT fk_member_organization FOREIGN KEY (organization_id) REFERENCES organization (id);

ALTER TABLE moim
    ADD COLUMN organization_id BIGINT NULL;

UPDATE moim
SET organization_id = @legacy_organization_id
WHERE organization_id IS NULL;

ALTER TABLE moim
    MODIFY COLUMN organization_id BIGINT NOT NULL,
    ADD KEY idx_moim_organization_id (organization_id),
    ADD CONSTRAINT fk_moim_organization FOREIGN KEY (organization_id) REFERENCES organization (id);
