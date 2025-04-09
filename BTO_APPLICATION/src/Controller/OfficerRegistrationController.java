package Controller;

import Models.OfficerRegistration;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class OfficerRegistrationController {
    private static final String FILE_PATH = "src/Resources/CSV/OfficerRegistrations.csv";
    private List<OfficerRegistration> registrations = new ArrayList<>();

    public OfficerRegistrationController() {
        loadFromCSV();
    }

    private void loadFromCSV() {
        try {
            if (!Files.exists(Paths.get(FILE_PATH))) return;

            List<String> lines = Files.readAllLines(Paths.get(FILE_PATH));
            lines.remove(0); // Skip header

            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    registrations.add(new OfficerRegistration(parts[0], parts[1], parts[2], parts[3]));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading OfficerRegistrations.csv: " + e.getMessage());
        }
    }

    private void saveToCSV() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(FILE_PATH))) {
            writer.write("RegistrationId,OfficerID,ProjectName,Status");
            writer.newLine();
            for (OfficerRegistration reg : registrations) {
                writer.write(reg.toCSV());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing OfficerRegistrations.csv: " + e.getMessage());
        }
    }

    public boolean isAlreadyRegistered(String officerId, String projectName) {
        return registrations.stream()
                .anyMatch(r -> r.getOfficerId().equals(officerId)
                        && r.getProjectName().equalsIgnoreCase(projectName));
    }

    public void registerOfficer(String officerId, String projectName) {
        if (isAlreadyRegistered(officerId, projectName)) {
            System.out.println("❌ You have already registered for this project.");
            return;
        }

        String regId = UUID.randomUUID().toString().substring(0, 8);
        OfficerRegistration newReg = new OfficerRegistration(regId, officerId, projectName, "PENDING");
        registrations.add(newReg);
        saveToCSV();
        System.out.println("✅ Registration submitted. Status: PENDING");
    }

    public List<OfficerRegistration> getRegistrationsByOfficer(String officerId) {
        List<OfficerRegistration> result = new ArrayList<>();
        for (OfficerRegistration r : registrations) {
            if (r.getOfficerId().equals(officerId)) {
                result.add(r);
            }
        }
        return result;
    }

    public void updateRegistrationStatus(String registrationId, String newStatus) {
        for (OfficerRegistration r : registrations) {
            if (r.getRegistrationId().equals(registrationId)) {
                r.setStatus(newStatus);
                break;
            }
        }
        saveToCSV();
    }
    
  
    
    
}
