DO $$ 
BEGIN 
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='job_offers' AND column_name='name') THEN
    ALTER TABLE job_offers ALTER COLUMN name DROP NOT NULL;
    ALTER TABLE job_offers RENAME COLUMN name TO name_old;
  END IF;
END $$;
ALTER TABLE job_offers DROP COLUMN IF EXISTS name_old;
ALTER TABLE job_offers DROP COLUMN IF EXISTS name;
