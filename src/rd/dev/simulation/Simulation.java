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

package rd.dev.simulation;

import java.util.List;
import javax.persistence.PersistenceException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rd.dev.simulation.datamanagement.DataManager;
import rd.dev.simulation.datamanagement.SelectionsProvider;
import rd.dev.simulation.model.Circuit;
import rd.dev.simulation.model.Global;
import rd.dev.simulation.model.Project;
import rd.dev.simulation.model.SocialClass;
import rd.dev.simulation.model.Stock;
import rd.dev.simulation.model.TimeStamp;
import rd.dev.simulation.model.UseValue;
import rd.dev.simulation.utils.Dialogues;
import rd.dev.simulation.utils.Reporter;

public class Simulation {

	private static final Logger logger = LogManager.getLogger(Simulation.class);

	// Application-wide persistent project that defines a simulation. All other persistent entities are confined to records defined by it.

	public static int projectCurrent;

	// Application-wide persistent TimeStamp ID. This ID of the current timeStamp record in the simulation

	public static int timeStampIDCurrent;

	// Application-wide variable saying what is the latest period in the current simulation

	public static int periodCurrent;

	// Application-wide cursors. By changing these, the user views entities from earlier timestamps and compares them with those from the current timeStamp.
	// These cursors are independent of timeStampIDCurrent and operations that involve them do not affect the database, or the simulation.
	public static int timeStampDisplayCursor; 				// determines which timeStamp is displayed
	private static int timeStampComparatorCursor; 			// the timeStamp with which the displayed data is to be compared

	// the precision for decimal calculations with large amounts (that is, anything except coefficients, the melt, rate of profit, etc)

	protected static int roundingPrecision=4;
	protected static double epsilon=10^(1/roundingPrecision);

	// Determines the way that the supply of labour power responds to demand
	// a primitive response function to be expanded and hopefully user-customized
	// if FLEXIBLE, labour power will expand to meet demand (reserve army)
	// if FIXED, labour power cannot expand to meet demand and provides a supply constraint on output
	
	public static enum SupplyResponse{
		FLEXIBLE, FIXED
	}
	
	public Simulation() {
	}

	/**
	 * startup. Initialise all variables that are derived from user data but not required explicitly
	 * TODO validate user data at this point
	 */
	public void startup() {
		Reporter.report(logger, 1, "Simulation startup: initialise data from user-supplied inputs");

		TimeStamp timeStampCurrentRecord;

		timeStampIDCurrent = 1;
		timeStampDisplayCursor = 1;
		timeStampComparatorCursor = 1;
		periodCurrent = 1;

		// Initialise all projects at the start
		for (Project p : SelectionsProvider.projectsAll()) {
			Reporter.report(logger, 0, "INITIALISING THE PROJECT WITH ID %d WHOSE DESCRIPTION IS %s", p.getProjectID(), p.getDescription());
			projectCurrent = p.getProjectID();

			// initialise each project record so that its cursors are 1

			DataManager.getProjectEntityManager().getTransaction().begin();
			p.setTimeStamp(1);
			p.setTimeStampDisplayCursor(1);
			p.setTimeStampComparatorCursor(timeStampComparatorCursor);

			// set all project buttonState initially to the end of the non-existent previous period
			p.setButtonState("Accumulate");
			DataManager.getProjectEntityManager().getTransaction().commit();

			// fetch this project's current timeStamp record (which must exist in the database or we flag an error but try to correct it)

			timeStampCurrentRecord = Capitalism.selectionsProvider.timeStampSingle(timeStampIDCurrent);
			if (timeStampCurrentRecord == null) {
				Reporter.report(logger, 1, "There is no initial timeStamp record for project %d, will create a record and carry on from there",
						p.getDescription());
				try {
					DataManager.getTimeStampEntityManager().getTransaction().begin();
					TimeStamp newStamp = new TimeStamp(1, p.getProjectID(), 1, "", 1, "Start");
					DataManager.getTimeStampEntityManager().persist(newStamp);
					DataManager.getTimeStampEntityManager().getTransaction().commit();
				} catch (PersistenceException e) {
					Dialogues.alert(logger, String.format("Could not create the initial timeStamp record for project %d", p.getDescription()));
				}
			}
			if (timeStampCurrentRecord.getTimeStampID() != 1) {
				Reporter.report(logger, 1,
						"The initial timeStamp record for project %d should have an ID of 1 but instead has  %d. We will try to carry on with the new ID",
						p.getDescription(), timeStampCurrentRecord);
			}
			overrideLabourPower();

			calculateStockAggregates();

			// the first time, we don't validate, so the 'validate' parameter of 'calculateAggregates' is set to false.

			calculateUseValueAggregates(false);
			setCapitals();
			checkInvariants();

			// Set the initial comparators for every project, circuit, class, use value and stock .
			// Since the comparator cursor and the cursor are already 1, this amounts to setting it to 1
			DataManager.setComparators(1);
		}

		// There will normally be more than one project. Choose the first.
		projectCurrent = 1;
	}

