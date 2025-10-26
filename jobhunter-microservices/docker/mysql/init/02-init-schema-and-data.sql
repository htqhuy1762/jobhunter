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

-- Insert default users
-- Password for all: 123456 (BCrypt hash)
INSERT INTO users (email, password, name, age, gender, address, role_id, created_by) VALUES
('admin@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2W69IH.UU8.MSVMLUKKdLNq', 'Admin User', 30, 'MALE', 'Hanoi, Vietnam',
    (SELECT id FROM roles WHERE name = 'ROLE_ADMIN' LIMIT 1), 'system'),
('user@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2W69IH.UU8.MSVMLUKKdLNq', 'Test User', 25, 'FEMALE', 'Ho Chi Minh, Vietnam',
    (SELECT id FROM roles WHERE name = 'ROLE_USER' LIMIT 1), 'system'),
('hr@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2W69IH.UU8.MSVMLUKKdLNq', 'HR Manager', 35, 'MALE', 'Da Nang, Vietnam',
    (SELECT id FROM roles WHERE name = 'ROLE_HR' LIMIT 1), 'system')
ON DUPLICATE KEY UPDATE email=email;

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

-- Insert sample companies
INSERT INTO companies (name, description, address, created_by) VALUES
('Google Vietnam', 'Leading technology company', 'District 1, Ho Chi Minh City', 'admin@gmail.com'),
('Amazon Vietnam', 'E-commerce and cloud computing', 'Hanoi, Vietnam', 'admin@gmail.com'),
('Microsoft Vietnam', 'Software and technology services', 'District 7, Ho Chi Minh City', 'admin@gmail.com'),
('Apple Vietnam', 'Consumer electronics and software', 'Hanoi, Vietnam', 'admin@gmail.com'),
('Netflix Vietnam', 'Streaming entertainment service', 'Ho Chi Minh City', 'admin@gmail.com')
ON DUPLICATE KEY UPDATE name=name;

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
INSERT INTO skills (name, created_by) VALUES
('Java', 'system'),
('Spring Boot', 'system'),
('MySQL', 'system'),
('PostgreSQL', 'system'),
('MongoDB', 'system'),
('Redis', 'system'),
('Docker', 'system'),
('Kubernetes', 'system'),
('AWS', 'system'),
('React', 'system'),
('Angular', 'system'),
('Vue.js', 'system'),
('Node.js', 'system'),
('Python', 'system'),
('JavaScript', 'system'),
('TypeScript', 'system')
ON DUPLICATE KEY UPDATE name=name;

-- Insert sample jobs
INSERT INTO jobs (name, location, salary, quantity, level, description, company_id, active, created_by) VALUES
('Backend Developer', 'Hanoi', 2000.00, 5, 'JUNIOR', 'Looking for Java Spring Boot developers', 1, TRUE, 'admin@gmail.com'),
('Frontend Developer', 'Ho Chi Minh', 1800.00, 3, 'MIDDLE', 'React developer needed', 2, TRUE, 'admin@gmail.com'),
('Full Stack Developer', 'Da Nang', 2500.00, 2, 'SENIOR', 'Full stack with Java and React', 3, TRUE, 'admin@gmail.com'),
('DevOps Engineer', 'Hanoi', 2800.00, 2, 'SENIOR', 'Experience with Docker and Kubernetes', 1, TRUE, 'admin@gmail.com'),
('Mobile Developer', 'Ho Chi Minh', 2200.00, 4, 'MIDDLE', 'iOS/Android development', 4, TRUE, 'admin@gmail.com')
ON DUPLICATE KEY UPDATE name=name;

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

