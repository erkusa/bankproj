CREATE TABLE users (
    user_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL
);

CREATE TABLE accounts (
    account_number VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) REFERENCES users(user_id) ON DELETE CASCADE,
    balance DECIMAL(15,2) DEFAULT 0.0 NOT NULL,
    currency VARCHAR(10) NOT NULL
);