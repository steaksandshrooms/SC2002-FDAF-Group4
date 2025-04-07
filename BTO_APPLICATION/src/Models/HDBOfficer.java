package Models;

//NEED to change the inheritance
public class HDBOfficer extends Users {

	public HDBOfficer(String userid,String name, int age, MaritalStatus mstatus) {
		super(userid,name, age, mstatus, Role.HDBOfficer);
		// TODO Auto-generated constructor stub
	}

}
