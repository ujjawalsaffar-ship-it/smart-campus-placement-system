package com.campus.placement.dao;

import com.campus.placement.DBConnection;
import com.campus.placement.models.Application;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ApplicationDAO {

    public List<Application> getAllApplications() {
        return getApplicationsByQuery("SELECT * FROM applications ORDER BY application_id DESC", null);
    }

    public List<Application> getApplicationsByStudent(int studentId) {
        return getApplicationsByQuery(
                "SELECT * FROM applications WHERE student_id = ? ORDER BY application_id DESC",
                studentId
        );
    }

    private List<Application> getApplicationsByQuery(String filterSql, Integer studentId) {
        List<Application> apps = new ArrayList<>();
        String sql = """
            SELECT 
                a.application_id,
                a.student_id,
                s.name AS student_name,
                s.department,
                s.cgpa,
                jp.job_id,
                c.company_name,
                jp.role_title,
                jp.package_lpa,
                a.application_date,
                a.status
            FROM applications a
            JOIN students s ON a.student_id = s.student_id
            JOIN job_postings jp ON a.job_id = jp.job_id
            JOIN companies c ON jp.company_id = c.company_id
        """;

        if (studentId != null) {
            sql += " WHERE a.student_id = ? ORDER BY a.application_id DESC";
        } else {
            sql += " ORDER BY a.application_id DESC";
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (studentId != null) {
                pstmt.setInt(1, studentId);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    apps.add(new Application(
                            rs.getInt("application_id"),
                            rs.getInt("student_id"),
                            rs.getString("student_name"),
                            rs.getString("department"),
                            rs.getDouble("cgpa"),
                            rs.getInt("job_id"),
                            rs.getString("company_name"),
                            rs.getString("role_title"),
                            rs.getDouble("package_lpa"),
                            rs.getString("application_date"),
                            rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching applications: " + e.getMessage());
        }
        return apps;
    }

    /**
     * Submits a job application.
     * Enforces the database trigger 'trg_check_cgpa_before_apply' and the UNIQUE constraint.
     */
    public Result applyForJob(int studentId, int jobId) {
        String sql = "INSERT INTO applications (student_id, job_id, status) VALUES (?, ?, 'Applied')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            pstmt.setInt(2, jobId);
            pstmt.executeUpdate();
            return new Result(true, "Application submitted successfully!");

        } catch (SQLException e) {
            String msg = e.getMessage();
            if (msg.contains("Ineligible") || msg.contains("CHECK_CGPA") || msg.contains("lower than minimum")) {
                return new Result(false, "❌ [DATABASE TRIGGER 1 ENFORCED]: Student CGPA is lower than the job requirement. Application rejected!");
            } else if (msg.contains("UNIQUE constraint failed") || msg.contains("uq_student_job")) {
                return new Result(false, "⚠️ [DATABASE INTEGRITY ERROR]: Student has already applied for this job.");
            } else {
                return new Result(false, "Error submitting application: " + msg);
            }
        }
    }

    /**
     * Updates an application status (e.g., to 'Selected').
     * Enforces trigger 'trg_auto_update_placement_status' which sets students.placement_status = 'Placed'.
     */
    public Result updateApplicationStatus(int applicationId, String newStatus) {
        String sql = "UPDATE applications SET status = ? WHERE application_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus);
            pstmt.setInt(2, applicationId);
            int affected = pstmt.executeUpdate();

            if (affected > 0) {
                if ("Selected".equalsIgnoreCase(newStatus)) {
                    return new Result(true, "✓ Status updated to 'Selected'. [DATABASE TRIGGER 2 FIRED]: Student placement_status synchronized to 'Placed'!");
                }
                return new Result(true, "✓ Status updated to '" + newStatus + "' successfully.");
            } else {
                return new Result(false, "Application ID #" + applicationId + " not found.");
            }

        } catch (SQLException e) {
            return new Result(false, "Error updating application status: " + e.getMessage());
        }
    }

    public static class Result {
        private final boolean success;
        private final String message;

        public Result(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
}
