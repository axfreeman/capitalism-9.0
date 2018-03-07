/*
 *  Copyright (C) Alan Freeman 2017-2019
 *  
 *  This file is part of the Capitalism Simulation, abbreviated to CapSim
 *  in the remainder of this project
 *
 *  Capsim is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either project 3 of the License, or
 *  (at your option) any later project.
*
*   Capsim is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU General Public License for more details.
*
*   You should have received a copy of the GNU General Public License
*   along with Capsim.  If not, see <http://www.gnu.org/licenses/>.
*/

package capitalism.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import capitalism.view.custom.ActionStates;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * The persistent class for the projects database table.
 * 
 */
@Entity
@Table(name = "projects")

@NamedQueries({
		@NamedQuery(name = "Project.findAll", query = "SELECT v FROM Project v"),
		@NamedQuery(name = "Project.findOne", query = "SELECT p from Project p where p.projectID= :project")
})
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="Project")
public class Project implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id @EmbeddedId @Column(unique = true, nullable = false) private int projectID;
	@XmlElement @Column(name = "description") private String description;
	@XmlElement @Column(name = "currentTimeStamp") private int timeStamp;
	@XmlElement @Column(name = "currentTimeStampCursor") private int timeStampDisplayCursor;
	@XmlElement @Column(name = "currentTimeStampComparatorCursor") private int timeStampComparatorCursor;
	@XmlElement @Column(name = "period") private int period;
	@XmlElement @Column(name = "buttonState") private String buttonState;
	
	private static EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("DB_PROJECT");
	private static EntityManager entityManager;
	private static TypedQuery<Project> projectByPrimaryKeyQuery;
	private static TypedQuery<Project> projectAllQuery;

	static {
		entityManager = entityManagerFactory.createEntityManager();
		projectAllQuery = entityManager.createNamedQuery("Project.findAll", Project.class);
		projectByPrimaryKeyQuery = entityManager.createNamedQuery("Project.findOne", Project.class);
	}


	public Project() {
	}

	public static enum PRICEDYNAMICS {
		SIMPLE("Simple"), EQUALISE("Equalise"),DYNAMIC("Dynamic");
		String text;

		private PRICEDYNAMICS(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}
	}
	
	/**
	 * To be used in startup: set button state to the end of the non-existent last state of the previous period
	 * Added because of a completely mysterious fault on 28 January when suddenly, the default project constructor
	 * would not work because 'ActionStates' had not been initialised. Until then, it always worked.
	 * We will probably need this at some point but the App seems to survive without it.
	 */

	public void setInitialButtonState() {
		this.buttonState = ActionStates.lastSuperState().text();

	}
	
	/**
	 * Get a single project identified by the primary key {@code projectID},
	 * 
	 * @param projectID
	 *            the projectID of a single project
	 * @return the project record containing this project
	 */
	public static Project projectSingle(int projectID) {
		projectByPrimaryKeyQuery.setParameter("project", projectID);
		return projectByPrimaryKeyQuery.getSingleResult();
	}

	/**
	 * a list of all projects
	 * 
	 * @return a list of all projects
	 */
	public static List<Project> projectsAll() {
		return projectAllQuery.getResultList();
	}


	public static EntityManager getEntityManager() {
		return entityManager;
	}

	/**
	 * get the timeStamp of a given project.
	 * 
	 * @param projectID
	 *            the projectID of a single project
	 * @return the timeStamp last created by this project, null if the project does not exist
	 */
	public static int timeStampOfProject(int projectID) {
		Project project = projectSingle(projectID);
		if (project == null) {
			return 0;
		} else {
			return project.getTimeStampID();
		}
	}

	/**
	 * Get the timeStampDisplayCursor of a given project.
	 * 
	 * @param projectID
	 *            the projectID of a single project
	 * @return the timeStamp last created by this project, null if the project does not exist
	 */
	public static int timeStampCursorOfProject(int projectID) {
		Project project = projectSingle(projectID);
		if (project == null) {
			return 0;
		} else {
			return project.getTimeStampDisplayCursor();
		}
	}

	/**
	 * an observable list of all projects
	 * 
	 * @return a list of all projects, in an observable wrapper
	 */
	public static ObservableList<Project> observableProjects() {
		ObservableList<Project> output = FXCollections.observableArrayList();
		List<Project> projects = Project.projectAllQuery.getResultList();
		for (Project g : projects) {
			output.add(g);
		}
		return output;
	}
	
	public int getProjectID() {
		return this.projectID;
	}

	public void setProjectID(int projectID) {
		this.projectID = projectID;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the timeStamp that this simulation has so far reached (only set when switching to a different project)
	 */
	public int getTimeStampID() {
		return timeStamp;
	}

	/**
	 * @param timeStamp
	 *            the timeStamp that this simulation has so far reached (only set when switching to a different project)
	 */
	public void setTimeStamp(int timeStamp) {
		this.timeStamp = timeStamp;
	}

	/**
	 * @return the timeStampDisplayCursor that the user is currently viewing (only set when switching to a different project)
	 */
	public int getTimeStampDisplayCursor() {
		return timeStampDisplayCursor;
	}

	/**
	 * @param timeStampDisplayCursor
	 *            the timeStampDisplayCursor that the user is currently viewing (only set when switching to a different project)
	 */
	public void setTimeStampDisplayCursor(int timeStampDisplayCursor) {
		this.timeStampDisplayCursor = timeStampDisplayCursor;
	}

	/**
	 * @return the timeStampComparatorCursor
	 */
	public int getTimeStampComparatorCursor() {
		return timeStampComparatorCursor;
	}

	/**
	 * @param currentTimeStampComparatorCursor
	 *            the timeStampComparatorCursor to set
	 */
	public void setTimeStampComparatorCursor(int currentTimeStampComparatorCursor) {
		this.timeStampComparatorCursor = currentTimeStampComparatorCursor;
	}

	/**
	 * @return the buttonState
	 */
	public String getButtonState() {
		return buttonState;
	}

	/**
	 * @param buttonState
	 *            the buttonState to set
	 */
	public void setButtonState(String buttonState) {
		this.buttonState = buttonState;

	}

	public String toString() {
		return description;
	}

	/**
	 * @return the period
	 */
	public int getPeriod() {
		return period;
	}

	/**
	 * @param period the period to set
	 */
	public void setPeriod(int period) {
		this.period = period;
	}
	
}