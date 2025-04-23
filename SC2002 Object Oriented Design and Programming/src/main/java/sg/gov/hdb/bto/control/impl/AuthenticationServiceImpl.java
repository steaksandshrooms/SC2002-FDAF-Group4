package main.java.sg.gov.hdb.bto.control.impl;

import java.util.*;
import main.java.sg.gov.hdb.bto.control.interfaces.*;
import main.java.sg.gov.hdb.bto.entity.user.*;
import main.java.sg.gov.hdb.bto.util.CSVHandler;
import main.java.sg.gov.hdb.bto.util.PasswordManager;

public class AuthenticationServiceImpl implements AuthenticationService {
    private Map<String, User> users = new HashMap<>();
    private List<Applicant> applicants = new ArrayList<>();
    private List<HDBOfficer> officers = new ArrayList<>();
    private List<HDBManager> managers = new ArrayList<>();
    private CSVHandler csvHandler;
    
    public AuthenticationServiceImpl(CSVHandler csvHandler) {
        this.csvHandler = csvHandler;
        loadUsers();
    }
    
    private void loadUsers() {
        List<String[]> applicantData = csvHandler.readCSVSkipHeader("ApplicantList.csv");
        System.out.println("Loaded " + applicantData.size() + " applicant records from CSV");
        for (String[] row : applicantData) {
            if (row.length < 5) {
                System.out.println("Incomplete applicant row: " + Arrays.toString(row));
                continue;
            }
            try {
                String name = row[0];
                String nric = row[1];
                int age = Integer.parseInt(row[2]);
                String maritalStatus = row[3];
                String hashedPassword = row[4];
                
                Applicant applicant = new Applicant(name, nric, age, maritalStatus, hashedPassword);
                applicants.add(applicant);
                users.put(nric, applicant);
                System.out.println("Loaded applicant: " + name + " (" + nric + ")");
            } catch (NumberFormatException e) {
                System.out.println("Error parsing applicant data: " + e.getMessage() + " for row: " + Arrays.toString(row));
            }
        }
        
        // Load officers
        List<String[]> officerData = csvHandler.readCSVSkipHeader("OfficerList.csv");
        System.out.println("Loaded " + officerData.size() + " officer records from CSV");
        for (String[] row : officerData) {
            if (row.length < 5) {
                System.out.println("Incomplete officer row: " + Arrays.toString(row));
                continue;
            }
            try {
                String name = row[0];
                String nric = row[1];
                int age = Integer.parseInt(row[2]);
                String maritalStatus = row[3];
                String hashedPassword = row[4];
                
                HDBOfficer officer = new HDBOfficer(name, nric, age, maritalStatus, hashedPassword);
                officers.add(officer);
                users.put(nric, officer);
                System.out.println("Loaded officer: " + name + " (" + nric + ")");
            } catch (NumberFormatException e) {
                System.out.println("Error parsing officer data: " + e.getMessage() + " for row: " + Arrays.toString(row));
            }
        }
        
        // Load managers
        List<String[]> managerData = csvHandler.readCSVSkipHeader("ManagerList.csv");
        System.out.println("Loaded " + managerData.size() + " manager records from CSV");
        for (String[] row : managerData) {
            if (row.length < 5) {
                System.out.println("Incomplete manager row: " + Arrays.toString(row));
                continue;
            }
            try {
                String name = row[0];
                String nric = row[1];
                int age = Integer.parseInt(row[2]);
                String maritalStatus = row[3];
                String hashedPassword = row[4];
                
                HDBManager manager = new HDBManager(name, nric, age, maritalStatus, hashedPassword);
                managers.add(manager);
                users.put(nric, manager);
                System.out.println("Loaded manager: " + name + " (" + nric + ")");
            } catch (NumberFormatException e) {
                System.out.println("Error parsing manager data: " + e.getMessage() + " for row: " + Arrays.toString(row));
            }
        }
        
        // Add default manager if none exists or Jessica is not found
        boolean jessicaFound = false;
        for (HDBManager manager : managers) {
            if ("Jessica".equals(manager.getName())) {
                jessicaFound = true;
                break;
            }
        }
        
        if (!jessicaFound) {
            System.out.println("Default manager 'Jessica' not found, creating...");
            HDBManager defaultManager = new HDBManager("Jessica", "S5678901G", 26, "Married", PasswordManager.hashPassword("password"));
            managers.add(defaultManager);
            users.put(defaultManager.getNric(), defaultManager);
        }
        
        System.out.println("Loaded managers:");
        for (HDBManager manager : managers) {
            System.out.println(manager.getName() + " - " + manager.getNric());
        }
    }
    
    @Override
    public User login(String nric, String password) {
        if (!validateNRIC(nric)) {
            System.out.println("Invalid NRIC format: " + nric);
            return null;
        }

        User user = users.get(nric);
        if (user == null) {
            System.out.println("User not found with NRIC: " + nric);
            return null;
        }
        
        // Try both direct comparison and hashed comparison
        if (user.authenticate(password) || user.getPassword().equals(password)) {
            return user;
        } else {
            System.out.println("Password authentication failed for user: " + nric);
            System.out.println("Provided password: " + password);
            return null;
        }
    }
    
    @Override
    public void registerUser(User user) {
        users.put(user.getNric(), user);
        
        if (user instanceof Applicant) {
            applicants.add((Applicant) user);
        } else if (user instanceof HDBOfficer) {
            officers.add((HDBOfficer) user);
        } else if (user instanceof HDBManager) {
            managers.add((HDBManager) user);
        }
    }
    
    @Override
    public User getUser(String nric) {
        return users.get(nric);
    }
    
    @Override
    public List<Applicant> getAllApplicants() {
        return new ArrayList<>(applicants);
    }
    
    @Override
    public List<HDBOfficer> getAllOfficers() {
        return new ArrayList<>(officers);
    }
    
    @Override
    public List<HDBManager> getAllManagers() {
        return new ArrayList<>(managers);
    }
    
    private boolean validateNRIC(String nric) {
        return nric != null && nric.matches("[ST]\\d{7}[A-Z]");
    }
}