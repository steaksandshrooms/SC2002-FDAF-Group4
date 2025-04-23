package main.java.sg.gov.hdb.bto.entity.project;

import java.time.LocalDate;
import java.util.*;
import main.java.sg.gov.hdb.bto.entity.user.*;
import main.java.sg.gov.hdb.bto.entity.application.*;

public class BTOProject {
    private String name;
    private String neighborhood;
    private Map<String, Integer> units;
    private Map<String, Integer> prices;
    private LocalDate openDate;
    private LocalDate closeDate;
    private HDBManager manager;
    private int officerSlots;
    private List<HDBOfficer> officers;
    private boolean visible;
    private List<Application> applications;
    private List<Enquiry> enquiries;

    public BTOProject(String name, String neighborhood,
                      Map<String, Integer> units, Map<String, Integer> prices,
                      LocalDate openDate, LocalDate closeDate,
                      HDBManager manager, int officerSlots) {
        this.name = name;
        this.neighborhood = neighborhood;
        this.units = new HashMap<>(units);
        this.prices = new HashMap<>(prices);
        this.openDate = openDate;
        this.closeDate = closeDate;
        this.manager = manager;
        this.officerSlots = officerSlots;
        this.officers = new ArrayList<>();
        this.visible = false;
        this.applications = new ArrayList<>();
        this.enquiries = new ArrayList<>();
    }

    public boolean hasAvailableUnits(String flatType) {
        return units.getOrDefault(flatType, 0) > 0;
    }

    public int getAvailableUnits(String flatType) {
        return units.getOrDefault(flatType, 0);
    }

    public Set<String> getFlatTypes() {
        return units.keySet();
    }
    
    public int getPrice(String flatType) {
        return prices.getOrDefault(flatType, 0);
    }

    public void bookFlat(String flatType) {
        if (units.containsKey(flatType) && units.get(flatType) > 0) {
            units.put(flatType, units.get(flatType) - 1);
        }
    }

    public void addApplication(Application application) {
        applications.add(application);
    }

    public List<Application> getApplications() {
        return new ArrayList<>(applications);
    }

    public void addEnquiry(Enquiry enquiry) {
        enquiries.add(enquiry);
    }

    public List<Enquiry> getEnquiries() {
        return new ArrayList<>(enquiries);
    }

    public void addOfficer(HDBOfficer officer) {
        if (officers.size() < officerSlots) {
            officers.add(officer);
        }
    }

    public List<HDBOfficer> getOfficers() {
        return new ArrayList<>(officers);
    }

    public int getOfficerSlots() {
        return officerSlots;
    }

    public void setVisibility(boolean visible) {
        this.visible = visible;
    }

    public void removeEnquiry(Enquiry enquiry) {
        enquiries.remove(enquiry);
    }

    public String getName() { return name; }
    public String getNeighborhood() { return neighborhood; }
    public LocalDate getOpenDate() { return openDate; }
    public LocalDate getCloseDate() { return closeDate; }
    public boolean isVisible() { return visible; }
    public HDBManager getManager() { return manager; }
    public Map<String, Integer> getUnits() { return new HashMap<>(units); }
    public Map<String, Integer> getPrices() { return new HashMap<>(prices); }
}
