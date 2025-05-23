package main.java.sg.gov.hdb.bto.control.impl;

import main.java.sg.gov.hdb.bto.control.interfaces.*;

public class DataServiceImpl implements DataService {
    private ProjectManager projectManager;
    private ApplicationService applicationService;
    private EnquiryService enquiryService;
    private BookingService bookingService;
    
    public DataServiceImpl(AuthenticationService authService, ProjectManager projectManager,
                          ApplicationService applicationService, EnquiryService enquiryService,
                          BookingService bookingService) {
        this.projectManager = projectManager;
        this.applicationService = applicationService;
        this.enquiryService = enquiryService;
        this.bookingService = bookingService;
    }
    
    @Override
    public void loadAllData() {
        System.out.println("All data loaded");
    }
    
    @Override
    public void saveAllData() {
        projectManager.saveProjects();
        projectManager.saveOfficerRegistrations();
        applicationService.saveApplications();
        enquiryService.saveEnquiries();
        bookingService.saveBookings();
        
        System.out.println("All data saved");
    }
}