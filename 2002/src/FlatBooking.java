import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

class FlatBooking {
    private Applicant applicant;
    private BTOProject project;
    private String flatType;
    private LocalDate bookingDate;

    public FlatBooking(Applicant applicant, BTOProject project, String flatType) {
        this.applicant = applicant;
        this.project = project;
        this.flatType = flatType;
        this.bookingDate = LocalDate.now();
    }
}