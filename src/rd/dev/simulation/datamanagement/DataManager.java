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
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rd.dev.simulation.Capitalism;
import rd.dev.simulation.Simulation;
import rd.dev.simulation.custom.ActionButtonsBox;
import rd.dev.simulation.model.Circuit;
import rd.dev.simulation.model.Global;
import rd.dev.simulation.model.Project;
import rd.dev.simulation.model.SocialClass;
import rd.dev.simulation.model.Stock;
import rd.dev.simulation.model.TimeStamp;
import rd.dev.simulation.model.UseValue;
import rd.dev.simulation.utils.Reporter;

// TODO the namespace has become somewhat chaotic. Rename consistently, especially with regard to the primary key queries

public class DataManager {

	private static final Logger logger = LogManager.getLogger(DataManager.class);

	// The Entity ManagerFactories
	private EntityManagerFactory timeStampsEntityManagerFactory = Persistence.createEntityManagerFactory("DB_TIMESTAMP");
	private EntityManagerFactory projectEntityManagerFactory = Persistence.createEntityManagerFactory("DB_PROJECT");
	private EntityManagerFactory useValuesEntityManagerFactory = Persistence.createEntityManagerFactory("DB_USEVALUES");
	private EntityManagerFactory capitalCircuitsEntityManagerFactory = Persistence.createEntityManagerFactory("DB_CAPITALCIRCUITS");
	private EntityManagerFactory socialClassEntityManagerFactory = Persistence.createEntityManagerFactory("DB_SOCIALCLASSES");
	private EntityManagerFactory globalsEntityManagerFactory = Persistence.createEntityManagerFactory("DB_GLOBALS");
	private EntityManagerFactory stocksEntityManagerFactory = Persistence.createEntityManagerFactory("DB_STOCKS");

	// The Entity Managers
	protected static EntityManager timeStampEntityManager;
	protected static EntityManager projectEntityManager;
	protected static EntityManager useValueEntityManager;
	protected static EntityManager circuitEntityManager;
	protected static EntityManager socialClassEntityManager;
	protected static EntityManager globalEntityManager;
	protected static EntityManager stocksEntityManager;

	// the Named Queries
	
	// TimeStamp and Project queries
	protected static TypedQuery<Project> projectAllQuery;
	protected static TypedQuery<TimeStamp> timeStampsAllByProjectQuery;
	protected static TypedQuery<TimeStamp> timeStampStatesQuery;
	
	// select single entities by primary key
	protected static TypedQuery<Stock> stockByPrimaryKeyQuery;
	protected static TypedQuery<TimeStamp> timeStampByPrimarykeyQuery;
	protected static TypedQuery<Project> projectByPrimaryKeyQuery;
	protected static TypedQuery<UseValue> useValueByPrimaryKeyQuery;
	protected static TypedQuery<Circuit> circuitByPrimaryKeyQuery;
	protected static TypedQuery<SocialClass> socialClassByPrimaryKeyQuery;
	
	// select a list of current entities
	protected static TypedQuery<Stock> stocksBasicQuery;
	protected static TypedQuery<UseValue> useValueBasicQuery;
	protected static TypedQuery<Circuit> circuitBasicQuery;
	protected static TypedQuery<SocialClass> socialClassBasicQuery;
	protected static TypedQuery<Global> globalBasicQuery;
	
	// specific stock queries
	protected static TypedQuery<Stock> stocksByUseValueQuery;
	protected static TypedQuery<Stock> stocksSalesQuery;
	protected static TypedQuery<Stock> stocksProductiveByCircuitQuery;
	protected static TypedQuery<Stock> stocksSourcesOfDemandQuery;
	protected static TypedQuery<Stock> stocksByStockTypeQuery;
	
	// specific queries for other types
	protected static TypedQuery<UseValue> useValuesProductiveQuery;
	protected static TypedQuery<TimeStamp> timeStampSuperStatesQuery;
	protected static TypedQuery<TimeStamp> timeStampsAllQuery;

	public DataManager() {
			}
	