	/**
	 * whatever the user has supplied, calculate the stock of Labour Power from the size of the workforce
	 * TODO this is a somewhat temporary measure. Needs to be thought through.
	 */

	public void overrideLabourPower() {
		SocialClass workers = DataManager.socialClassByName(timeStampIDCurrent, "Workers");
		double size = workers.getSize();
		Stock labourPower = workers.getLabourPower();
		labourPower.setQuantity(size);
	}

	/**
	 * Test the invariants of motion. Calculates, for each usevalue, the total price and total value based on what this usevalue knows. Compares it with the
	 * recorded totalprice and total value. Logs an error if they are not the same.
	 * <p>
	 * TODO incorporate further checks, as follows:
	 * <p>
	 * (1) no new value is created except in production
	 * <p>
	 * (2) total new value created in production is equal to total labour power used up
	 */

	public void checkInvariants() {
		for (UseValue u : DataManager.useValuesAll(timeStampIDCurrent)) {
			double listedQuantity = u.getTotalQuantity();
			double unitValue = u.getUnitValue();
			double listedValue = u.getTotalValue();
			double calculatedValue = listedQuantity * unitValue;
			if (listedValue != calculatedValue) {
				logger.error("ERROR: listed price is {} and total price is {}", listedValue, calculatedValue);
			} else {
				logger.debug("listed price of {} matches calculated price at  {}", u.getUseValueName(), calculatedValue);
			}
		}
	}

	/**
	 * Move the timeStampIDCurrent record to contain a new timestamp with the current project.
	 * Then create a new record for every entity in the simulation, with this timestamp and the current projectCurrent.
	 * In case of misuse, it flags an exception if there is a duplicate key error rather than allowing a fail.
	 * 
	 * TODO make this a bit more failsafe and foolproof
	 * 
	 * @param superState
	 *            if this operation is a component (child) of a 'superState', as for example 'Supply' is a
	 *            child of 'M-C', then this field will refer to an earlier record that contains the details of the superState.
	 *            This allows us to display the operations as a hierachy in the simulation's treeView
	 * 
	 * @param description
	 *            description of the new timeStampIDCurrent, which is displayed in the selection table for timeStamps.
	 *            if this record is a superState, the description also serves as the key that will be referenced by
	 *            the components(children) of this record, once these have been generated
	 */

	// Note: Project is the only persistent entity that does not need a new record when the timeStampIDCurrent changes

