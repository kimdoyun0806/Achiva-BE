-- ============================================================================
-- Deduplicate friendship rows by unordered member pair and enforce uniqueness
-- ============================================================================

CREATE TEMPORARY TABLE friendship_keep_ids AS
SELECT ranked.id
FROM (
    SELECT
        f.id,
        ROW_NUMBER() OVER (
            PARTITION BY LEAST(HEX(f.requester_id), HEX(f.receiver_id)),
                         GREATEST(HEX(f.requester_id), HEX(f.receiver_id))
            ORDER BY
                CASE f.status
                    WHEN 'BLOCKED' THEN 4
                    WHEN 'ACCEPTED' THEN 3
                    WHEN 'PENDING' THEN 2
                    WHEN 'REJECTED' THEN 1
                    ELSE 0
                END DESC,
                COALESCE(f.updated_at, f.created_at) DESC,
                f.id DESC
        ) AS rn
    FROM friendship f
) ranked
WHERE ranked.rn = 1;

DELETE f
FROM friendship f
LEFT JOIN friendship_keep_ids keep_ids ON keep_ids.id = f.id
WHERE keep_ids.id IS NULL;

DROP TEMPORARY TABLE friendship_keep_ids;

ALTER TABLE friendship
ADD COLUMN member_low_id BINARY(16)
    GENERATED ALWAYS AS (IF(HEX(requester_id) <= HEX(receiver_id), requester_id, receiver_id)) STORED,
ADD COLUMN member_high_id BINARY(16)
    GENERATED ALWAYS AS (IF(HEX(requester_id) <= HEX(receiver_id), receiver_id, requester_id)) STORED;

ALTER TABLE friendship
ADD CONSTRAINT uk_friendship_member_pair UNIQUE (member_low_id, member_high_id);
