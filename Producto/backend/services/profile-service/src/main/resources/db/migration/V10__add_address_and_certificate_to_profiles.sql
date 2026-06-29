-- V10__add_address_and_certificate_to_profiles.sql
-- Add dedicated columns for address, certificate_url, and representative_name to profiles table
ALTER TABLE profiles 
  ADD COLUMN IF NOT EXISTS address TEXT,
  ADD COLUMN IF NOT EXISTS certificate_url TEXT,
  ADD COLUMN IF NOT EXISTS representative_name TEXT;
