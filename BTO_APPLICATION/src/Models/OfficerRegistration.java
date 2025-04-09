package Models;

public class OfficerRegistration {
    private String registrationId;
    private String officerId;
    private String projectName;
    private String status; // PENDING, APPROVED, REJECTED

    public OfficerRegistration(String registrationId, String officerId, String projectName, String status) {
        this.registrationId = registrationId;
        this.officerId = officerId;
        this.projectName = projectName;
        this.status = status;
    }

    // Getters
    public String getRegistrationId() {
        return registrationId;
    }

    public String getOfficerId() {
        return officerId;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setStatus(String status) {
        this.status = status;
    }

    public String toCSV() {
        return registrationId + "," + officerId + "," + projectName + "," + status;
    }
}
