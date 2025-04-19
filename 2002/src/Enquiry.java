import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

class Enquiry {
    private User applicant; // Changed from Applicant to User
    private BTOProject project;
    private String message;
    private String reply;
    private LocalDate enquiryDate;

    public Enquiry(User applicant, BTOProject project, String message) {
        this.applicant = applicant;
        this.project = project;
        this.message = message;
        this.enquiryDate = LocalDate.now();
    }

    public String getMessage() { return message; }
    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }
    public User getApplicant() { return applicant; } // Changed return type to User

    @Override
    public String toString() {
        return String.format("Project: %s\nEnquiry: %s\nDate: %s",
                project.getName(), message, enquiryDate);
    }
}
