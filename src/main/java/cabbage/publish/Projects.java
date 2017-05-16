package cabbage.publish;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement( name = "Projects" )
public class Projects {
	private ArrayList<Project> projects;

	public ArrayList<Project> getProjects() {
		return projects;
	}
	public void setProjects(ArrayList<Project> projects) {
		this.projects = projects;
	}
}
