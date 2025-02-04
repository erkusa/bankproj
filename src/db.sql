CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       email VARCHAR(100) UNIQUE NOT NULL,
                       password_hash TEXT NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO users (username, email, password_hash)
VALUES
    ('test_user1', 'test1@example.com', 'hashed_password_1'),
    ('test_user2', 'test2@example.com', 'hashed_password_2'),
    ('test_user3', 'test3@example.com', 'hashed_password_3');