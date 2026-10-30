"""
Database connector and business logic for Smart Campus Placement Management System.
Handles schema initialization, seed data loading, and transactional CRUD operations.
"""

import os
import sqlite3
from typing import Any, Dict, List, Optional, Tuple

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DB_PATH = os.path.join(BASE_DIR, "placement.db")
SCHEMA_PATH = os.path.join(BASE_DIR, "sql", "01_schema.sql")
SEED_PATH = os.path.join(BASE_DIR, "sql", "02_seed_data.sql")


def get_connection() -> sqlite3.Connection:
    """Returns a SQLite connection with foreign keys enabled and Row factory."""
    conn = sqlite3.connect(DB_PATH)
    conn.execute("PRAGMA foreign_keys = ON;")
    conn.row_factory = sqlite3.Row
    return conn


def init_db(force: bool = False) -> None:
    """Initializes the database schema and optionally resets it."""
    if not force and os.path.exists(DB_PATH):
        return

    conn = get_connection()
    try:
        with open(SCHEMA_PATH, "r", encoding="utf-8") as f:
            schema_sql = f.read()
        conn.executescript(schema_sql)

        if os.path.exists(SEED_PATH):
            with open(SEED_PATH, "r", encoding="utf-8") as f:
                seed_sql = f.read()
            conn.executescript(seed_sql)
        conn.commit()
    finally:
        conn.close()


# -----------------------------------------------------------------------------
# Read Operations
# -----------------------------------------------------------------------------

def fetch_all(query: str, params: Tuple = ()) -> List[Dict[str, Any]]:
    """Executes a SELECT query and returns rows as dictionaries."""
    conn = get_connection()
    try:
        cursor = conn.cursor()
        cursor.execute(query, params)
        rows = cursor.fetchall()
        return [dict(row) for row in rows]
    finally:
        conn.close()


def get_students() -> List[Dict[str, Any]]:
    return fetch_all("SELECT * FROM students ORDER BY student_id ASC")


def get_companies() -> List[Dict[str, Any]]:
    return fetch_all("SELECT * FROM companies ORDER BY company_id ASC")


def get_job_postings() -> List[Dict[str, Any]]:
    sql = """
    SELECT 
        jp.job_id,
        jp.company_id,
        c.company_name,
        jp.role_title,
        jp.min_cgpa,
        jp.package_lpa,
        jp.deadline,
        COUNT(a.application_id) AS total_applications
    FROM job_postings jp
    JOIN companies c ON jp.company_id = c.company_id
    LEFT JOIN applications a ON jp.job_id = a.job_id
    GROUP BY jp.job_id, jp.company_id, c.company_name, jp.role_title, jp.min_cgpa, jp.package_lpa, jp.deadline
    ORDER BY jp.deadline ASC;
    """
    return fetch_all(sql)


def get_applications(student_id: Optional[int] = None) -> List[Dict[str, Any]]:
    sql = """
    SELECT 
        a.application_id,
        a.student_id,
        s.name AS student_name,
        s.department,
        s.cgpa,
        s.placement_status,
        jp.job_id,
        c.company_name,
        jp.role_title,
        jp.package_lpa,
        a.status,
        a.application_date
    FROM applications a
    JOIN students s ON a.student_id = s.student_id
    JOIN job_postings jp ON a.job_id = jp.job_id
    JOIN companies c ON jp.company_id = c.company_id
    """
    params: Tuple = ()
    if student_id is not None:
        sql += " WHERE a.student_id = ?"
        params = (student_id,)
    sql += " ORDER BY a.application_id DESC"
    return fetch_all(sql, params)


def get_placement_summary() -> List[Dict[str, Any]]:
    return fetch_all("SELECT * FROM v_placement_summary ORDER BY placement_percentage DESC")


def get_eligible_jobs_for_student(student_id: int) -> List[Dict[str, Any]]:
    sql = """
    SELECT 
        v.job_id,
        v.company_name,
        v.role_title,
        v.min_cgpa,
        v.package_lpa,
        v.deadline,
        CASE WHEN a.application_id IS NOT NULL THEN a.status ELSE 'Not Applied' END AS my_status
    FROM v_student_eligible_jobs v
    LEFT JOIN applications a ON v.job_id = a.job_id AND a.student_id = v.student_id
    WHERE v.student_id = ?
    ORDER BY v.package_lpa DESC;
    """
    return fetch_all(sql, (student_id,))


