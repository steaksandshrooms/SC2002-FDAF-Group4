import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

class HDBManager extends HDBStaff {
    private List<BTOProject> managedProjects;

    public HDBManager(String name, String nric, int age, String maritalStatus, String password) {
        super(name, nric, age, maritalStatus, password);
        this.managedProjects = new ArrayList<>();
    }

    @Override
    public String getRole() {
        return "HDB Manager";
    }

    @Override
    public boolean canViewProject(BTOProject project) {
        return true;
    }

    public void addManagedProject(BTOProject project) {
        managedProjects.add(project);
    }

    public List<BTOProject> getManagedProjects() {
        return new ArrayList<>(managedProjects);
    }
}