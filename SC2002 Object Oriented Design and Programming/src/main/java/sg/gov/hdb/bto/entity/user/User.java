package main.java.sg.gov.hdb.bto.entity.user;

import main.java.sg.gov.hdb.bto.util.PasswordManager;

public abstract class User {
    protected String name;
    protected String nric;
    protected int age;
    protected String maritalStatus;
    protected String password; // Stores the hashed password

    public User(String name, String nric, int age, String maritalStatus, String password) {
        this.name = name;
        this.nric = nric;
        this.age = age;
        this.maritalStatus = maritalStatus;
        this.password = password; // Password should already be hashed
    }

    public boolean authenticate(String inputPassword) {
        if (this.password.equals(inputPassword)) {
            return true;
        }
        
        return this.password.equals(PasswordManager.hashPassword(inputPassword));
    }

    public void changePassword(String newPassword) {
        this.password = PasswordManager.hashPassword(newPassword);
    }

    public String getName() { return name; }
    public String getNric() { return nric; }
    public int getAge() { return age; }
    public String getMaritalStatus() { return maritalStatus; }
    public String getPassword() { return password; }
    
    public abstract String getRole();
}