	/**
	 * startup
	 * initialises all the entityManagers and queries, once for all
	 */
	public void startup() {
		logger.log(Level.getLevel("OVERVIEW"), "");
		logger.log(Level.getLevel("OVERVIEW"), "DATA MANAGER STARTUP");

		// create the entityManagers, one for each persistent data type

		timeStampEntityManager = timeStampsEntityManagerFactory.createEntityManager();
		projectEntityManager = projectEntityManagerFactory.createEntityManager();
		stocksEntityManager = stocksEntityManagerFactory.createEntityManager();
		useValueEntityManager = useValuesEntityManagerFactory.createEntityManager();
		circuitEntityManager = capitalCircuitsEntityManagerFactory.createEntityManager();
		socialClassEntityManager = socialClassEntityManagerFactory.createEntityManager();
		globalEntityManager = globalsEntityManagerFactory.createEntityManager();

		// create named queries which are used to retrieve and update the persistent data types.

		// timeStamp and Project queries
		projectAllQuery = projectEntityManager.createNamedQuery("Project.findAll", Project.class);
		timeStampsAllByProjectQuery = timeStampEntityManager.createNamedQuery("timeStamp.project", TimeStamp.class);
//		timeStampStatesQuery=timeStampEntityManager.createNamedQuery("project.state", TimeStamp.class);
		
		timeStampSuperStatesQuery = timeStampEntityManager.createNamedQuery("superStates", TimeStamp.class);
		timeStampsAllQuery = timeStampEntityManager.createNamedQuery("timeStamp.project", TimeStamp.class);

		// queries that return a single entity identified by the primary key
		stockByPrimaryKeyQuery = stocksEntityManager.createNamedQuery("Stocks.Primary", Stock.class);
		useValueByPrimaryKeyQuery = useValueEntityManager.createNamedQuery("UseValues.Primary", UseValue.class);
		circuitByPrimaryKeyQuery = circuitEntityManager.createNamedQuery("Circuits.project.PrimaryKey", Circuit.class);
		socialClassByPrimaryKeyQuery = socialClassEntityManager.createNamedQuery("SocialClass.PrimaryKey", SocialClass.class);
		projectByPrimaryKeyQuery = projectEntityManager.createNamedQuery("Project.findOne", Project.class);
		timeStampByPrimarykeyQuery = timeStampEntityManager.createNamedQuery("timeStamp.project.timeStamp", TimeStamp.class);

		// queries that require only the project and timeStamp
		stocksBasicQuery = stocksEntityManager.createNamedQuery("Stocks.basic", Stock.class);
		useValueBasicQuery = useValueEntityManager.createNamedQuery("UseValues.project.timeStamp", UseValue.class);
		circuitBasicQuery = circuitEntityManager.createNamedQuery("Circuits.project.timeStamp", Circuit.class);
		socialClassBasicQuery = socialClassEntityManager.createNamedQuery("SocialClass.project.timeStamp", SocialClass.class);
		globalBasicQuery = globalEntityManager.createNamedQuery("globals.project.timeStamp", Global.class);

		// other, specific-purpose queries
		stocksByUseValueQuery = stocksEntityManager.createNamedQuery("Stocks.project.timeStamp.useValue", Stock.class);
		stocksSalesQuery = stocksEntityManager.createNamedQuery("Stocks.sales", Stock.class);
		stocksSourcesOfDemandQuery = stocksEntityManager.createNamedQuery("Stocks.project.timeStamp.sourcesOfDemand", Stock.class);
		stocksProductiveByCircuitQuery = stocksEntityManager.createNamedQuery("Stocks.project.timeStamp.circuit.productive", Stock.class);
		useValuesProductiveQuery = useValueEntityManager.createNamedQuery("UseValues.productive", UseValue.class);

		// Used in ObservableListProvider alone
		stocksByStockTypeQuery = stocksEntityManager.createNamedQuery("Stocks.project.timeStamp.stockType", Stock.class);
	}
	// QUERIES RETURNING SINGLE RESULTS THAT DEPEND ON THE PROJECT AS WELL AS THE TIMESTAMP

	/**
	 * get the single stock with the primary key given by all the parameters
	 * 
	 * @param project
	 *            the given project
	 * @param timeStamp
	 *            the given timeStamp
	 * @param circuit
	 *            the name of the owning circuit, as a String
	 * @param useValue
	 *            the name of the use value of this stock, as a String
	 * @param stockType
	 *            the type of this stock (money, productive, sales, consumption) as a String
	 * @return the single stock defined by this primary key, null if it does not exist
	 */
	public static Stock stockByPrimaryKey(int project, int timeStamp, String circuit, String useValue, String stockType) {
		stockByPrimaryKeyQuery.setParameter("project", project).setParameter("timeStamp", timeStamp).setParameter("circuit", circuit)
				.setParameter("useValue", useValue).setParameter("stockType", stockType);
		try {
			return stockByPrimaryKeyQuery.getSingleResult();
		} catch (NoResultException r) {
			return null;
		}
	}

