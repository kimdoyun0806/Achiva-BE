-- ============================================================================
-- Add new goal/setting columns to moim table
-- ============================================================================
ALTER TABLE moim
    ADD COLUMN target_amount INT NOT NULL DEFAULT 100,
    ADD COLUMN poke_days INT NOT NULL DEFAULT 5;
