package main.java.sg.gov.hdb.bto.entity.user;

import java.util.ArrayList;
import java.util.List;

import main.java.sg.gov.hdb.bto.entity.application.Application;
import main.java.sg.gov.hdb.bto.entity.application.Enquiry;
import main.java.sg.gov.hdb.bto.entity.application.FlatBooking;

public class Applicant extends User {
    private Application currentApplication;
    private List<Enquiry> enquiries;
    private FlatBooking flatBooking;

    public Applicant(String name, String nric, int age, String maritalStatus, String password) {
        super(name, nric, age, maritalStatus, password);
        this.enquiries = new ArrayList<>();
    }

    @Override
    public String getRole() {
        return "Applicant";
    }

    public Application getCurrentApplication() {
        return currentApplication;
    }
    
    public void setCurrentApplication(Application application) {
        this.currentApplication = application;
    }

    public List<Enquiry> getEnquiries() {
        return new ArrayList<>(enquiries);
    }
    
    public void addEnquiry(Enquiry enquiry) {
        enquiries.add(enquiry);
    }

    public void setFlatBooking(FlatBooking flatBooking) {
        this.flatBooking = flatBooking;
    }
    
    public FlatBooking getFlatBooking() {
        return flatBooking;
    }

    public void removeEnquiry(Enquiry enquiry) {
        enquiries.remove(enquiry);
    }
}
