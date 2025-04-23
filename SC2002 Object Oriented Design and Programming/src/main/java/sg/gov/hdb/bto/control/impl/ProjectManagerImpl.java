package main.java.sg.gov.hdb.bto.control.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import main.java.sg.gov.hdb.bto.control.interfaces.*;
import main.java.sg.gov.hdb.bto.entity.user.*;
import main.java.sg.gov.hdb.bto.entity.project.BTOProject;
import main.java.sg.gov.hdb.bto.entity.application.*;
import main.java.sg.gov.hdb.bto.util.CSVHandler;
import main.java.sg.gov.hdb.bto.util.IdGenerator;
import main.java.sg.gov.hdb.bto.util.ReportGenerator;
import main.java.sg.gov.hdb.bto.util.AuditLogger;

public class ProjectManagerImpl implements ProjectManager {
    private List<BTOProject> projects = new ArrayList<>();
    private Map<BTOProject, List<OfficerRegistration>> registrations = new HashMap<>();
    private CSVHandler csvHandler;
    private AuthenticationService authService;
    
    public ProjectManagerImpl(CSVHandler csvHandler, AuthenticationService authService) {
        this.csvHandler = csvHandler;
        this.authService = authService;
        loadProjects();
        loadOfficerRegistrations();
    }
    
    private void loadProjects() {
        List<String[]> projectData = csvHandler.readCSVSkipHeader("ProjectList.csv");
        for (String[] row : projectData) {
            if (row.length < 14) continue;
            
            try {
                String name = row[0];
                String neighborhood = row[1];
                
                // Handle flat types, units, and prices
                Map<String, Integer> units = new HashMap<>();
                Map<String, Integer> prices = new HashMap<>();
                
                String type1 = row[2];
                int units1 = Integer.parseInt(row[3]);
                int price1 = Integer.parseInt(row[4]);
                units.put(type1, units1);
                prices.put(type1, price1);
                
                String type2 = row[5];
                int units2 = Integer.parseInt(row[6]);
                int price2 = Integer.parseInt(row[7]);
                units.put(type2, units2);
                prices.put(type2, price2);
                
                // Handle dates
                LocalDate openDate = null;
                LocalDate closeDate = null;
                
                // Try multiple date formats
                DateTimeFormatter[] formatters = {
                    DateTimeFormatter.ofPattern("d/M/yyyy"),
                    DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")
                };
                
                boolean datesSuccessful = false;
                for (DateTimeFormatter formatter : formatters) {
                    try {
                        openDate = LocalDate.parse(row[8], formatter);
                        closeDate = LocalDate.parse(row[9], formatter);
                        datesSuccessful = true;
                        break;
                    } catch (DateTimeParseException e) {
                        // Try next formatter
                    }
                }
                
                if (!datesSuccessful) {
                    System.out.println("Could not parse dates: " + row[8] + ", " + row[9]);
                    continue; // Skip this project
                }
                
                // Handle manager
                String managerNric = row[10];
                HDBManager manager = null;
                for (HDBManager m : authService.getAllManagers()) {
                    if (m.getNric().equals(managerNric)) {
                        manager = m;
                        break;
                    }
                }
                
                if (manager == null) {
                    System.out.println("Manager not found: " + managerNric);
                    continue; // Skip if manager not found
                }
                
                int officerSlots = Integer.parseInt(row[11]);
                
                BTOProject project = new BTOProject(name, neighborhood, units, prices, 
                        openDate, closeDate, manager, officerSlots);
                
                // Add officers
                String officerNrics = row[12];
                if (officerNrics != null && !officerNrics.isEmpty()) {
                    String[] nrics = officerNrics.split(",");
                    for (String nric : nrics) {
                        User user = authService.getUser(nric.trim());
                        if (user instanceof HDBOfficer) {
                            HDBOfficer officer = (HDBOfficer) user;
                            project.addOfficer(officer);
                            officer.setAssignedProject(project);
                        }
                    }
                }
                
                // Set visibility
                boolean visible = "true".equalsIgnoreCase(row[13]);
                project.setVisibility(visible);
                
                projects.add(project);
                manager.addManagedProject(project);
                
                System.out.println("Loaded project: " + name);
            } catch (Exception e) {
                System.out.println("Error loading project: " + e.getMessage() + " for row: " + Arrays.toString(row));
                e.printStackTrace();
            }
        }
    }
    
