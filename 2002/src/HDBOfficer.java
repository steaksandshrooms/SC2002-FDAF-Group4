import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

class HDBOfficer extends HDBStaff {
    private BTOProject assignedProject;
    private Application currentApplication; // New field for officer's application
    private FlatBooking flatBooking; // For when officer books a flat
    private List<Enquiry> enquiries; // Allow officers to make enquiries too

    public HDBOfficer(String name, String nric, int age, String maritalStatus, String password) {
        super(name, nric, age, maritalStatus, password);
        this.enquiries = new ArrayList<>();
    }

    @Override
    public String getRole() {
        return "HDB Officer";
    }

    @Override
    public boolean canViewProject(BTOProject project) {
        return project.equals(assignedProject);
    }

    public void setAssignedProject(BTOProject project) {
        // Check if officer has applied for this project
        if (currentApplication != null && currentApplication.getProject().equals(project)) {
            System.out.println("You cannot be assigned to a project you've applied for.");
            return;
        }
        this.assignedProject = project;
    }

    public BTOProject getAssignedProject() {
        return assignedProject;
    }
    
    // New method to allow officers to apply for projects
    public void applyForProject(BTOProject project, String flatType) {
        // Check if officer is already assigned to this project
        if (assignedProject != null && assignedProject.equals(project)) {
            System.out.println("You cannot apply for a project you're handling!");
            return;
        }
        
        if (currentApplication != null && !currentApplication.getStatus().equals("Unsuccessful")) {
            System.out.println("You already have an active application!");
            return;
        }

        // Check eligibility - same rules as for Applicants
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
