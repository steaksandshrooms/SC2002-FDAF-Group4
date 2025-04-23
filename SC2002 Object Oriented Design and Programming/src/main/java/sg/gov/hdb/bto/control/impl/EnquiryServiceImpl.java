package main.java.sg.gov.hdb.bto.control.impl;

import java.util.*;
import main.java.sg.gov.hdb.bto.control.interfaces.*;
import main.java.sg.gov.hdb.bto.entity.user.*;
import main.java.sg.gov.hdb.bto.entity.project.BTOProject;
import main.java.sg.gov.hdb.bto.entity.application.*;
import main.java.sg.gov.hdb.bto.util.CSVHandler;
import main.java.sg.gov.hdb.bto.util.IdGenerator;
import main.java.sg.gov.hdb.bto.util.AuditLogger;

public class EnquiryServiceImpl implements EnquiryService {
    private List<Enquiry> enquiries = new ArrayList<>();
    private CSVHandler csvHandler;
    private AuthenticationService authService;
    private ProjectManager projectManager;
    
    public EnquiryServiceImpl(CSVHandler csvHandler, AuthenticationService authService, ProjectManager projectManager) {
        this.csvHandler = csvHandler;
        this.authService = authService;
        this.projectManager = projectManager;
        loadEnquiries();
    }
    
    private void loadEnquiries() {
        List<String[]> enquiryData = csvHandler.readCSVSkipHeader("Enquiry.csv");
        for (String[] row : enquiryData) {
            if (row.length < 4) continue;
            
            try {
                String enquiryId = row[0];
                String userNric = row[1];
                String projectName = row[2];
                String message = row[3];
                
                User user = authService.getUser(userNric);
                if (user == null) continue;
                
                BTOProject project = projectManager.getProjectByName(projectName);
                if (project == null) continue;
                
                Enquiry enquiry = new Enquiry(enquiryId, user, project, message);
                
                enquiries.add(enquiry);
                
                // Add to user's enquiries
                if (user instanceof Applicant) {
                    ((Applicant) user).addEnquiry(enquiry);
                } else if (user instanceof HDBOfficer) {
                    ((HDBOfficer) user).addEnquiry(enquiry);
                }
                
                // Add to project
                project.addEnquiry(enquiry);
            } catch (Exception e) {
                System.out.println("Error loading enquiry: " + e.getMessage());
            }
        }
    }
    
    @Override
    public void saveEnquiries() {
        List<String[]> enquiryData = new ArrayList<>();
        enquiryData.add(new String[]{"EnquiryId", "UserId", "ProjectName", "Message"});
        
        for (Enquiry enquiry : enquiries) {
            enquiryData.add(new String[]{
                enquiry.getEnquiryId(),
                enquiry.getApplicant().getNric(),
                enquiry.getProject().getName(),
                enquiry.getMessage()
            });
        }
        
        csvHandler.writeCSV("Enquiry.csv", enquiryData);
        System.out.println("Enquiries saved to Enquiry.csv");
    }
    
    @Override
    public Enquiry submitEnquiry(User applicant, BTOProject project, String message) {
        String enquiryId = IdGenerator.generateId("ENQ");
        Enquiry enquiry = new Enquiry(enquiryId, applicant, project, message);
        
        // Add to applicant's enquiries
        if (applicant instanceof Applicant) {
            ((Applicant)applicant).addEnquiry(enquiry);
        } else if (applicant instanceof HDBOfficer) {
            ((HDBOfficer)applicant).addEnquiry(enquiry);
        }
        
        // Add to project's enquiries
        project.addEnquiry(enquiry);
        
        // Add to list of all enquiries
        enquiries.add(enquiry);
        
        // Log the action
        AuditLogger.logAction(applicant, "ENQUIRY_SUBMITTED", 
                             "EnquiryId: " + enquiry.getEnquiryId() + 
                             ", Project: " + project.getName());
        
        return enquiry;
    }
    
    @Override
    public void updateEnquiry(Enquiry enquiry, String newMessage) {
        enquiry.setMessage(newMessage);
        
        // Log the action
        AuditLogger.logAction(enquiry.getApplicant(), "ENQUIRY_UPDATED", 
                             "EnquiryId: " + enquiry.getEnquiryId());
    }
    
    @Override
    public void deleteEnquiry(Enquiry enquiry) {
        // Remove from the applicant's enquiries
        User applicant = enquiry.getApplicant();
        if (applicant instanceof Applicant) {
            ((Applicant) applicant).removeEnquiry(enquiry);
        } else if (applicant instanceof HDBOfficer) {
            ((HDBOfficer) applicant).removeEnquiry(enquiry);
        }
        
        // Remove from the project's enquiries
        enquiry.getProject().removeEnquiry(enquiry);
        
        // Remove from the global list
        enquiries.remove(enquiry);
        
        // Log the action
        AuditLogger.logAction(enquiry.getApplicant(), "ENQUIRY_DELETED", 
                             "EnquiryId: " + enquiry.getEnquiryId() + 
                             ", Project: " + enquiry.getProject().getName());
    }
    
    @Override
    public void replyToEnquiry(Enquiry enquiry, String reply) {
        enquiry.setReply(reply);
        
        // Log the action
        AuditLogger.logAction(null, "ENQUIRY_REPLIED", 
                             "EnquiryId: " + enquiry.getEnquiryId());
    }
    
    @Override
    public List<Enquiry> getEnquiriesForProject(BTOProject project) {
        return project.getEnquiries();
    }
    
    @Override
    public List<Enquiry> getEnquiriesForUser(User user) {
        if (user instanceof Applicant) {
            return ((Applicant)user).getEnquiries();
        } else if (user instanceof HDBOfficer) {
            return ((HDBOfficer)user).getEnquiries();
        }
        return new ArrayList<>();
    }
    
    @Override
    public List<Enquiry> getAllEnquiries() {
        return new ArrayList<>(enquiries);
    }
}