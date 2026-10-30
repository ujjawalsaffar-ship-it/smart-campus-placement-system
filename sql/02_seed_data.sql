-- ================================================================
-- SMART CAMPUS PLACEMENT MANAGEMENT SYSTEM
-- File: 02_seed_data.sql
-- Description: Realistic seed data for testing schema, views, and queries.
-- ================================================================

PRAGMA foreign_keys = ON;

-- ----------------------------------------------------------------
-- 1. INSERT COMPANIES
-- ----------------------------------------------------------------
INSERT INTO companies (company_id, company_name, industry, location, contact_email) VALUES
(1, 'TechNova Solutions', 'Software Development', 'Bengaluru', 'recruitment@technova.com'),
(2, 'CloudScale Systems', 'Cloud & DevOps', 'Hyderabad', 'careers@cloudscale.io'),
(3, 'FinTech Global', 'Financial Technology', 'Mumbai', 'hr@fintechglobal.com'),
(4, 'NextGen Robotics', 'Embedded & Hardware', 'Pune', 'jobs@nextgenrobotics.com');

-- ----------------------------------------------------------------
-- 2. INSERT STUDENTS
-- ----------------------------------------------------------------
INSERT INTO students (student_id, name, email, department, cgpa, placement_status) VALUES
(1, 'Aarav Sharma',    'aarav.sharma@campus.edu',   'CSE', 9.20, 'Unplaced'),
(2, 'Diya Patel',      'diya.patel@campus.edu',     'CSE', 8.65, 'Unplaced'),
(3, 'Rohan Verma',     'rohan.verma@campus.edu',    'ECE', 7.80, 'Unplaced'),
(4, 'Ananya Iyer',     'ananya.iyer@campus.edu',    'IT',  8.95, 'Unplaced'),
(5, 'Kabir Mehta',     'kabir.mehta@campus.edu',    'ME',  7.10, 'Unplaced'),
(6, 'Pooja Reddy',     'pooja.reddy@campus.edu',    'CSE', 6.90, 'Unplaced'),
(7, 'Siddharth Rao',   'siddharth.rao@campus.edu',  'ECE', 8.40, 'Unplaced'),
(8, 'Tanvi Deshmukh',  'tanvi.d@campus.edu',        'IT',  9.40, 'Unplaced'),
(9, 'Vikram Nair',     'vikram.nair@campus.edu',    'ME',  6.50, 'Unplaced'),
(10, 'Sneha Kulkarni', 'sneha.k@campus.edu',        'CSE', 7.50, 'Unplaced');

-- ----------------------------------------------------------------
-- 3. INSERT JOB POSTINGS
-- ----------------------------------------------------------------
INSERT INTO job_postings (job_id, company_id, role_title, min_cgpa, package_lpa, deadline) VALUES
(1, 1, 'Associate Software Engineer', 7.50, 12.00, '2026-11-15'),
(2, 1, 'Full Stack Developer',        8.50, 16.50, '2026-11-20'),
(3, 2, 'Cloud Infrastructure Intern', 7.00,  9.50, '2026-11-10'),
(4, 3, 'Data Analyst',                7.50, 11.00, '2026-11-18'),
(5, 4, 'Embedded Systems Engineer',   7.00,  8.00, '2026-11-25');

-- ----------------------------------------------------------------
-- 4. INSERT APPLICATIONS
-- Note: All inserted student CGPAs satisfy min_cgpa for the respective jobs.
-- ----------------------------------------------------------------
INSERT INTO applications (student_id, job_id, application_date, status) VALUES
-- Job 1 (TechNova - ASE, min 7.50)
(1, 1, '2026-10-01', 'Shortlisted'),
(2, 1, '2026-10-02', 'Applied'),
(3, 1, '2026-10-02', 'Applied'),
(4, 1, '2026-10-03', 'Shortlisted'),
(7, 1, '2026-10-04', 'Applied'),
(10, 1, '2026-10-05', 'Rejected'),

-- Job 2 (TechNova - Full Stack, min 8.50)
(1, 2, '2026-10-06', 'Applied'),
(2, 2, '2026-10-06', 'Shortlisted'),
(4, 2, '2026-10-07', 'Applied'),
(8, 2, '2026-10-07', 'Applied'),

-- Job 3 (CloudScale - Cloud Intern, min 7.00)
(3, 3, '2026-10-08', 'Applied'),
(5, 3, '2026-10-08', 'Shortlisted'),
(7, 3, '2026-10-09', 'Applied'),

-- Job 4 (FinTech - Data Analyst, min 7.50)
(1, 4, '2026-10-10', 'Applied'),
(4, 4, '2026-10-10', 'Applied'),

-- Job 5 (NextGen - Embedded, min 7.00)
(3, 5, '2026-10-11', 'Applied'),
(5, 5, '2026-10-12', 'Applied');

-- ----------------------------------------------------------------
-- 5. SIMULATE SELECTION TO FIRE TRIGGER 2
-- Updating status to 'Selected' will automatically update students.placement_status = 'Placed'
-- ----------------------------------------------------------------
UPDATE applications 
SET status = 'Selected' 
WHERE student_id = 1 AND job_id = 1;

UPDATE applications 
SET status = 'Selected' 
WHERE student_id = 8 AND job_id = 2;

UPDATE applications 
SET status = 'Selected' 
WHERE student_id = 5 AND job_id = 3;
