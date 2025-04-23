package main.java.sg.gov.hdb.bto.util;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import main.java.sg.gov.hdb.bto.entity.user.User;

/**
 * Utility class for audit logging
 */
public class AuditLogger {
    private static final String LOG_FILE = "resources/audit_log.csv";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Log an action in the system
     * @param user The user performing the action
     * @param action The action being performed
     * @param details Additional details about the action
     */
    public static void logAction(User user, String action, String details) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String userName = user != null ? user.getName() : "System";
        String userNric = user != null ? user.getNric() : "N/A";
        String userRole = user != null ? user.getRole() : "N/A";
        
        String logEntry = String.format("%s,%s,%s,%s,%s,%s",
                timestamp, userName, userNric, userRole, action, details);
        
        try {
            // Create log file if it doesn't exist
            File logFile = new File(LOG_FILE);
            if (!logFile.exists()) {
                File parentDir = logFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
                
                // Create file with header
                try (FileWriter writer = new FileWriter(logFile)) {
                    writer.write("Timestamp,User Name,NRIC,Role,Action,Details\n");
                }
            }
            
            // Append log entry
            try (FileWriter writer = new FileWriter(LOG_FILE, true)) {
                writer.write(logEntry + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error writing to audit log: " + e.getMessage());
        }
    }
    
    /**
     * Retrieve log entries
     * @return List of log entries as arrays
     */
    public static List<String[]> getLogEntries() {
        List<String[]> entries = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(LOG_FILE))) {
            String line;
            boolean skipHeader = true;
            
            while ((line = reader.readLine()) != null) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }
                
                entries.add(line.split(","));
            }
        } catch (IOException e) {
            System.err.println("Error reading audit log: " + e.getMessage());
        }
        
        return entries;
    }
    
    /**
     * Get filtered log entries
     * @param userNric Filter by user NRIC
     * @param action Filter by action type
     * @param fromTimestamp Filter by start timestamp
     * @param toTimestamp Filter by end timestamp
     * @return Filtered list of log entries
     */
    public static List<String[]> getFilteredLogEntries(String userNric, String action, 
                                                      LocalDateTime fromTimestamp, LocalDateTime toTimestamp) {
        List<String[]> allEntries = getLogEntries();
        List<String[]> filteredEntries = new ArrayList<>();
        
        for (String[] entry : allEntries) {
            if (entry.length < 6) continue;
            
            boolean matches = true;
            
            if (userNric != null && !userNric.isEmpty() && !entry[2].equals(userNric)) {
                matches = false;
            }
            
            if (action != null && !action.isEmpty() && !entry[4].equals(action)) {
                matches = false;
            }
            
            if (fromTimestamp != null || toTimestamp != null) {
                LocalDateTime entryTimestamp = LocalDateTime.parse(entry[0], TIMESTAMP_FORMAT);
                
                if (fromTimestamp != null && entryTimestamp.isBefore(fromTimestamp)) {
                    matches = false;
                }
                
                if (toTimestamp != null && entryTimestamp.isAfter(toTimestamp)) {
                    matches = false;
                }
            }
            
            if (matches) {
                filteredEntries.add(entry);
            }
        }
        
        return filteredEntries;
    }
    
    /**
     * Export log entries to a CSV file
     * @param filePath Path to export the log
     * @param entries Log entries to export
     * @throws IOException If there's an error writing to the file
     */
    public static void exportLogToCSV(String filePath, List<String[]> entries) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            // Write header
            writer.write("Timestamp,User Name,NRIC,Role,Action,Details\n");
            
            // Write entries
            for (String[] entry : entries) {
                StringBuilder line = new StringBuilder();
                for (int i = 0; i < entry.length; i++) {
                    // Add quotes if the value contains commas
                    if (entry[i] != null && entry[i].contains(",")) {
                        line.append("\"").append(entry[i]).append("\"");
                    } else {
                        line.append(entry[i] != null ? entry[i] : "");
                    }
                    
                    if (i < entry.length - 1) {
                        line.append(",");
                    }
                }
                writer.write(line.toString() + "\n");
            }
        }
    }
}