	/**
	 * get the single usevalue with the primary key given by all the parameters, including the timeStamp
	 * 
	 * @param project
	 *            the given project
	 * @param timeStamp
	 *            the timeStamp to report on
	 * @param useValueName
	 *            the given UseValueName
	 * @return the single UseValue given by this primary key, null if it does not exist
	 */
	public static UseValue useValueByPrimaryKey(int project, int timeStamp, String useValueName) {
		useValueByPrimaryKeyQuery.setParameter("project", project).setParameter("timeStamp", timeStamp).setParameter("useValueName", useValueName);
		try {
			return useValueByPrimaryKeyQuery.getSingleResult();
		} catch (NoResultException r) {
			return null;
		}
	}

	/**
	 * the circuit that produces a given usevalue, for the current project and a given timeStamp (that may differ from the current timeStamp)
	 * 
	 * @param project
	 *            the project
	 * @param timeStamp
	 *            the timestamp
	 * @param useValueName
	 *            the name of the usevalue that is produced by this circuit
	 * @return the circuit that produces {@code useValueName}, or null if this does not exist
	 */
	public static Circuit circuitByPrimaryKey(int project, int timeStamp, String useValueName) {
		circuitByPrimaryKeyQuery.setParameter("project", project).setParameter("timeStamp", timeStamp).setParameter("type", useValueName);
		try {
			return circuitByPrimaryKeyQuery.getSingleResult();
		} catch (javax.persistence.NoResultException n) {
			return null;// getSingleResult does not return null if it fails; instead, it throws a fit
		}
	}

	/**
	 * Get a single social class defined by its primary key, including a timeStamp that may differ from the current timeStamp
	 * 
	 * @param socialClassName
	 *            the name of the social Class in the primary key
	 * @param project
	 *            the project in the primary key
	 * @param timeStamp
	 *            the timeStamp in the primary key
	 * @return the single social class with the name socialClassName, for the given project and timeStamp
	 */

	public static SocialClass socialClassByPrimaryKey(int project, int timeStamp, String socialClassName) {
		socialClassByPrimaryKeyQuery.setParameter("project", project).setParameter("timeStamp", timeStamp).setParameter("socialClassName", socialClassName);
		try {
			return socialClassByPrimaryKeyQuery.getSingleResult();
		} catch (javax.persistence.NoResultException e) {
			return null;// because this query throws a fit if it doesn't find anything
		}
	}

	// QUERIES THAT RETURN A SINGLE RESULT APPLYING ONLY TO THE CURRENT PROJECT

