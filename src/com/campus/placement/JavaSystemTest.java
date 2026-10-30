package com.campus.placement;

import com.campus.placement.dao.ApplicationDAO;
import com.campus.placement.dao.StudentDAO;
import com.campus.placement.models.Student;

public class JavaSystemTest {

    public static void main(String[] args) {
        System.out.println(">>> RUNNING JAVA BACKEND SYSTEM VERIFICATION SUITE <<<\n");

        // 1. Reset Database
        DBConnection.initializeDatabase(true);
        StudentDAO studentDAO = new StudentDAO();
        ApplicationDAO applicationDAO = new ApplicationDAO();

        // 2. Test Trigger 1: Ineligible application
        // Student 9 (Vikram Nair, CGPA 6.50) applying for Job 1 (Min CGPA 7.50)
        System.out.println("Test 1: Testing Trigger 1 (Eligibility / CGPA Check)...");
        ApplicationDAO.Result res1 = applicationDAO.applyForJob(9, 1);
        System.out.println("  Result: " + res1.getMessage());
        if (!res1.isSuccess() && res1.getMessage().contains("TRIGGER 1")) {
            System.out.println("  ✓ PASS: Trigger 1 successfully prevented ineligible student from applying.\n");
        } else {
            System.err.println("  ✗ FAIL: Trigger 1 did not block as expected!");
            System.exit(1);
        }

        // 3. Test Trigger 2: Auto-update placement_status to 'Placed'
        System.out.println("Test 2: Testing Trigger 2 (Auto-update placement status on Selection)...");
        // Student 7 (Siddharth Rao, CGPA 8.40) is currently 'Unplaced'
        Student s7Before = studentDAO.getStudentById(7);
        System.out.println("  Student 7 before: " + s7Before.getName() + " -> " + s7Before.getPlacementStatus());
        if (!"Unplaced".equals(s7Before.getPlacementStatus())) {
            System.err.println("  ✗ Unexpected starting status for Student 7");
            System.exit(1);
        }

        // Apply Student 7 to Job 5 (Min CGPA 7.00)
        ApplicationDAO.Result applyRes = applicationDAO.applyForJob(7, 5);
        System.out.println("  Student 7 application: " + applyRes.getMessage());

        // Find the application ID
        var apps = applicationDAO.getApplicationsByStudent(7);
        int appId = -1;
        for (var a : apps) {
            if (a.getJobId() == 5) {
                appId = a.getApplicationId();
                break;
            }
        }

        // Update application to 'Selected'
        ApplicationDAO.Result updateRes = applicationDAO.updateApplicationStatus(appId, "Selected");
        System.out.println("  Status Update: " + updateRes.getMessage());

        Student s7After = studentDAO.getStudentById(7);
        System.out.println("  Student 7 after: " + s7After.getName() + " -> " + s7After.getPlacementStatus());
        if ("Placed".equals(s7After.getPlacementStatus())) {
            System.out.println("  ✓ PASS: Trigger 2 successfully synchronized student placement_status to 'Placed'.\n");
        } else {
            System.err.println("  ✗ FAIL: Student status was not updated to Placed!");
            System.exit(1);
        }

        // 4. Test Views
        System.out.println("Test 3: Querying analytical views (v_placement_summary)...");
        var reportDAO = new com.campus.placement.dao.ReportDAO();
        reportDAO.printPlacementSummary();
        System.out.println("  ✓ PASS: Views queried successfully.\n");

        System.out.println("======================================================");
        System.out.println("  ALL JAVA JDBC BACKEND VERIFICATION TESTS PASSED!    ");
        System.out.println("======================================================");
    }
}
