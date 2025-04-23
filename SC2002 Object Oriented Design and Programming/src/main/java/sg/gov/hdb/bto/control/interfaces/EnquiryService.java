package main.java.sg.gov.hdb.bto.control.interfaces;

import java.util.*;
import main.java.sg.gov.hdb.bto.entity.user.*;
import main.java.sg.gov.hdb.bto.entity.project.BTOProject;
import main.java.sg.gov.hdb.bto.entity.application.*;

public interface EnquiryService {
    Enquiry submitEnquiry(User applicant, BTOProject project, String message);
    void replyToEnquiry(Enquiry enquiry, String reply);
    List<Enquiry> getEnquiriesForProject(BTOProject project);
    List<Enquiry> getEnquiriesForUser(User user);
    List<Enquiry> getAllEnquiries();
    void updateEnquiry(Enquiry enquiry, String newMessage);
    void deleteEnquiry(Enquiry enquiry);
    void saveEnquiries();
}
