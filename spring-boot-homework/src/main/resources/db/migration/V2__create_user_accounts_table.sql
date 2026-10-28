CREATE TABLE user_accounts (
    id            UUID         NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    CONSTRAINT pk_user_accounts PRIMARY KEY (id),
    CONSTRAINT uq_user_accounts_email UNIQUE (email),
    CONSTRAINT ck_user_accounts_role CHECK (role IN ('USER', 'ADMIN'))
);
