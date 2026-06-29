-- V8__unique_user_id_profiles.sql
-- Delete duplicate profiles keeping only the latest one (highest id)
DELETE FROM profiles a
USING profiles b
WHERE a.id < b.id 
  AND a.user_id = b.user_id;

-- Add unique constraint to user_id to prevent duplicates
ALTER TABLE profiles 
ADD CONSTRAINT unique_user_id UNIQUE (user_id);
