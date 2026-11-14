-- Zak Nizam | CSD 420 | Module 10.2 | November 14, 2026
-- Optional home database setup only. Run separately before the application.
-- The application itself only reads and updates an existing fans table.
USE databasedb;

CREATE TABLE IF NOT EXISTS fans (
    ID INT PRIMARY KEY,
    firstname VARCHAR(25),
    lastname VARCHAR(25),
    favoriteteam VARCHAR(25)
);

-- Add fictional examples without replacing records that are already present.
INSERT INTO fans (ID, firstname, lastname, favoriteteam)
SELECT 1, 'Alex', 'Rivera', 'Seattle Seahawks'
WHERE NOT EXISTS (SELECT 1 FROM fans WHERE ID = 1);

INSERT INTO fans (ID, firstname, lastname, favoriteteam)
SELECT 2, 'Jordan', 'Reed', 'Chicago Bulls'
WHERE NOT EXISTS (SELECT 1 FROM fans WHERE ID = 2);

INSERT INTO fans (ID, firstname, lastname, favoriteteam)
SELECT 3, 'Taylor', 'Brooks', 'New York Yankees'
WHERE NOT EXISTS (SELECT 1 FROM fans WHERE ID = 3);
