CREATE TABLE IF NOT EXISTS t_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    login VARCHAR(128)  NOT NULL UNIQUE,
    name VARCHAR(256) NOT NULL,
    email VARCHAR(256) NOT NULL UNIQUE,
    birthdate DATE,
    balance DECIMAL(10,2) NOT NULL DEFAULT 0.00
);



