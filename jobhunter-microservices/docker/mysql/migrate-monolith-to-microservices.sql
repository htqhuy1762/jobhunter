-- =====================================================
-- DATABASE MIGRATION SCRIPT
-- From Monolith (jobhunter) to Microservices
-- =====================================================

-- This script helps migrate data from the monolith database
-- to separate microservice databases

USE jobhunter;

-- =====================================================
-- 1. MIGRATE AUTH SERVICE DATA
-- =====================================================
-- Tables: users, roles, permissions, user_role, permission_role

-- Copy Users table
CREATE TABLE IF NOT EXISTS auth_db.users LIKE jobhunter.users;
INSERT INTO auth_db.users SELECT * FROM jobhunter.users;

-- Copy Roles table
CREATE TABLE IF NOT EXISTS auth_db.roles LIKE jobhunter.roles;
INSERT INTO auth_db.roles SELECT * FROM jobhunter.roles;

-- Copy Permissions table
CREATE TABLE IF NOT EXISTS auth_db.permissions LIKE jobhunter.permissions;
INSERT INTO auth_db.permissions SELECT * FROM jobhunter.permissions;

-- Copy User_Role junction table
CREATE TABLE IF NOT EXISTS auth_db.user_role LIKE jobhunter.user_role;
INSERT INTO auth_db.user_role SELECT * FROM jobhunter.user_role;

-- Copy Permission_Role junction table
CREATE TABLE IF NOT EXISTS auth_db.permission_role LIKE jobhunter.permission_role;
INSERT INTO auth_db.permission_role SELECT * FROM jobhunter.permission_role;

-- =====================================================
-- 2. MIGRATE COMPANY SERVICE DATA
-- =====================================================
-- Tables: companies

CREATE TABLE IF NOT EXISTS company_db.companies LIKE jobhunter.companies;
INSERT INTO company_db.companies SELECT * FROM jobhunter.companies;

-- =====================================================
-- 3. MIGRATE JOB SERVICE DATA
-- =====================================================
-- Tables: jobs, skills, job_skill

CREATE TABLE IF NOT EXISTS job_db.jobs LIKE jobhunter.jobs;
INSERT INTO job_db.jobs SELECT * FROM jobhunter.jobs;

CREATE TABLE IF NOT EXISTS job_db.skills LIKE jobhunter.skills;
INSERT INTO job_db.skills SELECT * FROM jobhunter.skills;

CREATE TABLE IF NOT EXISTS job_db.job_skill LIKE jobhunter.job_skill;
INSERT INTO job_db.job_skill SELECT * FROM jobhunter.job_skill;

-- =====================================================
-- 4. MIGRATE RESUME SERVICE DATA
-- =====================================================
-- Tables: resumes

CREATE TABLE IF NOT EXISTS resume_db.resumes LIKE jobhunter.resumes;
INSERT INTO resume_db.resumes SELECT * FROM jobhunter.resumes;

-- =====================================================
-- 5. MIGRATE NOTIFICATION SERVICE DATA
-- =====================================================
-- Tables: subscribers

CREATE TABLE IF NOT EXISTS notification_db.subscribers LIKE jobhunter.subscribers;
INSERT INTO notification_db.subscribers SELECT * FROM jobhunter.subscribers;

-- =====================================================
-- 6. VERIFICATION QUERIES
-- =====================================================

SELECT 'Auth DB - Users' AS 'Table', COUNT(*) AS 'Records' FROM auth_db.users
UNION ALL
SELECT 'Auth DB - Roles', COUNT(*) FROM auth_db.roles
UNION ALL
SELECT 'Auth DB - Permissions', COUNT(*) FROM auth_db.permissions
UNION ALL
SELECT 'Company DB - Companies', COUNT(*) FROM company_db.companies
UNION ALL
SELECT 'Job DB - Jobs', COUNT(*) FROM job_db.jobs
UNION ALL
SELECT 'Job DB - Skills', COUNT(*) FROM job_db.skills
UNION ALL
SELECT 'Resume DB - Resumes', COUNT(*) FROM resume_db.resumes
UNION ALL
SELECT 'Notification DB - Subscribers', COUNT(*) FROM notification_db.subscribers;

-- =====================================================
-- NOTES:
-- =====================================================
-- 1. Run this script AFTER the microservices databases are created
-- 2. This script assumes the monolith database still exists with name 'jobhunter'
-- 3. Foreign key relationships might need to be adjusted based on your schema
-- 4. Consider running this during a maintenance window
-- 5. Backup all databases before running this script