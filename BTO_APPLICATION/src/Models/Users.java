package Models;

public class Users {
	
	//UserID
	//password : default "Password"
	// Age 
	//Martial Status
	//Role 
	

	private String userid ;
	private String name ;
	private String password;
	private int Age;
	private MaritalStatus maritalStatus;
	private  Role role;
	
   

	
	//User:constructor
	
	public Users(String userid, String name ,int age, MaritalStatus mstatus, Role role )
	{
		   this.userid = userid;
		   this.name  = name;
		   this.Age =  age;
		   this.maritalStatus = mstatus;
		   this.role = role;
		   //default password
		   this .password = "password";
	}
	 
	//getters and setters 
	
	//set password
	public void  changePassword(String newPassword)
	{
		  this.password = newPassword;
	}
	
	
	//getPassword
	 
	public String getPassword()
	{
		return this.password;
	}
	
	// getMartialStatus
	
	public MaritalStatus getmaritalstatus()
	{
		 return this.maritalStatus;
	}
	
	//set MaritalStatus 
	
	public void  changeMaritalStatus(MaritalStatus mstatus)
	{
		 this.maritalStatus = mstatus;
	}
	
   // getUserID (FIXED)
	
	public String getUserID()
	{
		return this.userid;
	}
   //getAge
	
	public int getAge()
	{
		return this.Age;
	}
	
	public void changeAge(int age)
	{ 
		  this.Age  = age;
	}
   //getRole()
	
	public Role getRole()
	{
		 return this.role;
	}
	
	//set the  role
	
	public void setRole(Role r)
	{
		this.role = r;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public void setName(String newname)
	{
		 this.name = newname;
	}
   


	


}
