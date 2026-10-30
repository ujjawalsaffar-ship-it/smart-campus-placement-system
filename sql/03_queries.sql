-- ================================================================
-- SMART CAMPUS PLACEMENT MANAGEMENT SYSTEM
-- File: 03_queries.sql
-- Description: Standard evaluation queries showcasing joins, 
--              aggregations, subqueries, views, and trigger tests.
-- ================================================================

-- ----------------------------------------------------------------
-- QUERY 1: Multi-Table JOIN
-- Retrieve complete application records with student details, 
-- company name, job role, package, and application status.
-- ----------------------------------------------------------------
SELECT 
    a.application_id,
    s.name AS student_name,
    s.department,
    s.cgpa,
    c.company_name,
    jp.role_title,
    jp.package_lpa,
    a.status,
    a.application_date
FROM applications a
JOIN students s ON a.student_id = s.student_id
JOIN job_postings jp ON a.job_id = jp.job_id
JOIN companies c ON jp.company_id = c.company_id
ORDER BY a.application_date DESC;

-- ----------------------------------------------------------------
-- QUERY 2: Filter & Conditional Search
-- Find high-achieving unplaced students (CGPA >= 8.0) 
-- who need urgent placement assistance.
-- ----------------------------------------------------------------
SELECT 
    student_id,
    name,
    department,
    cgpa,
    placement_status
FROM students
WHERE placement_status = 'Unplaced' 
  AND cgpa >= 8.00
ORDER BY cgpa DESC;

-- ----------------------------------------------------------------
-- QUERY 3: Aggregation with GROUP BY
-- Count total applications received per job role and company.
-- ----------------------------------------------------------------
SELECT 
    c.company_name,
    jp.role_title,
    jp.package_lpa,
    COUNT(a.application_id) AS total_applicants
FROM job_postings jp
JOIN companies c ON jp.company_id = c.company_id
LEFT JOIN applications a ON jp.job_id = a.job_id
GROUP BY jp.job_id, c.company_name, jp.role_title, jp.package_lpa
ORDER BY total_applicants DESC;

-- ----------------------------------------------------------------
-- QUERY 4: Aggregation with HAVING
-- Find companies where the average package offered across all their 
-- postings is greater than 10 LPA.
-- ----------------------------------------------------------------
SELECT 
    c.company_name,
    c.industry,
    COUNT(jp.job_id) AS total_openings,
    ROUND(AVG(jp.package_lpa), 2) AS avg_package_lpa
FROM companies c
JOIN job_postings jp ON c.company_id = jp.company_id
GROUP BY c.company_id, c.company_name, c.industry
HAVING AVG(jp.package_lpa) >= 10.00
ORDER BY avg_package_lpa DESC;

-- ----------------------------------------------------------------
-- QUERY 5: Scalar Subquery
-- Find student(s) who received the highest package in the campus drive.
-- ----------------------------------------------------------------
SELECT 
    s.name AS student_name,
    s.department,
    c.company_name,
    jp.role_title,
    jp.package_lpa
FROM applications a
JOIN students s ON a.student_id = s.student_id
JOIN job_postings jp ON a.job_id = jp.job_id
JOIN companies c ON jp.company_id = c.company_id
WHERE a.status = 'Selected'
  AND jp.package_lpa = (
      SELECT MAX(jp2.package_lpa)
      FROM applications a2
      JOIN job_postings jp2 ON a2.job_id = jp2.job_id
      WHERE a2.status = 'Selected'
  );

-- ----------------------------------------------------------------
-- QUERY 6: Analytical View Evaluation
-- Department-wise placement rate, average package, and total placed.
-- ----------------------------------------------------------------
SELECT * FROM v_placement_summary ORDER BY placement_percentage DESC;

-- ----------------------------------------------------------------
-- QUERY 7: View Evaluation - Student Eligible Jobs
-- See all jobs that student 'Aarav Sharma' (student_id = 1) is eligible for.
-- ----------------------------------------------------------------
SELECT 
    student_name,
    department,
    cgpa,
    company_name,
    role_title,
    package_lpa,
    deadline
FROM v_student_eligible_jobs
WHERE student_id = 1
ORDER BY package_lpa DESC;

-- ----------------------------------------------------------------
-- QUERY 8: Trigger Verification Test
-- 8A: Verify Trigger 2 updated placement_status for selected students
-- ----------------------------------------------------------------
SELECT student_id, name, department, cgpa, placement_status 
FROM students 
WHERE placement_status = 'Placed';

-- 8B: Test Trigger 1 (Expect FAILURE):
-- Student 9 (Vikram Nair, CGPA 6.50) attempts to apply for Job 1 (min_cgpa 7.50).
-- Uncomment below line to test trigger constraint enforcement:
-- INSERT INTO applications (student_id, job_id, status) VALUES (9, 1, 'Applied');
