-- =====================================================
-- MICROSERVICES DATABASE INITIALIZATION
-- =====================================================
-- This script creates separate databases for each microservice
-- Run order: 01 (runs first automatically by Docker)
-- =====================================================

-- Create databases
CREATE DATABASE IF NOT EXISTS auth_db;
CREATE DATABASE IF NOT EXISTS company_db;
CREATE DATABASE IF NOT EXISTS job_db;
CREATE DATABASE IF NOT EXISTS resume_db;
CREATE DATABASE IF NOT EXISTS file_db;

GRANT ALL PRIVILEGES ON auth_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON company_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON job_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON resume_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON file_db.* TO 'root'@'%';

FLUSH PRIVILEGES;

SELECT 'Databases created successfully!' AS message;

