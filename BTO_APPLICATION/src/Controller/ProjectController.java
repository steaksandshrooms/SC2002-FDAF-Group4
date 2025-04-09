package Controller;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import Database.*;
import Models.*;

public class ProjectController {

    // Database instance to fetch projects
    Database db = new Database();
    int index = 1;

    // Change visibility of a project (HDB Manager)
    public void changeVisibility(String projectName, boolean visibility) {
        // Get the project by name
        Project project = getProjectByProjectName(projectName);
        if (project != null) {
            project.setVisible(visibility);

            // Update the visibility in the CSV file
            Path filePath = Paths.get("src/Resources/CSV/ProjectList.csv");
            try {
                List<String> lines = Files.readAllLines(filePath);
                List<String> updatedLines = new ArrayList<>();

                // Update the visibility in the CSV (assuming the 13th column is for visibility)
                for (String line : lines) {
                    String[] parts = line.split(",");
                    if (parts.length > 0 && parts[0].equals(projectName)) {
                        parts[13] = String.valueOf(visibility);  // Update the visibility column (column index 13)
                        updatedLines.add(String.join(",", parts));
                    } else {
                        updatedLines.add(line);
                    }
                }

                // Write updated lines back to the CSV
                Files.write(filePath, updatedLines, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

            } catch (IOException e) {
                e.printStackTrace();  // Handle file errors gracefully
            }
        }
    }

    // Display projects based on criteria (Applicant's marital status and age)
    public List<Project> displayProjectsByCriteria(Applicant applicant) {
    	 boolean isEligibleForType1Only = (applicant.getmaritalstatus().equals(MaritalStatus.SINGLE) && applicant.getAge() >=35); //True eligible for only type1 room
    	List<Project> filteredProjects = new ArrayList<>();
    	
    	for(Project project :  db.ProjectDB.values())
    	{
    		  boolean isvisible = (project.isVisible());
    		  if(isvisible)
    		  {
    			  List<FlatInfo> availableFlats = new ArrayList<>();
    			 
    			  for(FlatInfo flat : project.getFlats())
    			  {
    				  if(flat.getType() == 2 && isEligibleForType1Only)
    				  {
    					  //skip
    					  continue;
    				  }
    				  
    				  availableFlats.add(flat);
    			  }
    			  
    			  Project filteredProject = new Project(project.getProjectName(), project.getNeighbourhood(), project.getOpeningDate(),project.getOpeningDate() , isvisible, project.getAvailableSlots(), project.getManager());
    			  for (FlatInfo flat : availableFlats) {
    	                filteredProject.addFlat(flat); // Add filtered flats to the project
    	            }
    			  
    			  for(HDBOfficer officer : project.getOfficers())
    			  {
    				  filteredProject.addOfficer(officer);
    			  }
    			  
    			  filteredProjects.add(filteredProject);
    			  
    		  }
    		  
    		  
    		   
    	}
    	
    	
    	return filteredProjects;
    	
    }

    // View all projects (HDB Manager)
    public  List<Project> displayAllProjects() {
        return db.ProjectDB.values().stream()
                .collect(Collectors.toList());
    }

    // View only visible projects
    public List<Project> displayVisibleProjects() {
        return db.ProjectDB.values().stream()
                .filter(Project::isVisible)
                .collect(Collectors.toList());
    }

    // Get a project by officer userid 
    public List<Project> getProjectsByOfficerUserId(String userId) {
        return db.ProjectDB.values().stream()
                .filter(project -> project.getOfficers().stream()
                        .anyMatch(officer -> officer.getUserID().equalsIgnoreCase(userId)))
                .toList();
    }
    
    public Project getProjectByProjectName(String projectName) {
        return db.ProjectDB.values().stream()
                .filter(project -> project.getProjectName().equalsIgnoreCase(projectName))
                .findFirst()
                .orElse(null); // or throw an exception if preferred
    }
    
    
   
}
