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

import capitalism.utils.Reporter;
import capitalism.view.custom.DisplayControlsBox;

/**
 * Wrapper class for a project that will be saved in an XML file on the user's device.
 * UsesJAXB annotations to construct the XML file
 */

@XmlRootElement(name = "SavedProject")
public class OneProject {
	private static final Logger logger = LogManager.getLogger(OneProject.class);

	@XmlElementWrapper(name = "Commodities") List<Commodity> commodity;
	@XmlElementWrapper(name = "Industries") List<Industry> industry;
	@XmlElementWrapper(name = "SocialClasses") List<SocialClass> socialClass;
	@XmlElementWrapper(name = "Stocks") List<Stock> stock;
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
		commodity = Commodity.allCurrentProject(timeStampID);
		industry = Industry.currentProjectWithTimeStamp(timeStampID);
		socialClass = SocialClass.currentProjectWithTimeStamp(timeStampID);
		stock = Stock.allCurrentProject(timeStampID);
		timeStamp = TimeStamp.singleInCurrentProject(timeStampID);
		project = Project.get(projectID);
	}

	public void loadLists() {
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
		for (Commodity c : commodity) {
			logger.debug("Adding commodity called {}", c.name());
			c.setProjectID(maxProjectID+1);
			Commodity.getEntityManager().persist(c);
		}
		for (Industry i: industry) {
			logger.debug("Adding industry called {}", i.name());
			i.setProjectID(maxProjectID+1);
			Industry.getEntityManager().persist(i);
		}
		for (SocialClass sc : socialClass) {
			logger.debug("Adding socialClass called {}", sc.name());
			sc.setProjectID(maxProjectID+1);
			SocialClass.getEntityManager().persist(sc);
		}
		for (Stock s : stock) {
			logger.debug("Adding stock called {}", s.name());
			s.setProjectID(maxProjectID+1);
			Stock.getEntityManager().persist(s);
		}
		timeStamp.setProjectID(maxProjectID+1);
		TimeStamp.getEntityManager().persist(timeStamp);
		project.setProjectID(maxProjectID+1);
		Project.getEntityManager().persist(project);
		Commodity.getEntityManager().getTransaction().commit();
		Industry.getEntityManager().getTransaction().commit();
		SocialClass.getEntityManager().getTransaction().commit();
		Stock.getEntityManager().getTransaction().commit();
		Project.getEntityManager().getTransaction().commit();
		TimeStamp.getEntityManager().getTransaction().commit();
		DisplayControlsBox.rePopulateProjectCombo();
	}
}
