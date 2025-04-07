package Views;

import java.util.Scanner;
import Models.Role;
import Models.MaritalStatus;
import Controller.AuthenticationController;

public class AuthenticationView {

    Scanner s = new Scanner(System.in);
    private AuthenticationController authController = new AuthenticationController();

    // Register User
    public void RegisterUser () {
        String userid;
        while (true) {
            System.out.println("Enter your user id: ");
            userid = s.next();
            if (authController.UserIDvalidation(userid)) {
                break;
            } else {
                System.out.println("Invalid user ID pattern. Please enter a valid user ID.");
            }
        }
        
        //Enter your name 
        
        System.out.println("Enter your name : ");
        String name  = s.nextLine();

        int maritalChoice;
        MaritalStatus mstatus = null;
        while (true) {
            System.out.println("Please enter your choice");
            System.out.println("1. Married");
            System.out.println("2. Single");
            maritalChoice = s.nextInt();
            switch (maritalChoice) {
                case 1:
                    mstatus = MaritalStatus.MARRIED;
                    break;
                case 2:
                    mstatus = MaritalStatus.SINGLE;
                    break;
                default:
                    System.out.println("Invalid choice, please try again!");
                    continue; 
            }
            break;
        }

        int age;
        while (true) {
            System.out.println("Enter your age:");
            age = s.nextInt();
            if (age > 0) {
                break;
            } else {
                System.out.println("Age should be a positive number.");
            }
        }

        Role role = null;
        int rolechoice;
        while (true) {
            System.out.println("Enter your choice:");
            System.out.println("1. Applicant");
            System.out.println("2. HDB Officer");
            System.out.println("3. HDB Manager");
            rolechoice = s.nextInt();
            switch (rolechoice) {
                case 1:
                    role = Role.Applicant;
                    break;
                case 2:
                    role = Role.HDBOfficer;
                    break;
                case 3:
                    role = Role.HDBManager;
                    break;
                default:
                    System.out.println("Invalid choice! Please select 1-3.");
                    continue; 
            }
            break;
        }

        boolean registerSuccess = authController.Register(userid, name ,age, mstatus, role);

        if (registerSuccess) {
            System.out.println("Successfully Registered");
            System.out.println("Please log in to proceed.");
            Login(); 
        } else {
            System.out.println("Registration failed. The user may already be registered.");
            RegisterUser (); 
        }
    }

    // Login User
    public void Login() {
        String userid;
        while (true) {
            System.out.println("Please enter your user ID: ");
            userid = s.next();
            if (authController.UserIDvalidation(userid)) {
                break;
            }
            System.out.println("Invalid user id, please try again");
        }

        String password;
        System.out.println("Please enter your password: ");
        password = s.next();

        boolean loginstatus = authController.Login(userid, password);

        if (loginstatus) {
            System.out.println("Login Successful!");

            Role userRole = authController.getRole(userid);

            // Directly redirect to the role's view
            switch (userRole) {
                case Applicant:
                    ApplicantView applicant = new ApplicantView();
                    applicant.ApplicantStart(); 
                    break;
                case HDBOfficer:
                    HDBOfficerView officerView = new HDBOfficerView();
                    officerView.HDBOfficerStart(); 
                    break;
                case HDBManager:
                    HDBManagerView managerView = new HDBManagerView();
                    managerView.HDBManagerStart(); 
                    break;
                default:
                    System.out.println("Unknown role.");
            }
        } else {
        	 StartAuth();
            System.out.println("Login failed");
        }
    }

    // Change Password
    public void changePassword() {
        String userid;
        while (true) {
            System.out.println("Please enter your user ID: ");
            userid = s.next();
            if (authController.UserIDvalidation(userid)) {
                break;
            }
            System.out.println("Invalid user ID, please try again.");
        }

        String newpassword;
        System.out.println("Please enter your new password: ");
        newpassword = s.next();

        boolean changePasswordStatus = authController.changePassword(userid, newpassword);
        if (changePasswordStatus) {
        	 StartAuth();
            System.out.println("Password changed successfully!");
        } else {
        	 StartAuth();
            System.out.println("Password change failed. Please try again.");
        }
    }

    // Main Authentication Flow
    public void StartAuth() {
        int choice;
        
            System.out.println("AUTHENTICATION VIEW");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Change Password");
            System.out.println("4. Exit");
            choice = s.nextInt();

            switch (choice) {
                case 1:
                    RegisterUser ();
                    break;
                case 2:
                    Login(); 
                    // Do not return here; allow the program to continue running
                    break;
                case 3:
                    changePassword();
                    break;
                case 4:
                    return; 
                default:
                    System.out.println("Invalid choice, please try again.");
           
        }
    }
}