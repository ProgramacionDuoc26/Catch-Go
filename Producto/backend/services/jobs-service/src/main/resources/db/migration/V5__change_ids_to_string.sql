DO $$ 
BEGIN 
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='job_offers' AND column_name='empresa_id' AND data_type='bigint') THEN
    ALTER TABLE job_offers ALTER COLUMN empresa_id TYPE VARCHAR(255) USING empresa_id::varchar;
  END IF;
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='job_applications' AND column_name='user_id' AND data_type='bigint') THEN
    ALTER TABLE job_applications ALTER COLUMN user_id TYPE VARCHAR(255) USING user_id::varchar;
  END IF;
END $$;
