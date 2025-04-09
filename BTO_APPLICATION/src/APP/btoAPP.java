package APP;

import Controller.ProjectController;
import Database.Database;
import Views.AuthenticationView;

public class btoAPP {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Database d = new Database();
		ProjectController p = new ProjectController();
		 d.loadData();
	
//		d.displayAllProjects();
		
	AuthenticationView authView = new AuthenticationView();
	authView.StartAuth();
	
	}

}
