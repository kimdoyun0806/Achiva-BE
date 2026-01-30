-- ============================================================================
-- Achiva Database Migration V3
-- Description: Update existing members' push_enabled to TRUE (new default)
-- ============================================================================

-- 기존 회원들의 push_enabled를 TRUE로 변경
UPDATE member
SET push_enabled = TRUE
WHERE push_enabled = FALSE;
