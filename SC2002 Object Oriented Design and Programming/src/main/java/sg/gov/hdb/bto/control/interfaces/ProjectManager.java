package main.java.sg.gov.hdb.bto.control.interfaces;

import java.time.LocalDate;
import java.util.*;
import main.java.sg.gov.hdb.bto.entity.user.*;
import main.java.sg.gov.hdb.bto.entity.project.BTOProject;
import main.java.sg.gov.hdb.bto.entity.application.*;

public interface ProjectManager {
    void createProject(String name, String neighborhood, 
                      Map<String, Integer> units, Map<String, Integer> prices,
                      LocalDate openDate, LocalDate closeDate, 
                      HDBManager manager, int officerSlots);
    void toggleProjectVisibility(BTOProject project);
    void assignOfficerToProject(HDBOfficer officer, BTOProject project);
    List<BTOProject> getProjects();
    List<BTOProject> getVisibleProjects(User user);
    BTOProject getProjectByName(String name);
    void addPendingOfficerForProject(HDBOfficer officer, BTOProject project);
    List<OfficerRegistration> getPendingRegistrationsForProject(BTOProject project);
    void updateRegistrationStatus(OfficerRegistration registration, String status);
    void removePendingOfficer(HDBOfficer officer, BTOProject project);
    List<BTOProject> getProjectsSorted(String sortBy, boolean ascending);
    List<BTOProject> getProjectsFiltered(Map<String, Object> filters);
    String generateFilteredReport(BTOProject project, Map<String, Object> filters);
    List<Application> getApplicationsWithPendingWithdrawals(BTOProject project);
    void approveWithdrawal(Application application);
    void rejectWithdrawal(Application application);
    void saveProjects();
    void saveOfficerRegistrations();
}
