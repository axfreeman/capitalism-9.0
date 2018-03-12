/*
 *  Copyright (C) Alan Freeman 2017-2019
 *  
 *  This file is part of the Capitalism Simulation, abbreviated to CapSim
 *  in the remainder of this project
 *
 *  Capsim is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either project of the License, or
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

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.controller.Simulation;
import capitalism.utils.Reporter;
import capitalism.view.custom.ActionStates;
import capitalism.view.custom.DisplayControlsBox;

/**
 * Wrapper class for a project that will be saved in an XML file on the user's device.
 * UsesJAXB annotations to construct the XML file
 */

@XmlRootElement(name = "SavedProject")
public class OneProject {
	private static final Logger logger = LogManager.getLogger(OneProject.class);

	@XmlElementWrapper(name = "Commodities") List<Commodity> commodities;
	@XmlElementWrapper(name = "Industries") List<Industry> industries;
	@XmlElementWrapper(name = "SocialClasses") List<SocialClass> socialClasses;
	@XmlElementWrapper(name = "Stocks") List<Stock> stocks;
	@XmlElement(name = "TimeStamp") TimeStamp timeStamp;
	@XmlElement(name = "Project") Project project;

	/**
	 * element that is going to be marshaled in the xml
	 * 
	 * @param projectID
	 *            the projectID that is to be dumped to an XML file
	 * @param timeStampID
	 *            the timeStampID of the persistent entitities to be dumped to an XML file
	 */
	public void setLists(int projectID, int timeStampID) {
		commodities = Commodity.allCurrentProject(projectID, timeStampID);
		industries = Industry.allWithProjectAndTimeStamp(projectID, timeStampID);
		socialClasses = SocialClass.allInProjectAndTimeStamp(projectID, timeStampID);
		stocks = Stock.allInProjectAndTimeStamp(projectID, timeStampID);
		timeStamp = TimeStamp.singleInProjectAndTimeStamp(projectID,timeStampID);
		project = Project.get(projectID);
	}

	/**
	 * Create a new project from the XML file that has just been loaded into this OneProject entity.
	 */
	
	public void loadXML() {
		// find the largest project so far. We will add the new project with a project numeber one greater than this
		int maxProjectID = 0;
		for (Project p : Project.projectsAll()) {
			if (p.getProjectID() > maxProjectID)
				maxProjectID = p.getProjectID();
		}
		Reporter.report(logger, 1, "Adding a new project with project number %d", maxProjectID + 1);
		Commodity.getEntityManager().getTransaction().begin();
		Industry.getEntityManager().getTransaction().begin();
		SocialClass.getEntityManager().getTransaction().begin();
		Stock.getEntityManager().getTransaction().begin();
		Project.getEntityManager().getTransaction().begin();
		TimeStamp.getEntityManager().getTransaction().begin();
		for (Commodity c : commodities) {
			logger.debug("Adding commodity called {}", c.name());
			c.setProjectID(maxProjectID+1);
			Commodity.getEntityManager().persist(c);
		}
		for (Industry i: industries) {
			logger.debug("Adding industry called {}", i.name());
			i.setProjectID(maxProjectID+1);
			Industry.getEntityManager().persist(i);
		}
		for (SocialClass sc : socialClasses) {
			logger.debug("Adding socialClass called {}", sc.name());
			sc.setProjectID(maxProjectID+1);
			SocialClass.getEntityManager().persist(sc);
		}
		for (Stock s : stocks) {
			logger.debug("Adding stock called {}", s.name());
			s.setProjectID(maxProjectID+1);
			Stock.getEntityManager().persist(s);
		}
		timeStamp.setProjectID(maxProjectID+1);
		TimeStamp.getEntityManager().persist(timeStamp);
		project.setProjectID(maxProjectID+1);
		project.setButtonState(ActionStates.lastState().text());
		logger.debug("Committing timeStamp with project ID {} and timeStampID {}", timeStamp.getProjectID(), timeStamp.getTimeStampID());
		logger.debug("Committing project with project ID {} and timeStampID {}", project.getProjectID(), project.getTimeStampID());
		Project.getEntityManager().persist(project);
		Commodity.getEntityManager().getTransaction().commit();
		Industry.getEntityManager().getTransaction().commit();
		SocialClass.getEntityManager().getTransaction().commit();
		Stock.getEntityManager().getTransaction().commit();
		Project.getEntityManager().getTransaction().commit();
		TimeStamp.getEntityManager().getTransaction().commit();
		// we loaded the persistent fields, but now we must initialise all the derived fields
		project.initialise();
		DisplayControlsBox.rePopulateProjectCombo();
	}
}
