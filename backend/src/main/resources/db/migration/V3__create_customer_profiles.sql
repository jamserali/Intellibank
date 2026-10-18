ALTER TABLE user_accounts DROP CONSTRAINT ck_user_accounts_role;
ALTER TABLE user_accounts ADD CONSTRAINT ck_user_accounts_role CHECK (role IN ('CUSTOMER', 'ADMIN', 'BANKER', 'ANALYST', 'VIEWER'));
CREATE TABLE customer_profiles (
 user_id UUID PRIMARY KEY REFERENCES user_accounts(id),
 full_name VARCHAR(120) NOT NULL,
 phone VARCHAR(25) NOT NULL,
 terms_accepted_at TIMESTAMP WITH TIME ZONE NOT NULL
);
