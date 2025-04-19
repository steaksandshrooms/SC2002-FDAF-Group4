import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

class BTOSystemCore {
    private Map<String, User> users;
    private List<BTOProject> projects;
    private Map<BTOProject, List<HDBOfficer>> pendingOfficers;

    public BTOSystemCore() {
        this.users = new HashMap<>();
        this.projects = new ArrayList<>();
        this.pendingOfficers = new HashMap<>();
        initializeSampleData();
    }

    private void initializeSampleData() {
        // Create sample users with hashed passwords
        users.put("S1234567A", new Applicant("John", "S1234567A", 35, "Single", "password"));
        users.put("T7654321B", new Applicant("Sarah", "T7654321B", 40, "Married", "password"));

        users.put("T2109876H", new HDBOfficer("Daniel", "T2109876H", 36, "Single", "password"));
        users.put("S6543210I", new HDBOfficer("Emily", "S6543210I", 28, "Single", "password"));

        users.put("T8765432F", new HDBManager("Michael", "T8765432F", 36, "Single", "password"));
        users.put("S5678901G", new HDBManager("Jessica", "S5678901G", 26, "Married", "password"));

        // Create sample projects
        Map<String, Integer> units = new HashMap<>();
        units.put("2-Room", 2);
        units.put("3-Room", 3);

        Map<String, Integer> prices = new HashMap<>();
        prices.put("2-Room", 350000);
        prices.put("3-Room", 450000);

        LocalDate openDate = LocalDate.of(2025, 2, 15);
        LocalDate closeDate = LocalDate.of(2025, 3, 20);

        HDBManager manager = (HDBManager) users.get("S5678901G");
        BTOProject project = new BTOProject("Acacia Breeze", "Yishun", units, prices, openDate, closeDate, manager, 3);
        projects.add(project);
        manager.addManagedProject(project);

        // Set up pending officers for demonstration
        pendingOfficers.put(project, new ArrayList<>());
        pendingOfficers.get(project).add((HDBOfficer) users.get("S6543210I"));

        // Assign officer to project
        HDBOfficer officer = (HDBOfficer) users.get("T2109876H");
        project.addOfficer(officer);
        officer.setAssignedProject(project);
        
        // Make the project visible
        project.setVisibility(true);
    }

    public User login(String nric, String password) {
        if (!validateNRIC(nric)) {
            return null;
        }

        User user = users.get(nric);
        if (user != null && user.authenticate(password)) {
            return user;
        }
        return null;
    }

    private boolean validateNRIC(String nric) {
        return nric.matches("[ST]\\d{7}[A-Z]");
    }

    public User getUser(String nric) {
        return users.get(nric);
    }

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
                // Add age and marital status filtering as per requirement 5
                boolean eligible = true;
                
                // Rule: Singles under 35 can only see projects with 2-Room flats
                if (user.getMaritalStatus().equals("Single") && user.getAge() < 35) {
                    boolean has2RoomFlats = project.getFlatTypes().contains("2-Room") && 
                                            project.getAvailableUnits("2-Room") > 0;
                    if (!has2RoomFlats) {
                        eligible = false;
                    }
                }
                
                // Check age requirement for all users
                if (user.getAge() < 21) {
                    eligible = false;
                }
                
                if (eligible) {
                    visible.add(project);
                }
            }
        }

        return visible;
    }

    public List<BTOProject> getAllProjects() {
        return new ArrayList<>(projects);
    }

    public void addProject(BTOProject project) {
        projects.add(project);
    }

    public void addPendingOfficerForProject(HDBOfficer officer, BTOProject project) {
        // Initialize list if needed
        pendingOfficers.putIfAbsent(project, new ArrayList<>());
        
        // Check if officer has applied for this project
        if (officer.getCurrentApplication() != null && 
            officer.getCurrentApplication().getProject().equals(project)) {
            System.out.println("Cannot register to handle a project you've applied for!");
            return;
        }
        
        // Add to pending list
        pendingOfficers.get(project).add(officer);
        System.out.println("Registration submitted for approval.");
    }

    public List<HDBOfficer> getPendingOfficersForProject(BTOProject project) {
        return pendingOfficers.getOrDefault(project, new ArrayList<>());
    }

    public void removePendingOfficer(HDBOfficer officer, BTOProject project) {
        List<HDBOfficer> officers = pendingOfficers.get(project);
        if (officers != null) {
            officers.remove(officer);
        }
    }
}
