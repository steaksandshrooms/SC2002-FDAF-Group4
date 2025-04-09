package Models;

import java.time.LocalDate;
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
    public Project(String projectName, String neighbourhood, LocalDate openingDate, LocalDate closingDate, boolean isVisible, int officerSlots, HDBManager manager) {
        this.projectName = projectName;
        this.neighbourhood = neighbourhood;
        this.visible = isVisible;
        this.officerSlots = officerSlots;
        this.manager = manager;
        this.hdbOfficers = new ArrayList<>();
        this.flats = new ArrayList<>();
        this.openingDate = openingDate;
        this.closingDate = closingDate;
    }

    // Getters
    public String getProjectName() {
        return this.projectName;
    }

    public String getNeighbourhood() {
        return this.neighbourhood;
    }

    public LocalDate getOpeningDate() {
        return this.openingDate;
    }

    public LocalDate getClosingDate() {
        return this.closingDate;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public int getOfficerSlots() {
        return this.officerSlots;
    }

    public HDBManager getManager() {
        return this.manager;
    }

    public List<FlatInfo> getFlats() {
        return this.flats;
    }

    // Setters
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setOfficerSlots(int slots) {
        this.officerSlots = slots;
    }

    // Officers Management
    public void addOfficer(HDBOfficer officer) {
        if (this.officerSlots > 0) {
            this.hdbOfficers.add(officer);
            this.officerSlots--;
        } else {
            System.out.println("Officer slots are full!");
        }
    }

    public List<HDBOfficer> getOfficers() {
        return new ArrayList<>(this.hdbOfficers);
    }

    public int getAvailableSlots() {
        return this.officerSlots;
    }

    // Flats Management
    public void addFlat(FlatInfo flat) {
        this.flats.add(flat);
    }

    public void getFlatType(int type) {
        for (FlatInfo flat : this.flats) {
            if (flat.getType() == type) {
                flat.getinfo(); // Assuming this method prints or returns details about the flat.
            }
        }
    }

    public int getAvailableUnits(int type) {
        for (FlatInfo flat : this.flats) {
            if (flat.getType() == type) {
                return flat.getNoOfUnits();
            }
        }
        return 0;
    }

    // Utility Method to Display Project Details
    public void displayProjectDetails() {
        System.out.println("Project Name: " + getProjectName());
        System.out.println("Neighbourhood: " + getNeighbourhood());
        System.out.println("Opening Date: " + getOpeningDate());
        System.out.println("Closing Date: " + getClosingDate());
        System.out.println("Visibility: " + (isVisible() ? "Visible" : "Not Visible"));
        System.out.println("Available Officer Slots: " + getAvailableSlots());
        System.out.println("Manager: " + getManager());
    }
}
