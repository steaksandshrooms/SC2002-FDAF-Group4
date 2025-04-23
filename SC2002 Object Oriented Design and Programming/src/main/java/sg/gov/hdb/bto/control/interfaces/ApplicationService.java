package main.java.sg.gov.hdb.bto.control.interfaces;

import java.util.*;
import main.java.sg.gov.hdb.bto.entity.user.*;
import main.java.sg.gov.hdb.bto.entity.project.BTOProject;
import main.java.sg.gov.hdb.bto.entity.application.*;

public interface ApplicationService {
    Application createApplication(User applicant, BTOProject project, String flatType);
    void updateApplicationStatus(Application application, String newStatus);
    boolean isEligibleForApplication(User applicant, BTOProject project, String flatType);
    List<Application> getApplicationsForProject(BTOProject project);
    List<Application> getAllApplications();
    void requestWithdrawal(Application application, String reason);
    void updateWithdrawalStatus(Application application, String status);
    List<Application> getApplicationsWithPendingWithdrawals();
    void saveApplications();
}
