-- ================================================================
-- SMART CAMPUS PLACEMENT MANAGEMENT SYSTEM
-- File: 01_schema.sql
-- Description: DDL script creating tables, constraints, triggers, and views.
-- Database: SQLite / ANSI SQL compatible
-- ================================================================

-- Enforce Foreign Key constraints in SQLite
PRAGMA foreign_keys = ON;

-- Drop existing views and triggers if present
DROP VIEW IF EXISTS v_placement_summary;
DROP VIEW IF EXISTS v_student_eligible_jobs;
DROP TRIGGER IF EXISTS trg_check_cgpa_before_apply;
DROP TRIGGER IF EXISTS trg_auto_update_placement_status;

-- Drop existing tables in reverse dependency order
DROP TABLE IF EXISTS applications;
DROP TABLE IF EXISTS job_postings;
DROP TABLE IF EXISTS companies;
DROP TABLE IF EXISTS students;

-- ----------------------------------------------------------------
-- 1. STUDENTS TABLE
-- ----------------------------------------------------------------
CREATE TABLE students (
    student_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    department VARCHAR(50) NOT NULL,
    cgpa DECIMAL(3, 2) NOT NULL CHECK (cgpa >= 0.0 AND cgpa <= 10.0),
    placement_status VARCHAR(20) NOT NULL DEFAULT 'Unplaced' 
        CHECK (placement_status IN ('Unplaced', 'Placed'))
);

-- ----------------------------------------------------------------
-- 2. COMPANIES TABLE
-- ----------------------------------------------------------------
CREATE TABLE companies (
    company_id INTEGER PRIMARY KEY AUTOINCREMENT,
    company_name VARCHAR(100) NOT NULL UNIQUE,
    industry VARCHAR(100) NOT NULL,
    location VARCHAR(100) NOT NULL,
    contact_email VARCHAR(120) NOT NULL
);

-- ----------------------------------------------------------------
-- 3. JOB POSTINGS TABLE
-- ----------------------------------------------------------------
CREATE TABLE job_postings (
    job_id INTEGER PRIMARY KEY AUTOINCREMENT,
    company_id INTEGER NOT NULL,
    role_title VARCHAR(100) NOT NULL,
    min_cgpa DECIMAL(3, 2) NOT NULL CHECK (min_cgpa >= 0.0 AND min_cgpa <= 10.0),
    package_lpa DECIMAL(5, 2) NOT NULL CHECK (package_lpa > 0.0),
    deadline DATE NOT NULL,
    FOREIGN KEY (company_id) REFERENCES companies(company_id) ON DELETE CASCADE
);

-- ----------------------------------------------------------------
-- 4. APPLICATIONS TABLE
-- ----------------------------------------------------------------
CREATE TABLE applications (
    application_id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id INTEGER NOT NULL,
    job_id INTEGER NOT NULL,
    application_date DATE NOT NULL DEFAULT (CURRENT_DATE),
    status VARCHAR(20) NOT NULL DEFAULT 'Applied' 
        CHECK (status IN ('Applied', 'Shortlisted', 'Selected', 'Rejected')),
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (job_id) REFERENCES job_postings(job_id) ON DELETE CASCADE,
    CONSTRAINT uq_student_job UNIQUE (student_id, job_id)
);

-- ----------------------------------------------------------------
-- 5. BUSINESS RULE TRIGGERS
-- ----------------------------------------------------------------

-- Trigger 1: Check if student meets the minimum CGPA requirement before application is inserted
CREATE TRIGGER trg_check_cgpa_before_apply
BEFORE INSERT ON applications
FOR EACH ROW
WHEN (SELECT cgpa FROM students WHERE student_id = NEW.student_id) < 
     (SELECT min_cgpa FROM job_postings WHERE job_id = NEW.job_id)
BEGIN
    SELECT RAISE(ABORT, 'Ineligible: Student CGPA is lower than minimum CGPA required for this job posting.');
END;

-- Trigger 2: Automatically update student placement status to 'Placed' when an application is 'Selected'
CREATE TRIGGER trg_auto_update_placement_status
AFTER UPDATE OF status ON applications
FOR EACH ROW
WHEN NEW.status = 'Selected'
BEGIN
    UPDATE students
    SET placement_status = 'Placed'
    WHERE student_id = NEW.student_id;
END;

-- ----------------------------------------------------------------
-- 6. ANALYTICAL VIEWS
-- ----------------------------------------------------------------

-- View 1: Department-wise placement performance & package statistics
CREATE VIEW v_placement_summary AS
SELECT 
    s.department,
    COUNT(s.student_id) AS total_students,
    SUM(CASE WHEN s.placement_status = 'Placed' THEN 1 ELSE 0 END) AS placed_students,
    ROUND(
        (CAST(SUM(CASE WHEN s.placement_status = 'Placed' THEN 1 ELSE 0 END) AS FLOAT) / COUNT(s.student_id)) * 100.0, 
        2
    ) AS placement_percentage,
    ROUND(AVG(CASE WHEN a.status = 'Selected' THEN jp.package_lpa END), 2) AS avg_placed_package_lpa,
    MAX(CASE WHEN a.status = 'Selected' THEN jp.package_lpa END) AS highest_package_lpa
FROM students s
LEFT JOIN applications a ON s.student_id = a.student_id AND a.status = 'Selected'
LEFT JOIN job_postings jp ON a.job_id = jp.job_id
GROUP BY s.department;

-- View 2: Eligible jobs for each student based on current CGPA
CREATE VIEW v_student_eligible_jobs AS
SELECT 
    s.student_id,
    s.name AS student_name,
    s.department,
    s.cgpa,
    c.company_name,
    jp.job_id,
    jp.role_title,
    jp.min_cgpa,
    jp.package_lpa,
    jp.deadline
FROM students s
CROSS JOIN job_postings jp
JOIN companies c ON jp.company_id = c.company_id
WHERE s.cgpa >= jp.min_cgpa;
