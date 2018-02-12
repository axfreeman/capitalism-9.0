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
import rd.dev.simulation.Simulation;
import rd.dev.simulation.custom.ActionButtonsBox;
import rd.dev.simulation.model.Circuit;
import rd.dev.simulation.model.Global;
import rd.dev.simulation.model.Project;
import rd.dev.simulation.model.SocialClass;
import rd.dev.simulation.model.Stock;
import rd.dev.simulation.model.TimeStamp;
import rd.dev.simulation.model.UseValue;
import rd.dev.simulation.utils.Dialogues;
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

	// TimeStamp queries
	protected static TypedQuery<TimeStamp> timeStampsAllByProjectQuery;
	protected static TypedQuery<TimeStamp> timeStampStatesQuery;
	protected static TypedQuery<TimeStamp> timeStampByPrimarykeyQuery;
	protected static TypedQuery<TimeStamp> timeStampSuperStatesQuery;
	protected static TypedQuery<TimeStamp> timeStampsAllQuery;

	// Project queries
	protected static TypedQuery<Project> projectByPrimaryKeyQuery;
	protected static TypedQuery<Project> projectAllQuery;

	// Global queries
	protected static TypedQuery<Global> globalQuery;

	// Stock queries
	protected static TypedQuery<Stock> stockByPrimaryKeyQuery;
	protected static TypedQuery<Stock> stocksAllQuery;
	protected static TypedQuery<Stock> stocksByUseValueQuery;
	protected static TypedQuery<Stock> stocksByUseValueAndTypeQuery;
	protected static TypedQuery<Stock> stocksByOwnerAndTypeQuery;
	protected static TypedQuery<Stock> stocksByOneOfTwoStockTypesQuery;
	protected static TypedQuery<Stock> stocksByStockTypeQuery;

	// Use value queries
	protected static TypedQuery<UseValue> useValueByPrimaryKeyQuery;
	protected static TypedQuery<UseValue> useValuesAllQuery;
	protected static TypedQuery<UseValue> useValuesProductiveQuery;
	protected static TypedQuery<UseValue> useValuesByTypeQuery;
	protected static TypedQuery<UseValue> useValuesByCircuitTypeQuery;

	// Circuit queries
	protected static TypedQuery<Circuit> circuitPrimaryQuery;
	protected static TypedQuery<Circuit> circuitAllQuery;
	protected static TypedQuery<Circuit> circuitInitialCapitalQuery;

	// Social class queries
	protected static TypedQuery<SocialClass> socialClassByPrimaryKeyQuery;
	protected static TypedQuery<SocialClass> socialClassAllQuery;

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

		// Timestamp queries
		timeStampsAllByProjectQuery = timeStampEntityManager.createNamedQuery("timeStamp.project", TimeStamp.class);
		timeStampSuperStatesQuery = timeStampEntityManager.createNamedQuery("superStates", TimeStamp.class);
		timeStampsAllQuery = timeStampEntityManager.createNamedQuery("timeStamp.project", TimeStamp.class);
		timeStampByPrimarykeyQuery = timeStampEntityManager.createNamedQuery("timeStamp.project.timeStamp", TimeStamp.class);

		// Project queries
		projectAllQuery = projectEntityManager.createNamedQuery("Project.findAll", Project.class);
		projectByPrimaryKeyQuery = projectEntityManager.createNamedQuery("Project.findOne", Project.class);

		// Global queries
		globalQuery = globalEntityManager.createNamedQuery("globals.project.timeStamp", Global.class);

		// Stock queries
		stockByPrimaryKeyQuery = stocksEntityManager.createNamedQuery("Primary", Stock.class);
		stocksAllQuery = stocksEntityManager.createNamedQuery("All", Stock.class);
		stocksByStockTypeQuery = stocksEntityManager.createNamedQuery("StockType", Stock.class);
		stocksByOwnerAndTypeQuery = stocksEntityManager.createNamedQuery("Owner.StockType", Stock.class);
		stocksByUseValueQuery = stocksEntityManager.createNamedQuery("UseValue", Stock.class);
		stocksByUseValueAndTypeQuery = stocksEntityManager.createNamedQuery("UseValue.StockType", Stock.class);
		stocksByOneOfTwoStockTypesQuery = stocksEntityManager.createNamedQuery("Demand", Stock.class);

		// UseValue queries
		useValueByPrimaryKeyQuery = useValueEntityManager.createNamedQuery("Primary", UseValue.class);
		useValuesAllQuery = useValueEntityManager.createNamedQuery("All", UseValue.class);
		useValuesByTypeQuery = useValueEntityManager.createNamedQuery("UseValueType", UseValue.class);
		useValuesByCircuitTypeQuery = useValueEntityManager.createNamedQuery("UseValueCircuitType", UseValue.class);

		// Circuit queries
		circuitPrimaryQuery = circuitEntityManager.createNamedQuery("Primary", Circuit.class);
		circuitAllQuery = circuitEntityManager.createNamedQuery("All", Circuit.class);
		circuitInitialCapitalQuery = circuitEntityManager.createNamedQuery("InitialCapital", Circuit.class);
		// social class queries
		socialClassByPrimaryKeyQuery = socialClassEntityManager.createNamedQuery("Primary", SocialClass.class);
		socialClassAllQuery = socialClassEntityManager.createNamedQuery("All", SocialClass.class);
	}

	// GLOBAL QUERIES

	/**
	 * retrieve the global record for the specified timeStamp and project
	 * 
	 * @param project
	 *            the specified project
	 * @param timeStamp
	 *            the specified timeStamp
	 * @return the global record for the specified timeStamp and project
	 */
	public static Global getGlobal(int project, int timeStamp) {
		globalQuery.setParameter("project", project).setParameter("timeStamp", timeStamp);
		try {
			return globalQuery.getSingleResult();
		} catch (javax.persistence.NoResultException e) {
			return null;// because this query throws a fit if it doesn't find anything
		}
	}

	/**
	 * retrieve the global record for the specified timeStamp and the current Project
	 * 
	 * @param timeStamp
	 *            the specified timeStamp
	 * @return the global record for the current project and the current timeStamp, null if it does not exist (which is an error)
	 */
	public static Global getGlobal(int timeStamp) {
		globalQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp);
		try {
			return globalQuery.getSingleResult();
		} catch (javax.persistence.NoResultException e) {
			return null;// because this query throws a fit if it doesn't find anything
		}
	}

	/**
	 * retrieve the global record at the current timeStamp and project
	 * 
	 * @return the global record for the current project and the current timeStamp, null if it does not exist (which is an error)
	 */
	public static Global getGlobal() {
		globalQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", Simulation.timeStampIDCurrent);
		try {
			return globalQuery.getSingleResult();
		} catch (javax.persistence.NoResultException e) {
			return null;// because this query throws a fit if it doesn't find anything
		}
	}

	// STOCK QUERIES

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
		stockByPrimaryKeyQuery.setParameter("project", project).setParameter("timeStamp", timeStamp).setParameter("owner", circuit)
				.setParameter("useValue", useValue).setParameter("stockType", stockType);
		try {
			return stockByPrimaryKeyQuery.getSingleResult();
		} catch (NoResultException r) {
			return null;
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
		stockByPrimaryKeyQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp).setParameter("owner", circuit)
				.setParameter("stockType", Stock.STOCKTYPE.MONEY.text()).setParameter("useValue", "Money");
		try {
			return stockByPrimaryKeyQuery.getSingleResult();
		} catch (javax.persistence.NoResultException e) {
			return null;// because this query throws a fit if it doesn't find anything
		}
	}

	/**
	 * a list of all stocks at the current project and a given timeStamp
	 * 
	 * @param timeStamp
	 *            the given timeStamp
	 * 
	 * @return a list of stocks at the current project and the given timeStamp
	 */
	public static List<Stock> stocksAll(int timeStamp) {
		stocksAllQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp);
		return stocksAllQuery.getResultList();
	}

	/**
	 * a list of all stocks at the current project and timeStamp
	 * 
	 * @return a list of stocks at the current project and timeStamp
	 */
	public static List<Stock> stocksAll() {
		stocksAllQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", Simulation.timeStampIDCurrent);
		return stocksAllQuery.getResultList();
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
	 * @return a list of all stocks that constitute sources of demand
	 */
	public static List<Stock> stocksSourcesOfDemand() {
		stocksByOneOfTwoStockTypesQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", Simulation.timeStampIDCurrent);
		stocksByOneOfTwoStockTypesQuery.setParameter("stockType1", Stock.STOCKTYPE.PRODUCTIVE.text()).setParameter("stockType2",
				Stock.STOCKTYPE.CONSUMPTION.text());
		return stocksByOneOfTwoStockTypesQuery.getResultList();
	}

	/**
	 * a list of all the productive stocks that are managed by a given circuit, at the current project and a given timeStamp
	 *
	 * @param timeStamp
	 *            the given timeStamp
	 * @param circuit
	 *            the circuit that manages these productive stocks
	 * @return a list of the productive stocks managed by this circuit
	 */
	public static List<Stock> stocksProductiveByCircuit(int timeStamp, String circuit) {
		logger.log(Level.ALL, "  Fetching productive stocks for the circuit " + circuit);
		stocksByOwnerAndTypeQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp);
		stocksByOwnerAndTypeQuery.setParameter("owner", circuit).setParameter("stockType", Stock.STOCKTYPE.PRODUCTIVE.text());
		return stocksByOwnerAndTypeQuery.getResultList();
	}

	/**
	 * the single productive stock of a circuit defined by the name of the circuit, the use value it produces, for the current project and a given timeStamp
	 * 
	 * @param timeStamp
	 *            the given timeStamp
	 * @param circuit
	 *            the circuit to which the stock belongs
	 * @param useValue
	 *            the useValue of the stock
	 * @return the single productive stock, with the given useValue, of the named circuit
	 */
	public static Stock stockProductiveByNameSingle(int timeStamp, String circuit, String useValue) {
		stockByPrimaryKeyQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp)
				.setParameter("owner", circuit).setParameter("stockType", Stock.STOCKTYPE.PRODUCTIVE.text()).setParameter("useValue", useValue);
		try {
			return stockByPrimaryKeyQuery.getSingleResult();
		} catch (javax.persistence.NoResultException e) {
			return null;// because this query throws a fit if it doesn't find anything
		}
	}

	/**
	 * a list of the various consumer goods owned by a given social class, at the current project and a given timeStamp
	 *
	 * @param timeStamp
	 *            the given timeStamp
	 * @param socialClass
	 *            the socialClass that consumes these stocks
	 * @return a list of the consumption stocks owned by this social class
	 */
	public static List<Stock> stocksConsumptionByClass(int timeStamp, String socialClass) {
		logger.log(Level.ALL, "  Fetching productive stocks for the circuit " + socialClass);
		stocksByOwnerAndTypeQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp);
		stocksByOwnerAndTypeQuery.setParameter("owner", socialClass).setParameter("stockType", Stock.STOCKTYPE.CONSUMPTION.text());
		return stocksByOwnerAndTypeQuery.getResultList();
	}

	/**
	 * the single stock of a consumer good of the given use value owned by the given social class, at the current project and a given timeStamp
	 *
	 * @param timeStamp
	 *            the given timeStamp
	 * @param socialClass
	 *            the socialClass that consumes these stocks
	 * @param useValue
	 *            the required use value
	 * @return the single consumption stocks of the given useValue that is owned by this social class
	 */
	public static Stock stockConsumptionByClassSingle(int timeStamp, String socialClass, String useValue) {
		logger.debug("  Fetching consumption stocks for the owner {}", socialClass);
		stockByPrimaryKeyQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp);
		stockByPrimaryKeyQuery.setParameter("owner", socialClass).setParameter("stockType", Stock.STOCKTYPE.CONSUMPTION.text()).setParameter("useValue",
				useValue);
		try {
			return stockByPrimaryKeyQuery.getSingleResult();
		} catch (javax.persistence.NoResultException e) {
			return null;// because this query throws a fit if it doesn't find anything
		}
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
		stocksByUseValueAndTypeQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp).setParameter("useValue", useValue);
		stocksByUseValueAndTypeQuery.setParameter("stockType", Stock.STOCKTYPE.SALES.text());
		return stocksByUseValueAndTypeQuery.getResultList();
	}

	// USE VALUE QUERIES

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
	 * a list of all use values at the current project and timeStamp
	 * 
	 * 
	 * @return a list of all use values at the current timeStamp and the current project
	 */
	public static List<UseValue> useValuesAll() {
		useValuesAllQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", Simulation.timeStampIDCurrent);
		return useValuesAllQuery.getResultList();
	}

	/**
	 * @param useValueType
	 *            the type of the UseValue (LABOURPOWER, MONEY, PRODUCTIVE, etc)
	 * @return a list of useValues at the current project and timeStamp, of the given type.
	 * 
	 */
	public static List<UseValue> useValuesByType(UseValue.USEVALUETYPE useValueType) {
		useValuesByTypeQuery.setParameter("project", Simulation.projectCurrent);
		useValuesByTypeQuery.setParameter("timeStamp", Simulation.timeStampIDCurrent);
		useValuesByTypeQuery.setParameter("useValueType", useValueType);
		return useValuesByTypeQuery.getResultList();
	}

	/**
	 * the topmost useValue of the given type
	 * legacy method for the places where we assume a single use value of a particular type, eg Labour Power
	 * TODO phase this out
	 * 
	 * @param useValueType
	 *            the given USEVALUETYPE
	 * @return the topMost useValue of this type
	 */
	public static UseValue useValueByType(UseValue.USEVALUETYPE useValueType) {
		List<UseValue> useValues = useValuesByType(useValueType);
		return useValues.get(0);
	}

	/**
	 * a list of all use values of the given circuittype and the given timeStamp
	 * 
	 * @param useValueCircuitType
	 *            the type of the UseValue (SOCIAL, CLASS)
	 * @return a list of circuits at the latest timeStamp that has been persisted.
	 * 
	 */
	public static List<UseValue> useValuesByCircuitType(UseValue.USEVALUECIRCUITTYPE useValueCircuitType) {
		useValuesByCircuitTypeQuery.setParameter("project", Simulation.projectCurrent);
		useValuesByCircuitTypeQuery.setParameter("timeStamp", Simulation.timeStampIDCurrent);
		useValuesByCircuitTypeQuery.setParameter("useValueCircuitType", useValueCircuitType);
		return useValuesByCircuitTypeQuery.getResultList();
	}

	/**
	 * the topmost useValue of the given circuit type
	 * legacy method for the places where we assume a single use value of a particular type, eg Labour Power
	 * TODO phase this out
	 * 
	 * @param useValueCircuitType
	 *            the given USEVALUETYPE
	 * @return the topMost useValue of this type
	 */
	public static UseValue useValueByCircuitType(UseValue.USEVALUECIRCUITTYPE useValueCircuitType) {
		List<UseValue> useValues = useValuesByCircuitType(useValueCircuitType);
		return useValues.get(0);
	}

	// CIRCUIT QUERIES

	/**
	 * the circuit that produces a given usevalue, for the current project and a given timeStamp. This is also the primary key of the circuit entity
	 * 
	 * @param project
	 *            the project
	 * @param timeStamp
	 *            the timeStamp
	 * @param useValueName
	 *            the name of the usevalue that is produced by this circuit
	 * @return the circuit that produces {@code useValueName}, or null if this does not exist
	 */
	public static Circuit circuitByPrimaryKey(int project, int timeStamp, String useValueName) {
		circuitPrimaryQuery.setParameter("project", project).setParameter("timeStamp", timeStamp).setParameter("productUseValueName", useValueName);
		try {
			return circuitPrimaryQuery.getSingleResult();
		} catch (javax.persistence.NoResultException n) {
			return null;// getSingleResult does not return null if it fails; instead, it throws a fit
		}
	}

	/**
	 * a list of circuits, for the current project and timeStamp
	 * 
	 * @return a list of circuits for the current project at the latest timeStamp that has been persisted.
	 */
	public static List<Circuit> circuitsAll() {
		circuitAllQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", Simulation.timeStampIDCurrent);
		return circuitAllQuery.getResultList();
	}

	/**
	 * a list of circuits, for the current project and the given timeStamp
	 * 
	 * @param timeStamp
	 *            the given timeStamp
	 *            *
	 * @return a list of circuits for the current project at the specified timeStamp (which should, in general, be different from the currentTimeStamp)
	 */

	public static List<Circuit> circuitsAll(int timeStamp) {
		circuitAllQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp);
		return circuitAllQuery.getResultList();
	}

	/**
	 * a list of circuits, for the current project and the current timeStamp, that produce a given use value
	 * 
	 * 
	 * @param useValueName
	 *            the name of the use value that these circuits produce
	 * @return a list of circuits which produce the given use value at the latest timeStamp that has been persisted.
	 */

	public static List<Circuit> circuitsByProductUseValue(String useValueName) {
		circuitPrimaryQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", Simulation.timeStampIDCurrent)
				.setParameter("productUseValueName", useValueName);
		return circuitPrimaryQuery.getResultList();
	}

	/**
	 * a list of circuits, for the current project and the given timeStamp, that produce a given use value
	 * 
	 * 
	 * @param useValueName
	 *            the name of the use value that these circuits produce
	 * @param timeStamp
	 *            the given timeStamp
	 * @return a list of circuits which produce the given use value at the given timeStamp.
	 */

	public static List<Circuit> circuitsByProductUseValue(int timeStamp, String useValueName) {
		circuitPrimaryQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp)
				.setParameter("productUseValueName", useValueName);
		return circuitPrimaryQuery.getResultList();
	}

	/**
	 * retrieve the topmost circuit that produces a named use value for the current project and at a given timeStamp.
	 * TODO Note that in general, we must allow for a named use value to be produced by more than one circuit.
	 * This method should therefore be phased out.
	 * 
	 * @param useValueName
	 *            the produce that is made by the circuit
	 * @return the single circuit that produces this product, at the currently-selected timeStamp
	 */
	public static Circuit circuitByProductUseValue(String useValueName) {
		List<Circuit> circuits = circuitsByProductUseValue(useValueName);
		return circuits.get(0);
	}

	/**
	 * TODO get this working
	 * 
	 * @param timeStamp
	 *            the timeStamp
	 * 
	 * @return the sum of the initial capital in all circuits
	 */

	public static double circuitsInitialCapital(int timeStamp) {
		circuitInitialCapitalQuery.setParameter("timeStamp", timeStamp).setParameter("project", Simulation.projectCurrent);
		Object o = circuitInitialCapitalQuery.getSingleResult();
		double result = (double) o;
		return result;
	}

	// SOCIAL CLASS QUERIES

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

	/**
	 * a list of social classes, for the current project and timeStamp
	 * 
	 * @return a list of all social classes for the current project and timeStamp
	 */

	public static List<SocialClass> socialClassesAll() {
		socialClassAllQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", Simulation.timeStampIDCurrent);
		return socialClassAllQuery.getResultList();
	}

	/**
	 * a named social class for the current project and a given timeStamp.
	 * 
	 * @param socialClassName
	 *            the name of the social Class
	 * @return the single social class with the name socialClassName, for the given project and timeStamp
	 */

	public static SocialClass socialClassByName(String socialClassName) {
		socialClassByPrimaryKeyQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", Simulation.timeStampIDCurrent)
				.setParameter("socialClassName", socialClassName);
		try {
			return socialClassByPrimaryKeyQuery.getSingleResult();
		} catch (NoResultException r) {
			return null;
		}
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
			logger.debug("The user switched to project {} which  is already current. No action was taken", newProjectID);
			return;
		}
		Project newProject = SelectionsProvider.projectSingle(newProjectID);
		if ((newProject.getPriceDynamics() == Project.PRICEDYNAMICS.DYNAMIC) || (newProject.getPriceDynamics() == Project.PRICEDYNAMICS.EQUALISE)) {
			Dialogues.alert(logger, "Sorry, the Dynamic and Equalise options for price dynamics are not ready yet");
			return;
		}

		// record the current timeStamp, timeStampDisplayCursor and buttonState in the current project record, and persist it to the database

		Project thisProject = SelectionsProvider.projectSingle(Simulation.projectCurrent);

		projectEntityManager.getTransaction().begin();

		thisProject.setTimeStamp(Simulation.timeStampIDCurrent);
		thisProject.setTimeStampDisplayCursor(Simulation.timeStampDisplayCursor);
		thisProject.setTimeStampComparatorCursor(Simulation.getTimeStampComparatorCursor());
		thisProject.setButtonState(actionButtonsBox.getLastAction().getText());
		thisProject.setPeriod(Simulation.getPeriodCurrent());

		projectEntityManager.getTransaction().commit();

		// retrieve the selected project record, and copy its various cursors and into the simulation cursors

		Simulation.timeStampIDCurrent = newProject.getTimeStamp();
		Simulation.timeStampDisplayCursor = newProject.getTimeStampDisplayCursor();
		Simulation.setTimeStampComparatorCursor(newProject.getTimeStampComparatorCursor());
		Simulation.setPeriodCurrent(newProject.getPeriod());
		actionButtonsBox.setActionStateFromLabel(newProject.getButtonState());
		Simulation.projectCurrent = newProjectID;
		Reporter.report(logger, 0, "SWITCHED TO PROJECT %s (%s)", newProjectID, newProject.getDescription());
		// ViewManager.getTabbedTableViewer().buildTables();
	}

	/**
	 * for all persistent entities at the given timeStamp, set comparators that refer to the timeStampComparatorCursor
	 * TODO previousComparator not yet properly implemented.
	 * 
	 * @param timeStampID
	 *            all persistent records at this timeStampID will be given comparators equal to the timeStampComparatorCursor
	 */

	public static void setComparators(int timeStampID) {
		try {
			for (Stock s : stocksAll(timeStampID)) {
				s.setPreviousComparator(stockByPrimaryKey(Simulation.projectCurrent, Simulation.getTimeStampComparatorCursor(), s.getOwner(),
						s.getUseValueName(), s.getStockType()));
				s.setStartComparator(stockByPrimaryKey(Simulation.projectCurrent, 1, s.getOwner(), s.getUseValueName(), s.getStockType()));
				s.setEndComparator(
						stockByPrimaryKey(Simulation.projectCurrent, Simulation.timeStampIDCurrent, s.getOwner(), s.getUseValueName(), s.getStockType()));
				s.setCustomComparator(
						stockByPrimaryKey(Simulation.projectCurrent, Simulation.timeStampIDCurrent, s.getOwner(), s.getUseValueName(), s.getStockType()));
			}
			useValuesAllQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStampID);
			for (UseValue u : useValuesAllQuery.getResultList()) {
				u.setPreviousComparator(useValueByPrimaryKey(Simulation.projectCurrent, Simulation.getTimeStampComparatorCursor(), u.getUseValueName()));
				u.setStartComparator(useValueByPrimaryKey(Simulation.projectCurrent, 1, u.getUseValueName()));
				u.setEndComparator(useValueByPrimaryKey(Simulation.projectCurrent, Simulation.timeStampIDCurrent, u.getUseValueName()));
				u.setCustomComparator(useValueByPrimaryKey(Simulation.projectCurrent, Simulation.timeStampIDCurrent, u.getUseValueName()));
			}
			for (Circuit c : circuitsAll(timeStampID)) {
				c.setPreviousComparator(circuitByPrimaryKey(Simulation.projectCurrent, Simulation.getTimeStampComparatorCursor(), c.getProductUseValueName()));
				c.setStartComparator(circuitByPrimaryKey(Simulation.projectCurrent, 1, c.getProductUseValueName()));
				c.setEndComparator(circuitByPrimaryKey(Simulation.projectCurrent, Simulation.timeStampIDCurrent, c.getProductUseValueName()));
				c.setCustomComparator(circuitByPrimaryKey(Simulation.projectCurrent, Simulation.timeStampIDCurrent, c.getProductUseValueName()));
			}
			socialClassAllQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStampID);
			for (SocialClass sc : socialClassAllQuery.getResultList()) {
				sc.setPreviousComparator(
						socialClassByPrimaryKey(Simulation.projectCurrent, Simulation.getTimeStampComparatorCursor(), sc.getSocialClassName()));
				sc.setStartComparator(socialClassByPrimaryKey(Simulation.projectCurrent, 1, sc.getSocialClassName()));
				sc.setEndComparator(socialClassByPrimaryKey(Simulation.projectCurrent, Simulation.timeStampIDCurrent, sc.getSocialClassName()));
				sc.setCustomComparator(socialClassByPrimaryKey(Simulation.projectCurrent, Simulation.timeStampIDCurrent, sc.getSocialClassName()));
			}

			globalQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStampID);
			Global currentGlobal = globalQuery.getSingleResult();
			currentGlobal.setPreviousComparator(getGlobal(Simulation.getTimeStampComparatorCursor()));
			currentGlobal.setStartComparator(getGlobal(1));
			currentGlobal.setPreviousComparator(getGlobal(Simulation.timeStampIDCurrent));
			currentGlobal.setCustomComparator(getGlobal(Simulation.timeStampIDCurrent));

		} catch (Exception e) {
			Dialogues.alert(logger, "Database fubar. Sorry, please contact developer");
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

	// named query getters

	/**
	 * @return the socialClassByPrimaryKeyQuery
	 */
	public static TypedQuery<Stock> getStockByPrimaryKeyQuery() {
		return stockByPrimaryKeyQuery;
	}
}
