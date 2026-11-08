-- Sample records for the local Module 9.1 JDBC discussion example.
-- Run this once when creating a new demo database.
-- Names and balances are fictional role-play account data.

CREATE TABLE IF NOT EXISTS accounts (
    id INT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    balance DECIMAL(10, 2) NOT NULL
);

INSERT INTO accounts (id, username, balance) VALUES
    (1, 'Alex_Rivera', 1250.50),
    (2, 'Jordan_Reed', 875.25),
    (3, 'Taylor_Brooks', 2300.00);
