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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import rd.dev.simulation.Simulation;
import rd.dev.simulation.model.Industry;
import rd.dev.simulation.model.Project;
import rd.dev.simulation.model.SocialClass;
import rd.dev.simulation.model.Stock;
import rd.dev.simulation.model.UseValue;

public class ObservableListProvider extends DataManager {
	private static final Logger logger = LogManager.getLogger(ObservableListProvider.class);

	public ObservableListProvider() {
	}

	/**
	 * an observable list of all projects
	 * 
	 * @return a list of all projects, in an observable wrapper
	 */
	public ObservableList<Project> observableProjects() {
		logger.log(Level.getLevel("DEBUG"), " Constructing an observable list of projects");
		ObservableList<Project> output = FXCollections.observableArrayList();
		List<Project> projects = DataManager.projectAllQuery.getResultList();
		for (Project g : projects) {
			output.add(g);
		}
		return output;
	}

	/**
	 * an observable list of stocks of a particular stock type, for display by ViewManager, at the current project and timeStampDisplayCursor.
	 * timeStampDisplayCursor, which
	 * may diverge from timeStamp, identifies the row that the user last clicked on.
	 * 
	 * @param stockType
	 *            the stockType (Productive, Sales, Consumption, Money) of this stock
	 * 
	 * @return an observableList of stocks
	 */
	public ObservableList<Stock> stocksByStockTypeObservable(String stockType) {
		stocksByStockTypeQuery.setParameter("project", Simulation.projectCurrent).setParameter("stockType", stockType).setParameter("timeStamp",
				Simulation.timeStampDisplayCursor);
		ObservableList<Stock> result = FXCollections.observableArrayList();
		for (Stock s : stocksByStockTypeQuery.getResultList()) {
			result.add(s);
		}
		return result;
	}

	/**
	 * an observable list of type UseValue for display by ViewManager, at the current project and timeStampDisplayCursor. timeStampDisplayCursor, which
	 * may diverge from timeStamp, identifies the row that the user last clicked on.
	 * 
	 * @return a list of Observable UseValues for the current project and timeStamp
	 */

	public ObservableList<UseValue> useValuesObservable() {
		useValuesAllQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp",
				Simulation.timeStampDisplayCursor);
		ObservableList<UseValue> result = FXCollections.observableArrayList();
		for (UseValue u : useValuesAllQuery.getResultList()) {
			result.add(u);
		}
		return result;
	}

	/**
	 * an observable list of type Industry for display by ViewManager, at the current project and timeStampDisplayCursor. timeStampDisplayCursor, which
	 * may diverge from timeStamp, identifies the row that the user last clicked on.
	 * 
	 * @return an ObservableList of industries
	 */
	public ObservableList<Industry> industriesObservable() {
		industriesAllQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", Simulation.timeStampDisplayCursor);
		ObservableList<Industry> result = FXCollections.observableArrayList();
		for (Industry c : industriesAllQuery.getResultList()) {
			result.add(c);
		}
		return result;
	}

	/**
	 * an observable list of type SocialClass for display by ViewManager, at the current project and timeStampDisplayCursor. timeStampDisplayCursor, which
	 * may diverge from timeStamp, identifies the row that the user last clicked on.
	 * 
	 * @return an ObservableList of SocialClasses
	 */
	public ObservableList<SocialClass> socialClassesObservable() {
		socialClassAllQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", Simulation.timeStampDisplayCursor);
		ObservableList<SocialClass> result = FXCollections.observableArrayList();
		for (SocialClass s : socialClassAllQuery.getResultList()) {
			result.add(s);
		}
		return result;
	}
}
