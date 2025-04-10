import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

abstract class HDBStaff extends User {
    public HDBStaff(String name, String nric, int age, String maritalStatus, String password) {
        super(name, nric, age, maritalStatus, password);
    }

    public abstract boolean canViewProject(BTOProject project);
}