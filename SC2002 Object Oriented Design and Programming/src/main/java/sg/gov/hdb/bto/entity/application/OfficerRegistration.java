package main.java.sg.gov.hdb.bto.entity.application;

import main.java.sg.gov.hdb.bto.entity.project.BTOProject;
import main.java.sg.gov.hdb.bto.entity.user.HDBOfficer;

public class OfficerRegistration {
    private String registrationId;
    private HDBOfficer officer;
    private BTOProject project;
    private String status;
    
    public OfficerRegistration(String registrationId, HDBOfficer officer, BTOProject project) {
        this.registrationId = registrationId;
        this.officer = officer;
        this.project = project;
        this.status = "Pending";
    }
    
    public String getRegistrationId() { return registrationId; }
    public HDBOfficer getOfficer() { return officer; }
    public BTOProject getProject() { return project; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
