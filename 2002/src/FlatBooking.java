import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

class FlatBooking {
    private User applicant; // Changed from Applicant to User
    private BTOProject project;
    private String flatType;
    private LocalDate bookingDate;

    public FlatBooking(User applicant, BTOProject project, String flatType) {
        this.applicant = applicant;
        this.project = project;
        this.flatType = flatType;
        this.bookingDate = LocalDate.now();
    }
}
