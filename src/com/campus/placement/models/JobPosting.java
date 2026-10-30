package com.campus.placement.models;

public class JobPosting {
    private int jobId;
    private int companyId;
    private String companyName;
    private String roleTitle;
    private double minCgpa;
    private double packageLpa;
    private String deadline;
    private int totalApplications;

    public JobPosting() {}

    public JobPosting(int jobId, int companyId, String companyName, String roleTitle, double minCgpa, double packageLpa, String deadline, int totalApplications) {
        this.jobId = jobId;
        this.companyId = companyId;
        this.companyName = companyName;
        this.roleTitle = roleTitle;
        this.minCgpa = minCgpa;
        this.packageLpa = packageLpa;
        this.deadline = deadline;
        this.totalApplications = totalApplications;
    }

    public int getJobId() { return jobId; }
    public void setJobId(int jobId) { this.jobId = jobId; }

    public int getCompanyId() { return companyId; }
    public void setCompanyId(int companyId) { this.companyId = companyId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getRoleTitle() { return roleTitle; }
    public void setRoleTitle(String roleTitle) { this.roleTitle = roleTitle; }

    public double getMinCgpa() { return minCgpa; }
    public void setMinCgpa(double minCgpa) { this.minCgpa = minCgpa; }

    public double getPackageLpa() { return packageLpa; }
    public void setPackageLpa(double packageLpa) { this.packageLpa = packageLpa; }

    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }

    public int getTotalApplications() { return totalApplications; }
    public void setTotalApplications(int totalApplications) { this.totalApplications = totalApplications; }

    @Override
    public String toString() {
        return String.format("[Job #%-2d] %-20s | %-26s | Min CGPA: %4.2f | Package: %5.2f LPA | Deadline: %s | Applicants: %d",
                jobId, companyName, roleTitle, minCgpa, packageLpa, deadline, totalApplications);
    }
}
