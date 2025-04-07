package Models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Project {

    private String projectName;
    private String neighbourhood;
    private LocalDate openingDate;
    private LocalDate closingDate;
    private boolean visible;
    private int officerSlots;
    private List<HDBOfficer> hdbOfficers;
    private HDBManager manager;
    private List<FlatInfo> flats;

    // Constructor
    public Project(String name, String neighbourhood, String openingDate, String closingDate, boolean isVisible, int slots, HDBManager manager) {
        this.projectName = name;
        this.neighbourhood = neighbourhood;
        this.visible = isVisible;
        this.officerSlots = slots;
        this.manager = manager;
        this.hdbOfficers = new ArrayList<>();
        this.flats = new ArrayList<>();

        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        this.openingDate = LocalDate.parse(openingDate, format);
        this.closingDate = LocalDate.parse(closingDate, format);
    }

    // Visibility
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return this.visible;
    }

    // Officers
    public void addOfficer(HDBOfficer officer) {
        if (this.officerSlots > 0) {
            this.hdbOfficers.add(officer);
            System.out.println("HDB Officer successfully added.");
            this.officerSlots--;
        } else {
            System.out.println("Slots filled!");
        }
    }

    public void setOfficerSlots(int slots) {
        this.officerSlots = slots;
    }

    public int getAvailableSlots() {
        return this.officerSlots;
    }

    // Flats
    public void addFlat(FlatInfo flat) {
        this.flats.add(flat);
        System.out.println("Flat added successfully.");
    }

    public int getAvailableUnits(int type) {
        for (FlatInfo flat : this.flats) {
            if (flat.getType() == type) {
                return flat.getNoOfUnits();
            }
        }
        return 0;
    }

    public String getName() {
        return this.projectName;
    }

    // Project Details
    public void getProjectDetails() {
        System.out.println("****************************************");
        System.out.println("*           PROJECT DETAILS            *");
        System.out.println("****************************************");
        System.out.println("Project name: " + this.projectName);
        System.out.println("Neighborhood: " + this.neighbourhood);
        System.out.println("Application opening date: " + this.openingDate);
        System.out.println("Application closing date: " + this.closingDate);
        System.out.println("Manager: " + this.manager.getName());
        System.out.println("Officer Slots Available: " + this.getAvailableSlots());

        System.out.println("Officers:");
        for (HDBOfficer officer : this.hdbOfficers) {
            System.out.println("  - " + officer.getName());
        }

        System.out.println("\nFlat Details:");
        for (FlatInfo flat : this.flats) {
            flat.getinfo();
        }

        System.out.println("****************************************");
    }
}
