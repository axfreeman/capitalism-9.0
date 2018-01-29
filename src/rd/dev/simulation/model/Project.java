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

package rd.dev.simulation.model;

import java.io.Serializable;
import javax.persistence.*;

import rd.dev.simulation.custom.ActionStates;

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

public class Project implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id @EmbeddedId @Column(unique = true, nullable = false) private int projectID;
	@Column(name="description") private String description;
	@Column(name="currentTimeStamp") private int timeStamp;
	@Column(name="currentTimeStampCursor") private int timeStampDisplayCursor;
	@Column(name="currentTimeStampComparatorCursor") private int timeStampComparatorCursor;
	@Column(name="buttonState") private String buttonState;

	public Project() {
	}
	
	/**
	 * To be used in startup: set button state to the end of the non-existent last state of the previous period
	 * Added because of a completely mysterious fault on 28 January when suddenly, the default project constructor
	 * would not work because 'ActionStates' had not been initialised. Until then, it always worked.
	 * We will probably need this at some point but the App seems to survive without it.
	 */
	
	public void setInitialButtonState() {
		String distributeText=ActionStates.C_M_Distribute.getText();
		this.buttonState=distributeText;

	}

//	public Project(int projectID, String description) {
//		// set button state to the end of the non-existent last state of the previous period
//		this.buttonState=ActionStates.C_M_Distribute.getText();
//		this.projectID = projectID;
//		this.description = description;
//	}

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
	public int getTimeStamp() {
		return timeStamp;
	}

	/**
	 * @param timeStamp the timeStamp that this simulation has so far reached (only set when switching to a different project)
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
	 * @param timeStampDisplayCursor the timeStampDisplayCursor that the user is currently viewing (only set when switching to a different project)
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
	 * @param currentTimeStampComparatorCursor the timeStampComparatorCursor to set
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
	 * @param buttonState the buttonState to set
	 */
	public void setButtonState(String buttonState) {
		this.buttonState = buttonState;
		
	}

	public String toString() {
		return description;
	}
}