    private void loadOfficerRegistrations() {
        List<String[]> regData = csvHandler.readCSVSkipHeader("OfficerRegistrations.csv");
        for (String[] row : regData) {
            if (row.length < 4) continue;
            
            try {
                String regId = row[0];
                String officerNric = row[1];
                String projectName = row[2];
                String status = row[3];
                
                User user = authService.getUser(officerNric);
                if (!(user instanceof HDBOfficer)) continue;
                
                HDBOfficer officer = (HDBOfficer) user;
                BTOProject project = getProjectByName(projectName);
                
                if (project == null) continue;
                
                OfficerRegistration registration = new OfficerRegistration(regId, officer, project);
                registration.setStatus(status);
                
                registrations.putIfAbsent(project, new ArrayList<>());
                registrations.get(project).add(registration);
                
                System.out.println("Loaded registration: " + regId);
            } catch (Exception e) {
                System.out.println("Error loading registration: " + e.getMessage());
            }
        }
    }
    
    @Override
    public void saveProjects() {
        List<String[]> projectData = new ArrayList<>();
        projectData.add(new String[]{"Project Name", "Neighborhood", "Type 1", "Number of units for Type 1", 
                "Selling price for Type 1", "Type 2", "Number of units for Type 2", "Selling price for Type 2",
                "Application opening date", "Application closing date", "Manager", "Officer Slot", 
                "Officer", "Visible"});
        
        for (BTOProject project : projects) {
            Map<String, Integer> units = project.getUnits();
            Map<String, Integer> prices = project.getPrices();
            
            // Get the first two flat types
            List<String> flatTypes = new ArrayList<>(units.keySet());
            if (flatTypes.size() < 2) continue;
            
            String type1 = flatTypes.get(0);
            String type2 = flatTypes.get(1);
            
            // Format officer NRICs
            StringBuilder officerNrics = new StringBuilder();
            for (HDBOfficer officer : project.getOfficers()) {
                if (officerNrics.length() > 0) officerNrics.append(",");
                officerNrics.append(officer.getNric());
            }
            
            projectData.add(new String[]{
                project.getName(),
                project.getNeighborhood(),
                type1,
                String.valueOf(units.get(type1)),
                String.valueOf(prices.get(type1)),
                type2,
                String.valueOf(units.get(type2)),
                String.valueOf(prices.get(type2)),
                project.getOpenDate().toString(),
                project.getCloseDate().toString(),
                project.getManager().getNric(),
                String.valueOf(project.getOfficerSlots()),
                officerNrics.toString(),
                String.valueOf(project.isVisible())
            });
        }
        
        csvHandler.writeCSV("ProjectList.csv", projectData);
        System.out.println("Projects saved to ProjectList.csv");
    }
    
    @Override
    public void saveOfficerRegistrations() {
        List<String[]> regData = new ArrayList<>();
        regData.add(new String[]{"RegistrationId", "OfficerID", "ProjectName", "Status"});
        
        for (BTOProject project : registrations.keySet()) {
            for (OfficerRegistration reg : registrations.get(project)) {
                regData.add(new String[]{
                    reg.getRegistrationId(),
                    reg.getOfficer().getNric(),
                    reg.getProject().getName(),
                    reg.getStatus()
                });
            }
        }
        
        csvHandler.writeCSV("OfficerRegistrations.csv", regData);
        System.out.println("Officer registrations saved to OfficerRegistrations.csv");
    }
    
    @Override
    public void createProject(String name, String neighborhood, 
                             Map<String, Integer> units, Map<String, Integer> prices,
                             LocalDate openDate, LocalDate closeDate, 
                             HDBManager manager, int officerSlots) {
        BTOProject project = new BTOProject(name, neighborhood, units, prices, 
                                           openDate, closeDate, manager, officerSlots);
        projects.add(project);
        manager.addManagedProject(project);
        registrations.put(project, new ArrayList<>());
        
        // Log the action
        AuditLogger.logAction(manager, "PROJECT_CREATED", 
                             "Project: " + name + ", Neighborhood: " + neighborhood);
    }
    