	public void advanceOneStep(String description, String superState) {
		timeStampComparatorCursor = timeStampIDCurrent;
		timeStampIDCurrent++;
		timeStampDisplayCursor = timeStampIDCurrent;
		logger.debug("Move One Step in the project called {} by creating a new timeStamp {} with description {}", projectCurrent, timeStampIDCurrent,
				description);

		TimeStamp newTimeStamp = new TimeStamp(timeStampIDCurrent, projectCurrent, periodCurrent, superState, timeStampIDCurrent - 1, description);

		try {
			DataManager.getTimeStampEntityManager().getTransaction().begin();
			DataManager.getTimeStampEntityManager().persist(newTimeStamp);
			DataManager.getTimeStampEntityManager().getTransaction().commit();
		} catch (PersistenceException p) {
			logger.error("Could not advance to timeStampIDCurrent " + timeStampIDCurrent + " because of " + p.getMessage());
			logger.error("Probably, this time stamp already exists. Try re-initialising the database");
			return;
		}

		// record the present timeStamp and cursor in the current project persistent record
		// do not create a new project record - modify the existing one.

		DataManager.getProjectEntityManager().getTransaction().begin();
		Capitalism.selectionsProvider.setTimeStampOfProject(projectCurrent, timeStampIDCurrent);
		Capitalism.selectionsProvider.setTimeStampCursorOfProject(projectCurrent, timeStampDisplayCursor);
		DataManager.getProjectEntityManager().getTransaction().commit();

		// persist a new version of all simulation entities, with the same project, and the new timeStamp...

		DataManager.getUseValueEntityManager().getTransaction().begin();
		DataManager.getStocksEntityManager().getTransaction().begin();
		DataManager.getCircuitEntityManager().getTransaction().begin();
		DataManager.getSocialClassEntityManager().getTransaction().begin();
		DataManager.getGlobalEntityManager().getTransaction().begin();

		// Use values

		logger.debug(" Persisting a new set of use values with timeStamp {}", timeStampIDCurrent);
		List<UseValue> results = DataManager.useValuesAll(timeStampIDCurrent - 1);
		UseValue newUseValue;
		logger.debug("  Scanning existing use values of which there are " + results.size());
		for (UseValue u : results) {
			logger.debug(String.format("  Persisting the use value [%s]", u.getUseValueName()));
			newUseValue = new UseValue();
			newUseValue.copyUseValue(u);
			newUseValue.setTimeStamp(timeStampIDCurrent);
			DataManager.getUseValueEntityManager().persist(newUseValue);
		}

		// Stocks

		logger.debug(" Persisting a new set of stocks with timeStamp {} ", timeStampIDCurrent);
		List<Stock> stockList = DataManager.stocksAll(timeStampIDCurrent - 1);
		Stock newStock;
		logger.debug("  Scanning existing stocks of which there are " + stockList.size());
		for (Stock s : stockList) {
			logger.log(Level.ALL, "   Persisting " + s.primaryKeyAsString());
			newStock = new Stock();
			newStock.copyStock(s);
			newStock.setTimeStamp(timeStampIDCurrent);
			DataManager.getStocksEntityManager().persist(newStock);
		}

		// Circuits

		logger.debug(" Persisting a new set of circuits with timeStamp ", timeStampIDCurrent);
		List<Circuit> circuitList = DataManager.circuitsAll(timeStampIDCurrent - 1);
		Circuit newCircuit;
		logger.debug("  Scanning existing circuits of which there are " + circuitList.size());
		for (Circuit c : circuitList) {
			logger.debug("  Persisting a circuit whose use value is " + c.getProductUseValueType());
			newCircuit = new Circuit();
			newCircuit.copyCircuit(c);
			newCircuit.setTimeStamp(timeStampIDCurrent);
			DataManager.getCircuitEntityManager().persist(newCircuit);
		}

		// Social Classes

		logger.debug(" Persisting a new set of social classes with timeStamp {}", timeStampIDCurrent);
		List<SocialClass> socialClassList = DataManager.socialClassesAll(timeStampIDCurrent - 1);
		SocialClass newSocialClass;
		logger.debug("  Scanning existing social classes of which there are " + socialClassList.size());
		for (SocialClass sc : socialClassList) {
			logger.debug("  Persisting a social class whose name is " + sc.getSocialClassName());

			newSocialClass = new SocialClass();
			newSocialClass.copySocialClass(sc);
			newSocialClass.setTimeStamp(timeStampIDCurrent);
			DataManager.getSocialClassEntityManager().persist(newSocialClass);
		}

		// Globals

		logger.debug(" Persisting a new globals record with timeStamp {} ", timeStampIDCurrent);
		Global g = DataManager.getGlobal(timeStampIDCurrent - 1);
		Global newGlobal = new Global();
		newGlobal.copyGlobal(g);
		newGlobal.setTimeStamp(timeStampIDCurrent);

		DataManager.setComparators(timeStampIDCurrent);

		DataManager.getGlobalEntityManager().persist(newGlobal);

		DataManager.getSocialClassEntityManager().getTransaction().commit();
		DataManager.getCircuitEntityManager().getTransaction().commit();
		DataManager.getStocksEntityManager().getTransaction().commit();
		DataManager.getUseValueEntityManager().getTransaction().commit();
		DataManager.getGlobalEntityManager().getTransaction().commit();

		logger.debug("Done Persisting: exit AdvanceOneStep");
		logger.debug("");
		timeStampDisplayCursor = timeStampIDCurrent; // synchronise cursor with what was just done

		// We do not refresh the display because this is done from within ViewManager and may depend on what is done with the newly-persisted objects
	}

