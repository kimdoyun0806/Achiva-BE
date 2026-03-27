-- ============================================================================
-- Add moim score support
-- - Adds cached total score to moim
-- - Creates per-member moim_score table
-- - Backfills initial scores using all existing articles for current moim members
-- ============================================================================

ALTER TABLE moim
    ADD COLUMN score INT NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS moim_score (
    id BIGINT NOT NULL AUTO_INCREMENT,
    moim_id BIGINT NOT NULL,
    member_id BINARY(16) NOT NULL,
    score INT NOT NULL DEFAULT 0,
    left_at DATETIME(6),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    INDEX idx_moim_score_moim_id (moim_id),
    INDEX idx_moim_score_member_left_at (member_id, left_at),
    INDEX idx_moim_score_member_created_left (member_id, created_at, left_at),
    INDEX idx_moim_score_moim_member_left (moim_id, member_id, left_at),
    INDEX idx_moim_score_is_deleted (is_deleted),
    CONSTRAINT fk_moim_score_moim
        FOREIGN KEY (moim_id) REFERENCES moim(id) ON DELETE CASCADE,
    CONSTRAINT fk_moim_score_member
        FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO moim_score (moim_id, member_id, score, left_at, created_at, updated_at, is_deleted)
SELECT
    mm.moim_id,
    mm.member_id,
    COALESCE(article_counts.article_count, 0) AS score,
    NULL AS left_at,
    DATE(CONVERT_TZ(UTC_TIMESTAMP(6), '+00:00', '+09:00'))
        - INTERVAL WEEKDAY(CONVERT_TZ(UTC_TIMESTAMP(6), '+00:00', '+09:00')) DAY AS created_at,
    DATE(CONVERT_TZ(UTC_TIMESTAMP(6), '+00:00', '+09:00'))
        - INTERVAL WEEKDAY(CONVERT_TZ(UTC_TIMESTAMP(6), '+00:00', '+09:00')) DAY AS updated_at,
    FALSE AS is_deleted
FROM moim_member mm
LEFT JOIN (
    SELECT a.member_id, COUNT(*) AS article_count
    FROM article a
    WHERE a.is_deleted = FALSE
    GROUP BY a.member_id
) article_counts ON article_counts.member_id = mm.member_id
WHERE mm.is_deleted = FALSE;

UPDATE moim m
LEFT JOIN (
    SELECT ms.moim_id, COALESCE(SUM(ms.score), 0) AS total_score
    FROM moim_score ms
    WHERE ms.is_deleted = FALSE
    GROUP BY ms.moim_id
) score_sum ON score_sum.moim_id = m.id
SET m.score = COALESCE(score_sum.total_score, 0)
WHERE m.is_deleted = FALSE;
