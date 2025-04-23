package main.java.sg.gov.hdb.bto.entity.application;

import java.time.LocalDate;
import main.java.sg.gov.hdb.bto.entity.user.User;
import main.java.sg.gov.hdb.bto.entity.project.BTOProject;

public class Application {
    private String applicationId;
    private User applicant;
    private BTOProject project;
    private String flatType;
    private String status;
    private LocalDate applicationDate;
    private boolean withdrawalRequested;
    private String withdrawalReason;
    private String withdrawalStatus;

    public Application(String applicationId, User applicant, BTOProject project, String flatType) {
        this.applicationId = applicationId;
        this.applicant = applicant;
        this.project = project;
        this.flatType = flatType;
        this.status = "Pending";
        this.applicationDate = LocalDate.now();
        this.withdrawalRequested = false;
        this.withdrawalReason = null;
        this.withdrawalStatus = null;
    }

    public String getApplicationId() { return applicationId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BTOProject getProject() { return project; }
    public String getFlatType() { return flatType; }
    public User getApplicant() { return applicant; }
    public LocalDate getApplicationDate() { return applicationDate; }
    public boolean isWithdrawalRequested() { return withdrawalRequested; }
    
    public void requestWithdrawal(String reason) {
        this.withdrawalRequested = true;
        this.withdrawalReason = reason;
        this.withdrawalStatus = "Pending";
    }
    
    public String getWithdrawalReason() { return withdrawalReason; }
    
    public String getWithdrawalStatus() { return withdrawalStatus; }
    
    public void setWithdrawalStatus(String status) { this.withdrawalStatus = status; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Project: %s\nFlat Type: %s\nStatus: %s\nApplication Date: %s",
                project.getName(), flatType, status, applicationDate));
        
        if (withdrawalRequested) {
            sb.append("\nWithdrawal Requested: Yes");
            sb.append("\nWithdrawal Reason: ").append(withdrawalReason);
            sb.append("\nWithdrawal Status: ").append(withdrawalStatus);
        }
        
        return sb.toString();
    }
}
