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
package rd.dev.simulation.datamanagement;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import rd.dev.simulation.Simulation;
import rd.dev.simulation.model.Project;
import rd.dev.simulation.model.TimeStamp;

/**
 * this class is a specialized extension of DataManager which provides access to the timeStamp and project entities.
 * These are separated out for convenience; there is no special reason other than conciseness and ease of access to the code
 *
 */
public class SelectionsProvider extends DataManager {

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
			return project.getTimeStamp();
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
	 * Get the single TimeStamp entity of the current project and the current timeStamp
	 * @param timeStampID
	 *            the timeStampID of the TimeStamp entity
	 * @return the TimeStamp that has this timeStampID and the current project
	 */

	public static TimeStamp timeStampSingle(int timeStampID) {
		timeStampByPrimarykeyQuery.setParameter("project", Simulation.projectCurrent);
		timeStampByPrimarykeyQuery.setParameter("timeStamp", timeStampID);
		return timeStampByPrimarykeyQuery.getSingleResult();
	}

	/**
	 * a list of all projects
	 * 
	 * @return a list of all projects
	 */
	public static List<Project> projectsAll() {
		return projectAllQuery.getResultList();
	}

	public static ObservableList<TimeStamp> timeStampsBySuperState(int period, String superStateName) {
		ObservableList<TimeStamp> timeStamps = FXCollections.observableArrayList();
		timeStampSuperStatesQuery.setParameter("project",Simulation.projectCurrent).setParameter("superState", superStateName).setParameter("period", period);
		for (TimeStamp timeStamp : timeStampSuperStatesQuery.getResultList())
			timeStamps.add(timeStamp);
		return timeStamps;
	}

	public static ObservableList<TimeStamp> timeStampsAll() {
		ObservableList<TimeStamp> timeStamps = FXCollections.observableArrayList();
		timeStampsAllQuery.setParameter("project", 1);
		for (TimeStamp timeStamp : timeStampsAllQuery.getResultList())
			timeStamps.add(timeStamp);
		return timeStamps;
	}
	
	/**
	 * All timeStamps for the current project.
	 * 
	 * @return a list of timeStamps for the current project
	 */
	public static List<TimeStamp> timeStampsByProject() {
		timeStampsAllByProjectQuery.setParameter("project", Simulation.projectCurrent);
		return timeStampsAllByProjectQuery.getResultList();
	}

	/**
	 * set the timeStamp of a given project.
	 * 
	 * @param projectID
	 *            the projectID of a single project
	 * @param timeStamp
	 *            the timeStamp to be set for this project - normally, the timeStamp when the user switches a simulation
	 * @return 0 if fail, the timeStamp otherwise
	 */
	public static int setTimeStampOfProject(int projectID, int timeStamp) {
		Project project = projectSingle(projectID);
		if (project == null) {
			return 0;
		} else {
			project.setTimeStamp(timeStamp);
			return timeStamp;
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
	public static int setTimeStampCursorOfProject(int projectID, int timeStampCursor) {
		Project project = projectSingle(projectID);
		if (project == null) {
			return 0;
		} else {
			project.setTimeStampDisplayCursor(timeStampCursor);
			return timeStampCursor;
		}
	}
}
