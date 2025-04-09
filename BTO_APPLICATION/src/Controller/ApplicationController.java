package Controller;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import Models.Application;
import Models.ApplicationStatus;
import Models.*;

public class ApplicationController {
    private static final String APPLICATION_FILE = "src/Resources/CSV/CSV/Applications.csv";
    private Map<String, Application> applicationMap = new HashMap<>();

    public ApplicationController() {
        loadApplicationsFromCSV();
    }

    private void loadApplicationsFromCSV() {
        Path filePath = Paths.get(APPLICATION_FILE);

        if (Files.exists(filePath)) {
            try (BufferedReader reader = Files.newBufferedReader(filePath)) {
                String line;
                reader.readLine();  // Skip header

                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",", -1); // handle missing values
                    if (fields.length == 12) {
                        try {
                            Application application = new Application(
                                fields[0],                             // Application ID
                                fields[1],                             // Applicant Name
                                fields[2],                             // User ID
                                Integer.parseInt(fields[3]),           // Age
                                fields[4],                             // Marital Status
                                fields[5],                             // Role
                                fields[6],                             // Project Name
                                fields[7],                             // Flat Type
                                Double.parseDouble(fields[8]),         // Selling Price
                                Integer.parseInt(fields[9]),           // No. of Units
                                fields[10],                            // Booked Date
                                ApplicationStatus.valueOf(fields[11].toUpperCase())
                            );

                            applicationMap.put(application.getApplicationId(), application);
                        } catch (Exception e) {
                            System.err.println("⚠️ Error parsing line: " + line);
                            e.printStackTrace();
                        }
                    } else {
                        System.err.println("⚠️ Skipped malformed line (wrong number of fields): " + line);
                    }
                }
                System.out.println("✅ Applications loaded from CSV file successfully.");
            } catch (IOException e) {
                System.err.println("❌ Error reading from CSV file: " + e.getMessage());
            }
        } else {
            System.out.println("📂 CSV file does not exist. Starting with an empty set of applications.");
        }
    }

    public void addApplicationToHashMap(Application application) {
        String userId = application.getApplicant().getUserID();
        String projectName = application.getSelectedProject().getProjectName();
        List<Application> existingApps = getApplicationsByUserId(userId);

        // Check application period
        if (application.getBookedDate().isBefore(application.getSelectedProject().getOpeningDate()) ||
            application.getBookedDate().isAfter(application.getSelectedProject().getClosingDate())) {
            System.out.println("❌ Application date is outside the project's application period.");
            return;
        }

        // Check for previous application to the same project
        for (Application existing : existingApps) {
            if (existing.getSelectedProject().getProjectName().equalsIgnoreCase(projectName)) {
                if (existing.getStatus() != ApplicationStatus.UNSUCCESSFUL) {
                    System.out.println("❌ You have already applied to this project with status: " + existing.getStatus());
                    return;
                }
            }
        }

        // All checks passed — add and write to CSV
        applicationMap.put(application.getApplicationId(), application);
        writeApplicationsToCSV();
        System.out.println("✅ Application submitted successfully.");
    }

    private void writeApplicationsToCSV() {
        Path filePath = Paths.get(APPLICATION_FILE);

        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            writer.write("ApplicationId,ApplicantName,UserID,Age,MaritalStatus,Role,ProjectName,FlatType,SellingPrice,NoOfUnits,BookedDate,Status");
            writer.newLine();

            for (Application app : applicationMap.values()) {
                writer.write(app.toCSV());
                writer.newLine();
            }

            System.out.println("✅ Application written to CSV successfully.");
        } catch (IOException e) {
            System.err.println("❌ Error writing to CSV: " + e.getMessage());
        }
    }

    public List<Application> getApplicationsByUserId(String userId) {
        List<Application> apps = new ArrayList<>();
        for (Application app : applicationMap.values()) {
            if (app.getApplicant().getUserID().equalsIgnoreCase(userId)) {
                apps.add(app);
            }
        }
        return apps;
    }

    public void displayAllApplications() {
        if (applicationMap.isEmpty()) {
            System.out.println("❗ No applications found.");
            return;
        }

        for (Application app : applicationMap.values()) {
            System.out.println("==================================");
            System.out.println("Application ID    : " + app.getApplicationId());
            System.out.println("Applicant Name    : " + app.getApplicant().getName());
            System.out.println("NRIC              : " + app.getApplicant().getUserID());
            System.out.println("Age               : " + app.getApplicant().getAge());
            System.out.println("Marital Status    : " + app.getApplicant().getmaritalstatus());
            System.out.println("Flat Type         : " + app.getSelectedFlat().getTypeName());
            System.out.println("Project Name      : " + app.getSelectedProject().getProjectName());
            System.out.println("Booking Date      : " + app.getFormattedBookedDate());
            System.out.println("Application Status: " + app.getStatus());
            System.out.println("==================================\n");
        }
    }
    
    public List<Application> getApplicationsByApplicant(Applicant applicant) {
        return getApplicationsByUserId(applicant.getUserID());
    }
    
    public void updateApplicationStatus(Application updatedApp) {
        applicationMap.put(updatedApp.getApplicationId(), updatedApp);
        writeApplicationsToCSV(); // persist changes
    }
    
    public void deleteApplication(Application appToDelete) {
        if (applicationMap.containsKey(appToDelete.getApplicationId())) {
            applicationMap.remove(appToDelete.getApplicationId());
            writeApplicationsToCSV();  // Persist change to CSV
            System.out.println("✅ Application deleted successfully.");
        } else {
            System.out.println("❌ Application not found.");
        }
    }
    
    
    
    public void generateFlatBookingReceipt(HDBOfficer officer) {
        List<String> officerProjects = new ProjectController()
            .getProjectsByOfficerUserId(officer.getUserID())
            .stream()
            .map(Project::getProjectName)
            .toList();

        boolean found = false;

        for (Application app : applicationMap.values()) {
            if (officerProjects.contains(app.getSelectedProject().getProjectName())
                    && app.getStatus() == ApplicationStatus.BOOKED) {

                System.out.println("\n======= Booking Receipt =======");
                System.out.println("Applicant Name   : " + app.getApplicant().getName());
                System.out.println("NRIC             : " + app.getApplicant().getUserID());
                System.out.println("Age              : " + app.getApplicant().getAge());
                System.out.println("Marital Status   : " + app.getApplicant().getmaritalstatus());
                System.out.println("Flat Type Booked : " + app.getSelectedFlat().getTypeName());
                System.out.println("Project Name     : " + app.getSelectedProject().getProjectName());
                System.out.println("Booking Date     : " + app.getFormattedBookedDate());
                System.out.println("================================\n");

                found = true;
            }
        }

        if (!found) {
            System.out.println("❗ No booked applications found for your handled projects.");
        }
    }

 


}
