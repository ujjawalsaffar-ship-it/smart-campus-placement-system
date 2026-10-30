package com.campus.placement.dao;

import com.campus.placement.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class ReportDAO {

    public void printPlacementSummary() {
        String sql = "SELECT * FROM v_placement_summary ORDER BY placement_percentage DESC";
        printFormattedQuery("DEPARTMENT-WISE PLACEMENT SUMMARY (VIEW: v_placement_summary)", sql);
    }

    public void printEligibleJobsForStudent(int studentId) {
        String sql = """
            SELECT 
                student_name,
                department,
                cgpa,
                company_name,
                role_title,
                package_lpa,
                deadline
            FROM v_student_eligible_jobs
            WHERE student_id = ?
            ORDER BY package_lpa DESC
        """;

        System.out.println("\n=========================================================================================");
        System.out.println("  ELIGIBLE JOBS FOR STUDENT #" + studentId + " (VIEW: v_student_eligible_jobs)");
        System.out.println("=========================================================================================");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                printResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error running eligible jobs query: " + e.getMessage());
        }
    }

    public void runCustomQuery(String sql) {
        printFormattedQuery("QUERY RESULT", sql);
    }

    private void printFormattedQuery(String title, String sql) {
        System.out.println("\n=========================================================================================");
        System.out.println("  " + title);
        System.out.println("=========================================================================================");

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            printResultSet(rs);

        } catch (SQLException e) {
            System.err.println("SQL Execution Error: " + e.getMessage());
        }
    }

    private void printResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int cols = meta.getColumnCount();

        // Print header
        StringBuilder header = new StringBuilder();
        StringBuilder divider = new StringBuilder();
        for (int i = 1; i <= cols; i++) {
            String colName = meta.getColumnLabel(i);
            header.append(String.format("%-22s | ", colName));
            divider.append("-----------------------+-");
        }
        System.out.println(header);
        System.out.println(divider);

        // Print rows
        int rowCount = 0;
        while (rs.next()) {
            rowCount++;
            StringBuilder row = new StringBuilder();
            for (int i = 1; i <= cols; i++) {
                String val = rs.getString(i);
                if (val == null) val = "NULL";
                row.append(String.format("%-22s | ", val));
            }
            System.out.println(row);
        }

        if (rowCount == 0) {
            System.out.println("  (No rows returned)");
        } else {
            System.out.println("-----------------------------------------------------------------------------------------");
            System.out.println("Total Rows: " + rowCount);
        }
    }
}
