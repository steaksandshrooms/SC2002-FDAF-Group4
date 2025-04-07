package Models;
import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class Application {
    
    // Applicant 
    private String ApplicationId;
    private Applicant applicant;  // Applicant object
    private Project project;      // Project object
    private ApplicationStatus status;  // Enum for application status
    private FlatInfo selectedflatType; // Selected flat info
    private LocalDate bookeddate;      // Booking date
    
    // Constructor to initialize the Application object
    public Application(Applicant applicant, Project project, FlatInfo flat, String bookingdate) { 
        this.ApplicationId = UUID.randomUUID().toString().substring(0, 8); 
        this.applicant = applicant;
        this.project = project;
        this.status = ApplicationStatus.PENDING;  // Default status
        this.selectedflatType = flat;
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        this.bookeddate = LocalDate.parse(bookingdate, format);  // Parsing the booking date
    }
    
    // Get Application ID
    public String getApplicationId() {
        return this.ApplicationId;
    }

    // Set Application status
    public void setStatus(ApplicationStatus updateStatus) {
        this.status = updateStatus;
    }
    
    // Get Application status
    public ApplicationStatus getStatus() {
        return this.status;
    }
    
    // Get Application details (Prints all details of the application)
    public void getApplicationDetails() {
        System.out.println("===============================================");
        System.out.println("              Application Details             ");
        System.out.println("===============================================");
        
        // Application ID
        System.out.println("\nApplication Id:");
        System.out.println(this.getApplicationId());  // Printing the Application ID
        
        // Applicant Details
        System.out.println("\nApplicant Details:");
        this.applicant.getApplicantDetails();
        
        // Project Details
        System.out.println("\nProject Details:");
        System.out.printf("%-25s: %s\n", "Project Name", this.project.getName());
        
        // Flat Type Details
        System.out.println("\nFlat Type Details:");
        System.out.printf("%-25s: %s\n", "Flat Type Booked", this.selectedflatType.getType());
        System.out.printf("%-25s: %s\n", "Flat Type Name", this.selectedflatType.getTypeName());
        System.out.printf("%-25s: %.2f\n", "Selling Price", this.selectedflatType.sellingprice);
        
        // Application Status and Submission Date
        System.out.println("\nApplication Status:");
        System.out.printf("%-25s: %s\n", "Status", this.status);
        System.out.printf("%-25s: %s\n", "Submission Date", this.bookeddate);
        
        System.out.println("===============================================");
    }

    // Getter for booked date
    public LocalDate getBookedDate() {
        return this.bookeddate;
    }

    // Getter for the selected flat type
    public FlatInfo getSelectedFlatType() {
        return this.selectedflatType;
    }
}
