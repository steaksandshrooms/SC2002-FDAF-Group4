package Models;

public class Applicant extends Users{
	
	
	
	public Applicant(String userid, String name, int age, MaritalStatus mstatus) {
		super(userid, name, age, mstatus, Role.Applicant);
		
	}
	
    
     
	public void getApplicantDetails()
	{
	       
	        System.out.println("===============================================");
	        System.out.println("              Applicant Details               ");
	        System.out.println("===============================================");
	       // Header for each field
	        System.out.printf("%-20s: %s\n", "Name", super.getName());
	        System.out.printf("%-20s: %s\n", "IC Number", super.getUserID()); // Assuming getUserId() gives IC number
	        System.out.printf("%-20s: %d\n", "Age", super.getAge());
	        System.out.printf("%-20s: %s\n", "Marital Status", super.getmaritalstatus());
	        System.out.printf("%-20s: %s\n", "Role", super.getRole());
	        System.out.println("===============================================");
	}
	
	
	
	
	
	
	

}
