package main.java.sg.gov.hdb.bto.entity.user;

import main.java.sg.gov.hdb.bto.entity.project.BTOProject;

public abstract class HDBStaff extends User {
    public HDBStaff(String name, String nric, int age, String maritalStatus, String password) {
        super(name, nric, age, maritalStatus, password);
    }

    public abstract boolean canViewProject(BTOProject project);
}