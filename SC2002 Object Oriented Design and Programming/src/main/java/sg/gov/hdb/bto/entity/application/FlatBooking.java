package main.java.sg.gov.hdb.bto.entity.application;

import java.time.LocalDate;

import main.java.sg.gov.hdb.bto.entity.project.BTOProject;
import main.java.sg.gov.hdb.bto.entity.user.User;

public class FlatBooking {
    private String bookingId;
    private User applicant;
    private BTOProject project;
    private String flatType;
    private LocalDate bookingDate;

    public FlatBooking(String bookingId, User applicant, BTOProject project, String flatType) {
        this.bookingId = bookingId;
        this.applicant = applicant;
        this.project = project;
        this.flatType = flatType;
        this.bookingDate = LocalDate.now();
    }
    
    public String getBookingId() { return bookingId; }
    public User getApplicant() { return applicant; }
    public BTOProject getProject() { return project; }
    public String getFlatType() { return flatType; }
    public LocalDate getBookingDate() { return bookingDate; }
}
