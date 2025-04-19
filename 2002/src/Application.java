import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

class Application {
    private User applicant; // Changed from Applicant to User
    private BTOProject project;
    private String flatType;
    private String status;
    private LocalDate applicationDate;

    public Application(User applicant, BTOProject project, String flatType) {
        this.applicant = applicant;
        this.project = project;
        this.flatType = flatType;
        this.status = "Pending";
        this.applicationDate = LocalDate.now();
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BTOProject getProject() { return project; }
    public String getFlatType() { return flatType; }
    public User getApplicant() { return applicant; } // Changed return type to User

    @Override
    public String toString() {
        return String.format("Project: %s\nFlat Type: %s\nStatus: %s\nApplication Date: %s",
                project.getName(), flatType, status, applicationDate);
    }
}

abstract class User {
    protected String name;
    protected String nric;
    protected int age;
    protected String maritalStatus;
    protected String password; // Will now store the hashed password

    public User(String name, String nric, int age, String maritalStatus, String password) {
        this.name = name;
        this.nric = nric;
        this.age = age;
        this.maritalStatus = maritalStatus;
        this.password = hashPassword(password); // Store hashed password
    }

    private String hashPassword(String plainPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(
                    plainPassword.getBytes(StandardCharsets.UTF_8));
            
            // Convert byte array to hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error hashing password: " + e.getMessage());
            return plainPassword; // Fallback to plaintext if hashing fails
        }
    }

    public boolean authenticate(String password) {
        // Compare the hash of the provided password with the stored hash
        return this.password.equals(hashPassword(password));
    }

    public void changePassword(String newPassword) {
        this.password = hashPassword(newPassword);
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
