-- Create separate databases for each microservice
CREATE DATABASE IF NOT EXISTS auth_db;
CREATE DATABASE IF NOT EXISTS company_db;
CREATE DATABASE IF NOT EXISTS job_db;
CREATE DATABASE IF NOT EXISTS resume_db;
CREATE DATABASE IF NOT EXISTS notification_db;

-- Create database users (optional - for better security)
-- CREATE USER IF NOT EXISTS 'auth_user'@'%' IDENTIFIED BY 'auth_password';
-- CREATE USER IF NOT EXISTS 'company_user'@'%' IDENTIFIED BY 'company_password';
-- CREATE USER IF NOT EXISTS 'job_user'@'%' IDENTIFIED BY 'job_password';
-- CREATE USER IF NOT EXISTS 'resume_user'@'%' IDENTIFIED BY 'resume_password';
-- CREATE USER IF NOT EXISTS 'notification_user'@'%' IDENTIFIED BY 'notification_password';

-- Grant privileges (optional)
-- GRANT ALL PRIVILEGES ON auth_db.* TO 'auth_user'@'%';
-- GRANT ALL PRIVILEGES ON company_db.* TO 'company_user'@'%';
-- GRANT ALL PRIVILEGES ON job_db.* TO 'job_user'@'%';
-- GRANT ALL PRIVILEGES ON resume_db.* TO 'resume_user'@'%';
-- GRANT ALL PRIVILEGES ON notification_db.* TO 'notification_user'@'%';
-- FLUSH PRIVILEGES;

-- For now, using root user for all databases (simpler for development)
GRANT ALL PRIVILEGES ON auth_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON company_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON job_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON resume_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON notification_db.* TO 'root'@'%';
FLUSH PRIVILEGES;

