package main.java.sg.gov.hdb.bto.control.interfaces;

import java.util.*;
import main.java.sg.gov.hdb.bto.entity.user.*;
import main.java.sg.gov.hdb.bto.entity.project.BTOProject;
import main.java.sg.gov.hdb.bto.entity.application.*;

public interface BookingService {
    FlatBooking bookFlat(User applicant, BTOProject project, String flatType);
    List<FlatBooking> getBookingsForProject(BTOProject project);
    List<FlatBooking> getAllBookings();
    String generateBookingReceipt(FlatBooking booking);
    void saveBookings();
}