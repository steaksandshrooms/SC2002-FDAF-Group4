import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

class Enquiry {
    private Applicant applicant;
    private BTOProject project;
    private String message;
    private String reply;
    private LocalDate enquiryDate;

    public Enquiry(Applicant applicant, BTOProject project, String message) {
        this.applicant = applicant;
        this.project = project;
        this.message = message;
        this.enquiryDate = LocalDate.now();
    }

    public String getMessage() { return message; }
    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }
    public Applicant getApplicant() { return applicant; }

    @Override
    public String toString() {
        return String.format("Project: %s\nEnquiry: %s\nDate: %s",
                project.getName(), message, enquiryDate);
    }
}