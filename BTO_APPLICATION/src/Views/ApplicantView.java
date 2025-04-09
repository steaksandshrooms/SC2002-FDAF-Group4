package Views;

import Controller.*;
import Models.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ApplicantView {
    protected Scanner scanner = new Scanner(System.in);
    protected ApplicationController applicationController = new ApplicationController();
    protected ProjectController projectController = new ProjectController();
    protected EnquiryController enquiryController = new EnquiryController();

    public void ApplicantStart(Users user) {
        while (true) {
            System.out.println("\n=====================================");
            System.out.println("        APPLICANT VIEW");
            System.out.println("=====================================");
            System.out.println("1. View All Projects");
            System.out.println("2. View My Applications");
            System.out.println("3. Withdraw Application"); 
            System.out.println("4. Enquiry Menu");
            System.out.println("5. Exit");
            System.out.print("Enter your choice (1-5): ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> viewAllProjects(user);
                case 2 -> viewApplications(user);
                case 3 -> withdrawApplication(user);
                case 4 -> enquiryMenu(user);
                case 5 -> {
                    System.out.println("Exiting Applicant View.");
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    public void viewAllProjects(Users user) {
        List<Project> availableProjects = projectController.displayProjectsByCriteria((Applicant) user);
        System.out.println("\n==== Available Projects ====");
        int index = 1;

        for (Project project : availableProjects) {
            System.out.println(index + ". " + project.getProjectName());
            index++;
        }

        if (availableProjects.isEmpty()) {
            System.out.println("No eligible projects available.");
            return;
        }

        System.out.print("Enter your project choice (1-" + availableProjects.size() + "): ");
        int pchoice = scanner.nextInt();
        scanner.nextLine();

        if (pchoice < 1 || pchoice > availableProjects.size()) {
            System.out.println("Invalid project choice.");
            return;
        }

        Project selectedProject = availableProjects.get(pchoice - 1);
        displayProjectDetails((Applicant) user, selectedProject);
    }

    public void displayProjectDetails(Applicant applicant, Project selectedProject) {
        System.out.println("\n==== Project Details ====");
        System.out.println("Project Name       : " + selectedProject.getProjectName());
        System.out.println("Neighbourhood      : " + selectedProject.getNeighbourhood());
        System.out.println("Opening Date       : " + selectedProject.getOpeningDate());
        System.out.println("Closing Date       : " + selectedProject.getClosingDate());
        System.out.println("Visible            : " + (selectedProject.isVisible() ? "Yes" : "No"));
        System.out.println("Available Officers : " + selectedProject.getAvailableSlots());

        System.out.println("\nFlats:");
        for (FlatInfo flat : selectedProject.getFlats()) {
            System.out.println("Type: " + flat.getTypeName() + " - Units: " + flat.getNoOfUnits());
        }

        System.out.println("\nHDB Officers:");
        for (HDBOfficer officer : selectedProject.getOfficers()) {
            System.out.println("Officer Name: " + officer.getName());
        }

        promptForApplication(applicant, selectedProject);
    }

    public void promptForApplication(Applicant applicant, Project selectedProject) {
        System.out.print("\nWould you like to apply for this project? (Yes/No): ");
        String choice = scanner.nextLine().trim().toLowerCase();

        if (!choice.equals("yes")) {
            System.out.println("Returning to main menu.");
            return;
        }

        System.out.println("\nSelect flat type:");
        for (int i = 0; i < selectedProject.getFlats().size(); i++) {
            FlatInfo flat = selectedProject.getFlats().get(i);
            System.out.println((i + 1) + ". " + flat.getTypeName() + " - Units Available: " + flat.getNoOfUnits());
        }

        int flatChoice = scanner.nextInt();
        scanner.nextLine();

        if (flatChoice < 1 || flatChoice > selectedProject.getFlats().size()) {
            System.out.println("Invalid flat type choice.");
            return;
        }

        FlatInfo selectedFlat = selectedProject.getFlats().get(flatChoice - 1);
        if (selectedFlat.getNoOfUnits() <= 0) {
            System.out.println("❌ No units available for this flat type.");
            return;
        }

        System.out.print("Enter booking date (d/M/yyyy or d/MM/yyyy): ");
        String dateStr = scanner.nextLine().trim();
        LocalDate parsedDate = tryParseDate(dateStr);

        if (parsedDate == null) {
            System.out.println("❌ Invalid date. Please use format d/M/yyyy or d/MM/yyyy.");
            return;
        }

        if (parsedDate.isBefore(selectedProject.getOpeningDate()) || parsedDate.isAfter(selectedProject.getClosingDate())) {
            System.out.println("❌ Booking date must be within the project's application period: " +
                    selectedProject.getOpeningDate() + " to " + selectedProject.getClosingDate());
            return;
        }

        Application newApp = new Application(applicant, selectedProject, selectedFlat, parsedDate);
        applicationController.addApplicationToHashMap(newApp);
    }

    public void applyForProject(Users user) {
        viewAllProjects(user);
    }

    public void viewApplications(Users user) {
        List<Application> applications = applicationController.getApplicationsByUserId(user.getUserID());

        if (applications.isEmpty()) {
            System.out.println("You have no applications.");
            return;
        }

        System.out.println("\n==== Your Applications ====");
        int index = 1;
        for (Application app : applications) {
            System.out.println(index++ + ". Project: " + app.getSelectedProject().getProjectName() +
                    ", Flat: " + app.getSelectedFlat().getTypeName() +
                    ", Date: " + app.getFormattedBookedDate() +
                    ", Status: " + app.getStatus());
        }
    }

    public void withdrawApplication(Users user) {
        List<Application> applications = applicationController.getApplicationsByUserId(user.getUserID());

        if (applications.isEmpty()) {
            System.out.println("❌ You have no applications to withdraw.");
            return;
        }

        System.out.println("\n==== Your Applications ====");
        int index = 1;
        for (Application app : applications) {
            System.out.println(index++ + ". Project: " + app.getSelectedProject().getProjectName() +
                    ", Flat: " + app.getSelectedFlat().getTypeName() +
                    ", Date: " + app.getFormattedBookedDate() +
                    ", Status: " + app.getStatus());
        }

        System.out.print("Enter the number of the application to withdraw: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice < 1 || choice > applications.size()) {
            System.out.println("❌ Invalid selection.");
            return;
        }

        Application selected = applications.get(choice - 1);

        if (selected.getStatus() == ApplicationStatus.BOOKED) {
            System.out.println("❌ Cannot withdraw a booked application.");
            return;
        }

        applicationController.deleteApplication(selected);
    }

    protected LocalDate tryParseDate(String input) {
        List<String> patterns = Arrays.asList("d/M/yyyy", "d/MM/yyyy");

        for (String pattern : patterns) {
            try {
                return LocalDate.parse(input, DateTimeFormatter.ofPattern(pattern));
            } catch (Exception ignored) {}
        }
        return null;
    }

    public void enquiryMenu(Users user) {
        while (true) {
            System.out.println("\n==== ENQUIRY MENU ====");
            System.out.println("1. Submit Enquiry");
            System.out.println("2. View My Enquiries");
            System.out.println("3. Edit Enquiry");
            System.out.println("4. Delete Enquiry");
            System.out.println("5. Back");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> submitEnquiry(user);
                case 2 -> viewEnquiries(user);
                case 3 -> editEnquiry(user);
                case 4 -> deleteEnquiry(user);
                case 5 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    public void submitEnquiry(Users user) {
        System.out.print("Enter project name: ");
        String project = scanner.nextLine().trim();
        System.out.print("Enter your enquiry: ");
        String message = scanner.nextLine().trim();

        String id = UUID.randomUUID().toString().substring(0, 8);
        Enquiry enquiry = new Enquiry(id, user.getUserID(), project, message);
        enquiryController.submitEnquiry(enquiry);
        System.out.println("✅ Enquiry submitted.");
    }

    public void viewEnquiries(Users user) {
        List<Enquiry> list = enquiryController.getEnquiriesByUserId(user.getUserID());
        if (list.isEmpty()) {
            System.out.println("You have no enquiries.");
            return;
        }
        int index = 1;
        for (Enquiry e : list) {
            System.out.println(index++ + ". [" + e.getEnquiryId() + "] Project: " + e.getProjectName() + ", Message: " + e.getMessage());
        }
    }

    public void editEnquiry(Users user) {
        List<Enquiry> list = enquiryController.getEnquiriesByUserId(user.getUserID());
        if (list.isEmpty()) {
            System.out.println("You have no enquiries to edit.");
            return;
        }
        viewEnquiries(user);
        System.out.print("Select enquiry number to edit: ");
        int num = scanner.nextInt();
        scanner.nextLine();
        if (num < 1 || num > list.size()) {
            System.out.println("Invalid selection.");
            return;
        }
        Enquiry selected = list.get(num - 1);
        System.out.print("Enter new message: ");
        String newMsg = scanner.nextLine().trim();
        enquiryController.updateEnquiryMessage(selected.getEnquiryId(), newMsg);
        System.out.println("✅ Enquiry updated.");
    }

    public void deleteEnquiry(Users user) {
        List<Enquiry> list = enquiryController.getEnquiriesByUserId(user.getUserID());
        if (list.isEmpty()) {
            System.out.println("You have no enquiries to delete.");
            return;
        }
        viewEnquiries(user);
        System.out.print("Select enquiry number to delete: ");
        int num = scanner.nextInt();
        scanner.nextLine();
        if (num < 1 || num > list.size()) {
            System.out.println("Invalid selection.");
            return;
        }
        Enquiry selected = list.get(num - 1);
        enquiryController.deleteEnquiry(selected.getEnquiryId());
        System.out.println("✅ Enquiry deleted.");
    }
}
