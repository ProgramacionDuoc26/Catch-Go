-- V3__add_skills_to_profiles.sql
-- Add skills column as JSONB for PostgreSQL to store structured profile skill data

ALTER TABLE profiles ADD COLUMN IF NOT EXISTS skills TEXT;
-- Note: Using TEXT for simplicity in the Java entity mapping if not using custom Json converters, 
-- but in PostgreSQL we can cast it to JSON if needed for queries.
-- For now, we store it as a JSON string.
