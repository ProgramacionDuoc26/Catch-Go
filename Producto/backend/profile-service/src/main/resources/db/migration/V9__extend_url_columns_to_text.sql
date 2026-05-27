-- V9__extend_url_columns_to_text.sql
-- Extend URL columns to TEXT to support long Supabase Storage URLs
ALTER TABLE profiles 
  ALTER COLUMN photo_url TYPE TEXT,
  ALTER COLUMN cv_url TYPE TEXT;
