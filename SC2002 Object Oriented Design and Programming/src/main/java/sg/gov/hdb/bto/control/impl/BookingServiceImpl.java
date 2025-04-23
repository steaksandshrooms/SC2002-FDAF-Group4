package main.java.sg.gov.hdb.bto.control.impl;

import java.util.*;
import main.java.sg.gov.hdb.bto.control.interfaces.*;
import main.java.sg.gov.hdb.bto.entity.user.*;
import main.java.sg.gov.hdb.bto.entity.project.BTOProject;
import main.java.sg.gov.hdb.bto.entity.application.*;
import main.java.sg.gov.hdb.bto.util.CSVHandler;
import main.java.sg.gov.hdb.bto.util.IdGenerator;
import main.java.sg.gov.hdb.bto.util.AuditLogger;
import main.java.sg.gov.hdb.bto.util.ReportGenerator;

public class BookingServiceImpl implements BookingService {
    private List<FlatBooking> bookings = new ArrayList<>();
    private CSVHandler csvHandler;
    private AuthenticationService authService;
    private ProjectManager projectManager;
    
    public BookingServiceImpl(CSVHandler csvHandler) {
        this.csvHandler = csvHandler;
        this.projectManager = projectManager;
        this.csvHandler = csvHandler;
        // Load bookings if CSV exists
        loadBookings();
    }
    
    private void loadBookings() {
        try {
            List<String[]> bookingData = csvHandler.readCSVSkipHeader("Bookings.csv");
            for (String[] row : bookingData) {
                if (row.length < 5) continue;

                String bookingId = row[0];
                String applicantNric = row[1];
                String projectName = row[2];
                String flatType = row[3];
                User applicant = authService.getUser(applicantNric);
                if (applicant == null) continue;

                BTOProject project = projectManager.getProjectByName(projectName);
                if (project == null) continue;

                FlatBooking booking = new FlatBooking(bookingId, applicant, project, flatType);
                bookings.add(booking);

                if (applicant instanceof Applicant) {
                    ((Applicant) applicant).setFlatBooking(booking);
                } else if (applicant instanceof HDBOfficer) {
                    ((HDBOfficer) applicant).setFlatBooking(booking);
                }
            }
        } catch (Exception e) {
            System.out.println("No bookings found or error loading bookings: " + e.getMessage());
        }
    }
    
    @Override
    public void saveBookings() {
        // Save bookings to CSV
        List<String[]> bookingData = new ArrayList<>();
        bookingData.add(new String[]{"BookingId", "ApplicantNRIC", "ProjectName", "FlatType", "BookingDate"});
        
        for (FlatBooking booking : bookings) {
            bookingData.add(new String[]{
                booking.getBookingId(),
                booking.getApplicant().getNric(),
                booking.getProject().getName(),
                booking.getFlatType(),
                booking.getBookingDate().toString()
            });
        }
        
        csvHandler.writeCSV("Bookings.csv", bookingData);
        System.out.println("Bookings saved to Bookings.csv");
    }
    
    @Override
    public FlatBooking bookFlat(User applicant, BTOProject project, String flatType) {
        // Check if there are available units
        if (!project.hasAvailableUnits(flatType)) {
            return null;
        }
        
        // Book the flat (reduce available units)
        project.bookFlat(flatType);
        
        // Create booking
        String bookingId = IdGenerator.generateId("BKG");
        FlatBooking booking = new FlatBooking(bookingId, applicant, project, flatType);
        
        // Add to list of all bookings
        bookings.add(booking);
        
        // Assign to applicant
        if (applicant instanceof Applicant) {
            ((Applicant)applicant).setFlatBooking(booking);
        } else if (applicant instanceof HDBOfficer) {
            ((HDBOfficer)applicant).setFlatBooking(booking);
        }
        
        // Log the action
        AuditLogger.logAction(applicant, "FLAT_BOOKED", 
                             "BookingId: " + booking.getBookingId() + 
                             ", Project: " + project.getName() + 
                             ", Flat Type: " + flatType);
        
        return booking;
    }
    
    @Override
    public List<FlatBooking> getBookingsForProject(BTOProject project) {
        List<FlatBooking> projectBookings = new ArrayList<>();
        for (FlatBooking booking : bookings) {
            if (booking.getProject().equals(project)) {
                projectBookings.add(booking);
            }
        }
        return projectBookings;
    }
    
    @Override
    public List<FlatBooking> getAllBookings() {
        return new ArrayList<>(bookings);
    }
    
    @Override
    public String generateBookingReceipt(FlatBooking booking) {
        return ReportGenerator.generateBookingReceipt(booking);
    }
}