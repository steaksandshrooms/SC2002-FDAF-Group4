package Controller;

import Models.Users;
import Models.MaritalStatus;
import Models.Role;
import Database.Database;

// This handles user registration and login authentication
public class AuthenticationController {
	
	// Regular Expression for UserID validation
	private static final String Userid_pattern = "^[ST]\\d{7}[A-Za-z]$";

	// Validate UserID Format
	public boolean UserIDvalidation(String userid) {
		return userid.matches(Userid_pattern);
	}

	// Find user by UserID
	public Users getUser(String userid) {
		return Database.UserDB.get(userid);
	}

	// Check if User Exists
	public boolean Exist(String userid) {
		return Database.UserDB.containsKey(userid);
	}
	
	// Registration - Add user to the database
	public boolean Register(String userid, int age, MaritalStatus mstatus, Role role) {
		// Check if the user already exists
		if (Exist(userid)) {
			System.out.println("User has already registered, please sign in.");
			return false;
		}

		// Validate UserID format
		if (!UserIDvalidation(userid)) {
			System.out.println("User ID pattern doesn't match.");
			return false;
		}

		// Create a new user and add to database
		Users newUser = new Users(userid, age, mstatus, role);
		Database.UserDB.put(userid, newUser);
	
		return true;
	}

	// Login Authentication
	public boolean Login(String userid, String password) {
		// Fetch user from the database
		Users user = getUser(userid);

		// Check if user exists and password matches
		if (user != null && user.getPassword().equals(password)) {
			
			
			return true;
		}

		System.out.println("Invalid UserID or Password.");
		return false;
	}

	// Change Password - Update the database
	public boolean changePassword(String userid, String newPassword) {
		// Check if the user exists in the database
		Users user = getUser(userid);

		if (user != null) {
			// Change the password
			user.changePassword(newPassword);
			
			return true;
		}

		System.out.println("User ID does not exist.");
		return false;
	}
	
	
	//get the role of the user
	
	public Role getRole(String userid)
	{
		//check if the user exists
		
		Users user = getUser(userid);
		
		if(user != null)
		{
			//fetch the role number from the database 
			
			return  user.getRole();
		}
		
		else
		{
			System.out.println("user not found ");
			return null;
		}
		
	}
	
}
