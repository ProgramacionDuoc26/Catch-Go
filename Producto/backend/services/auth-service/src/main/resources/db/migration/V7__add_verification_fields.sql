ALTER TABLE user_accounts 
ADD COLUMN IF NOT EXISTS is_verified BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS verification_otp VARCHAR(6);

UPDATE user_accounts SET is_verified = TRUE;
