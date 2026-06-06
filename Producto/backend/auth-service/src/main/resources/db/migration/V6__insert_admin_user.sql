INSERT INTO user_accounts (name, email, password, tipo)
VALUES ('Miguel Admin', 'miguel@admin.cl', '$2b$10$F3233tW4cf3OZT2.2XWUreMUDFoyWtFwfLjN3G4h6787ZjgjCaXJG', 'ADMIN')
ON CONFLICT (email) DO NOTHING;
