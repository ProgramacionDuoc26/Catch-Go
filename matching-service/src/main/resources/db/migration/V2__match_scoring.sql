ALTER TABLE match_suggestions
    ADD COLUMN IF NOT EXISTS job_offer_id BIGINT,
    ADD COLUMN IF NOT EXISTS worker_profile_id BIGINT,
    ADD COLUMN IF NOT EXISTS worker_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS total_score DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS skills_score DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS experience_score DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS distance_score DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS availability_score DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

DELETE FROM match_suggestions
WHERE job_offer_id IS NULL
   OR worker_profile_id IS NULL;

UPDATE match_suggestions SET worker_name = 'Trabajador' WHERE worker_name IS NULL;
UPDATE match_suggestions SET total_score = 0 WHERE total_score IS NULL;
UPDATE match_suggestions SET skills_score = 0 WHERE skills_score IS NULL;
UPDATE match_suggestions SET experience_score = 0 WHERE experience_score IS NULL;
UPDATE match_suggestions SET distance_score = 0 WHERE distance_score IS NULL;
UPDATE match_suggestions SET availability_score = 0 WHERE availability_score IS NULL;
UPDATE match_suggestions SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL;

ALTER TABLE match_suggestions
    ALTER COLUMN job_offer_id SET NOT NULL,
    ALTER COLUMN worker_profile_id SET NOT NULL,
    ALTER COLUMN worker_name SET NOT NULL,
    ALTER COLUMN total_score SET NOT NULL,
    ALTER COLUMN skills_score SET NOT NULL,
    ALTER COLUMN experience_score SET NOT NULL,
    ALTER COLUMN distance_score SET NOT NULL,
    ALTER COLUMN availability_score SET NOT NULL,
    ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE match_suggestions
    DROP COLUMN IF EXISTS name;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uq_match_suggestion_offer_worker'
    ) THEN
        ALTER TABLE match_suggestions
            ADD CONSTRAINT uq_match_suggestion_offer_worker
            UNIQUE (job_offer_id, worker_profile_id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_match_suggestions_job_offer_score
    ON match_suggestions (job_offer_id, total_score DESC);
