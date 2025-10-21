-- =====================================================
-- MICROSERVICES DATABASE INITIALIZATION
-- =====================================================
-- This script creates separate databases for each microservice
-- Run order: 01 (runs first automatically by Docker)
-- =====================================================

-- Create databases
CREATE DATABASE IF NOT EXISTS auth_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS company_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS job_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS resume_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS notification_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Grant privileges to root user
GRANT ALL PRIVILEGES ON auth_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON company_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON job_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON resume_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON notification_db.* TO 'root'@'%';
FLUSH PRIVILEGES;

SELECT 'Databases created successfully!' AS message;

