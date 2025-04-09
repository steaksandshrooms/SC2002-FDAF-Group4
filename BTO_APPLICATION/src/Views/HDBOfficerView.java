package Views;

import Controller.*;
import Models.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class HDBOfficerView extends ApplicantView {
    private final OfficerRegistrationController officerRegController = new OfficerRegistrationController();
    private final ProjectController projectController = new ProjectController();
    private final ApplicationController applicationController = new ApplicationController();
    private final EnquiryController enquiryController = new EnquiryController();
    private final Scanner scanner = new Scanner(System.in);

    public void OfficerStart(HDBOfficer officer) {
        while (true) {
            System.out.println("\n========= HDB OFFICER VIEW =========");
            System.out.println("1. View All Projects");
            System.out.println("2. Apply for BTO Project");
            System.out.println("3. Withdraw Application");
            System.out.println("4. View My Applications");
            System.out.println("5. Register to Handle a Project");
            System.out.println("6. View My Registration Status");
            System.out.println("7. View & Respond to Enquiries");
            System.out.println("8. Generate Flat Booking Receipts");
            System.out.println("9. Exit");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> viewAllProjects(officer);
                case 2 -> applyForProject(officer);
                case 3 -> withdrawApplication(officer);
                case 4 -> viewApplications(officer);
                case 5 -> registerAsProjectOfficer(officer);
                case 6 -> viewOfficerRegistrationStatus(officer);
                case 7 -> viewAndRespondToEnquiries(officer);
                case 8 -> generateReceipt(officer);
                case 9 -> {
                    System.out.println("Exiting HDB Officer View.");
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    public void applyForProject(Users user) {
        if (user instanceof HDBOfficer officer) {
            List<Project> handled = projectController.getProjectsByOfficerUserId(officer.getUserID());

            List<Project> availableProjects = projectController.displayProjectsByCriteria(new Applicant(officer.getUserID(), officer.getName(), officer.getAge(), officer.getmaritalstatus()));
            if (availableProjects.isEmpty()) {
                System.out.println("No eligible projects available.");
                return;
            }

            System.out.println("\n==== Available Projects ====");
            int index = 1;
            for (Project project : availableProjects) {
                System.out.println(index++ + ". " + project.getProjectName());
            }

            System.out.print("Enter your project choice (1-" + availableProjects.size() + "): ");
            int pchoice = scanner.nextInt();
            scanner.nextLine();

            if (pchoice < 1 || pchoice > availableProjects.size()) {
                System.out.println("❌ Invalid project choice.");
                return;
            }

            Project selectedProject = availableProjects.get(pchoice - 1);

            boolean isOfficerForProject = handled.stream()
                .anyMatch(p -> p.getProjectName().equalsIgnoreCase(selectedProject.getProjectName()));
            if (isOfficerForProject) {
                System.out.println("❌ You cannot apply for a project you are handling as an officer.");
                return;
            }

            displayProjectDetails(new Applicant(officer.getUserID(), officer.getName(), officer.getAge(), officer.getmaritalstatus()), selectedProject);
        }
    }

    private void registerAsProjectOfficer(HDBOfficer officer) {
        List<Project> allProjects = projectController.displayAllProjects();

        System.out.println("\nAvailable Projects for Registration:");
        int index = 1;
        for (Project project : allProjects) {
            System.out.println(index++ + ". " + project.getProjectName());
        }

        System.out.print("Select a project to register for: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice < 1 || choice > allProjects.size()) {
            System.out.println("❌ Invalid selection.");
            return;
        }

        Project selected = allProjects.get(choice - 1);
        String projectName = selected.getProjectName();

        List<Application> officerApplications = applicationController.getApplicationsByUserId(officer.getUserID());
        boolean hasApplied = officerApplications.stream()
                .anyMatch(app -> app.getSelectedProject().getProjectName().equalsIgnoreCase(projectName));

        if (hasApplied) {
            System.out.println("❌ You cannot register as an officer for a project you have applied for.");
            return;
        }

        officerRegController.registerOfficer(officer.getUserID(), projectName);
    }

    private void viewOfficerRegistrationStatus(HDBOfficer officer) {
        List<OfficerRegistration> myRegs = officerRegController.getRegistrationsByOfficer(officer.getUserID());

        if (myRegs.isEmpty()) {
            System.out.println("You have no registration records.");
            return;
        }

        System.out.println("\n==== Your Registration Requests ====");
        int index = 1;
        for (OfficerRegistration reg : myRegs) {
            System.out.println(index++ + ". Project: " + reg.getProjectName() +
                               ", Status: " + reg.getStatus());
        }
    }

    private void viewAndRespondToEnquiries(HDBOfficer officer) {
        List<Project> handledProjects = projectController.getProjectsByOfficerUserId(officer.getUserID());
        Set<String> handledProjectNames = handledProjects.stream()
                                                         .map(Project::getProjectName)
                                                         .collect(Collectors.toSet());

        List<Enquiry> relevantEnquiries = enquiryController.getAllEnquiries().stream()
            .filter(e -> handledProjectNames.contains(e.getProjectName()))
            .collect(Collectors.toList());

        if (relevantEnquiries.isEmpty()) {
            System.out.println("No enquiries found for your projects.");
            return;
        }

        int index = 1;
        for (Enquiry e : relevantEnquiries) {
            System.out.println(index++ + ". [" + e.getEnquiryId() + "] Project: " + e.getProjectName());
            System.out.println("    Message: " + e.getMessage());
            System.out.println("    Response: " + (e.getResponse().isEmpty() ? "(No response yet)" : e.getResponse()));
            System.out.println();
        }

        System.out.print("Enter enquiry number to respond to (0 to cancel): ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice < 1 || choice > relevantEnquiries.size()) return;

        Enquiry selected = relevantEnquiries.get(choice - 1);
        System.out.print("Enter your response: ");
        String reply = scanner.nextLine().trim();
        enquiryController.addResponseToEnquiry(selected.getEnquiryId(), reply);

        System.out.println("✅ Response saved.");
    }

    private void generateReceipt(HDBOfficer officer) {
        applicationController.generateFlatBookingReceipt(officer);
    }
}
