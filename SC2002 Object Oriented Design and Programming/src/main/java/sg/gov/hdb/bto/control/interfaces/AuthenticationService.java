package main.java.sg.gov.hdb.bto.control.interfaces;

import java.util.*;
import main.java.sg.gov.hdb.bto.entity.user.*;

public interface AuthenticationService {
    User login(String nric, String password);
    void registerUser(User user);
    User getUser(String nric);
    List<Applicant> getAllApplicants();
    List<HDBOfficer> getAllOfficers();
    List<HDBManager> getAllManagers();
}
