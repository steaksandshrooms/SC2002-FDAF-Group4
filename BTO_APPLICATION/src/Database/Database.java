package Database;
import Models.*;
import java.io.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;

public class Database {

    // Date format
    static SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    // USERS DATABASE USING HASHMAP
    public static HashMap<String, Users> UserDB = new HashMap<>();

    // PROJECTS DATABASE
    public static HashMap<String, Project> ProjectDB = new HashMap<>();

    // Manage, Officer and Applicant Maps
    public static HashMap<String, HDBManager> ManagerDB = new HashMap<>();
    public static HashMap<String, HDBOfficer> OfficerDB = new HashMap<>();
    public static HashMap<String, Applicant> ApplicantDB = new HashMap<>();

    // Method to load data from CSV files
    public static void loadData() {
        loadManagersFromCSV("src/Resources/CSV/ManagerList.csv");
        loadOfficersFromCSV("src/Resources/CSV/OfficerList.csv");
        loadProjectsFromCSV("src/Resources/CSV/ProjectList.csv");
        loadApplicantsFromCSV("src/Resources/CSV/ApplicantList.csv");
    }

    // Method to load Managers from CSV
    private static void loadManagersFromCSV(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            br.readLine();  // Skip header line
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");

                String managerName = fields[0];
                String userid = fields[1];
                int age = Integer.parseInt(fields[2]);
                MaritalStatus maritalStatus = MaritalStatus.valueOf(fields[3].toUpperCase());
                Role r = Role.HDBManager;

                HDBManager manager = new HDBManager(userid, managerName, age, maritalStatus);
                ManagerDB.put(userid, manager);
                Users managerUser = new Users(userid, managerName, age, maritalStatus, r); // Add to UserDB
                UserDB.put(userid, managerUser);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to load Officers from CSV
    private static void loadOfficersFromCSV(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            br.readLine();  // Skip header line
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");

                String officerName = fields[0];
                String userid = fields[1];
                int age = Integer.parseInt(fields[2]);
                MaritalStatus maritalStatus = MaritalStatus.valueOf(fields[3].toUpperCase());
                Role r = Role.HDBOfficer;

                HDBOfficer officer = new HDBOfficer(userid, officerName, age, maritalStatus);
                OfficerDB.put(userid, officer);
                Users officerUser = new Users(userid, officerName, age, maritalStatus, r); // Add to UserDB
                UserDB.put(userid, officerUser);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to load Applicants from CSV
    private static void loadApplicantsFromCSV(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            br.readLine();  // Skip header line
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");

                String applicantName = fields[0];
                String userid = fields[1];
                int age = Integer.parseInt(fields[2]);
                MaritalStatus maritalStatus = MaritalStatus.valueOf(fields[3].toUpperCase());
                Role r = Role.Applicant;

                Applicant applicant = new Applicant(userid, applicantName, age, maritalStatus);
                ApplicantDB.put(userid, applicant);
                Users applicantUser = new Users(userid, applicantName, age, maritalStatus, r); // Add to UserDB
                UserDB.put(userid, applicantUser);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to load Projects from CSV
    public static void loadProjectsFromCSV(String filePath) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/M/yyyy");

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                // Split using regex that ignores commas inside quotes
                String[] projectDetails = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                if (projectDetails.length < 14) {
                    System.out.println("Skipping malformed line: " + line);
                    continue;
                }

                String projectName = projectDetails[0].trim();
                String neighbourhood = projectDetails[1].trim();
                String type1 = projectDetails[2].trim();
                int unitsType1 = Integer.parseInt(projectDetails[3].trim());
                float priceType1 = Float.parseFloat(projectDetails[4].trim());
                String type2 = projectDetails[5].trim();
                int unitsType2 = Integer.parseInt(projectDetails[6].trim());
                float priceType2 = Float.parseFloat(projectDetails[7].trim());
                LocalDate openingDate = LocalDate.parse(projectDetails[8].trim(), format);
                LocalDate closingDate = LocalDate.parse(projectDetails[9].trim(), format);
                String managerName = projectDetails[10].trim();
                int officerSlots = Integer.parseInt(projectDetails[11].trim());

                // Remove surrounding quotes and split officer names
                String[] officers = projectDetails[12].replace("\"", "").split(",");

                boolean visibility = Boolean.parseBoolean(projectDetails[13].trim());

                // Find manager
                HDBManager manager = null;
                for (HDBManager mgr : ManagerDB.values()) {
                    if (mgr.getName().equals(managerName)) {
                        manager = mgr;
                        break;
                    }
                }

                if (manager == null) {
                    System.out.println("Manager not found for project: " + projectName);
                    continue;
                }

                // Create new project
                Project project = new Project(projectName, neighbourhood, openingDate, closingDate, visibility, officerSlots, manager);

                // Add flats
                FlatInfo flat1 = new FlatInfo(1, type1, priceType1, unitsType1);
                FlatInfo flat2 = new FlatInfo(2, type2, priceType2, unitsType2);
                project.addFlat(flat1);
                project.addFlat(flat2);

                // Add officers
                for (String officerName : officers) {
                    officerName = officerName.trim();

                    HDBOfficer officer = null;
                    for (HDBOfficer ofc : OfficerDB.values()) {
                        if (ofc.getName().equals(officerName)) {
                            officer = ofc;
                            break;
                        }
                    }

                    if (officer != null) {
                        project.addOfficer(officer);
                    } else {
                        System.out.println("Officer not found: " + officerName);
                    }
                }

                // Add to project database
                ProjectDB.put(projectName, project);
            }

            System.out.println("\nProjects loaded successfully.");
        } catch (IOException e) {
            System.out.println("Error reading CSV file: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Error in number format: " + e.getMessage());
        }
    }
}
