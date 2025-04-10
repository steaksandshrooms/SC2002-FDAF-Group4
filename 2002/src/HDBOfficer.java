import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

class HDBOfficer extends HDBStaff {
    private BTOProject assignedProject;

    public HDBOfficer(String name, String nric, int age, String maritalStatus, String password) {
        super(name, nric, age, maritalStatus, password);
    }

    @Override
    public String getRole() {
        return "HDB Officer";
    }

    @Override
    public boolean canViewProject(BTOProject project) {
        return project.equals(assignedProject);
    }

    public void setAssignedProject(BTOProject project) {
        this.assignedProject = project;
    }

    public BTOProject getAssignedProject() {
        return assignedProject;
    }
}