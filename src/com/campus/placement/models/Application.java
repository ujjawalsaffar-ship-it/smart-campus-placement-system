package com.campus.placement.models;

public class Application {
    private int applicationId;
    private int studentId;
    private String studentName;
    private String department;
    private double cgpa;
    private int jobId;
    private String companyName;
    private String roleTitle;
    private double packageLpa;
    private String applicationDate;
    private String status;

    public Application() {}

    public Application(int applicationId, int studentId, String studentName, String department, double cgpa,
                       int jobId, String companyName, String roleTitle, double packageLpa,
                       String applicationDate, String status) {
        this.applicationId = applicationId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.department = department;
        this.cgpa = cgpa;
        this.jobId = jobId;
        this.companyName = companyName;
        this.roleTitle = roleTitle;
        this.packageLpa = packageLpa;
        this.applicationDate = applicationDate;
        this.status = status;
    }

    public int getApplicationId() { return applicationId; }
    public void setApplicationId(int applicationId) { this.applicationId = applicationId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public double getCgpa() { return cgpa; }
    public void setCgpa(double cgpa) { this.cgpa = cgpa; }

    public int getJobId() { return jobId; }
    public void setJobId(int jobId) { this.jobId = jobId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getRoleTitle() { return roleTitle; }
    public void setRoleTitle(String roleTitle) { this.roleTitle = roleTitle; }

    public double getPackageLpa() { return packageLpa; }
    public void setPackageLpa(double packageLpa) { this.packageLpa = packageLpa; }

    public String getApplicationDate() { return applicationDate; }
    public void setApplicationDate(String applicationDate) { this.applicationDate = applicationDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return String.format("[App #%-2d] %-18s (%-4s, CGPA: %4.2f) -> %-18s | %-22s | %5.2f LPA | Status: %-11s | Date: %s",
                applicationId, studentName, department, cgpa, companyName, roleTitle, packageLpa, status, applicationDate);
    }
}
