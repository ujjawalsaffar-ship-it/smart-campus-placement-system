package com.campus.placement;

import com.campus.placement.dao.ApplicationDAO;
import com.campus.placement.dao.JobDAO;
import com.campus.placement.dao.ReportDAO;
import com.campus.placement.dao.StudentDAO;
import com.campus.placement.models.Application;
import com.campus.placement.models.Company;
import com.campus.placement.models.JobPosting;
import com.campus.placement.models.Student;

import java.util.List;
import java.util.Scanner;

public class PlacementApp {

    private static final StudentDAO studentDAO = new StudentDAO();
    private static final JobDAO jobDAO = new JobDAO();
    private static final ApplicationDAO applicationDAO = new ApplicationDAO();
    private static final ReportDAO reportDAO = new ReportDAO();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("     🎓 SMART CAMPUS PLACEMENT MANAGEMENT SYSTEM (RDBMS + JAVA JDBC)      ");
        System.out.println("==========================================================================");

        // Ensure database is initialized
        DBConnection.initializeDatabase(false);

        boolean running = true;
        while (running) {
            printMainMenu();
            System.out.print("👉 Enter your choice [0-11]: ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> viewAllStudents();
                case "2" -> viewCompaniesAndJobs();
                case "3" -> viewAllApplications();
                case "4" -> submitApplicationFlow();
                case "5" -> updateStatusFlow();
                case "6" -> addNewStudentFlow();
                case "7" -> addNewJobPostingFlow();
                case "8" -> reportDAO.printPlacementSummary();
                case "9" -> checkEligibleJobsFlow();
                case "10" -> runEvaluationQueriesFlow();
                case "11" -> resetDatabaseFlow();
                case "0" -> {
                    System.out.println("\nThank you for using Smart Campus Placement Management System. Goodbye!\n");
                    running = false;
                }
                default -> System.out.println("⚠️ Invalid option. Please enter a number between 0 and 11.");
            }
        }
    }

    private static void printMainMenu() {
        System.out.println("\n------------------------------ MAIN MENU ---------------------------------");
        System.out.println(" [1]  List All Students (Check CGPA & Placement Status)");
        System.out.println(" [2]  List Companies & Active Job Openings");
        System.out.println(" [3]  List All Applications");
        System.out.println(" [4]  Apply for a Job (🧪 Tests Trigger 1: Eligibility & CGPA Check)");
        System.out.println(" [5]  Update Application Status (🧪 Tests Trigger 2: Auto-update to Placed)");
        System.out.println(" [6]  Register New Student (Tests CHECK & UNIQUE Constraints)");
        System.out.println(" [7]  Post New Job Drive");
        System.out.println(" [8]  View Placement Summary (View: v_placement_summary)");
        System.out.println(" [9]  Find Eligible Jobs for a Student (View: v_student_eligible_jobs)");
        System.out.println(" [10] Run Evaluation Showcase Queries (Joins, HAVING, Subqueries)");
        System.out.println(" [11] Reset Database to Fresh Seed Data");
        System.out.println(" [0]  Exit");
        System.out.println("--------------------------------------------------------------------------");
    }

    private static void viewAllStudents() {
        List<Student> students = studentDAO.getAllStudents();
        System.out.println("\n--- REGISTERED STUDENTS (" + students.size() + ") ---");
        for (Student s : students) {
            System.out.println("  " + s);
        }
    }

    private static void viewCompaniesAndJobs() {
        List<Company> companies = jobDAO.getAllCompanies();
        System.out.println("\n--- PARTICIPATING COMPANIES (" + companies.size() + ") ---");
        for (Company c : companies) {
            System.out.println("  " + c);
        }

        List<JobPosting> jobs = jobDAO.getAllJobPostings();
        System.out.println("\n--- ACTIVE JOB POSTINGS (" + jobs.size() + ") ---");
        for (JobPosting j : jobs) {
            System.out.println("  " + j);
        }
    }

    private static void viewAllApplications() {
        List<Application> apps = applicationDAO.getAllApplications();
        System.out.println("\n--- APPLICATION RECORDS (" + apps.size() + ") ---");
        for (Application a : apps) {
            System.out.println("  " + a);
        }
    }

    private static void submitApplicationFlow() {
        System.out.println("\n--- SUBMIT JOB APPLICATION ---");
        viewAllStudents();
        System.out.print("\nEnter Student ID: ");
        int studentId = readInt();

        Student s = studentDAO.getStudentById(studentId);
        if (s == null) {
            System.out.println("❌ Student not found.");
            return;
        }

        System.out.println("Selected: " + s.getName() + " | CGPA: " + s.getCgpa());

        List<JobPosting> jobs = jobDAO.getAllJobPostings();
        System.out.println("\nAvailable Jobs:");
        for (JobPosting j : jobs) {
            System.out.printf("  [Job #%d] %s - %s (Min CGPA: %.2f | %.2f LPA)\n",
                    j.getJobId(), j.getCompanyName(), j.getRoleTitle(), j.getMinCgpa(), j.getPackageLpa());
        }

        System.out.print("\nEnter Job ID to apply: ");
        int jobId = readInt();

        ApplicationDAO.Result result = applicationDAO.applyForJob(studentId, jobId);
        System.out.println("\n" + result.getMessage());
    }

    private static void updateStatusFlow() {
        System.out.println("\n--- UPDATE APPLICATION STATUS ---");
        viewAllApplications();

        System.out.print("\nEnter Application ID: ");
        int appId = readInt();

        System.out.println("Select New Status:");
        System.out.println(" 1. Shortlisted");
        System.out.println(" 2. Selected (Fires Trigger 2 to mark student Placed)");
        System.out.println(" 3. Rejected");
        System.out.print("Choice: ");
        String choice = scanner.nextLine().trim();

        String status = switch (choice) {
            case "1" -> "Shortlisted";
            case "2" -> "Selected";
            case "3" -> "Rejected";
            default -> null;
        };

        if (status == null) {
            System.out.println("❌ Invalid status choice.");
            return;
        }

        ApplicationDAO.Result result = applicationDAO.updateApplicationStatus(appId, status);
        System.out.println("\n" + result.getMessage());
    }

    private static void addNewStudentFlow() {
        System.out.println("\n--- REGISTER NEW STUDENT ---");
        System.out.print("Enter Full Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter Email Address: ");
        String email = scanner.nextLine().trim();

        System.out.print("Enter Department (CSE / ECE / ME / IT): ");
        String dept = scanner.nextLine().trim().toUpperCase();

        System.out.print("Enter CGPA (0.00 - 10.00): ");
        double cgpa = readDouble();

        Student s = new Student(name, email, dept, cgpa);
        boolean ok = studentDAO.addStudent(s);
        if (ok) {
            System.out.println("✓ Student registered successfully with ID #" + s.getStudentId());
        } else {
            System.out.println("❌ Failed to add student. Check email uniqueness or CGPA constraint.");
        }
    }

    private static void addNewJobPostingFlow() {
        System.out.println("\n--- POST NEW JOB DRIVE ---");
        List<Company> companies = jobDAO.getAllCompanies();
        for (Company c : companies) {
            System.out.println("  [ID: " + c.getCompanyId() + "] " + c.getCompanyName());
        }
        System.out.print("\nEnter Company ID: ");
        int companyId = readInt();

        System.out.print("Enter Role Title (e.g. Data Scientist): ");
        String role = scanner.nextLine().trim();

        System.out.print("Enter Minimum CGPA Required: ");
        double minCgpa = readDouble();

        System.out.print("Enter Package in LPA: ");
        double lpa = readDouble();

        System.out.print("Enter Application Deadline (YYYY-MM-DD): ");
        String deadline = scanner.nextLine().trim();

        boolean ok = jobDAO.addJobPosting(companyId, role, minCgpa, lpa, deadline);
        if (ok) {
            System.out.println("✓ Job drive posted successfully!");
        } else {
            System.out.println("❌ Failed to post job drive.");
        }
    }

    private static void checkEligibleJobsFlow() {
        System.out.print("\nEnter Student ID to check eligible drives: ");
        int studentId = readInt();
        Student s = studentDAO.getStudentById(studentId);
        if (s != null) {
            System.out.println("Student: " + s.getName() + " | Dept: " + s.getDepartment() + " | CGPA: " + s.getCgpa());
            reportDAO.printEligibleJobsForStudent(studentId);
        } else {
            System.out.println("❌ Student not found.");
        }
    }

    private static void runEvaluationQueriesFlow() {
        System.out.println("\n--- SELECT EVALUATION SHOWCASE QUERY ---");
        System.out.println(" [1] High-achieving unplaced students (CGPA >= 8.0)");
        System.out.println(" [2] Companies offering above 10 LPA average package (HAVING clause)");
        System.out.println(" [3] Student(s) with highest package secured (Subquery with MAX)");
        System.out.println(" [4] Application count per job role (GROUP BY with JOIN)");
        System.out.print("Choice [1-4]: ");
        String qChoice = scanner.nextLine().trim();

        switch (qChoice) {
            case "1" -> reportDAO.runCustomQuery("""
                SELECT student_id, name, department, cgpa, placement_status 
                FROM students 
                WHERE placement_status = 'Unplaced' AND cgpa >= 8.00 
                ORDER BY cgpa DESC;
            """);
            case "2" -> reportDAO.runCustomQuery("""
                SELECT c.company_name, c.industry, COUNT(jp.job_id) AS total_jobs, ROUND(AVG(jp.package_lpa), 2) AS avg_package_lpa
                FROM companies c
                JOIN job_postings jp ON c.company_id = jp.company_id
                GROUP BY c.company_id, c.company_name, c.industry
                HAVING AVG(jp.package_lpa) >= 10.00
                ORDER BY avg_package_lpa DESC;
            """);
            case "3" -> reportDAO.runCustomQuery("""
                SELECT s.name AS student_name, s.department, c.company_name, jp.role_title, jp.package_lpa
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
            """);
            case "4" -> reportDAO.runCustomQuery("""
                SELECT c.company_name, jp.role_title, jp.package_lpa, COUNT(a.application_id) AS total_applicants
                FROM job_postings jp
                JOIN companies c ON jp.company_id = c.company_id
                LEFT JOIN applications a ON jp.job_id = a.job_id
                GROUP BY jp.job_id, c.company_name, jp.role_title, jp.package_lpa
                ORDER BY total_applicants DESC;
            """);
            default -> System.out.println("Invalid selection.");
        }
    }

    private static void resetDatabaseFlow() {
        System.out.print("Are you sure you want to reset the database to initial seed state? (y/n): ");
        String ans = scanner.nextLine().trim();
        if ("y".equalsIgnoreCase(ans) || "yes".equalsIgnoreCase(ans)) {
            DBConnection.initializeDatabase(true);
            System.out.println("✓ Database reset complete.");
        }
    }

    private static int readInt() {
        while (true) {
            try {
                String line = scanner.nextLine().trim();
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.print("Invalid integer. Enter again: ");
            }
        }
    }

    private static double readDouble() {
        while (true) {
            try {
                String line = scanner.nextLine().trim();
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.print("Invalid number. Enter again: ");
            }
        }
    }
}
