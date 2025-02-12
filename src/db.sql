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
CREATE TABLE transactions (
                              transaction_id SERIAL PRIMARY KEY,
                              from_account VARCHAR(50) REFERENCES accounts(account_number) ON DELETE SET NULL,
                              to_account VARCHAR(50) REFERENCES accounts(account_number) ON DELETE SET NULL,
                              amount DECIMAL(15,2) NOT NULL,
                              transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_user_id ON accounts(user_id);
CREATE INDEX idx_account_number ON accounts(account_number);
CREATE INDEX idx_transaction_date ON transactions(transaction_date);