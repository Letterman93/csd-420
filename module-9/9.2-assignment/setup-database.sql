-- Zak Nizam | CSD 420 | Module 9.2 | November 8, 2026
-- Create the required database and allow student1 to use it from this computer.

CREATE DATABASE IF NOT EXISTS databasedb;
CREATE USER IF NOT EXISTS 'student1'@'localhost' IDENTIFIED BY 'pass';
ALTER USER 'student1'@'localhost' IDENTIFIED BY 'pass';
GRANT ALL PRIVILEGES ON databasedb.* TO 'student1'@'localhost';

-- Confirm the account and its database permissions.
SHOW GRANTS FOR 'student1'@'localhost';
