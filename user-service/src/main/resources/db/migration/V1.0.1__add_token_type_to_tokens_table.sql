ALTER TABLE my_user.tokens
ADD COLUMN token_type VARCHAR(255) NOT NULL DEFAULT 'ACCESS';