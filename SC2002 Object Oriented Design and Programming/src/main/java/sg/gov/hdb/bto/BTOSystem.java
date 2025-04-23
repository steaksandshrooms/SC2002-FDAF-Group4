package main.java.sg.gov.hdb.bto;

import main.java.sg.gov.hdb.bto.boundary.BTOConsole;
import main.java.sg.gov.hdb.bto.control.impl.*;
import main.java.sg.gov.hdb.bto.control.interfaces.*;
import main.java.sg.gov.hdb.bto.util.CSVHandler;

public class BTOSystem {
    public static void main(String[] args) {
        // Check if resources directory exists
        checkResourcesDirectory();

        // Initialize CSV reader
        CSVHandler csvReader = new CSVHandler();

        // Initialize services
        AuthenticationService authService = new AuthenticationServiceImpl(csvReader);
        ProjectManager projectManager = new ProjectManagerImpl(csvReader, authService);
        ApplicationService applicationService = new ApplicationServiceImpl(csvReader, authService, projectManager);
        EnquiryService enquiryService = new EnquiryServiceImpl(csvReader, authService, projectManager);
        BookingService bookingService = new BookingServiceImpl(csvReader);
        DataService dataService = new DataServiceImpl(authService, projectManager, applicationService, 
                                                     enquiryService, bookingService);

        // Start the console UI
        BTOConsole console = new BTOConsole(
            authService, 
            projectManager, 
            applicationService, 
            enquiryService,
            bookingService,
            dataService
        );

        console.start();
    }

    private static void checkResourcesDirectory() {
        java.io.File resourcesDir = new java.io.File("resources");
        if (!resourcesDir.exists()) {
            System.out.println("Resources directory not found. Please create a 'resources' directory with the required CSV files.");
            System.exit(1);
        }

        // Check for required CSV files
        String[] requiredFiles = {
            "ApplicantList.csv", 
            "Applications.csv", 
            "Enquiry.csv", 
            "ManagerList.csv", 
            "OfficerList.csv", 
            "OfficerRegistrations.csv", 
            "ProjectList.csv",
            "Bookings.csv" // <-- Ensure Bookings.csv is listed
        };

        boolean missingFiles = false;
        for (String file : requiredFiles) {
            java.io.File csvFile = new java.io.File("resources/" + file);
            if (!csvFile.exists()) {
                System.out.println("Missing required file: " + file);
                missingFiles = true;
            }
        }

        if (missingFiles) {
            System.out.println("Please ensure all required CSV files are in the resources directory.");
            System.exit(1);
        }
    }
}