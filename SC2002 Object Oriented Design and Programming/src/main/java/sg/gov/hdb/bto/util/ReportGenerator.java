package main.java.sg.gov.hdb.bto.util;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import main.java.sg.gov.hdb.bto.entity.application.Application;
import main.java.sg.gov.hdb.bto.entity.application.FlatBooking;
import main.java.sg.gov.hdb.bto.entity.user.User;
import main.java.sg.gov.hdb.bto.entity.project.BTOProject;

public class ReportGenerator {
    
    public static String generateBookingReceipt(FlatBooking booking) {
        User applicant = booking.getApplicant();
        BTOProject project = booking.getProject();
        String flatType = booking.getFlatType();
        LocalDate bookingDate = booking.getBookingDate();
        
        // Format the receipt
        StringBuilder receipt = new StringBuilder();
        receipt.append("======================================\n");
        receipt.append("            BOOKING RECEIPT           \n");
        receipt.append("======================================\n");
        receipt.append("Booking ID: ").append(booking.getBookingId()).append("\n");
        receipt.append("Date: ").append(bookingDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");
        receipt.append("--------------------------------------\n");
        receipt.append("APPLICANT INFORMATION\n");
        receipt.append("Name: ").append(applicant.getName()).append("\n");
        receipt.append("NRIC: ").append(applicant.getNric()).append("\n");
        receipt.append("Marital Status: ").append(applicant.getMaritalStatus()).append("\n");
        receipt.append("--------------------------------------\n");
        receipt.append("PROJECT INFORMATION\n");
        receipt.append("Project Name: ").append(project.getName()).append("\n");
        receipt.append("Location: ").append(project.getNeighborhood()).append("\n");
        receipt.append("--------------------------------------\n");
        receipt.append("FLAT INFORMATION\n");
        receipt.append("Flat Type: ").append(flatType).append("\n");
        receipt.append("Price: $").append(project.getPrice(flatType)).append("\n");
        receipt.append("--------------------------------------\n");
        receipt.append("This receipt serves as proof of booking.\n");
        receipt.append("Please retain for your records.\n");
        receipt.append("======================================\n");
        
        return receipt.toString();
    }
    
    public static String generateFilteredReport(List<Application> applications, Map<String, Object> filters) {
        // Filter applications based on criteria
        List<Application> filteredApps = new ArrayList<>(applications);
        
        for (Map.Entry<String, Object> filter : filters.entrySet()) {
            String key = filter.getKey();
            Object value = filter.getValue();
            
            if ("maritalStatus".equals(key) && value instanceof String) {
                filteredApps.removeIf(app -> !app.getApplicant().getMaritalStatus().equals(value));
            } else if ("flatType".equals(key) && value instanceof String) {
                filteredApps.removeIf(app -> !app.getFlatType().equals(value));
            } else if ("status".equals(key) && value instanceof String) {
                filteredApps.removeIf(app -> !app.getStatus().equals(value));
            } else if ("project".equals(key) && value instanceof BTOProject) {
                filteredApps.removeIf(app -> !app.getProject().equals(value));
            } else if ("ageAbove".equals(key) && value instanceof Integer) {
                filteredApps.removeIf(app -> app.getApplicant().getAge() <= (Integer)value);
            } else if ("ageBelow".equals(key) && value instanceof Integer) {
                filteredApps.removeIf(app -> app.getApplicant().getAge() >= (Integer)value);
            }
            // Add more filter options as needed
        }
        
        // Generate the report
        StringBuilder report = new StringBuilder();
        report.append("======================================\n");
        report.append("         FILTERED APPLICATIONS        \n");
        report.append("======================================\n");
        report.append("Generated on: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");
        report.append("Filter criteria: ").append(filters.toString()).append("\n");
        report.append("Number of matching applications: ").append(filteredApps.size()).append("\n");
        report.append("--------------------------------------\n");
        
        for (Application app : filteredApps) {
            report.append("Applicant: ").append(app.getApplicant().getName()).append("\n");
            report.append("NRIC: ").append(app.getApplicant().getNric()).append("\n");
            report.append("Age: ").append(app.getApplicant().getAge()).append("\n");
            report.append("Marital Status: ").append(app.getApplicant().getMaritalStatus()).append("\n");
            report.append("Project: ").append(app.getProject().getName()).append("\n");
            report.append("Flat Type: ").append(app.getFlatType()).append("\n");
            report.append("Status: ").append(app.getStatus()).append("\n");
            report.append("Application Date: ").append(app.getApplicationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");
            report.append("--------------------------------------\n");
        }
        
        return report.toString();
    }
    
    public static void exportToCSV(String filePath, String content) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(content);
        }
    }
}