# -----------------------------------------------------------------------------
# Write Operations (Triggers & Constraints are enforced)
# -----------------------------------------------------------------------------

def apply_for_job(student_id: int, job_id: int) -> Tuple[bool, str]:
    """
    Submits a new application.
    Enforces check constraint and trigger trg_check_cgpa_before_apply.
    """
    conn = get_connection()
    try:
        cursor = conn.cursor()
        cursor.execute(
            "INSERT INTO applications (student_id, job_id, status) VALUES (?, ?, 'Applied')",
            (student_id, job_id),
        )
        conn.commit()
        return True, "Application submitted successfully!"
    except sqlite3.IntegrityError as e:
        err_msg = str(e)
        if "UNIQUE constraint failed" in err_msg:
            return False, "You have already applied for this job posting."
        elif "Ineligible" in err_msg:
            return False, f"Eligibility Check Failed: {err_msg}"
        return False, f"Integrity error: {err_msg}"
    except Exception as e:
        return False, f"Database error: {str(e)}"
    finally:
        conn.close()


def update_application_status(application_id: int, new_status: str) -> Tuple[bool, str]:
    """
    Updates the status of an application.
    If new_status is 'Selected', trigger trg_auto_update_placement_status automatically fires.
    """
    if new_status not in ("Applied", "Shortlisted", "Selected", "Rejected"):
        return False, f"Invalid status: {new_status}"

    conn = get_connection()
    try:
        cursor = conn.cursor()
        cursor.execute(
            "UPDATE applications SET status = ? WHERE application_id = ?",
            (new_status, application_id),
        )
        conn.commit()
        return True, f"Status updated to '{new_status}' successfully! (Student record synchronized)"
    except Exception as e:
        return False, f"Update failed: {str(e)}"
    finally:
        conn.close()


def add_student(name: str, email: str, department: str, cgpa: float) -> Tuple[bool, str]:
    conn = get_connection()
    try:
        cursor = conn.cursor()
        cursor.execute(
            "INSERT INTO students (name, email, department, cgpa) VALUES (?, ?, ?, ?)",
            (name.strip(), email.strip(), department.strip().upper(), cgpa),
        )
        conn.commit()
        return True, f"Student '{name}' added with ID {cursor.lastrowid}."
    except sqlite3.IntegrityError as e:
        if "UNIQUE constraint failed" in str(e):
            return False, "A student with this email already exists."
        elif "CHECK constraint failed" in str(e):
            return False, "CGPA must be between 0.0 and 10.0."
        return False, f"Integrity error: {str(e)}"
    except Exception as e:
        return False, f"Error: {str(e)}"
    finally:
        conn.close()


def add_company(company_name: str, industry: str, location: str, contact_email: str) -> Tuple[bool, str]:
    conn = get_connection()
    try:
        cursor = conn.cursor()
        cursor.execute(
            "INSERT INTO companies (company_name, industry, location, contact_email) VALUES (?, ?, ?, ?)",
            (company_name.strip(), industry.strip(), location.strip(), contact_email.strip()),
        )
        conn.commit()
        return True, f"Company '{company_name}' registered successfully."
    except sqlite3.IntegrityError:
        return False, "A company with this name already exists."
    except Exception as e:
        return False, f"Error: {str(e)}"
    finally:
        conn.close()


def add_job_posting(company_id: int, role_title: str, min_cgpa: float, package_lpa: float, deadline: str) -> Tuple[bool, str]:
    conn = get_connection()
    try:
        cursor = conn.cursor()
        cursor.execute(
            "INSERT INTO job_postings (company_id, role_title, min_cgpa, package_lpa, deadline) VALUES (?, ?, ?, ?, ?)",
            (company_id, role_title.strip(), min_cgpa, package_lpa, deadline.strip()),
        )
        conn.commit()
        return True, f"Job posting '{role_title}' added successfully."
    except Exception as e:
        return False, f"Error: {str(e)}"
    finally:
        conn.close()


if __name__ == "__main__":
    print("Initializing database...")
    init_db(force=True)
    print("Database initialized successfully.")
    print("Current Placement Summary:")
    for row in get_placement_summary():
        print(row)
