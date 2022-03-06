CREATE TABLE users(
    id SERIAL NOT NULL PRIMARY KEY,
    user_name VARCHAR(64) NOT NULL,
    first_name VARCHAR(64) NOT NULL,
    last_name VARCHAR(64) NOT NULL,
    email VARCHAR(128) NOT NULL,
    password_hash VARCHAR(256) NOT NULL
)