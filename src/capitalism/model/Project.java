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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.controller.Simulation;
import capitalism.reporting.Dialogues;
import capitalism.reporting.Reporter;
import capitalism.utils.StringStuff;
import capitalism.view.custom.ActionStates;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * The persistent class for the projects database table.
 * 
 */
@Entity
@Table(name = "projects")

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "Project")
public class Project implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger("Project");
	@Id @EmbeddedId @Column(unique = true, nullable = false) private int projectID;
	@XmlElement @Column(name = "description") private String description;
	@XmlElement @Column(name = "currentTimeStamp") private int timeStampID;

	/**
	 * Application-wide display cursor. By changing this, the user views entities from earlier timestamps and compares them with
	 * those from the current timeStamp. The cursor is independent of timeStampIDCurrent and operations that involve it do not
	 * affect the entities stored in the database other than the timeStamp and project records, which keep track of
	 * what the user is looking at
	 */
	@XmlElement @Column(name = "currentTimeStampCursor") private int timeStampDisplayCursor;

	/**
	 * Application-wide comparator cursor. By changing this, the user selects the earlier timeStamp with which the current
	 * state of the simulation is compared.The cursor is independent of timeStampIDCurrent and operations that involve it do not
	 * affect the entities stored in the database other than the timeStamp and project records, which keep track of
	 * what the user is looking at
	 */
	@XmlElement @Column(name = "currentTimeStampComparatorCursor") private int timeStampComparatorCursor;

	@XmlElement @Column(name = "buttonState") private String buttonState;

	private static EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("DB_PROJECT");
	private static EntityManager entityManager;
	private static TypedQuery<Project> primaryQuery;
	private static TypedQuery<Project> allQuery;

	static {
		entityManager = entityManagerFactory.createEntityManager();
		allQuery = entityManager.createQuery("SELECT p FROM Project p", Project.class);
		primaryQuery = entityManager.createQuery("SELECT p from Project p where p.projectID= :project", Project.class);
	}

	public Project() {
	}

	public static enum PRICEDYNAMICS {
		SIMPLE("Simple"), EQUALISE("Equalise"), DYNAMIC("Dynamic");
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
	 * @return the largest projectID so far - allows us to add new projects without clashes
	 */
	public static int maxProjectID() {
		int maxProjectID = 0;
		for (Project p : Project.all()) {
			if (p.getProjectID() > maxProjectID)
				maxProjectID = p.getProjectID();
		}
		return maxProjectID;
	}
	
	/**
	 * Get a single project identified by the primary key {@code projectID},
	 * 
	 * @param projectID
	 *            the projectID of a single project
	 * @return the project record containing this project, null if the project does not exist
	 */
	public static Project get(int projectID) {
		primaryQuery.setParameter("project", projectID);
		try {
			return primaryQuery.getSingleResult();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * a list of all projects
	 * 
	 * @return a list of all projects
	 */
	public static List<Project> all() {
		return allQuery.getResultList();
	}

	public static EntityManager getEntityManager() {
		return entityManager;
	}

	/**
	 * set the timeStamp of a given project.
	 * 
	 * @param projectID
	 *            the projectID of a single project
	 * @param timeStampID
	 *            the timeStampID to be set for this project - normally, the timeStamp when the user switches a simulation
	 *            or when the simulation moves one step forward
	 * @return 0 if fail, the timeStamp otherwise
	 */
	public static int setTimeStamp(int projectID, int timeStampID) {
		Project project = Project.get(projectID);
		if (project == null) {
			return 0;
		} else {
			project.setTimeStampID(timeStampID);
			return timeStampID;
		}
	}

	/**
	 * set the timeStampDisplayCursor of a given project.
	 * 
	 * @param projectID
	 *            the projectID of a single project
	 * @param timeStampCursor
	 *            the timeStampDisplayCursor to be set for this project - normally, the timeStampDisplayCursor when the user switches a simulation
	 * @return 0 if fail, the timeStampDisplayCursor otherwise
	 */
	public static int setTimeStampCursor(int projectID, int timeStampCursor) {
		Project project = Project.get(projectID);
		if (project == null) {
			return 0;
		} else {
			project.setTimeStampDisplayCursor(timeStampCursor);
			return timeStampCursor;
		}
	}

	/**
	 * an observable list of all projects
	 * 
	 * @return a list of all projects, in an observable wrapper
	 */
	public static ObservableList<Project> observableProjects() {
		ObservableList<Project> output = FXCollections.observableArrayList();
		List<Project> projects = Project.allQuery.getResultList();
		for (Project g : projects) {
			// ProjectID <1 reserved for editor and storage
			if (g.getProjectID() > 0)
				output.add(g);
		}
		return output;
	}

	public void initialise() {
		// temporary repository for all timeStamp information
		TimeStamp currentStamp = null;
		try {
			// since we are initialising, we start with timeStampID 1
			currentStamp = TimeStamp.single(projectID, 1);
			logger.debug(" Initialising with timeStamp {}",currentStamp.toString());
		} catch (Exception e) {
			Dialogues.alert(logger, "There is no timeStamp record for the project called " +
					description + "\nPlease check your data. Will attempt to continue with other projects");
			return;
		}
		Reporter.report(logger, 1, "Initialising project %d called '%s'", projectID, getDescription());

		// initialise the project record so that its cursors are 1
		Project.getEntityManager().getTransaction().begin();
		setTimeStampID(1);
		setTimeStampDisplayCursor(1);
		setTimeStampComparatorCursor(1);

		// set the project buttonState initially to the end of the non-existent previous period
		setButtonState(ActionStates.lastState().text());
		getEntityManager().getTransaction().commit();

		// Set the initial comparators for every timeStamp, project, industry, class, use value and stock .
		// Since the comparator cursor and the cursor are already 1, this amounts to setting it to 1

		// little tweak to handle currency symbols encoded in UTF8
		logger.debug("Character Symbol for Project {} is {}", currentStamp.getCurrencySymbol());
		String utfjava = StringStuff.convertFromUTF8(currentStamp.getCurrencySymbol());
		logger.debug("Character symbol after conversion is {}", utfjava);
		currentStamp.setCurrencySymbol(utfjava);
		Simulation.setComparators(projectID, 1);

		Simulation.convertMagnitudesToCoefficients(projectID, timeStampID);
		Simulation.calculateStockAggregates(projectID, timeStampID);
		Simulation.setCapitals(projectID, timeStampID);
		Simulation.checkInvariants();// TODO Stub at present
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
		return timeStampID;
	}

	/**
	 * @param timeStamp
	 *            the timeStamp that this simulation has so far reached (only set when switching to a different project)
	 */
	public void setTimeStampID(int timeStamp) {
		this.timeStampID = timeStamp;
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

}