    @Override
    public void toggleProjectVisibility(BTOProject project) {
        boolean oldVisibility = project.isVisible();
        project.setVisibility(!oldVisibility);
        
        // Log the action
        AuditLogger.logAction(project.getManager(), "PROJECT_VISIBILITY_CHANGED", 
                             "Project: " + project.getName() + 
                             ", Visibility: " + oldVisibility + " -> " + project.isVisible());
    }
    
    @Override
    public void assignOfficerToProject(HDBOfficer officer, BTOProject project) {
        // Check if officer has applied for this project
        if (officer.getCurrentApplication() != null && 
            officer.getCurrentApplication().getProject().equals(project)) {
            throw new IllegalStateException("Officer cannot be assigned to a project they've applied for");
        }
        
        if (project.getOfficers().size() < project.getOfficerSlots()) {
            project.addOfficer(officer);
            officer.setAssignedProject(project);
            
            // Log the action
            AuditLogger.logAction(project.getManager(), "OFFICER_ASSIGNED", 
                                 "Officer: " + officer.getNric() + 
                                 ", Project: " + project.getName());
        } else {
            throw new IllegalStateException("Project already has maximum number of officers");
        }
    }
    
    @Override
    public List<BTOProject> getProjects() {
        return new ArrayList<>(projects);
    }
    
    @Override
    public BTOProject getProjectByName(String name) {
        for (BTOProject project : projects) {
            if (project.getName().equals(name)) {
                return project;
            }
        }
        return null;
    }
    
    @Override
    public void addPendingOfficerForProject(HDBOfficer officer, BTOProject project) {
        // Check if officer has applied for this project
        if (officer.getCurrentApplication() != null && 
            officer.getCurrentApplication().getProject().equals(project)) {
            throw new IllegalStateException("Cannot register to handle a project you've applied for");
        }
        
        registrations.putIfAbsent(project, new ArrayList<>());
        
        String registrationId = IdGenerator.generateId("REG");
        OfficerRegistration registration = new OfficerRegistration(registrationId, officer, project);
        registrations.get(project).add(registration);
        
        // Log the action
        AuditLogger.logAction(officer, "OFFICER_REGISTRATION", 
                             "RegistrationId: " + registrationId + 
                             ", Project: " + project.getName());
    }
    
    @Override
    public List<OfficerRegistration> getPendingRegistrationsForProject(BTOProject project) {
        List<OfficerRegistration> pending = new ArrayList<>();
        
        if (registrations.containsKey(project)) {
            for (OfficerRegistration reg : registrations.get(project)) {
                if (reg.getStatus().equals("Pending")) {
                    pending.add(reg);
                }
            }
        }
        
        return pending;
    }
    
    @Override
    public void updateRegistrationStatus(OfficerRegistration registration, String status) {
        registration.setStatus(status);
        
        if (status.equals("Approved")) {
            assignOfficerToProject(registration.getOfficer(), registration.getProject());
        }
        
        // Log the action
        AuditLogger.logAction(registration.getProject().getManager(), "REGISTRATION_STATUS_CHANGED", 
                             "RegistrationId: " + registration.getRegistrationId() + 
                             ", Status: " + status);
    }
    
    @Override
    public void removePendingOfficer(HDBOfficer officer, BTOProject project) {
        List<OfficerRegistration> officers = registrations.get(project);
        if (officers != null) {
            officers.removeIf(reg -> reg.getOfficer().equals(officer));
        }
    }
    
    @Override
    public List<BTOProject> getVisibleProjects(User user) {
        List<BTOProject> visible = new ArrayList<>();

        for (BTOProject project : projects) {
            // HDB Staff can see projects based on their role
            if (user instanceof HDBStaff && ((HDBStaff)user).canViewProject(project)) {
                visible.add(project);
                continue;
            }
            
            // For regular applicants
            if (project.isVisible()) {
                // Add eligibility filtering
                if (isEligibleToViewProject(user, project)) {
                    visible.add(project);
                }
            }
        }

        return visible;
    }
    
