import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
        // Create sample users
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

        // Assign officer to project
        HDBOfficer officer = (HDBOfficer) users.get("T2109876H");
        project.addOfficer(officer);
        officer.setAssignedProject(project);
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

    public User getUser(String nric) {
        return users.get(nric);
    }

    private boolean validateNRIC(String nric) {
        return nric.matches("[ST]\\d{7}[A-Z]");
    }

    public List<BTOProject> getVisibleProjects(User user) {
        List<BTOProject> visible = new ArrayList<>();

        for (BTOProject project : projects) {
            if (project.isVisible() ||
                    (user instanceof HDBStaff && ((HDBStaff)user).canViewProject(project))) {
                visible.add(project);
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

abstract class User {
    protected String name;
    protected String nric;
    protected int age;
    protected String maritalStatus;
    protected String password;

    public User(String name, String nric, int age, String maritalStatus, String password) {
        this.name = name;
        this.nric = nric;
        this.age = age;
        this.maritalStatus = maritalStatus;
        this.password = password;
    }

    public boolean authenticate(String password) {
        return this.password.equals(password);
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    public String getName() { return name; }
    public String getNric() { return nric; }
    public int getAge() { return age; }
    public String getMaritalStatus() { return maritalStatus; }

    public abstract String getRole();
}

class Applicant extends User {
    private Application currentApplication;
    private List<Enquiry> enquiries;
    private FlatBooking flatBooking;

    public Applicant(String name, String nric, int age, String maritalStatus, String password) {
        super(name, nric, age, maritalStatus, password);
        this.enquiries = new ArrayList<>();
    }

    @Override
    public String getRole() {
        return "Applicant";
    }

    public void applyForProject(BTOProject project, String flatType) {
        if (currentApplication != null && !currentApplication.getStatus().equals("Unsuccessful")) {
            System.out.println("You already have an active application!");
            return;
        }

        // Check eligibility
        if (maritalStatus.equals("Single") && age < 35 && !flatType.equals("2-Room")) {
            System.out.println("As a single under 35, you can only apply for 2-Room flats.");
            return;
        }

        if (maritalStatus.equals("Single") && age < 21) {
            System.out.println("You must be at least 21 years old to apply.");
            return;
        }

        if (maritalStatus.equals("Married") && age < 21) {
            System.out.println("You must be at least 21 years old to apply.");
            return;
        }

        // Check if flat type is available
        if (!project.hasAvailableUnits(flatType)) {
            System.out.println("No available units of this type.");
            return;
        }

        currentApplication = new Application(this, project, flatType);
        project.addApplication(currentApplication);
        System.out.println("Application submitted successfully!");
    }

    public Application getCurrentApplication() {
        return currentApplication;
    }

    public void submitEnquiry(BTOProject project, String message) {
        Enquiry enquiry = new Enquiry(this, project, message);
        enquiries.add(enquiry);
        project.addEnquiry(enquiry);
        System.out.println("Enquiry submitted successfully!");
    }

    public List<Enquiry> getEnquiries() {
        return new ArrayList<>(enquiries);
    }

    public void setFlatBooking(FlatBooking flatBooking) {
        this.flatBooking = flatBooking;
    }
}
