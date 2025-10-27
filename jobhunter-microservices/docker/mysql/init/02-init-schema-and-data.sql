-- =====================================================
-- MICROSERVICES SCHEMA AND DATA INITIALIZATION
-- =====================================================
-- This script creates tables and inserts sample data
-- Run order: 02 (runs after 01-init-databases.sql)
-- =====================================================

-- ========================================
-- AUTH_DB - Authentication Service
-- ========================================
USE auth_db;

-- Roles table
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    age INT,
    gender VARCHAR(50),
    address VARCHAR(500),
    company_id BIGINT,
    role_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    INDEX idx_email (email),
    INDEX idx_role_id (role_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Permissions table
CREATE TABLE IF NOT EXISTS permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    api_path VARCHAR(255),
    method VARCHAR(10),
    module VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Role-Permission junction table
CREATE TABLE IF NOT EXISTS permission_role (
    permission_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (permission_id, role_id),
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default roles
INSERT INTO roles (name, description, active, created_by) VALUES
('ROLE_ADMIN', 'Administrator with full access', TRUE, 'system'),
('ROLE_USER', 'Regular user with limited access', TRUE, 'system'),
('ROLE_HR', 'HR manager', TRUE, 'system')
ON DUPLICATE KEY UPDATE name=name;

-- ============================================
-- NOTE: Users are seeded by application code!
-- ============================================
-- Users need BCrypt password hashing which depends on Spring Security configuration.
-- See: auth-service/src/main/java/vn/hoidanit/authservice/config/DatabaseSeeder.java
-- Default users will be created on first application startup:
--   - admin@gmail.com (password: 123456)
--   - user@gmail.com (password: 123456)
--   - hr@gmail.com (password: 123456)
-- ============================================

-- Insert sample permissions
INSERT INTO permissions (name, api_path, method, module, created_by) VALUES
('CREATE_USER', '/api/v1/users', 'POST', 'USER', 'system'),
('UPDATE_USER', '/api/v1/users/*', 'PUT', 'USER', 'system'),
('DELETE_USER', '/api/v1/users/*', 'DELETE', 'USER', 'system'),
('VIEW_USER', '/api/v1/users', 'GET', 'USER', 'system'),
('CREATE_COMPANY', '/api/v1/companies', 'POST', 'COMPANY', 'system'),
('UPDATE_COMPANY', '/api/v1/companies/*', 'PUT', 'COMPANY', 'system'),
('CREATE_JOB', '/api/v1/jobs', 'POST', 'JOB', 'system'),
('UPDATE_JOB', '/api/v1/jobs/*', 'PUT', 'JOB', 'system')
ON DUPLICATE KEY UPDATE name=name;

-- Assign permissions to ADMIN role
INSERT INTO permission_role (permission_id, role_id)
SELECT p.id, r.id
FROM permissions p, roles r
WHERE r.name = 'ROLE_ADMIN'
ON DUPLICATE KEY UPDATE permission_id=permission_id;

SELECT 'Auth DB initialized successfully!' AS status;

-- ========================================
-- COMPANY_DB - Company Service
-- ========================================
USE company_db;

CREATE TABLE IF NOT EXISTS companies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    address VARCHAR(500),
    logo VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- NOTE: Sample companies are seeded by application code!
-- ============================================
-- Sample companies for dev/test are created by CompanyService seeder.
-- See: company-service/src/main/java/vn/hoidanit/companyservice/config/DatabaseSeeder.java
-- Production should start with empty companies table.
-- ============================================

SELECT 'Company DB initialized successfully!' AS status;

-- ========================================
-- JOB_DB - Job Service
-- ========================================
USE job_db;

CREATE TABLE IF NOT EXISTS jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    salary DECIMAL(15,2),
    quantity INT,
    level VARCHAR(50),
    description TEXT,
    start_date DATE,
    end_date DATE,
    active BOOLEAN DEFAULT TRUE,
    company_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    INDEX idx_company_id (company_id),
    INDEX idx_level (level),
    INDEX idx_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS job_skill (
    job_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    PRIMARY KEY (job_id, skill_id),
    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample skills
-- ============================================
-- NOTE: Sample skills and jobs are seeded by application code!
-- ============================================
-- Sample data for dev/test is created by JobService seeder.
-- See: job-service/src/main/java/vn/hoidanit/jobservice/config/DatabaseSeeder.java
-- Production should start with empty skills/jobs tables.
-- ============================================
SELECT 'Job DB initialized successfully!' AS status;

-- ========================================
-- RESUME_DB - Resume Service
-- ========================================
USE resume_db;

CREATE TABLE IF NOT EXISTS resumes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    url VARCHAR(500) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    user_id BIGINT,
    job_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    INDEX idx_user_id (user_id),
    INDEX idx_job_id (job_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SELECT 'Resume DB initialized successfully!' AS status;

-- ========================================
-- NOTIFICATION_DB - Notification Service
-- ========================================
USE notification_db;

CREATE TABLE IF NOT EXISTS subscribers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    skills TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SELECT 'Notification DB initialized successfully!' AS status;

-- ========================================
-- SUMMARY
-- ========================================
SELECT '============================================' AS '';
SELECT 'ALL MICROSERVICES DATABASES INITIALIZED!' AS 'STATUS';
SELECT '============================================' AS '';
SELECT 'Default credentials:' AS '';
SELECT 'Email: admin@gmail.com | Password: 123456' AS 'Admin';
SELECT 'Email: user@gmail.com | Password: 123456' AS 'User';
SELECT 'Email: hr@gmail.com | Password: 123456' AS 'HR';
SELECT '============================================' AS '';