	/**
	 * tell every stock to record its value and price, based on the quantity of the stock, its unit value and its price
	 */
	public void calculateStockAggregates() {
		Reporter.report(logger, 1, " Calculating stock values and prices from stock quantities, unit values and unit prices");
		List<Stock> allStocks = DataManager.stocksAll(timeStampIDCurrent);
		for (Stock s : allStocks) {
			s.modifyTo(s.getQuantity());
		}
	}

	/**
	 * Calculate all the aggregates that are only present in the stock data but not registered in other entities.
	 * At present, just use values, but could conceivably be extended
	 * Tell every use value to sum, independent of each other, the quantity, value and price of all stocks of this type.
	 * Use this to reset the global total
	 * NOTE:Assumes that the values and prices of the stocks have been set correctly beforehand
	 * 
	 * @param validate
	 *            if this is true, check the calculated magnitudes against those already recorded. These should always be the same, so if an alert appears in
	 *            the user log, something is wrong
	 */
	public void calculateUseValueAggregates(boolean validate) {
		Reporter.report(logger, 1, " Registering use value total prices and values based on stocks");
		double globalTotalValue = 0;
		double globalTotalPrice = 0;
		for (UseValue u : DataManager.useValuesAll(timeStampIDCurrent)) {
			u.calculateAggregates(validate);
			globalTotalValue += u.getTotalValue();
			globalTotalPrice += u.getTotalPrice();
		}
		Reporter.report(logger, 1, " Setting global totals");
		Global currentGlobal = DataManager.getGlobal(timeStampDisplayCursor);
		currentGlobal.setTotalValue(globalTotalValue);
		currentGlobal.setTotalPrice(globalTotalPrice);
		Reporter.report(logger, 2, " Global total value reset to %.2f and price to %.2f", globalTotalValue, globalTotalPrice);
	}

	/**
	 * initialise the initialCapital of each circuit to be the price of its stocks when the period starts.
	 * Initialise the currentCapital to be the same.
	 * Called by 'startup' and then AFTER Supply. The reason is only to display to the user how the profit is calculated.
	 */
	protected void setCapitals() {
		Global global = DataManager.getGlobal(Simulation.timeStampIDCurrent);
		double globalInitialCapital=0.0;
		for (Circuit c : DataManager.circuitsAll(timeStampIDCurrent)) {
			c.calculateCurrentCapital();
			double initialCapital = c.getCurrentCapital();
			Reporter.report(logger, 2, "  The initial capital of the industry[%s] is now $%.2f (intrinsic %.2f)", c.getProductUseValueType(),initialCapital,initialCapital/global.getMelt());
			c.setInitialCapital(initialCapital);
			globalInitialCapital+=initialCapital;
		}
		Reporter.report(logger, 2, "  Total initial capital is now $%.2f (intrinsic %.2f)", globalInitialCapital,globalInitialCapital/global.getMelt());
		
		global.setInitialCapital(globalInitialCapital);
		global.setCurrentCapital(globalInitialCapital);
	}

	public void advanceOnePeriod() {
		periodCurrent++;
		Reporter.report(logger, 0, "ADVANCING ONE PERIOD TO %d", periodCurrent);
	}

	/**
	 * @return the timeStampComparatorCursor
	 */
	public static int getTimeStampComparatorCursor() {
		return timeStampComparatorCursor;
	}

	/**
	 * @param timeStampComparatorCursor
	 *            the timeStampComparatorCursor to set
	 */
	public static void setTimeStampComparatorCursor(int timeStampComparatorCursor) {
		Simulation.timeStampComparatorCursor = timeStampComparatorCursor;
	}

	/**
	 * @return the roundingPrecision
	 */
	public static int getRoundingPrecision() {
		return roundingPrecision;
	}

	/**
	 * @param roundingPrecision
	 *            the roundingPrecision to set
	 */
	public static void setRoundingPrecision(int roundingPrecision) {
		Simulation.roundingPrecision = roundingPrecision;
	}

	/**
	 * @return the epsilon
	 */
	public static double getEpsilon() {
		return epsilon;
	}

	/**
	 * @param epsilon the epsilon to set
	 */
	public static void setEpsilon(double epsilon) {
		Simulation.epsilon = epsilon;
	}
}
