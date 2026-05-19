ALTER TABLE job_offers ALTER COLUMN empresa_id TYPE VARCHAR(255) USING empresa_id::varchar;
ALTER TABLE job_applications ALTER COLUMN user_id TYPE VARCHAR(255) USING user_id::varchar;
