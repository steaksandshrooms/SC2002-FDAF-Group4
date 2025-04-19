import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

class BTOConsole {
    private BTOSystemCore system;
    private Scanner scanner;
    private User currentUser;

    public BTOConsole() {
        this.system = new BTOSystemCore();
        this.scanner = new Scanner(System.in);
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

        currentUser = system.login(nric, password);

        if (currentUser != null) {
            System.out.println("\nLogin successful! Welcome, " + currentUser.getName() + " (" + currentUser.getRole() + ")");
        } else {
            System.out.println("Login failed. Please try again.");
        }
    }

    private void showMainMenu() {
        System.out.println("\n=== Main Menu ===");
        System.out.println("1. View Projects");
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
                viewProjects();
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
        List<BTOProject> projects = system.getVisibleProjects(currentUser);
    
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
    
        if (currentUser instanceof Applicant) {
            ((Applicant)currentUser).applyForProject(project, flatType);
        } else if (currentUser instanceof HDBOfficer) {
            ((HDBOfficer)currentUser).applyForProject(project, flatType);
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
        } else {
            System.out.println("\nYou don't have any active applications.");
        }
    }

    private void submitEnquiry() {
        List<BTOProject> projects = system.getAllProjects();
    
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
    
            if (currentUser instanceof Applicant) {
                ((Applicant)currentUser).submitEnquiry(project, message);
            } else if (currentUser instanceof HDBOfficer) {
                ((HDBOfficer)currentUser).submitEnquiry(project, message);
            }
        }
    }

    private void viewEnquiries() {
        List<Enquiry> enquiries = null;
        
        if (currentUser instanceof Applicant) {
            enquiries = ((Applicant)currentUser).getEnquiries();
        } else if (currentUser instanceof HDBOfficer) {
            enquiries = ((HDBOfficer)currentUser).getEnquiries();
        }
        
        if (enquiries == null || enquiries.isEmpty()) {
            System.out.println("\nYou haven't submitted any enquiries.");
        } else {
            System.out.println("\n=== Your Enquiries ===");
            for (Enquiry e : enquiries) {
                System.out.println(e);
                if (e.getReply() != null) {
                    System.out.println("  Reply: " + e.getReply());
                }
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

    private void showOfficerMenu() {
        System.out.println("\n=== Officer Functions ===");
        System.out.println("1. View Assigned Project");
        System.out.println("2. View Project Applications");
        System.out.println("3. Process Flat Booking");
        System.out.println("4. View Project Enquiries");
        System.out.println("5. Reply to Enquiry");
        System.out.println("6. Register to Handle Project"); // New option
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
                processFlatBooking();
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
                List<Application> applications = project.getApplications();
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

    private void processFlatBooking() {
        if (currentUser instanceof HDBOfficer) {
            BTOProject project = ((HDBOfficer)currentUser).getAssignedProject();
            if (project == null) {
                System.out.println("\nYou are not assigned to any project.");
                return;
            }
    
            System.out.print("\nEnter applicant NRIC: ");
            String nric = scanner.nextLine().trim().toUpperCase();
            User user = system.getUser(nric);
    
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
    
            if (!project.hasAvailableUnits(flatType)) {
                System.out.println("No available units of this type.");
                return;
            }
    
            project.bookFlat(flatType);
            app.setStatus("Booked");
            
            FlatBooking booking = new FlatBooking(user, project, flatType);
            
            if (user instanceof Applicant) {
                ((Applicant)user).setFlatBooking(booking);
            } else if (user instanceof HDBOfficer) {
                ((HDBOfficer)user).setFlatBooking(booking);
            }
            
            System.out.println("Flat booked successfully for " + user.getName());
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
            
            // Get projects without applied project
            List<BTOProject> availableProjects = new ArrayList<>();
            for (BTOProject project : system.getAllProjects()) {
                // Skip project officer has applied for
                if (officer.getCurrentApplication() != null && 
                    officer.getCurrentApplication().getProject().equals(project)) {
                    continue;
                }
                availableProjects.add(project);
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
                
                // Check if the project has open slots
                if (selected.getOfficers().size() >= selected.getOfficerSlots()) {
                    System.out.println("This project already has the maximum number of officers.");
                    return;
                }
                
                system.addPendingOfficerForProject(officer, selected);
            }
        }
    }

    private void viewProjectEnquiries() {
        if (currentUser instanceof HDBOfficer) {
            BTOProject project = ((HDBOfficer)currentUser).getAssignedProject();
            if (project != null) {
                List<Enquiry> enquiries = project.getEnquiries();
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

            List<Enquiry> enquiries = project.getEnquiries();
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
                enquiry.setReply(reply);
                System.out.println("Reply submitted successfully.");
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
        System.out.println("0. Back");
        System.out.print("Choose an option: ");

        int choice = getIntInput(0, 5);

        switch (choice) {
            case 1:
                createProject();
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
            case 0:
                break;
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

            HDBManager manager = (HDBManager)currentUser;
            BTOProject project = new BTOProject(name, neighborhood, units, prices,
                    openDate, closeDate, manager, officerSlots);
            system.addProject(project);
            manager.addManagedProject(project);

            System.out.println("\nProject created successfully!");
        }
    }

    private LocalDate getDateInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String dateStr = scanner.nextLine();
                return LocalDate.parse(dateStr);
            } catch (Exception e) {
                System.out.println("Invalid date format. Please use yyyy-mm-dd.");
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
                project.setVisibility(!project.isVisible());
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
                List<Application> applications = project.getApplications();

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
                            app.setStatus("Successful");
                            System.out.println("Application approved.");
                        } else {
                            System.out.println("No available units of this type. Application cannot be approved.");
                        }
                    } else if (action == 2) {
                        app.setStatus("Unsuccessful");
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
                List<HDBOfficer> pendingOfficers = system.getPendingOfficersForProject(project);

                if (pendingOfficers.isEmpty()) {
                    System.out.println("\nNo pending officer registrations for this project.");
                    return;
                }

                System.out.println("\n=== Pending Officer Registrations ===");
                for (HDBOfficer officer : pendingOfficers) {
                    System.out.println("Officer: " + officer.getName());
                    System.out.println("1. Approve");
                    System.out.println("2. Reject");
                    System.out.print("Choose action: ");

                    int action = getIntInput(1, 2);
                    if (action == 1) {
                        if (project.getOfficers().size() < project.getOfficerSlots()) {
                            project.addOfficer(officer);
                            officer.setAssignedProject(project);
                            system.removePendingOfficer(officer, project);
                            System.out.println("Officer approved.");
                        } else {
                            System.out.println("No available officer slots.");
                        }
                    } else {
                        system.removePendingOfficer(officer, project);
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
}
