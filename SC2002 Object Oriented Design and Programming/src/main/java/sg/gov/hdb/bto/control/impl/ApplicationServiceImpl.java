package main.java.sg.gov.hdb.bto.control.impl;

import java.util.*;
import main.java.sg.gov.hdb.bto.control.interfaces.*;
import main.java.sg.gov.hdb.bto.entity.user.*;
import main.java.sg.gov.hdb.bto.entity.project.BTOProject;
import main.java.sg.gov.hdb.bto.entity.application.*;
import main.java.sg.gov.hdb.bto.util.CSVHandler;
import main.java.sg.gov.hdb.bto.util.IdGenerator;
import main.java.sg.gov.hdb.bto.util.AuditLogger;

public class ApplicationServiceImpl implements ApplicationService {
    private List<Application> applications = new ArrayList<>();
    private CSVHandler csvHandler;
    private AuthenticationService authService;
    private ProjectManager projectManager;
    
    public ApplicationServiceImpl(CSVHandler csvHandler, AuthenticationService authService, ProjectManager projectManager) {
        this.csvHandler = csvHandler;
        this.authService = authService;
        this.projectManager = projectManager;
        loadApplications();
    }
    
    private void loadApplications() {
        List<String[]> appData = csvHandler.readCSVSkipHeader("Applications.csv");
        for (String[] row : appData) {
            if (row.length < 13) continue;
            
            try {
                String appId = row[0];
                String applicantNric = row[2];
                String projectName = row[6];
                String flatType = row[8];
                String status = row[12];
                
                User user = authService.getUser(applicantNric);
                if (user == null) continue;
                
                BTOProject project = projectManager.getProjectByName(projectName);
                if (project == null) continue;
                
                Application app = new Application(appId, user, project, flatType);
                app.setStatus(status);
                
                applications.add(app);
                
                // Set as current application for user
                if (user instanceof Applicant) {
                    ((Applicant) user).setCurrentApplication(app);
                } else if (user instanceof HDBOfficer) {
                    ((HDBOfficer) user).setCurrentApplication(app);
                }
                
                // Add to project
                project.addApplication(app);
            } catch (Exception e) {
                System.out.println("Error loading application: " + e.getMessage());
            }
        }
    }
    
    @Override
    public void saveApplications() {
        List<String[]> appData = new ArrayList<>();
        appData.add(new String[]{"ApplicationId", "ApplicantName", "NRIC", "Age", "MaritalStatus", 
                "Role", "ProjectName", "FlatType", "FlatTypeName", "SellingPrice", "NoOfUnits", 
                "AppliedDate", "ApplicationStatus"});
        
        for (Application app : applications) {
            User applicant = app.getApplicant();
            BTOProject project = app.getProject();
            
            appData.add(new String[]{
                app.getApplicationId(),
                applicant.getName(),
                applicant.getNric(),
                String.valueOf(applicant.getAge()),
                applicant.getMaritalStatus(),
                applicant.getRole(),
                project.getName(),
                app.getFlatType(),
                app.getFlatType(),
                String.valueOf(project.getPrice(app.getFlatType())),
                String.valueOf(project.getAvailableUnits(app.getFlatType())),
                app.getApplicationDate().toString(),
                app.getStatus()
            });
        }
        
        csvHandler.writeCSV("Applications.csv", appData);
        System.out.println("Applications saved to Applications.csv");
    }
    
    @Override
    public Application createApplication(User applicant, BTOProject project, String flatType) {
        // Check eligibility
        if (!isEligibleForApplication(applicant, project, flatType)) {
            throw new IllegalStateException("Applicant is not eligible for this application");
        }
        
        // Check if User already has active application
        if ((applicant instanceof Applicant && ((Applicant)applicant).getCurrentApplication() != null && 
            !((Applicant)applicant).getCurrentApplication().getStatus().equals("Unsuccessful")) ||
            (applicant instanceof HDBOfficer && ((HDBOfficer)applicant).getCurrentApplication() != null && 
            !((HDBOfficer)applicant).getCurrentApplication().getStatus().equals("Unsuccessful"))) {
            throw new IllegalStateException("Applicant already has an active application");
        }
        
        // Check if flat type is available
        if (!project.hasAvailableUnits(flatType)) {
            throw new IllegalStateException("No available units of this type");
        }
        
        // Create application
        String applicationId = IdGenerator.generateId("APP");
        Application application = new Application(applicationId, applicant, project, flatType);
        
        // Update applicant's current application
        if (applicant instanceof Applicant) {
            ((Applicant)applicant).setCurrentApplication(application);
        } else if (applicant instanceof HDBOfficer) {
            ((HDBOfficer)applicant).setCurrentApplication(application);
        }
        
        // Add to project applications
        project.addApplication(application);
        
        // Add to list of all applications
        applications.add(application);
        
        // Log the action
        AuditLogger.logAction(applicant, "APPLICATION_CREATED", 
                             "Project: " + project.getName() + ", Flat Type: " + flatType);
        
        return application;
    }
    
    @Override
    public void updateApplicationStatus(Application application, String newStatus) {
        String oldStatus = application.getStatus();
        application.setStatus(newStatus);
        
        // Log the action
        AuditLogger.logAction(null, "APPLICATION_STATUS_UPDATED", 
                             "ApplicationId: " + application.getApplicationId() + 
                             ", Applicant: " + application.getApplicant().getNric() +
                             ", Status: " + oldStatus + " -> " + newStatus);
    }
    
    @Override
    public void requestWithdrawal(Application application, String reason) {
        application.requestWithdrawal(reason);
        
        // Log the action
        AuditLogger.logAction(application.getApplicant(), "WITHDRAWAL_REQUESTED", 
                             "ApplicationId: " + application.getApplicationId() + 
                             ", Reason: " + reason);
    }
    
    @Override
    public void updateWithdrawalStatus(Application application, String status) {
        application.setWithdrawalStatus(status);
        
        // If withdrawal is approved, update application status
        if ("Approved".equals(status)) {
            application.setStatus("Withdrawn");
        }
        
        // Log the action
        AuditLogger.logAction(null, "WITHDRAWAL_STATUS_UPDATED", 
                             "ApplicationId: " + application.getApplicationId() + 
                             ", Status: " + status);
    }
    
    @Override
    public List<Application> getApplicationsWithPendingWithdrawals() {
        List<Application> pendingWithdrawals = new ArrayList<>();
        for (Application app : applications) {
            if (app.isWithdrawalRequested() && "Pending".equals(app.getWithdrawalStatus())) {
                pendingWithdrawals.add(app);
            }
        }
        return pendingWithdrawals;
    }
    
    @Override
    public boolean isEligibleForApplication(User applicant, BTOProject project, String flatType) {
        // Check if HDBOfficer is trying to apply for a project they're handling
        if (applicant instanceof HDBOfficer && 
            ((HDBOfficer)applicant).getAssignedProject() != null &&
            ((HDBOfficer)applicant).getAssignedProject().equals(project)) {
            return false;
        }
        
        // Singles under 35 can only apply for 2-Room flats
        if (applicant.getMaritalStatus().equals("Single") && applicant.getAge() < 35 && !flatType.equals("2-Room")) {
            return false;
        }
        
        // Age requirement - at least 21 years old
        if (applicant.getAge() < 21) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public List<Application> getApplicationsForProject(BTOProject project) {
        return project.getApplications();
    }
    
    @Override
    public List<Application> getAllApplications() {
        return new ArrayList<>(applications);
    }
}