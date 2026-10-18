CREATE TABLE user_accounts (
    id UUID PRIMARY KEY,
    email VARCHAR(180) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_user_accounts_role CHECK (role IN ('ADMIN', 'BANKER', 'ANALYST', 'VIEWER'))
);
