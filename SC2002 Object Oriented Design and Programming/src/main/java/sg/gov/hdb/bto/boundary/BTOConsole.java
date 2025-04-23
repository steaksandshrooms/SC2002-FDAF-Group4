package main.java.sg.gov.hdb.bto.boundary;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import main.java.sg.gov.hdb.bto.control.interfaces.*;
import main.java.sg.gov.hdb.bto.entity.user.*;
import main.java.sg.gov.hdb.bto.util.AuditLogger;
import main.java.sg.gov.hdb.bto.util.ReportGenerator;
import main.java.sg.gov.hdb.bto.entity.project.BTOProject;
import main.java.sg.gov.hdb.bto.entity.application.*;

public class BTOConsole {
    private Scanner scanner;
    private User currentUser;
    
    private AuthenticationService authService;
    private ProjectManager projectManager;
    private ApplicationService applicationService;
    private EnquiryService enquiryService;
    private BookingService bookingService;
    public BTOConsole(AuthenticationService authService, 
                      ProjectManager projectManager,
                      ApplicationService applicationService,
                      EnquiryService enquiryService,
                      BookingService bookingService,
                      DataService dataService) {
        this.scanner = new Scanner(System.in);
        this.authService = authService;
        this.projectManager = projectManager;
        this.applicationService = applicationService;
        this.enquiryService = enquiryService;
        this.bookingService = bookingService;
    }

    public void start() {
        System.out.println("=== BTO Management System ===");

        while (true) {
            if (currentUser == null) {
                showLoginMenu();
            } else {
                showMainMenu();
            }
        }
    }

    private void showLoginMenu() {
        System.out.println("\n1. Login");
        System.out.println("2. Exit");
        System.out.print("Choose an option: ");

        int choice = getIntInput(1, 2);

        switch (choice) {
            case 1:
                login();
                break;
            case 2:
                System.out.println("Exiting system...");
                System.exit(0);
        }
    }

    private void login() {
        System.out.print("\nEnter NRIC (e.g., S1234567A): ");
        String nric = scanner.nextLine().trim().toUpperCase();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        currentUser = authService.login(nric, password);

        if (currentUser != null) {
            System.out.println("\nLogin successful! Welcome, " + currentUser.getName() + " (" + currentUser.getRole() + ")");
        } else {
            System.out.println("Login failed. Please try again.");
        }
    }

    private void showMainMenu() {
        System.out.println("\n=== Main Menu ===");
    System.out.println("1. Browse Projects (with Sort/Filter)");
    System.out.println("2. View My Application");
    System.out.println("3. Submit Enquiry");
    System.out.println("4. View My Enquiries");
    System.out.println("5. Change Password");
    
    if (currentUser instanceof HDBOfficer) {
        System.out.println("6. Officer Functions");
    } else if (currentUser instanceof HDBManager) {
        System.out.println("6. Manager Functions");
    }
    
    System.out.println("0. Logout");
    System.out.print("Choose an option: ");
    
    int choice = getIntInput(0, currentUser instanceof HDBStaff ? 6 : 5);
    
        switch (choice) {
            case 1:
                viewProjectsWithSortAndFilter();
                break;
            case 2:
                viewApplication();
                break;
            case 3:
                submitEnquiry();
                break;
            case 4:
                viewEnquiries();
                break;
            case 5:
                changePassword();
                break;
            case 6:
                if (currentUser instanceof HDBOfficer) {
                    showOfficerMenu();
                } else if (currentUser instanceof HDBManager) {
                    showManagerMenu();
                }
                break;
            case 0:
                currentUser = null;
                System.out.println("Logged out successfully.");
                break;
        }
    }

    private void viewProjects() {
        List<BTOProject> projects = projectManager.getVisibleProjects(currentUser);
    
        if (projects.isEmpty()) {
            System.out.println("\nNo projects available.");
            return;
        }
    
        System.out.println("\n=== Available Projects ===");
        for (int i = 0; i < projects.size(); i++) {
            BTOProject p = projects.get(i);
            System.out.printf("%d. %s (%s) - %s to %s\n",
                    i+1, p.getName(), p.getNeighborhood(),
                    p.getOpenDate(), p.getCloseDate());
        }
    
        // Allow both Applicants and HDBOfficers to apply for projects
        if (currentUser instanceof Applicant || currentUser instanceof HDBOfficer) {
            System.out.print("\nEnter project number to apply (0 to go back): ");
            int choice = getIntInput(0, projects.size());
    
            if (choice > 0) {
                BTOProject selected = projects.get(choice-1);
                
                // Check if HDBOfficer is trying to apply for a project they're handling
                if (currentUser instanceof HDBOfficer && 
                    ((HDBOfficer)currentUser).getAssignedProject() != null &&
                    ((HDBOfficer)currentUser).getAssignedProject().equals(selected)) {
                    System.out.println("You cannot apply for a project you're handling!");
                    return;
                }
                
                applyForProject(selected);
            }
        }
    }

