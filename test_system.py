"""
Test suite for Smart Campus Placement Management System.
Validates database schema, constraints, triggers, and analytical queries.
"""

import os
import sqlite3
import database

def test_triggers_and_constraints():
    print("\n--- [TEST 1] Trigger 1: Eligibility Check (CGPA < min_cgpa) ---")
    database.init_db(force=True)
    
    # Student 9: Vikram Nair, CGPA 6.50
    # Job 1: TechNova ASE, Min CGPA 7.50
    success, msg = database.apply_for_job(student_id=9, job_id=1)
    print(f"Applying ineligible student (CGPA 6.50 to Min 7.50): Success={success}, Message='{msg}'")
    assert not success, "Trigger failed: Ineligible student application was accepted!"
    assert "Ineligible" in msg, f"Unexpected error message: {msg}"
    print("[PASS] Trigger 1 correctly blocked ineligible applicant.")

    print("\n--- [TEST 2] Duplicate Application Constraint ---")
    # Student 1 already applied for Job 1 in seed data
    success, msg = database.apply_for_job(student_id=1, job_id=1)
    print(f"Duplicate application attempt: Success={success}, Message='{msg}'")
    assert not success, "Unique constraint failed: Duplicate application accepted!"
    assert "already applied" in msg or "UNIQUE" in msg
    print("[PASS] Unique constraint correctly prevented duplicate application.")

    print("\n--- [TEST 3] Trigger 2: Auto Update Placement Status ---")
    # Check student 7 (Siddharth Rao, currently Unplaced)
    student_before = database.fetch_all("SELECT placement_status FROM students WHERE student_id = 7")[0]
    print(f"Student 7 initial status: {student_before['placement_status']}")
    assert student_before['placement_status'] == 'Unplaced'

    # Apply Student 7 to Job 5 (Min CGPA 7.00, Siddharth has 8.40)
    success, msg = database.apply_for_job(student_id=7, job_id=5)
    print(f"Student 7 apply to Job 5: {msg}")
    
    # Retrieve application id
    app = database.fetch_all("SELECT application_id FROM applications WHERE student_id = 7 AND job_id = 5")[0]
    app_id = app['application_id']

    # Update status to 'Selected'
    success, msg = database.update_application_status(app_id, "Selected")
    print(f"Updating application #{app_id} to 'Selected': {msg}")
    assert success

    # Verify student status changed to 'Placed' via Trigger 2
    student_after = database.fetch_all("SELECT placement_status FROM students WHERE student_id = 7")[0]
    print(f"Student 7 status after selection: {student_after['placement_status']}")
    assert student_after['placement_status'] == 'Placed', "Trigger 2 failed to update placement_status!"
    print("[PASS] Trigger 2 correctly updated student placement status to 'Placed'.")

    print("\n--- [TEST 4] Foreign Key Cascade Delete ---")
    # Delete Company 4 (NextGen Robotics)
    conn = database.get_connection()
    cursor = conn.cursor()
    cursor.execute("DELETE FROM companies WHERE company_id = 4;")
    conn.commit()
    conn.close()

    # Check if Job 5 was cascaded
    remaining_jobs = database.fetch_all("SELECT * FROM job_postings WHERE job_id = 5")
    assert len(remaining_jobs) == 0, "Cascade failed: Job posting was not deleted!"
    
    # Check if applications for Job 5 were cascaded
    remaining_apps = database.fetch_all("SELECT * FROM applications WHERE job_id = 5")
    assert len(remaining_apps) == 0, "Cascade failed: Applications were not deleted!"
    print("[PASS] ON DELETE CASCADE correctly removed dependent job postings and applications.")

def test_queries_file():
    print("\n--- [TEST 5] Verify 03_queries.sql Execution ---")
    database.init_db(force=True)
    queries_path = os.path.join(database.BASE_DIR, "sql", "03_queries.sql")
    with open(queries_path, "r", encoding="utf-8") as f:
        sql_content = f.read()

    # Split statements by semicolon and run each SELECT query
    conn = database.get_connection()
    cursor = conn.cursor()
    statements = [stmt.strip() for stmt in sql_content.split(";") if stmt.strip()]
    executed_count = 0
    for stmt in statements:
        # Ignore comments-only chunks or commented-out trigger test
        clean_stmt = "\n".join([line for line in stmt.splitlines() if not line.strip().startswith("--")]).strip()
        if not clean_stmt:
            continue
        if clean_stmt.upper().startswith("SELECT"):
            cursor.execute(clean_stmt)
            results = cursor.fetchall()
            executed_count += 1
            print(f"Query #{executed_count} executed successfully, returned {len(results)} rows.")
    conn.close()
    print(f"[PASS] All {executed_count} analytical evaluation queries ran cleanly.")

if __name__ == "__main__":
    test_triggers_and_constraints()
    test_queries_file()
    print("\nALL SYSTEM TESTS PASSED SUCCESSFULLY!")
