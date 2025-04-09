package Models;

public class HDBOfficer extends Users {
    
    private String projectInCharge; // optional: stores the project they are assigned to

    public HDBOfficer(String userid, String name, int age, MaritalStatus mstatus) {
        super(userid, name, age, mstatus, Role.HDBOfficer);
        this.projectInCharge = null; // default not assigned
    }

    public String getProjectInCharge() {
        return projectInCharge;
    }

    public void setProjectInCharge(String projectInCharge) {
        this.projectInCharge = projectInCharge;
    }

    public boolean isAssigned() {
        return projectInCharge != null;
    }
}