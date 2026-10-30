package com.campus.placement.models;

public class Company {
    private int companyId;
    private String companyName;
    private String industry;
    private String location;
    private String contactEmail;

    public Company() {}

    public Company(int companyId, String companyName, String industry, String location, String contactEmail) {
        this.companyId = companyId;
        this.companyName = companyName;
        this.industry = industry;
        this.location = location;
        this.contactEmail = contactEmail;
    }

    public Company(String companyName, String industry, String location, String contactEmail) {
        this(0, companyName, industry, location, contactEmail);
    }

    public int getCompanyId() { return companyId; }
    public void setCompanyId(int companyId) { this.companyId = companyId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    @Override
    public String toString() {
        return String.format("[ID: %-2d] %-22s | %-20s | %-12s | %s",
                companyId, companyName, industry, location, contactEmail);
    }
}
