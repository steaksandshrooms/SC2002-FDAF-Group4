package Models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;

public class Application {
    private String applicationId;
    private Applicant applicant;
    private Project selectedProject;
    private FlatInfo selectedFlat;
    private LocalDate bookedDate;
    private ApplicationStatus status;

    // Constructor for new applications
    public Application(Applicant applicant, Project selectedProject, FlatInfo selectedFlat, LocalDate bookedDate) {
        this.applicationId = UUID.randomUUID().toString().substring(0, 8); // Random short ID
        this.applicant = applicant;
        this.selectedProject = selectedProject;
        this.selectedFlat = selectedFlat;
        this.bookedDate = bookedDate;
        this.status = ApplicationStatus.PENDING;
    }

    // Constructor for reading from CSV
    public Application(String applicationId, String applicantName, String userId, int age, String maritalStatusStr,
                       String roleStr, String projectName, String flatType, double sellingPrice, int noOfUnits,
                       String bookedDateStr, ApplicationStatus status) {

        this.applicationId = applicationId;

        // Case-insensitive enum parsing
        MaritalStatus maritalStatus = Arrays.stream(MaritalStatus.values())
                .filter(m -> m.name().equalsIgnoreCase(maritalStatusStr))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid marital status: " + maritalStatusStr));

        Role role = Arrays.stream(Role.values())
                .filter(r -> r.name().equalsIgnoreCase(roleStr))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid role: " + roleStr));

        // Create Applicant using parsed enums
        this.applicant = new Applicant(userId, applicantName, age, maritalStatus);

        // Dummy project & flat data (used for display only, real linkage handled elsewhere)
        this.selectedProject = new Project(projectName, "Unknown", LocalDate.now(), LocalDate.now().plusDays(1), true, 0, null);
        this.selectedFlat = new FlatInfo(Integer.parseInt(flatType), flatType + "-Room", (float) sellingPrice, noOfUnits);

        // Date parsing
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
        this.bookedDate = LocalDate.parse(bookedDateStr, formatter);

        this.status = status;
    }

    // Getters
    public String getApplicationId() { return applicationId; }
    public Applicant getApplicant() { return applicant; }
    public Project getSelectedProject() { return selectedProject; }
    public FlatInfo getSelectedFlat() { return selectedFlat; }
    public LocalDate getBookedDate() { return bookedDate; }
    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }

    // Validate if a date is within the project’s application period
    public boolean isApplicationOpen(String bookedDateStr) {
        LocalDate bookingDate = LocalDate.parse(bookedDateStr, DateTimeFormatter.ofPattern("d/M/yyyy"));
        return bookingDate.isAfter(selectedProject.getOpeningDate()) &&
               bookingDate.isBefore(selectedProject.getClosingDate());
    }

    // Format booking date for display and export
    public String getFormattedBookedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
        return bookedDate.format(formatter);
    }

    // Export application as CSV
    public String toCSV() {
        return applicationId + "," + applicant.getName() + "," + applicant.getUserID() + "," + applicant.getAge() + "," +
               applicant.getmaritalstatus() + "," + applicant.getRole() + "," + selectedProject.getProjectName() + "," +
               selectedFlat.getType() + "," + selectedFlat.getSellingPrice() + "," + selectedFlat.getNoOfUnits() + "," +
               getFormattedBookedDate() + "," + status;
    }
}