    @Override
    public List<BTOProject> getProjectsSorted(String sortBy, boolean ascending) {
        List<BTOProject> sortedProjects = new ArrayList<>(projects);
        
        Comparator<BTOProject> comparator = null;
        
        switch (sortBy) {
            case "name":
                comparator = Comparator.comparing(BTOProject::getName);
                break;
            case "neighborhood":
                comparator = Comparator.comparing(BTOProject::getNeighborhood);
                break;
            case "openDate":
                comparator = Comparator.comparing(BTOProject::getOpenDate);
                break;
            case "closeDate":
                comparator = Comparator.comparing(BTOProject::getCloseDate);
                break;
            default:
                return sortedProjects; // Return unsorted if sort criteria not recognized
        }
        
        if (!ascending) {
            comparator = comparator.reversed();
        }
        
        sortedProjects.sort(comparator);
        return sortedProjects;
    }
    
    @Override
    public List<BTOProject> getProjectsFiltered(Map<String, Object> filters) {
        List<BTOProject> filteredProjects = new ArrayList<>(projects);
        
        for (Map.Entry<String, Object> filter : filters.entrySet()) {
            String key = filter.getKey();
            Object value = filter.getValue();
            
            if ("neighborhood".equals(key) && value instanceof String) {
                filteredProjects.removeIf(p -> !p.getNeighborhood().equals(value));
            } else if ("flatType".equals(key) && value instanceof String) {
                filteredProjects.removeIf(p -> !p.getFlatTypes().contains(value));
            } else if ("visible".equals(key) && value instanceof Boolean) {
                filteredProjects.removeIf(p -> p.isVisible() != (Boolean)value);
            } else if ("minPrice".equals(key) && value instanceof Integer) {
                filteredProjects.removeIf(p -> {
                    // Check if any flat type's price is below the minimum
                    for (String flatType : p.getFlatTypes()) {
                        if (p.getPrice(flatType) < (Integer)value) {
                            return true;
                        }
                    }
                    return false;
                });
            } else if ("maxPrice".equals(key) && value instanceof Integer) {
                filteredProjects.removeIf(p -> {
                    // Check if any flat type's price is above the maximum
                    for (String flatType : p.getFlatTypes()) {
                        if (p.getPrice(flatType) > (Integer)value) {
                            return true;
                        }
                    }
                    return false;
                });
            }
        }
        
        return filteredProjects;
    }
    
    @Override
    public String generateFilteredReport(BTOProject project, Map<String, Object> filters) {
        return ReportGenerator.generateFilteredReport(project.getApplications(), filters);
    }
    
    @Override
    public List<Application> getApplicationsWithPendingWithdrawals(BTOProject project) {
        List<Application> pendingWithdrawals = new ArrayList<>();
        for (Application app : project.getApplications()) {
            if (app.isWithdrawalRequested() && "Pending".equals(app.getWithdrawalStatus())) {
                pendingWithdrawals.add(app);
            }
        }
        return pendingWithdrawals;
    }
    
    @Override
    public void approveWithdrawal(Application application) {
        application.setWithdrawalStatus("Approved");
        application.setStatus("Withdrawn");
        
        // Log the action
        AuditLogger.logAction(null, "WITHDRAWAL_APPROVED", 
                             "ApplicationId: " + application.getApplicationId() + 
                             ", Applicant: " + application.getApplicant().getNric());
    }
    
    @Override
    public void rejectWithdrawal(Application application) {
        application.setWithdrawalStatus("Rejected");
        
        // Log the action
        AuditLogger.logAction(null, "WITHDRAWAL_REJECTED", 
                             "ApplicationId: " + application.getApplicationId() + 
                             ", Applicant: " + application.getApplicant().getNric());
    }
    
    private boolean isEligibleToViewProject(User user, BTOProject project) {
        // Rule: Singles under 35 can only see projects with 2-Room flats
        if (user.getMaritalStatus().equals("Single") && user.getAge() < 35) {
            boolean has2RoomFlats = project.getFlatTypes().contains("2-Room") && 
                                    project.getAvailableUnits("2-Room") > 0;
            if (!has2RoomFlats) {
                return false;
            }
        }
        
        // Check age requirement for all users
        if (user.getAge() < 21) {
            return false;
        }
        
        return true;
    }
}