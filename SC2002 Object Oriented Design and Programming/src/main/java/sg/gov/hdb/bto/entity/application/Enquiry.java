package main.java.sg.gov.hdb.bto.entity.application;

import java.time.LocalDate;

import main.java.sg.gov.hdb.bto.entity.project.BTOProject;
import main.java.sg.gov.hdb.bto.entity.user.User;

public class Enquiry {
    private String enquiryId;
    private User applicant;
    private BTOProject project;
    private String message;
    private String reply;
    private LocalDate enquiryDate;

    public Enquiry(String enquiryId, User applicant, BTOProject project, String message) {
        this.enquiryId = enquiryId;
        this.applicant = applicant;
        this.project = project;
        this.message = message;
        this.enquiryDate = LocalDate.now();
    }

    public String getEnquiryId() { return enquiryId; }
    public String getMessage() { return message; }
    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }
    public User getApplicant() { return applicant; }
    public BTOProject getProject() { return project; }
    public LocalDate getEnquiryDate() { return enquiryDate; }
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("Project: %s\nEnquiry: %s\nDate: %s",
                project.getName(), message, enquiryDate);
    }
}
