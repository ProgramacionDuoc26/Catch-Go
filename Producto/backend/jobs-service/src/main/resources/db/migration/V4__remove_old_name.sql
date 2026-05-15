ALTER TABLE job_offers ALTER COLUMN name DROP NOT NULL;
ALTER TABLE job_offers RENAME COLUMN name TO name_old; -- Opcional, solo para no perder datos si hubiera
ALTER TABLE job_offers DROP COLUMN IF EXISTS name_old;
ALTER TABLE job_offers DROP COLUMN IF EXISTS name;
