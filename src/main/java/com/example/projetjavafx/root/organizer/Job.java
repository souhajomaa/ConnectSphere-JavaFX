package com.example.projetjavafx.root.organizer;



public class Job {
    private int jobId; // Add this field
    private String jobTitle;
    private String eventTitle;
    private String jobLocation;
    private String employmentType;
    private String applicationDeadline;
    private double minSalary;
    private double maxSalary;
    private String currency;
    private String jobDescription;
    private String recruiterName;
    private String recruiterEmail;
    private String createdAt;

    // Constructor
    public Job(int jobId, String jobTitle, String eventTitle, String jobLocation, String employmentType,
               String applicationDeadline, double minSalary, double maxSalary, String currency,
               String jobDescription, String recruiterName, String recruiterEmail, String createdAt) {
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.eventTitle = eventTitle;
        this.jobLocation = jobLocation;
        this.employmentType = employmentType;
        this.applicationDeadline = applicationDeadline;
        this.minSalary = minSalary;
        this.maxSalary = maxSalary;
        this.currency = currency;
        this.jobDescription = jobDescription;
        this.recruiterName = recruiterName;
        this.recruiterEmail = recruiterEmail;
        this.createdAt = createdAt;
    }

    // Add getter for jobId
    public int getJobId() {
        return jobId;
    }

    // Getters and Setters
    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getJobLocation() {
        return jobLocation;
    }

    public void setJobLocation(String jobLocation) {
        this.jobLocation = jobLocation;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    public String getApplicationDeadline() {
        return applicationDeadline;
    }

    public void setApplicationDeadline(String applicationDeadline) {
        this.applicationDeadline = applicationDeadline;
    }

    public double getMinSalary() {
        return minSalary;
    }

    public void setMinSalary(double minSalary) {
        this.minSalary = minSalary;
    }

    public double getMaxSalary() {
        return maxSalary;
    }

    public void setMaxSalary(double maxSalary) {
        this.maxSalary = maxSalary;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public String getRecruiterName() {
        return recruiterName;
    }

    public void setRecruiterName(String recruiterName) {
        this.recruiterName = recruiterName;
    }

    public String getRecruiterEmail() {
        return recruiterEmail;
    }

    public void setRecruiterEmail(String recruiterEmail) {
        this.recruiterEmail = recruiterEmail;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}