    private void applyForProject(BTOProject project) {
        System.out.println("\nAvailable flat types:");
        for (String type : project.getFlatTypes()) {
            System.out.println("- " + type + " (" + project.getAvailableUnits(type) + " available)");
        }
    
        System.out.print("Enter flat type to apply for: ");
        String flatType = scanner.nextLine();
        
        try {
            applicationService.createApplication(currentUser, project, flatType);
            System.out.println("Application submitted successfully!");
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }

    private void viewApplication() {
        Application app = null;
        
        if (currentUser instanceof Applicant) {
            app = ((Applicant)currentUser).getCurrentApplication();
        } else if (currentUser instanceof HDBOfficer) {
            app = ((HDBOfficer)currentUser).getCurrentApplication();
        }
        
        if (app != null) {
            System.out.println("\n=== Your Application ===");
            System.out.println(app);
            
            // Add option to request withdrawal
            System.out.println("\n1. Request Withdrawal");
            System.out.println("0. Back");
            System.out.print("Choose an option: ");
            
            int choice = getIntInput(0, 1);
            if (choice == 1) {
                requestWithdrawal(app);
            }
        } else {
            System.out.println("\nYou don't have any active applications.");
        }
    }
    
    private void requestWithdrawal(Application app) {
        if (app.isWithdrawalRequested()) {
            System.out.println("You have already requested a withdrawal. Status: " + app.getWithdrawalStatus());
            return;
        }
        
        System.out.println("\n=== Request Application Withdrawal ===");
        System.out.print("Please provide a reason for withdrawal: ");
        String reason = scanner.nextLine();
        
        try {
            applicationService.requestWithdrawal(app, reason);
            System.out.println("Withdrawal request submitted successfully. Please wait for manager approval.");
        } catch (Exception e) {
            System.out.println("Error submitting withdrawal request: " + e.getMessage());
        }
    }

    private void submitEnquiry() {
        List<BTOProject> projects = projectManager.getProjects();
    
        System.out.println("\n=== Select Project for Enquiry ===");
        for (int i = 0; i < projects.size(); i++) {
            System.out.printf("%d. %s\n", i+1, projects.get(i).getName());
        }
    
        System.out.print("Enter project number (0 to cancel): ");
        int choice = getIntInput(0, projects.size());
    
        if (choice > 0) {
            BTOProject project = projects.get(choice-1);
            System.out.print("Enter your enquiry: ");
            String message = scanner.nextLine();
    
            try {
                enquiryService.submitEnquiry(currentUser, project, message);
                System.out.println("Enquiry submitted successfully!");
            } catch (Exception e) {
                System.out.println("Error submitting enquiry: " + e.getMessage());
            }
        }
    }

    private void viewEnquiries() {
        List<Enquiry> enquiries = enquiryService.getEnquiriesForUser(currentUser);
        
        if (enquiries.isEmpty()) {
            System.out.println("\nYou haven't submitted any enquiries.");
            return;
        }
        
        System.out.println("\n=== Your Enquiries ===");
        for (int i = 0; i < enquiries.size(); i++) {
            Enquiry e = enquiries.get(i);
            System.out.println((i+1) + ". " + e.getMessage());
            if (e.getReply() != null) {
                System.out.println("   Reply: " + e.getReply());
            }
        }
        
        System.out.println("\n1. Edit an Enquiry");
        System.out.println("2. Delete an Enquiry");
        System.out.println("0. Back");
        System.out.print("Choose an option: ");
        
        int choice = getIntInput(0, 2);
        if (choice == 1) {
            editEnquiry(enquiries);
        } else if (choice == 2) {
            deleteEnquiry(enquiries);
        }
    }
    
    private void editEnquiry(List<Enquiry> enquiries) {
        System.out.print("Enter enquiry number to edit: ");
        int index = getIntInput(1, enquiries.size()) - 1;
        
        Enquiry enquiry = enquiries.get(index);
        System.out.println("Current message: " + enquiry.getMessage());
        System.out.print("Enter new message: ");
        String newMessage = scanner.nextLine();
        
        try {
            enquiryService.updateEnquiry(enquiry, newMessage);
            System.out.println("Enquiry updated successfully.");
        } catch (Exception e) {
            System.out.println("Error updating enquiry: " + e.getMessage());
        }
    }
    
    private void deleteEnquiry(List<Enquiry> enquiries) {
        System.out.print("Enter enquiry number to delete: ");
        int index = getIntInput(1, enquiries.size()) - 1;
        
        Enquiry enquiry = enquiries.get(index);
        System.out.print("Are you sure you want to delete this enquiry? (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        
        if ("y".equals(confirm)) {
            try {
                enquiryService.deleteEnquiry(enquiry);
                System.out.println("Enquiry deleted successfully.");
            } catch (Exception e) {
                System.out.println("Error deleting enquiry: " + e.getMessage());
            }
        }
    }

    private void changePassword() {
        System.out.print("\nEnter current password: ");
        String current = scanner.nextLine();

        if (!currentUser.authenticate(current)) {
            System.out.println("Incorrect password.");
            return;
        }

        System.out.print("Enter new password: ");
        String newPass = scanner.nextLine();

        currentUser.changePassword(newPass);
        System.out.println("Password changed successfully.");
    }

    private void processFlatBookingWithReceipt() {
        if (currentUser instanceof HDBOfficer) {
            BTOProject project = ((HDBOfficer)currentUser).getAssignedProject();
            if (project == null) {
                System.out.println("\nYou are not assigned to any project.");
                return;
            }
    
            System.out.print("\nEnter applicant NRIC: ");
            String nric = scanner.nextLine().trim().toUpperCase();
            User user = authService.getUser(nric);
    
            if (user == null) {
                System.out.println("User not found.");
                return;
            }
    
            Application app = null;
            if (user instanceof Applicant) {
                app = ((Applicant)user).getCurrentApplication();
            } else if (user instanceof HDBOfficer) {
                app = ((HDBOfficer)user).getCurrentApplication();
            }
    
            if (app == null || !app.getProject().equals(project)) {
                System.out.println("User has no active application for this project.");
                return;
            }
    
            if (!app.getStatus().equals("Successful")) {
                System.out.println("Application is not in 'Successful' status.");
                return;
            }
    
            System.out.println("Available flat types:");
            for (String type : project.getFlatTypes()) {
                System.out.println("- " + type + " (" + project.getAvailableUnits(type) + " available)");
            }
    
            System.out.print("Enter flat type to book: ");
            String flatType = scanner.nextLine();
    
            FlatBooking booking = bookingService.bookFlat(user, project, flatType);
            if (booking != null) {
                app.setStatus("Booked");
                
                // Generate receipt
                String receipt = bookingService.generateBookingReceipt(booking);
                System.out.println("\n" + receipt);
                
                // Ask if they want to export the receipt
                System.out.print("Do you want to export this receipt to a file? (y/n): ");
                String exportChoice = scanner.nextLine().trim().toLowerCase();
                
                if ("y".equals(exportChoice)) {
                    System.out.print("Enter filename (without extension): ");
                    String filename = scanner.nextLine().trim();
                    if (filename.isEmpty()) {
                        filename = "receipt_" + booking.getBookingId();
                    }
                    
                    try {
                        ReportGenerator.exportToCSV("receipts/" + filename + ".txt", receipt);
                        System.out.println("Receipt exported to receipts/" + filename + ".txt");
                    } catch (IOException e) {
                        System.out.println("Error exporting receipt: " + e.getMessage());
                    }
                }
            } else {
                System.out.println("Failed to book flat. No available units of this type.");
            }
        }
    }

    private void createProject() {
        if (currentUser instanceof HDBManager) {
            System.out.println("\n=== Create New Project ===");
    
            System.out.print("Project Name: ");
            String name = scanner.nextLine();
    
            System.out.print("Neighborhood: ");
            String neighborhood = scanner.nextLine();
    
            Map<String, Integer> units = new HashMap<>();
            Map<String, Integer> prices = new HashMap<>();
    
            System.out.print("Number of 2-Room units: ");
            int twoRoomUnits = getIntInput(0, Integer.MAX_VALUE);
            units.put("2-Room", twoRoomUnits);
    
            System.out.print("Price for 2-Room: ");
            int twoRoomPrice = getIntInput(0, Integer.MAX_VALUE);
            prices.put("2-Room", twoRoomPrice);
    
            System.out.print("Number of 3-Room units: ");
            int threeRoomUnits = getIntInput(0, Integer.MAX_VALUE);
            units.put("3-Room", threeRoomUnits);
    
            System.out.print("Price for 3-Room: ");
            int threeRoomPrice = getIntInput(0, Integer.MAX_VALUE);
            prices.put("3-Room", threeRoomPrice);
    
            LocalDate openDate = getDateInput("Application opening date (yyyy-mm-dd): ");
            LocalDate closeDate = getDateInput("Application closing date (yyyy-mm-dd): ");
    
            System.out.print("Number of officer slots: ");
            int officerSlots = getIntInput(1, 10);
    
            HDBManager manager = (HDBManager) currentUser;
            
            try {
                projectManager.createProject(name, neighborhood, units, prices,
                        openDate, closeDate, manager, officerSlots);
                System.out.println("\nProject created successfully!");
            } catch (Exception e) {
                System.out.println("Error creating project: " + e.getMessage());
            }
        }
    }

    /**
     * Gets date input from the user with the specified prompt
     * @param prompt The message to display to the user
     * @return The entered date as a LocalDate object
     */
    private LocalDate getDateInput(String prompt) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        while (true) {
            try {
                System.out.print(prompt);
                if (!scanner.hasNextLine()) {
                    System.out.println("No input found. Returning to menu...");
                    return null;
                }
                String dateStr = scanner.nextLine().trim();
                return LocalDate.parse(dateStr, formatter);
            } catch (Exception e) {
                System.out.println("Invalid date format. Please use yyyy-MM-dd.");
            }
        }
    }
    
    private void showOfficerMenu() {
        System.out.println("\n=== Officer Functions ===");
        System.out.println("1. View Assigned Project");
        System.out.println("2. View Project Applications");
        System.out.println("3. Process Flat Booking (with Receipt)");
        System.out.println("4. View Project Enquiries");
        System.out.println("5. Reply to Enquiry");
        System.out.println("6. Register to Handle Project");
        System.out.println("0. Back");
        System.out.print("Choose an option: ");
    
        int choice = getIntInput(0, 6);
    
        switch (choice) {
            case 1:
                viewAssignedProject();
                break;
            case 2:
                viewProjectApplications();
                break;
            case 3:
                processFlatBookingWithReceipt();
                break;
            case 4:
                viewProjectEnquiries();
                break;
            case 5:
                replyToEnquiry();
                break;
            case 6:
                registerForProject();
                break;
            case 0:
                break;
        }
    }

    private void viewAssignedProject() {
        if (currentUser instanceof HDBOfficer) {
            BTOProject project = ((HDBOfficer)currentUser).getAssignedProject();
            if (project != null) {
                System.out.println("\n=== Assigned Project ===");
                System.out.println("Name: " + project.getName());
                System.out.println("Neighborhood: " + project.getNeighborhood());
                System.out.println("Application Period: " + project.getOpenDate() + " to " + project.getCloseDate());
                System.out.println("Available Units:");
                for (String type : project.getFlatTypes()) {
                    System.out.println("- " + type + ": " + project.getAvailableUnits(type) + " available");
                }
            } else {
                System.out.println("\nYou are not assigned to any project.");
            }
        }
    }

    private void viewProjectApplications() {
        if (currentUser instanceof HDBOfficer) {
            BTOProject project = ((HDBOfficer)currentUser).getAssignedProject();
            if (project != null) {
                List<Application> applications = applicationService.getApplicationsForProject(project);
                if (applications.isEmpty()) {
                    System.out.println("\nNo applications for this project.");
                } else {
                    System.out.println("\n=== Project Applications ===");
                    for (Application app : applications) {
                        System.out.println(app);
                        System.out.println("Applicant: " + app.getApplicant().getName());
                        System.out.println("-----------------------");
                    }
                }
            } else {
                System.out.println("\nYou are not assigned to any project.");
            }
        }
    }

    // Add to BTOConsole.java
    private void viewProjectsWithSortAndFilter() {
        System.out.println("\n=== Project Listing Options ===");
        System.out.println("1. View All Projects");
        System.out.println("2. Sort Projects");
        System.out.println("3. Filter Projects");
        System.out.println("0. Back");
        System.out.print("Choose an option: ");
        
        int choice = getIntInput(0, 3);
        
        switch (choice) {
            case 1:
                viewProjects(); // Original view projects method
                break;
            case 2:
                viewSortedProjects();
                break;
            case 3:
                viewFilteredProjects();
                break;
            case 0:
                break;
        }
    }

    private void viewSortedProjects() {
        System.out.println("\n=== Sort Projects By ===");
        System.out.println("1. Project Name");
        System.out.println("2. Neighborhood");
        System.out.println("3. Opening Date");
        System.out.println("4. Closing Date");
        System.out.print("Choose an option: ");
        
        int sortOption = getIntInput(1, 4);
        
        System.out.println("\n=== Sort Order ===");
        System.out.println("1. Ascending");
        System.out.println("2. Descending");
        System.out.print("Choose an option: ");
        
        int orderOption = getIntInput(1, 2);
        
        String sortBy;
        switch (sortOption) {
            case 1:
                sortBy = "name";
                break;
            case 2:
                sortBy = "neighborhood";
                break;
            case 3:
                sortBy = "openDate";
                break;
            case 4:
                sortBy = "closeDate";
                break;
            default:
                sortBy = "name";
        }
        
        boolean ascending = orderOption == 1;
        
        List<BTOProject> sortedProjects = projectManager.getProjectsSorted(sortBy, ascending);
        displayProjectList(sortedProjects);
    }

    private void viewFilteredProjects() {
        Map<String, Object> filters = new HashMap<>();
        
        boolean addingFilters = true;
        while (addingFilters) {
            System.out.println("\n=== Add Filter ===");
            System.out.println("1. Neighborhood");
            System.out.println("2. Flat Type");
            System.out.println("3. Price Range");
            System.out.println("4. Visibility");
            System.out.println("0. Apply Filters and View Results");
            System.out.print("Choose an option: ");
            
            int filterOption = getIntInput(0, 4);
            
            if (filterOption == 0) {
                addingFilters = false;
            } else {
                switch (filterOption) {
                    case 1:
                        System.out.print("Enter neighborhood name: ");
                        String neighborhood = scanner.nextLine().trim();
                        filters.put("neighborhood", neighborhood);
                        break;
                    case 2:
                        System.out.print("Enter flat type (e.g., 2-Room): ");
                        String flatType = scanner.nextLine().trim();
                        filters.put("flatType", flatType);
                        break;
                    case 3:
                        System.out.print("Enter minimum price: ");
                        int minPrice = getIntInput(0, Integer.MAX_VALUE);
                        filters.put("minPrice", minPrice);
                        
                        System.out.print("Enter maximum price: ");
                        int maxPrice = getIntInput(minPrice, Integer.MAX_VALUE);
                        filters.put("maxPrice", maxPrice);
                        break;
                    case 4:
                        System.out.println("1. Visible Projects");
                        System.out.println("2. Hidden Projects");
                        System.out.print("Choose an option: ");
                        int visibilityOption = getIntInput(1, 2);
                        filters.put("visible", visibilityOption == 1);
                        break;
                }
                
                System.out.println("Current filters: " + filters);
            }
        }
        
        List<BTOProject> filteredProjects = projectManager.getProjectsFiltered(filters);
        displayProjectList(filteredProjects);
    }

    private void displayProjectList(List<BTOProject> projects) {
        if (projects.isEmpty()) {
            System.out.println("\nNo projects match your criteria.");
            return;
        }
        
        System.out.println("\n=== Projects ===");
        for (int i = 0; i < projects.size(); i++) {
            BTOProject p = projects.get(i);
            System.out.printf("%d. %s (%s) - %s to %s\n",
                    i+1, p.getName(), p.getNeighborhood(),
                    p.getOpenDate(), p.getCloseDate());
        }
        
        // Allow application for projects if user is eligible
        if (currentUser instanceof Applicant || currentUser instanceof HDBOfficer) {
            System.out.print("\nEnter project number to apply (0 to go back): ");
            int choice = getIntInput(0, projects.size());
            
            if (choice > 0) {
                BTOProject selected = projects.get(choice-1);
                
                // Check if HDBOfficer is trying to apply for a project they're handling
                if (currentUser instanceof HDBOfficer && 
                    ((HDBOfficer)currentUser).getAssignedProject() != null &&
                    ((HDBOfficer)currentUser).getAssignedProject().equals(selected)) {
                    System.out.println("You cannot apply for a project you're handling!");
                    return;
                }
                
                applyForProject(selected);
            }
        }
    }

    private void viewProjectEnquiries() {
        if (currentUser instanceof HDBOfficer) {
            BTOProject project = ((HDBOfficer)currentUser).getAssignedProject();
            if (project != null) {
                List<Enquiry> enquiries = enquiryService.getEnquiriesForProject(project);
                if (enquiries.isEmpty()) {
                    System.out.println("\nNo enquiries for this project.");
                } else {
                    System.out.println("\n=== Project Enquiries ===");
                    for (Enquiry e : enquiries) {
                        System.out.println(e);
                        System.out.println("From: " + e.getApplicant().getName());
                        if (e.getReply() != null) {
                            System.out.println("Reply: " + e.getReply());
                        }
                        System.out.println("-----------------------");
                    }
                }
            } else {
                System.out.println("\nYou are not assigned to any project.");
            }
        }
    }

    private void replyToEnquiry() {
        if (currentUser instanceof HDBOfficer) {
            BTOProject project = ((HDBOfficer)currentUser).getAssignedProject();
            if (project == null) {
                System.out.println("\nYou are not assigned to any project.");
                return;
            }

            List<Enquiry> enquiries = enquiryService.getEnquiriesForProject(project);
            if (enquiries.isEmpty()) {
                System.out.println("\nNo enquiries to reply to.");
                return;
            }

            System.out.println("\n=== Select Enquiry to Reply ===");
            for (int i = 0; i < enquiries.size(); i++) {
                System.out.printf("%d. %s\n", i+1, enquiries.get(i).getMessage());
            }

            System.out.print("Enter enquiry number (0 to cancel): ");
            int choice = getIntInput(0, enquiries.size());

            if (choice > 0) {
                Enquiry enquiry = enquiries.get(choice-1);
                System.out.print("Enter your reply: ");
                String reply = scanner.nextLine();
                enquiryService.replyToEnquiry(enquiry, reply);
                System.out.println("Reply submitted successfully.");
            }
        }
    }

    private void registerForProject() {
        if (currentUser instanceof HDBOfficer) {
            HDBOfficer officer = (HDBOfficer) currentUser;
            
            // Check if officer is already assigned to a project
            if (officer.getAssignedProject() != null) {
                System.out.println("\nYou are already assigned to a project. You must complete your current assignment first.");
                return;
            }
            
            // Get projects
            List<BTOProject> availableProjects = new ArrayList<>();
            for (BTOProject project : projectManager.getProjects()) {
                // Skip project officer has applied for
                if (officer.getCurrentApplication() != null && 
                    officer.getCurrentApplication().getProject().equals(project)) {
                    continue;
                }
                
                // Only show projects with open slots
                if (project.getOfficers().size() < project.getOfficerSlots()) {
                    availableProjects.add(project);
                }
            }
            
            if (availableProjects.isEmpty()) {
                System.out.println("\nNo projects available for registration.");
                return;
            }
            
            System.out.println("\n=== Available Projects for Registration ===");
            for (int i = 0; i < availableProjects.size(); i++) {
                BTOProject p = availableProjects.get(i);
                int currentOfficers = p.getOfficers().size();
                int maxOfficers = p.getOfficerSlots();
                
                System.out.printf("%d. %s (%s) - %d/%d officers\n",
                        i+1, p.getName(), p.getNeighborhood(), 
                        currentOfficers, maxOfficers);
            }
            
            System.out.print("\nEnter project number to register (0 to go back): ");
            int choice = getIntInput(0, availableProjects.size());
            
            if (choice > 0) {
                BTOProject selected = availableProjects.get(choice-1);
                try {
                    projectManager.addPendingOfficerForProject(officer, selected);
                    System.out.println("Registration submitted for approval.");
                } catch (IllegalStateException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
    
    private void showManagerMenu() {
        System.out.println("\n=== Manager Functions ===");
        System.out.println("1. Create New Project");
        System.out.println("2. View My Projects");
        System.out.println("3. Toggle Project Visibility");
        System.out.println("4. Review Applications");
        System.out.println("5. Approve Officer Registrations");
        System.out.println("6. Review Withdrawal Requests");
        System.out.println("7. Generate Filtered Reports");
        System.out.println("8. View Audit Log");
        System.out.println("0. Back");
        System.out.print("Choose an option: ");

        int choice = getIntInput(0, 8);

        switch (choice) {
            case 1:
                createProject(); // Changed from displaying the "not available" message
                break;
            case 2:
                viewManagerProjects();
                break;
            case 3:
                toggleVisibility();
                break;
            case 4:
                reviewApplications();
                break;
            case 5:
                approveOfficers();
                break;
            case 6:
                reviewWithdrawalRequests();
                break;
            case 7:
                generateFilteredReports();
                break;
            case 8:
                viewAuditLog();
                break;
            case 0:
                break;
            }
        }

    private void reviewWithdrawalRequests() {
        if (currentUser instanceof HDBManager) {
            List<BTOProject> projects = ((HDBManager)currentUser).getManagedProjects();
            if (projects.isEmpty()) {
                System.out.println("\nYou haven't created any projects.");
                return;
            }

            System.out.println("\n=== Select Project to Review Withdrawals ===");
            for (int i = 0; i < projects.size(); i++) {
                BTOProject project = projects.get(i);
                int withdrawals = projectManager.getApplicationsWithPendingWithdrawals(project).size();
                System.out.printf("%d. %s (%d pending withdrawals)\n", i+1, project.getName(), withdrawals);
            }

            System.out.print("Enter project number (0 to cancel): ");
            int choice = getIntInput(0, projects.size());

            if (choice > 0) {
                BTOProject project = projects.get(choice-1);
                List<Application> pendingWithdrawals = projectManager.getApplicationsWithPendingWithdrawals(project);

                if (pendingWithdrawals.isEmpty()) {
                    System.out.println("\nNo pending withdrawal requests for this project.");
                    return;
                }

                System.out.println("\n=== Pending Withdrawal Requests ===");
                for (Application app : pendingWithdrawals) {
                    System.out.println("Applicant: " + app.getApplicant().getName());
                    System.out.println("NRIC: " + app.getApplicant().getNric());
                    System.out.println("Flat Type: " + app.getFlatType());
                    System.out.println("Withdrawal Reason: " + app.getWithdrawalReason());
                    System.out.println("1. Approve Withdrawal");
                    System.out.println("2. Reject Withdrawal");
                    System.out.println("0. Skip");
                    System.out.print("Choose action: ");

                    int action = getIntInput(0, 2);
                    if (action == 1) {
                        projectManager.approveWithdrawal(app);
                        System.out.println("Withdrawal approved.");
                    } else if (action == 2) {
                        projectManager.rejectWithdrawal(app);
                        System.out.println("Withdrawal rejected.");
                    }
                    System.out.println("-----------------------");
                }
            }
        }
    }
    
    private void generateFilteredReports() {
        if (currentUser instanceof HDBManager) {
            List<BTOProject> projects = ((HDBManager)currentUser).getManagedProjects();
            if (projects.isEmpty()) {
                System.out.println("\nYou haven't created any projects.");
                return;
            }

            System.out.println("\n=== Select Project for Report ===");
            for (int i = 0; i < projects.size(); i++) {
                System.out.printf("%d. %s\n", i+1, projects.get(i).getName());
            }

            System.out.print("Enter project number (0 to cancel): ");
            int choice = getIntInput(0, projects.size());

            if (choice > 0) {
                BTOProject project = projects.get(choice-1);
                
                // Set up filters
                Map<String, Object> filters = new HashMap<>();
                
                System.out.println("\n=== Set Report Filters ===");
                System.out.println("1. Filter by Marital Status");
                System.out.println("2. Filter by Flat Type");
                System.out.println("3. Filter by Application Status");
                System.out.println("4. Filter by Age Range");
                System.out.println("0. Generate Report with Current Filters");
                System.out.print("Choose an option: ");
                
                int filterChoice = getIntInput(0, 4);
                
                while (filterChoice != 0) {
                    switch (filterChoice) {
                        case 1:
                            System.out.print("Enter marital status (Single/Married): ");
                            String status = scanner.nextLine().trim();
                            filters.put("maritalStatus", status);
                            break;
                        case 2:
                            System.out.print("Enter flat type (e.g., 2-Room): ");
                            String flatType = scanner.nextLine().trim();
                            filters.put("flatType", flatType);
                            break;
                        case 3:
                            System.out.print("Enter application status (Pending/Successful/Unsuccessful/Booked/Withdrawn): ");
                            String appStatus = scanner.nextLine().trim();
                            filters.put("status", appStatus);
                            break;
                        case 4:
                            System.out.print("Enter minimum age: ");
                            int minAge = getIntInput(0, 150);
                            filters.put("ageAbove", minAge);
                            System.out.print("Enter maximum age: ");
                            int maxAge = getIntInput(minAge, 150);
                            filters.put("ageBelow", maxAge);
                            break;
                    }
                    
                    System.out.println("\nCurrent filters: " + filters);
                    System.out.println("\n1. Filter by Marital Status");
                    System.out.println("2. Filter by Flat Type");
                    System.out.println("3. Filter by Application Status");
                    System.out.println("4. Filter by Age Range");
                    System.out.println("0. Generate Report with Current Filters");
                    System.out.print("Choose an option: ");
                    
                    filterChoice = getIntInput(0, 4);
                }
                
                // Generate the report
                String report = projectManager.generateFilteredReport(project, filters);
                System.out.println("\n" + report);
                
                // Ask if they want to export the report
                System.out.print("Do you want to export this report to a file? (y/n): ");
                String exportChoice = scanner.nextLine().trim().toLowerCase();
                
                if ("y".equals(exportChoice)) {
                    System.out.print("Enter filename (without extension): ");
                    String filename = scanner.nextLine().trim();
                    if (filename.isEmpty()) {
                        filename = "report_" + project.getName() + "_" + LocalDate.now();
                    }
                    
                    try {
                        ReportGenerator.exportToCSV("reports/" + filename + ".txt", report);
                        System.out.println("Report exported to reports/" + filename + ".txt");
                    } catch (IOException e) {
                        System.out.println("Error exporting report: " + e.getMessage());
                    }
                }
            }
        }
    }
    
    private void viewManagerProjects() {
        if (currentUser instanceof HDBManager) {
            List<BTOProject> projects = ((HDBManager)currentUser).getManagedProjects();
            if (projects.isEmpty()) {
                System.out.println("\nYou haven't created any projects.");
            } else {
                System.out.println("\n=== Your Projects ===");
                for (BTOProject p : projects) {
                    System.out.println("Name: " + p.getName());
                    System.out.println("Neighborhood: " + p.getNeighborhood());
                    System.out.println("Status: " + (p.isVisible() ? "Visible" : "Hidden"));
                    System.out.println("Application Period: " + p.getOpenDate() + " to " + p.getCloseDate());
                    System.out.println("Available Units:");
                    for (String type : p.getFlatTypes()) {
                        System.out.println("- " + type + ": " + p.getAvailableUnits(type) + " available");
                    }
                    System.out.println("-----------------------");
                }
            }
        }
    }
    
    private void toggleVisibility() {
        if (currentUser instanceof HDBManager) {
            List<BTOProject> projects = ((HDBManager)currentUser).getManagedProjects();
            if (projects.isEmpty()) {
                System.out.println("\nYou haven't created any projects.");
                return;
            }

            System.out.println("\n=== Select Project to Toggle ===");
            for (int i = 0; i < projects.size(); i++) {
                System.out.printf("%d. %s (%s)\n", i+1,
                        projects.get(i).getName(),
                        projects.get(i).isVisible() ? "Visible" : "Hidden");
            }

            System.out.print("Enter project number (0 to cancel): ");
            int choice = getIntInput(0, projects.size());

            if (choice > 0) {
                BTOProject project = projects.get(choice-1);
                projectManager.toggleProjectVisibility(project);
                System.out.println("Project visibility set to: " + (project.isVisible() ? "Visible" : "Hidden"));
            }
        }
    }

    private void reviewApplications() {
        if (currentUser instanceof HDBManager) {
            List<BTOProject> projects = ((HDBManager)currentUser).getManagedProjects();
            if (projects.isEmpty()) {
                System.out.println("\nYou haven't created any projects.");
                return;
            }

            System.out.println("\n=== Select Project to Review ===");
            for (int i = 0; i < projects.size(); i++) {
                System.out.printf("%d. %s (%d applications)\n", i+1,
                        projects.get(i).getName(),
                        projects.get(i).getApplications().size());
            }

            System.out.print("Enter project number (0 to cancel): ");
            int choice = getIntInput(0, projects.size());

            if (choice > 0) {
                BTOProject project = projects.get(choice-1);
                List<Application> applications = applicationService.getApplicationsForProject(project);

                if (applications.isEmpty()) {
                    System.out.println("\nNo applications for this project.");
                    return;
                }

                System.out.println("\n=== Applications for " + project.getName() + " ===");
                for (Application app : applications) {
                    System.out.println("Applicant: " + app.getApplicant().getName());
                    System.out.println("Flat Type: " + app.getFlatType());
                    System.out.println("Status: " + app.getStatus());
                    System.out.println("1. Approve");
                    System.out.println("2. Reject");
                    System.out.println("0. Skip");
                    System.out.print("Choose action: ");

                    int action = getIntInput(0, 2);
                    if (action == 1) {
                        if (project.hasAvailableUnits(app.getFlatType())) {
                            applicationService.updateApplicationStatus(app, "Successful");
                            System.out.println("Application approved.");
                        } else {
                            System.out.println("No available units of this type. Application cannot be approved.");
                        }
                    } else if (action == 2) {
                        applicationService.updateApplicationStatus(app, "Unsuccessful");
                        System.out.println("Application rejected.");
                    }
                    System.out.println("-----------------------");
                }
            }
        }
    }

    private void approveOfficers() {
        if (currentUser instanceof HDBManager) {
            List<BTOProject> projects = ((HDBManager)currentUser).getManagedProjects();
            if (projects.isEmpty()) {
                System.out.println("\nYou haven't created any projects.");
                return;
            }

            System.out.println("\n=== Select Project to Approve Officers ===");
            for (int i = 0; i < projects.size(); i++) {
                System.out.printf("%d. %s (%d/%d officers)\n", i+1,
                        projects.get(i).getName(),
                        projects.get(i).getOfficers().size(),
                        projects.get(i).getOfficerSlots());
            }

            System.out.print("Enter project number (0 to cancel): ");
            int choice = getIntInput(0, projects.size());

            if (choice > 0) {
                BTOProject project = projects.get(choice-1);
                List<OfficerRegistration> pendingRegistrations = projectManager.getPendingRegistrationsForProject(project);

                if (pendingRegistrations.isEmpty()) {
                    System.out.println("\nNo pending officer registrations for this project.");
                    return;
                }

                System.out.println("\n=== Pending Officer Registrations ===");
                for (OfficerRegistration reg : pendingRegistrations) {
                    System.out.println("Officer: " + reg.getOfficer().getName());
                    System.out.println("1. Approve");
                    System.out.println("2. Reject");
                    System.out.print("Choose action: ");

                    int action = getIntInput(1, 2);
                    if (action == 1) {
                        try {
                            projectManager.updateRegistrationStatus(reg, "Approved");
                            System.out.println("Officer approved.");
                        } catch (IllegalStateException e) {
                            System.out.println(e.getMessage());
                        }
                    } else {
                        projectManager.updateRegistrationStatus(reg, "Rejected");
                        System.out.println("Officer rejected.");
                    }
                    System.out.println("-----------------------");
                }
            }
        }
    }

    private int getIntInput(int min, int max) {
        while (true) {
            try {
                int input = Integer.parseInt(scanner.nextLine());
                if (input >= min && input <= max) {
                    return input;
                }
                System.out.print("Please enter a number between " + min + " and " + max + ": ");
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a number: ");
            }
        }
    }

    private void viewAuditLog() {
    if (!(currentUser instanceof HDBManager)) {
        System.out.println("Only managers can view the audit log.");
        return;
    }
    
    System.out.println("\n=== Audit Log Options ===");
    System.out.println("1. View All Log Entries");
    System.out.println("2. Filter Log Entries");
    System.out.println("3. Export Log to CSV");
    System.out.println("0. Back");
    System.out.print("Choose an option: ");
    
    int choice = getIntInput(0, 3);
    
    switch (choice) {
        case 1:
            displayAuditLog(AuditLogger.getLogEntries());
            break;
        case 2:
            filterAuditLog();
            break;
        case 3:
            exportAuditLog();
            break;
        case 0:
            break;
        }
    }

    private void displayAuditLog(List<String[]> entries) {
        if (entries.isEmpty()) {
            System.out.println("\nNo log entries found.");
            return;
        }
        
        System.out.println("\n=== Audit Log ===");
        System.out.println("Timestamp | User | NRIC | Role | Action | Details");
        System.out.println("---------------------------------------------------------------");
        
        for (String[] entry : entries) {
            if (entry.length < 6) continue;
            
            System.out.printf("%s | %s | %s | %s | %s | %s\n",
                    entry[0], entry[1], entry[2], entry[3], entry[4], entry[5]);
        }
        
        System.out.println("---------------------------------------------------------------");
        System.out.println("Total entries: " + entries.size());
    }

    private void filterAuditLog() {
    String userNric = null;
    String action = null;
    LocalDateTime fromTimestamp = null;
    LocalDateTime toTimestamp = null;
    
    System.out.println("\n=== Filter Audit Log ===");
    
    System.out.print("Filter by NRIC (leave empty for all): ");
    String nricInput = scanner.nextLine().trim();
    if (!nricInput.isEmpty()) {
        userNric = nricInput;
    }
    
    System.out.print("Filter by action (e.g., APPLICATION_CREATED, FLAT_BOOKED, leave empty for all): ");
    String actionInput = scanner.nextLine().trim();
    if (!actionInput.isEmpty()) {
        action = actionInput;
    }
    
    System.out.print("Filter from date (yyyy-MM-dd, leave empty for all): ");
    String fromDateInput = scanner.nextLine().trim();
    if (!fromDateInput.isEmpty()) {
        try {
            fromTimestamp = LocalDateTime.parse(fromDateInput + " 00:00:00", 
                                             DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            System.out.println("Invalid date format. Using no from-date filter.");
        }
    }
    
    System.out.print("Filter to date (yyyy-MM-dd, leave empty for all): ");
    String toDateInput = scanner.nextLine().trim();
    if (!toDateInput.isEmpty()) {
        try {
            toTimestamp = LocalDateTime.parse(toDateInput + " 23:59:59", 
                                           DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            System.out.println("Invalid date format. Using no to-date filter.");
        }
    }
    
    List<String[]> filteredEntries = AuditLogger.getFilteredLogEntries(userNric, action, fromTimestamp, toTimestamp);
    displayAuditLog(filteredEntries);
    }

    private void exportAuditLog() {
        List<String[]> entries = AuditLogger.getLogEntries();
        
        if (entries.isEmpty()) {
            System.out.println("\nNo log entries to export.");
            return;
        }
        
        System.out.print("Enter filename (without extension): ");
        String filename = scanner.nextLine().trim();
        if (filename.isEmpty()) {
            filename = "audit_log_export_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        }
        
        try {
            AuditLogger.exportLogToCSV("logs/" + filename + ".csv", entries);
            System.out.println("Audit log exported to logs/" + filename + ".csv");
        } catch (IOException e) {
            System.out.println("Error exporting audit log: " + e.getMessage());
        }
    }
}