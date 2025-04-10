import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

class Application {
    private User applicant;
    private BTOProject project;
    private String flatType;
    private String status;
    private LocalDate applicationDate;

    public Application(User applicant, BTOProject project, String flatType) {
        this.applicant = applicant;
        this.project = project;
        this.flatType = flatType;
        this.status = "Pending";
        this.applicationDate = LocalDate.now();
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BTOProject getProject() { return project; }
    public String getFlatType() { return flatType; }
    public User getApplicant() { return applicant; }

    @Override
    public String toString() {
        return String.format("Project: %s\nFlat Type: %s\nStatus: %s\nApplication Date: %s",
                project.getName(), flatType, status, applicationDate);
    }
}