	/**
	 * retrieve the global record at the given timeStamp and return it to the caller
	 * 
	 * @return the global record for the current project and the given timeStamp, null if it does not exist (which is an error)
	 * @param timeStamp
	 *            the timeStamp for which this global is required
	 */
	public static Global getGlobal(int timeStamp) {
		globalBasicQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp);
		try {
			return globalBasicQuery.getSingleResult();
		} catch (javax.persistence.NoResultException e) {
			return null;// because this query throws a fit if it doesn't find anything
		}
	}

	/**
	 * the single sales stock of a circuit defined by the name of the circuit and the use value it produces, for the current project and a given timeStamp
	 * 
	 * @param timeStamp
	 *            the given timeStammp
	 * @param circuit
	 *            the circuit to which the stock belongs
	 * @param useValue
	 *            the useValue produced by this circuit
	 * @return the single sales stock of the named circuit
	 */
	public static Stock stockSalesByCircuitSingle(int timeStamp, String circuit, String useValue) {
		stockByPrimaryKeyQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp)
				.setParameter("circuit", circuit).setParameter("stockType", "Sales").setParameter("useValue", useValue);
		try {
			return stockByPrimaryKeyQuery.getSingleResult();
		} catch (javax.persistence.NoResultException e) {
			return null;// because this query throws a fit if it doesn't find anything
		}
	}
	
	
	/**
	 * the single productive stock of a circuit defined by the name of the circuit, the use value it produces, for the current project and a given timeStamp
	 * 
	 * @param timeStamp
	 *            the given timeStammp
	 * @param circuit
	 *            the circuit to which the stock belongs
	 * @param useValue
	 *            the useValue of the stock
	 * @return the single productive stock, with the given useValue, of the named circuit
	 */
	public static Stock stockProductiveByNameSingle(int timeStamp, String circuit, String useValue) {
		stockByPrimaryKeyQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp)
				.setParameter("circuit", circuit).setParameter("stockType", "Productive").setParameter("useValue", useValue);
		try {
			return stockByPrimaryKeyQuery.getSingleResult();
		} catch (javax.persistence.NoResultException e) {
			return null;// because this query throws a fit if it doesn't find anything
		}
	}
	
	/**
	 * the money stock of a Circuit defined by the name of the circuit and the use value it produces, for the current project and a given timeStamp
	 * 
	 * @param timeStamp
	 *            the given timeStammp
	 * @param circuit
	 *            the Circuit to which the stock belongs
	 * 
	 * @return the single stock of money owned by the circuit
	 */
	public static Stock stockMoneyByCircuitSingle(int timeStamp, String circuit) {
		stockByPrimaryKeyQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp).setParameter("circuit", circuit)
				.setParameter("stockType", "Money").setParameter("useValue", "Money");
		try {
			return stockByPrimaryKeyQuery.getSingleResult();
		} catch (javax.persistence.NoResultException e) {
			return null;// because this query throws a fit if it doesn't find anything
		}
	}

	/**
	 * the single consumption stock that is owned by a given social class, for the current project and a given timeStamp
	 * 
	 * @param timeStamp
	 *            the given timeStamp
	 * @param socialClassName
	 *            the name of the class that owns this consumption stock
	 * @return the single consumption stock owned by this class
	 */
	public static Stock stockConsumptionByCircuitSingle(int timeStamp, String socialClassName) {
		logger.log(Level.ALL, "  Fetching consumption stocks for the social class " + socialClassName);
		stockByPrimaryKeyQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp).setParameter("circuit",
				socialClassName).setParameter("stockType", "Consumption").setParameter("useValue", "Consumption");
		try {
			return stockByPrimaryKeyQuery.getSingleResult();
		} catch (javax.persistence.NoResultException e) {
			return null;// because this query throws a fit if it doesn't find anything
		}
	}
	
	public static List<Stock> productiveStocks(){
		//TODO this assumes no additional inputs are added as the simulation progresses (uses timeStamp 1)
		return circuitByProductType(1, "Consumption").productiveStocks();
	}
	
	/**
	 * retrieve a UseValue by its name for the current project and a given timestamp
	 * 
	 * @param timeStamp
	 *            the given timeStamp
	 * 
	 * @param useValueName
	 *            the name of the use value
	 * @return the useValue entity whose name is useValueName, unless it doesn't exist, in which case null
	 */
	public static UseValue useValueByName(int timeStamp, String useValueName) {
		useValueByPrimaryKeyQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp).setParameter("useValueName",
				useValueName);
		try {
			return useValueByPrimaryKeyQuery.getSingleResult();
		} catch (NoResultException r) {
			return null;
		}
	}

	/**
	 * the useValue of Consumption goods for the current project and a given timeStamp
	 * 
	 * @param timeStamp
	 *            the given timeStamp
	 * @return the unique use Value of consumption goods or null if it doesn't exist
	 */
	public static UseValue useValueOfConsumptionGoods(int timeStamp) {
		useValueByPrimaryKeyQuery.setParameter("timeStamp", timeStamp).setParameter("project", Simulation.projectCurrent)
				.setParameter("useValueName", "Consumption");
		try {
			return useValueByPrimaryKeyQuery.getSingleResult();
		} catch (NoResultException r) {
			return null;
		}
	}

	/**
	 * retrieve the single circuit that produces a named use value for the current project and at a given timeStamp.
	 * TODO Note that in general, we must allow for a named use value to be produced by more than one circuit
	 * 
	 * @param timeStamp
	 *            the given timeStamp
	 * 
	 * @param useValueName
	 *            the produce that is made by the circuit
	 * @return the single circuit that produces this product, at the currently-selected timeStamp
	 */
	public static Circuit circuitByProductType(int timeStamp, String useValueName) {
		circuitByPrimaryKeyQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp).setParameter("type",
				useValueName);
		try {
			return circuitByPrimaryKeyQuery.getSingleResult();
		} catch (javax.persistence.NoResultException n) {
			return null;// getSingleResult does not return null if it fails; instead, it throws a fit
		}
	}

	/**
	 * a named social class for the current project and a given timeStamp.
	 * 
	 * @param timeStamp
	 *            the given timeStamp
	 * 
	 * @param socialClassName
	 *            the name of the social Class
	 * @return the single social class with the name socialClassName, for the given project and timeStamp
	 */
	public static SocialClass socialClassByName(int timeStamp, String socialClassName) {
		socialClassByPrimaryKeyQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp)
				.setParameter("socialClassName", socialClassName);
		try {
			return socialClassByPrimaryKeyQuery.getSingleResult();
		} catch (NoResultException r) {
			return null;
		}
	}

	// QUERIES RETURNING A LIST THAT DEPENDS ON THE TIMESTAMP THOUGH APPLYING ONLY TO THE CURRENT PROJECT

	/**
	 * a list of all stocks at the current project and a given timeStamp
	 * 
	 * @param timeStamp
	 *            the given timeStamp
	 * 
	 * @return a list of stocks at the current project and timeStamp
	 */
	public static List<Stock> stocksAll(int timeStamp) {
		stocksBasicQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp);
		return stocksBasicQuery.getResultList();
	}

	/**
	 * a list of all stocks for the given usevalue at the current project and a given timestamp.
	 * 
	 * @param timeStamp
	 *            the given timeStamp
	 * @param useValueName
	 *            the use value of the stocks
	 * @return a list of stocks for the given use value at the currently selected time and for the currently selected project
	 */
	public static List<Stock> stocksByUseValue(int timeStamp, String useValueName) {
		stocksByUseValueQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp).setParameter("useValue",
				useValueName);
		return stocksByUseValueQuery.getResultList();
	}

	/**
	 * a list of all stocks that constitute sources of demand (productive and consumption but not money or sales), for the current project and a given timeStamp
	 * 
	 * @param timeStamp
	 *            the given timeStamp
	 * 
	 * @return a list of all stocks that constitute sources of demand
	 */
	public static List<Stock> stocksSourcesOfDemand(int timeStamp) {
		stocksSourcesOfDemandQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp);
		return stocksSourcesOfDemandQuery.getResultList();
	}

	/**
	 * a list of all the productive stocks that are managed by a given circuit, at the current project and a given timeStamp
	 *
	 * @param timeStamp
	 *            the given timeStamp
	 * 
	 * @param circuit
	 *            the circuit that manages these productive stocks
	 * @return a list of the productive stocks managed by this circuit
	 */
	public static List<Stock> stocksProductiveByCircuit(int timeStamp, String circuit) {
		logger.log(Level.ALL, "  Fetching productive stocks for the circuit " + circuit);
		stocksProductiveByCircuitQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp).setParameter("circuit",
				circuit);
		return stocksProductiveByCircuitQuery.getResultList();
	}

	/**
	 * a list of sales Stock of a given use value for the current project and a given timeStamp.
	 * NOTE only the circuit will vary, and at present only one of these circuits will produce this use value. However in general more than one circuit may
	 * produce it so we yield a list here.
	 * 
	 * @param timeStamp
	 *            the given timeStamp
	 * 
	 * @param useValue
	 *            the use value that the sales stocks contain
	 * @return a list of the sales stocks that contain the given use value
	 *         Note: there can be more than one seller of the same use value
	 */
	public static List<Stock> stocksSalesByUseValue(int timeStamp, String useValue) {
		stocksSalesQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp).setParameter("useValue", useValue);
		return stocksSalesQuery.getResultList();
	}

	/**
	 * a list of all use values at the current project and for a given timeStamp
	 * 
	 * @param timeStamp
	 *            the given timeStamp
	 * 
	 * @return a list of all use values at the current timeStamp and the current project
	 */
	public static List<UseValue> useValuesAll(int timeStamp) {
		useValueBasicQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp);
		return useValueBasicQuery.getResultList();
	}

	/**
	 * a list of all productive Use Values (the named query sets useValueCircuitType to 'Capitalist') for the current project and the given timeStamp
	 * 
	 * @param timeStamp
	 *            the given timeStamp
	 * 
	 * @return a list of productive Use Values
	 */
	public static List<UseValue> useValuesProductive(int timeStamp) {
		useValuesProductiveQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp);
		return useValuesProductiveQuery.getResultList();
	}

	/**
	 * a list of circuits, for the current project and the given timeStamp
	 * 
	 * @param timeStamp
	 *            the given timeStamp
	 * 
	 * @return a list of circuits at the latest timeStamp that has been persisted.
	 */
	public static List<Circuit> circuitsAll(int timeStamp) {
		circuitBasicQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp);
		return circuitBasicQuery.getResultList();
	}

	/**
	 * a list of social classes, for the current project and a given timeStamp
	 * 
	 * @param timeStamp
	 *            the given timeStamp
	 * 
	 * @return a list of all social classes for the current project and timeStamp
	 */
	public static List<SocialClass> socialClassesAll(int timeStamp) {
		socialClassBasicQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp);
		return socialClassBasicQuery.getResultList();
	}

	/**
	 * Switch from one project to another.
	 * <p>
	 * (1)copy the current timeStamp and timeStampDisplayCursor into the current Project record
	 * <p>
	 * (2)retrieve the timeStamp and timeStampDisplayCursor from the new Project
	 * <p>
	 * (4)save the current Project record to the database
	 * <p>
	 * (3)set 'currentProject' to be the new project
	 * <p>
	 * (4)the calling method must refresh the display
	 * 
	 * @param newProjectID
	 *            the ID of the project to switch to
	 * @param actionButtonsBox
	 *            the actionButtonsBox which has invoked the switch (and which knows the buttonState of the current project)
	 */
	public static void switchProjects(int newProjectID, ActionButtonsBox actionButtonsBox) {
		if (newProjectID == Simulation.projectCurrent) {
			logger.error("The programme attempted to switch to project {} which  is already current", newProjectID);
			return;
		}

		// record the current timeStamp, timeStampDisplayCursor and buttonState in the current project record, and persist it to the database

		Project thisProject = Capitalism.selectionsProvider.projectSingle(Simulation.projectCurrent);
		projectEntityManager.getTransaction().begin();

		thisProject.setTimeStamp(Simulation.timeStampIDCurrent);
		thisProject.setTimeStampDisplayCursor(Simulation.timeStampDisplayCursor);
		thisProject.setTimeStampComparatorCursor(Simulation.getTimeStampComparatorCursor());
		thisProject.setButtonState(actionButtonsBox.getLastAction().getText());

		projectEntityManager.getTransaction().commit();

		// retrieve the selected project record, and copy its timeStamp and its timeStampDisplayCursor into the simulation timeStamp and timeStampDisplayCursor

		Project newProject = Capitalism.selectionsProvider.projectSingle(newProjectID);
		Simulation.timeStampIDCurrent=newProject.getTimeStamp();
		Simulation.timeStampDisplayCursor=newProject.getTimeStampDisplayCursor();
		Simulation.setTimeStampComparatorCursor(newProject.getTimeStampComparatorCursor());
		actionButtonsBox.setActionStateFromLabel(newProject.getButtonState());
		Simulation.projectCurrent=newProjectID;
		Reporter.report(logger, 0, "SWITCH TO PROJECT %s (%s)", newProjectID,newProject.getDescription());
	}
	
	/**
	 * for all persistent entities at the given timeStamp, set comparators that refer to the timeStampComparatorCursor
	 * 
	 * @param timeStampID all persistent records at this timeStampID will be given comparators equal to the timeStampComparatorCursor
	 */

	public static void setComparators(int timeStampID) {
		for (Stock s : stocksAll(timeStampID)) {
			s.setComparator();
		}
		for (UseValue u : useValuesAll(timeStampID)) {
			u.setComparator();
		}
		for (Circuit c : circuitsAll(timeStampID)) {
			c.setComparator();
		}
		for (SocialClass sc : socialClassesAll(timeStampID)) {
			sc.setComparator();
		}
	}


	// NON-QUERY GETTERS AND SETTERS

	public static EntityManager getTimeStampEntityManager() {
		return timeStampEntityManager;
	}

	public static EntityManager getProjectEntityManager() {
		return projectEntityManager;
	}

	/**
	 * @return the globalEntityManager
	 */
	public static EntityManager getGlobalEntityManager() {
		return globalEntityManager;
	}

	/**
	 * @return the useValueEntityManager
	 */
	public static EntityManager getUseValueEntityManager() {
		return useValueEntityManager;
	}

	/**
	 * @return the circuitEntityManager
	 */
	public static EntityManager getCircuitEntityManager() {
		return circuitEntityManager;
	}

	/**
	 * @return the socialClassEntityManager
	 */
	public static EntityManager getSocialClassEntityManager() {
		return socialClassEntityManager;
	}

	/**
	 * @return the stocksEntityManager
	 */
	public static EntityManager getStocksEntityManager() {
		return stocksEntityManager;
	}
}
