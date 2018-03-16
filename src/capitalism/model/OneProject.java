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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.utils.Reporter;
import capitalism.utils.Validate;
import capitalism.view.custom.ActionStates;
import capitalism.view.custom.DisplayControlsBox;

/**
 * Wrapper class for a project that will be saved in an XML file on the user's device.
 * UsesJAXB annotations to construct the XML file
 */

@XmlRootElement(name = "SavedProject")
@XmlAccessorType(XmlAccessType.NONE)
public class OneProject {
	private static final Logger logger = LogManager.getLogger(OneProject.class);

	@XmlElementWrapper(name = "Commodities") List<Commodity> commodities;
	@XmlElementWrapper(name = "Industries") List<Industry> industries;
	@XmlElementWrapper(name = "SocialClasses") List<SocialClass> socialClasses;
	@XmlElementWrapper(name = "Stocks") List<Stock> stocks;
	@XmlElementWrapper(name ="TimeStamps") List<TimeStamp> timeStamps;
	@XmlElement(name = "Project") Project project;
	
	/**
	 * Wrap the lists from the given state of the database for a specific project
	 * @param projectID the project to wrap 
	 */
	public void wrap(int projectID) {
		commodities = Commodity.all(projectID);
		industries = Industry.all(projectID);
		socialClasses=SocialClass.all(projectID);
		stocks=Stock.all(projectID);
		timeStamps=TimeStamp.allInProject(projectID);
		project=Project.get(projectID);
	}

	/**
	 * Create a new project in the database from whatever has been loaded into this oneProject entity.
	 */
	public void sendToDatabase() {
		// find the largest project so far. We will add the new project with a project numeber one greater than this
		int maxProjectID = Project.maxProjectID();
		Reporter.report(logger, 1, "Adding a new project with project number %d", maxProjectID + 1);
		Project.getEntityManager().getTransaction().begin();
		project.setProjectID(maxProjectID+1);
		project.setButtonState(ActionStates.lastState().text());
		Project.getEntityManager().persist(project);
		Project.getEntityManager().getTransaction().commit();

		TimeStamp.getEntityManager().getTransaction().begin();
		for (TimeStamp ts:timeStamps) {
			ts.setProjectID(maxProjectID+1);
			TimeStamp.getEntityManager().persist(ts);
			logger.debug("Committing timeStamp with project ID {} and timeStampID {}", ts.getProjectID(), ts.getTimeStampID());
		}
		TimeStamp.getEntityManager().getTransaction().commit();

		Commodity.getEntityManager().getTransaction().begin();
		Industry.getEntityManager().getTransaction().begin();
		SocialClass.getEntityManager().getTransaction().begin();
		Stock.getEntityManager().getTransaction().begin();
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
		Commodity.getEntityManager().getTransaction().commit();
		Industry.getEntityManager().getTransaction().commit();
		SocialClass.getEntityManager().getTransaction().commit();
		Stock.getEntityManager().getTransaction().commit();

		// we loaded the persistent fields, but now we must initialise all the derived fields
		
		Validate.validate(maxProjectID);
		project.initialise();
		
		DisplayControlsBox.rePopulateProjectCombo();
	}
	
	/**
	 * @return the commodities
	 */
	public List<Commodity> getCommodities() {
		return commodities;
	}

	/**
	 * @param commodities the commodities to set
	 */
	public void setCommodities(List<Commodity> commodities) {
		this.commodities = commodities;
	}

	/**
	 * @return the industries
	 */
	public List<Industry> getIndustries() {
		return industries;
	}

	/**
	 * @param industries the industries to set
	 */
	public void setIndustries(List<Industry> industries) {
		this.industries = industries;
	}

	/**
	 * @return the socialClasses
	 */
	public List<SocialClass> getSocialClasses() {
		return socialClasses;
	}

	/**
	 * @param socialClasses the socialClasses to set
	 */
	public void setSocialClasses(List<SocialClass> socialClasses) {
		this.socialClasses = socialClasses;
	}

	/**
	 * @return the stocks
	 */
	public List<Stock> getStocks() {
		return stocks;
	}

	/**
	 * @param stocks the stocks to set
	 */
	public void setStocks(List<Stock> stocks) {
		this.stocks = stocks;
	}

	/**
	 * @return the project
	 */
	public Project getProject() {
		return project;
	}

	/**
	 * @param project the project to set
	 */
	public void setProject(Project project) {
		this.project = project;
	}

	/**
	 * @return the timeStamps
	 */
	public List<TimeStamp> getTimeStamps() {
		return timeStamps;
	}

	/**
	 * @param timeStamps the timeStamps to set
	 */
	public void setTimeStamps(List<TimeStamp> timeStamps) {
		this.timeStamps = timeStamps;
	}

}
