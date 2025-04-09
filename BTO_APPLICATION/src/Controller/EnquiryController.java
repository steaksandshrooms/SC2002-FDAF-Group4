package Controller;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import Models.Enquiry;

public class EnquiryController {
    private static final String ENQUIRY_FILE = "src/Resources/CSV/CSV/Enquiry.csv";
    private Map<String, Enquiry> enquiryMap = new HashMap<>();

    public EnquiryController() {
        loadEnquiriesFromCSV();
    }

    private void loadEnquiriesFromCSV() {
        Path filePath = Paths.get(ENQUIRY_FILE);

        if (!Files.exists(filePath)) return;

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            reader.readLine(); // skip header

            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",", 5);
                if (fields.length >= 4) {
                    String enquiryId = fields[0];
                    String userId = fields[1];
                    String projectName = fields[2];
                    String message = fields[3];
                    String response = fields.length == 5 ? fields[4] : "";
                    Enquiry enquiry = new Enquiry(enquiryId, userId, projectName, message);
                    enquiry.setResponse(response);
                    enquiryMap.put(enquiryId, enquiry);
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Failed to load enquiries: " + e.getMessage());
        }
    }

    private void saveEnquiriesToCSV() {
        Path filePath = Paths.get(ENQUIRY_FILE);
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            writer.write("EnquiryId,UserId,ProjectName,Message,Response");
            writer.newLine();

            for (Enquiry enquiry : enquiryMap.values()) {
                writer.write(enquiry.toCSV());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("❌ Failed to write enquiries: " + e.getMessage());
        }
    }

    public void submitEnquiry(Enquiry enquiry) {
        enquiryMap.put(enquiry.getEnquiryId(), enquiry);
        saveEnquiriesToCSV();
    }

    public List<Enquiry> getEnquiriesByUserId(String userId) {
        List<Enquiry> result = new ArrayList<>();
        for (Enquiry e : enquiryMap.values()) {
            if (e.getUserId().equalsIgnoreCase(userId)) {
                result.add(e);
            }
        }
        return result;
    }

    public void updateEnquiryMessage(String enquiryId, String newMessage) {
        if (enquiryMap.containsKey(enquiryId)) {
            enquiryMap.get(enquiryId).setMessage(newMessage);
            saveEnquiriesToCSV();
        }
    }

    public void deleteEnquiry(String enquiryId) {
        if (enquiryMap.containsKey(enquiryId)) {
            enquiryMap.remove(enquiryId);
            saveEnquiriesToCSV();
        }
    }

    public List<Enquiry> getAllEnquiries() {
        return new ArrayList<>(enquiryMap.values());
    }

    public void addResponseToEnquiry(String enquiryId, String response) {
        Enquiry e = enquiryMap.get(enquiryId);
        if (e != null) {
            e.setResponse(response);
            saveEnquiriesToCSV();
        }
    }
}