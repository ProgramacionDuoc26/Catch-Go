CREATE TABLE IF NOT EXISTS profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(255),
    birth_date DATE,
    photo_url VARCHAR(255),
    cv_url VARCHAR(255),
    description TEXT,
    bank_name VARCHAR(255),
    account_type VARCHAR(255),
    account_number VARCHAR(255),
    type VARCHAR(255)
);
