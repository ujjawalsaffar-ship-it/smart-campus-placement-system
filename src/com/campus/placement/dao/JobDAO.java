package com.campus.placement.dao;

import com.campus.placement.DBConnection;
import com.campus.placement.models.Company;
import com.campus.placement.models.JobPosting;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JobDAO {

    public List<Company> getAllCompanies() {
        List<Company> companies = new ArrayList<>();
        String sql = "SELECT * FROM companies ORDER BY company_id ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                companies.add(new Company(
                        rs.getInt("company_id"),
                        rs.getString("company_name"),
                        rs.getString("industry"),
                        rs.getString("location"),
                        rs.getString("contact_email")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching companies: " + e.getMessage());
        }
        return companies;
    }

    public List<JobPosting> getAllJobPostings() {
        List<JobPosting> jobs = new ArrayList<>();
        String sql = """
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
        """;

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                jobs.add(new JobPosting(
                        rs.getInt("job_id"),
                        rs.getInt("company_id"),
                        rs.getString("company_name"),
                        rs.getString("role_title"),
                        rs.getDouble("min_cgpa"),
                        rs.getDouble("package_lpa"),
                        rs.getString("deadline"),
                        rs.getInt("total_applications")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching job postings: " + e.getMessage());
        }
        return jobs;
    }

    public boolean addCompany(Company company) {
        String sql = "INSERT INTO companies (company_name, industry, location, contact_email) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, company.getCompanyName());
            pstmt.setString(2, company.getIndustry());
            pstmt.setString(3, company.getLocation());
            pstmt.setString(4, company.getContactEmail());

            int affected = pstmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding company: " + e.getMessage());
            return false;
        }
    }

    public boolean addJobPosting(int companyId, String roleTitle, double minCgpa, double packageLpa, String deadline) {
        String sql = "INSERT INTO job_postings (company_id, role_title, min_cgpa, package_lpa, deadline) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, companyId);
            pstmt.setString(2, roleTitle);
            pstmt.setDouble(3, minCgpa);
            pstmt.setDouble(4, packageLpa);
            pstmt.setString(5, deadline);

            int affected = pstmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding job posting: " + e.getMessage());
            return false;
        }
    }
}
