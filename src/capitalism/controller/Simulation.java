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

package capitalism.controller;

import java.util.List;
import javax.persistence.PersistenceException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.model.Commodity;
import capitalism.model.Global;
import capitalism.model.Industry;
import capitalism.model.Project;
import capitalism.model.SocialClass;
import capitalism.model.Stock;
import capitalism.model.TimeStamp;
import capitalism.utils.Dialogues;
import capitalism.utils.MathStuff;
import capitalism.utils.Reporter;
import capitalism.utils.StringStuff;
import capitalism.view.custom.ActionButtonsBox;
import capitalism.view.custom.ActionStates;
import capitalism.view.custom.DisplayControlsBox;

public class Simulation extends Parameters{

	private static final Logger logger = LogManager.getLogger(Simulation.class);

	// Application-wide persistent project that defines a simulation. All other persistent entities are confined to records defined by it.

	public static int projectCurrent;

	// Application-wide persistent TimeStamp ID. This ID of the current timeStamp record in the simulation

	public static int timeStampIDCurrent;

	// Application-wide variable saying what is the latest period in the current simulation

	public static int periodCurrent;

	// Application-wide cursors. By changing these, the user views entities from earlier timestamps and compares them with 
	// those from the current timeStamp. These cursors are independent of timeStampIDCurrent and operations that involve 
	// them do not affect the database, or the simulation.
	
	public static int timeStampDisplayCursor; 				// determines which timeStamp is displayed
	private static int timeStampComparatorCursor; 			// the timeStamp with which the displayed data is to be compared

	/**
	 * A copy of the melt, for convenience and speed. MUST be updated by global.setMelt(). That way it will always synchronise.
	 * TODO under demelopvent
	 */
	public static double melt;	

	public Simulation() {
	}

	/**
	 * startup. Initialise all variables that are derived from user data but not required explicitly
	 * TODO validate user data at this point
	 */
	public static void startup() {
		Reporter.report(logger, 0, "INITIALISE DATA FROM USER-DEFINED PROJECTS");

		TimeStamp timeStampCurrentRecord;

		timeStampIDCurrent = 1;
		timeStampDisplayCursor = 1;
		timeStampComparatorCursor = 1;
		periodCurrent = 1;

		// Initialise all projects at the start
		for (Project p : Project.projectsAll()) {
			Reporter.report(logger, 1, "Initialising project %d called '%s'", p.getProjectID(), p.getDescription());
			projectCurrent = p.getProjectID();

			// initialise each project record so that its cursors are 1

			Project.getEntityManager().getTransaction().begin();
			p.setTimeStamp(1);
			p.setTimeStampDisplayCursor(1);
			p.setTimeStampComparatorCursor(timeStampComparatorCursor);

			// set all project buttonState initially to the end of the non-existent previous period
			p.setButtonState(ActionStates.lastState().text());
			Project.getEntityManager().getTransaction().commit();

			// fetch this project's current timeStamp record (which must exist in the database or we flag an error but try to correct it)

			timeStampCurrentRecord = TimeStamp.timeStampSingle(timeStampIDCurrent);
			if (timeStampCurrentRecord == null) {
				Reporter.report(logger, 1, " There is no initial timeStamp record for project %d, will create a record and carry on from there",
						p.getDescription());
				try {
					TimeStamp.getEntityManager().getTransaction().begin();
					TimeStamp newStamp = new TimeStamp(1, p.getProjectID(), 1, "", 1, "Start");
					TimeStamp.getEntityManager().persist(newStamp);
					TimeStamp.getEntityManager().getTransaction().commit();
				} catch (PersistenceException e) {
					Dialogues.alert(logger, String.format("Could not create the initial timeStamp record for project %d", p.getDescription()));
				}
			}
			if (timeStampCurrentRecord.getTimeStampID() != 1) {
				Reporter.report(logger, 1,
						" The initial timeStamp record for project %d should have an ID of 1 but instead has  %d. We will try to carry on with the new ID",
						p.getDescription(), timeStampCurrentRecord);
			}

			calculateStockAggregates();
			setCapitals();
			checkInvariants();

			// Set the initial comparators for every project, industry, class, use value and stock .
			// Since the comparator cursor and the cursor are already 1, this amounts to setting it to 1
			setComparators(1);

			// little tweak to handle currency symbols encoded in UTF8

			Global global = Global.getGlobal(p.getProjectID(), 1);
			logger.debug("Character Symbol for Project {} is {}", global.getCurrencySymbol());
			String utfjava = StringStuff.convertFromUTF8(global.getCurrencySymbol());
			logger.debug("Character symbol after conversion is {}", utfjava);
			global.setCurrencySymbol(utfjava);
		}

		// There will normally be more than one project. Choose the first.
		projectCurrent = 1;
	}

	/**
	 * Test the invariants of motion. Calculates, for each commodity, the total price and total value based on what this commodity knows. Compares it with the
	 * recorded totalprice and total value. Logs an error if they are not the same.
	 * 
	 * TODO incorporate further checks, as follows:
	 * (1) no new value is created except in production
	 * (2) total new value created in production is equal to total labour power used up
	 */

	public static void checkInvariants() {
		for (Commodity u : Commodity.commoditiesAll()) {
			double listedQuantity = u.totalQuantity();
			double unitValue = u.getUnitValue();
			double listedValue = u.totalValue();
			double calculatedValue = listedQuantity * unitValue;
			if (listedValue != calculatedValue) {
				logger.error("Listed price is {} and total price is {}", listedValue, calculatedValue);
			} else {
				logger.debug("Listed price of {} matches calculated price at  {}", u.commodityName(), calculatedValue);
			}
		}
	}

	/**
	 * Move the timeStampIDCurrent record to contain a new timestamp with the current project.
	 * Then create a new record for every entity in the simulation, with this timestamp and the current projectCurrent.
	 * In case of misuse, it flags an exception if there is a duplicate key error rather than allowing a fail.
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

	public static void advanceOneStep(String description, String superState) {

		checkMoneySufficiency();

		timeStampComparatorCursor = timeStampIDCurrent;
		timeStampDisplayCursor = timeStampIDCurrent + 1;
		logger.debug("Move One Step in project {} by creating a new timeStamp {} called {}", projectCurrent, timeStampIDCurrent, description);

		TimeStamp newTimeStamp = new TimeStamp(timeStampIDCurrent + 1, projectCurrent, periodCurrent, superState, timeStampIDCurrent, description);

		try {
			TimeStamp.getEntityManager().getTransaction().begin();
			TimeStamp.getEntityManager().persist(newTimeStamp);
			TimeStamp.getEntityManager().getTransaction().commit();
		} catch (PersistenceException p) {
			logger.error("Could not advance to timeStampIDCurrent " + timeStampIDCurrent + " because of " + p.getMessage());
			logger.error("Probably, this time stamp already exists. Try re-initialising the database");
			return;
		}

		// record the present timeStamp and cursor in the current project persistent record
		// do not create a new project record - modify the existing one.

		Project.getEntityManager().getTransaction().begin();
		TimeStamp.setTimeStampOfProject(projectCurrent, timeStampIDCurrent + 1);
		TimeStamp.setTimeStampCursorOfProject(projectCurrent, timeStampDisplayCursor);
		Project.getEntityManager().getTransaction().commit();

		// persist a new version of all simulation entities, with the same project, and the new timeStamp...

		Commodity.getEntityManager().getTransaction().begin();
		Stock.getEntityManager().getTransaction().begin();
		Industry.getEntityManager().getTransaction().begin();
		SocialClass.getEntityManager().getTransaction().begin();
		Global.getEntityManager().getTransaction().begin();

		// Use values

		logger.debug(" Persisting a new set of use values with timeStamp {}", timeStampIDCurrent + 1);
		Commodity comodity;
		for (Commodity u : Commodity.commoditiesAll()) {
			comodity = new Commodity(u);
			comodity.setTimeStamp(timeStampIDCurrent + 1);
			Commodity.getEntityManager().persist(comodity);
		}

		// Stocks

		logger.debug(" Persisting a new set of stocks with timeStamp {} ", timeStampIDCurrent + 1);
		Stock newStock;
		for (Stock s : Stock.all()) {
			logger.debug("Persisting {}", s.primaryKeyAsString());
			newStock = new Stock(s);
			newStock.setTimeStamp(timeStampIDCurrent + 1);
			Stock.getEntityManager().persist(newStock);
		}

		// industries

		logger.debug("Persisting a new set of industries with timeStamp {} ", timeStampIDCurrent + 1);
		Industry newIndustry;
		for (Industry c : Industry.industriesAll()) {
			logger.debug("Persisting an industry whose use value is " + c.getName());
			newIndustry = new Industry(c);
			newIndustry.setTimeStamp(timeStampIDCurrent + 1);
			Industry.getEntityManager().persist(newIndustry);
		}

		// Social Classes

		logger.debug("Persisting a new set of social classes with timeStamp {}", timeStampIDCurrent + 1);
		SocialClass newSocialClass;
		for (SocialClass sc : SocialClass.socialClassesAll()) {
			logger.debug("  Persisting a social class whose name is " + sc.getSocialClassName());
			newSocialClass = new SocialClass();
			newSocialClass.copy(sc);
			newSocialClass.setTimeStamp(timeStampIDCurrent + 1);
			SocialClass.getEntityManager().persist(newSocialClass);
		}

		// Globals

		logger.debug("Persisting a new globals record with timeStamp {} ", timeStampIDCurrent + 1);
		Global newGlobal = new Global(Global.getGlobal());
		newGlobal.setTimeStamp(timeStampIDCurrent + 1);
		Global.getEntityManager().persist(newGlobal);

		setComparators(timeStampIDCurrent + 1);

		SocialClass.getEntityManager().getTransaction().commit();
		Industry.getEntityManager().getTransaction().commit();
		Stock.getEntityManager().getTransaction().commit();
		Commodity.getEntityManager().getTransaction().commit();
		Global.getEntityManager().getTransaction().commit();

		timeStampIDCurrent++;

		logger.debug("Done Persisting: exit AdvanceOneStep");
	}

	/**
	 * A consistency check: does everyone have at least some money?
	 */
	private static void checkMoneySufficiency() {
		// a little consistency check
		for (Stock s : Stock.all()) {
			if (s.getQuantity() < 0 - MathStuff.epsilon) {
				if (s.getStockType().equals(Stock.STOCKTYPE.MONEY.text())) {
					Dialogues.alert(logger, "The owner %s has run out of money. "
							+ "This may be a data error:try giving it more. "
							+ "If the problem persists, contact the developer", s.getOwner());
				}
			}
		}
	}

	/**
	 * tell every stock to record its value and price, based on the quantity of the stock, its unit value and its price
	 */
	private static void calculateStockAggregates() {
		Reporter.report(logger, 2, "Calculating stock values and prices from stock quantities, unit values and unit prices");
		List<Stock> allStocks = Stock.all();
		for (Stock s : allStocks) {
			s.modifyTo(s.getQuantity());
		}
	}

	/**
	 * Reset all unit values to be the average value of all stocks.
	 * Then recalculate stock values. Important. The new unit values were calculated on a
	 * per-commodity basis. They are 'social' values; however production creates 'individual'
	 * values on a per-industry basis. Now, individual industries must reconcile the value
	 * of their product with social average values.
	 */
	public static void computeUnitValues() {
		for (Commodity u : Commodity.commoditiesAll()) {
			if (u.getFunction() != Commodity.FUNCTION.MONEY) {
				double quantity = u.totalQuantity();
				double newUnitValue = u.totalValue() / quantity;
				Reporter.report(logger, 2, "The unit value of commodity [%s] was %.4f, and will be reset to %.4f", 
						u.commodityName(), u.getUnitValue(),newUnitValue);
				u.setUnitValue(newUnitValue);
			}
		}
		for (Stock s : Stock.all(timeStampIDCurrent)) {
			s.reCalculateStockTotalValuesAndPrices();
		}
	}

	/**
	 * this helper method simply checks consistency
	 */

	public static void checkGlobalConsistency() {
		double globalTotalValue = 0.0;
		double globalTotalPrice = 0.0;
		Global global = Global.getGlobal();
		
		//TODO this is somewhat hamfisted. Need queries to do this stuff.
		if (Parameters.isFullPricing()) {
			for (Commodity u : Commodity.commoditiesAll()) {
				Reporter.report(logger, 2, "Commodity [%s] Total value is %.0f, and total price is %.0f", u.commodityName(), u.totalValue(), u.totalPrice());
				globalTotalValue += u.totalValue();
				globalTotalPrice += u.totalPrice();
			}
		} else {
			for (Commodity u : Commodity.commoditiesByFunction(Commodity.FUNCTION.PRODUCTIVE_INPUT)) {
				Reporter.report(logger, 2, "Commodity [%s] Total value is %.0f, and total price is %.0f", u.commodityName(), u.totalValue(), u.totalPrice());
				globalTotalValue += u.totalValue();
				globalTotalPrice += u.totalPrice();

			}
			for (Commodity u : Commodity.commoditiesByFunction(Commodity.FUNCTION.CONSUMER_GOOD)) {
				Reporter.report(logger, 2, "Commodity [%s] Total value is %.0f, and total price is %.0f", u.commodityName(), u.totalValue(), u.totalPrice());
				globalTotalValue += u.totalValue();
				globalTotalPrice += u.totalPrice();
			}
		}
		Reporter.report(logger, 1, "Global total value is %.0f, and total price is %.0f", globalTotalValue, globalTotalPrice);

		logger.debug("Recorded global total value is {}, and total value calculated from commodities is {}", global.totalValue(), globalTotalValue);
		logger.debug("Recorded global total price is {}, and total price calculated from commodities is {}", global.totalPrice(), globalTotalPrice);

		if (!MathStuff.equals(globalTotalValue, global.totalValue()))
			Dialogues.alert(logger, "The total value of stocks is out of sync");
		if (!MathStuff.equals(globalTotalPrice, global.totalPrice()))
			Dialogues.alert(logger, "The total price of stocks is out of sync");
	}

	/**
	 * initialise the initialCapital of each industry to be the price of its stocks when the period starts.
	 * Initialise the currentCapital to be the same.
	 * Called at startup and thereafter afterAccumulate (i.e. at the very end of the whole industry and start of the next)
	 */
	protected static void setCapitals() {
		Global global = Global.getGlobal();
		for (Industry c : Industry.industriesAll()) {
			double initialCapital = c.currentCapital();
			Reporter.report(logger, 3, "The initial capital of the industry[%s] is now $%.0f (intrinsic %.0f)", c.getName(), initialCapital,
					initialCapital / global.getMelt());
			c.setInitialCapital(initialCapital);
		}
		Reporter.report(logger, 2, "Total initial capital is now $%.0f (intrinsic %.0f)", global.initialCapital(),
				global.initialCapital() / global.getMelt());
		Reporter.report(logger, 2, "The profit of the previous period has been erased from the record; it was stored during the price calculation");
		// Erase the public record of the profit of the previous period
		for (Industry c : Industry.industriesAll()) {
			c.persistProfit();
		}
	}
	
	/**
	 * initialise the initial productive capital of the industry, to be the price of its productive stocks
	 * Chiefly used for profit-rate equalization when we don't do full repricing
	 */
	
	protected static void setInitialProductiveCapitals() {
		
		for (Industry c:Industry.industriesAll()) {
			double productiveCapital=0.0;
			for (Stock s:c.productiveStocks()) {
				productiveCapital+=s.getPrice();
			}
			productiveCapital+=c.getSalesPrice();
			c.setProductiveCapital(productiveCapital);
		}
	}

	public static void advanceOnePeriod() {
		periodCurrent++;
		Reporter.report(logger, 0, "ADVANCING ONE PERIOD TO %d", periodCurrent);
		// start another period so recompute the initial capitals and profits
		setCapitals();
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
		Project newProject = Project.projectSingle(newProjectID);
		
		// record the current timeStamp, timeStampDisplayCursor and buttonState in the current project record, and persist it to the database
		Project thisProject = Project.projectSingle(Simulation.projectCurrent);

		Project.getEntityManager().getTransaction().begin();

		thisProject.setTimeStamp(Simulation.timeStampIDCurrent);
		thisProject.setTimeStampDisplayCursor(Simulation.timeStampDisplayCursor);
		thisProject.setTimeStampComparatorCursor(Simulation.getTimeStampComparatorCursor());
		thisProject.setButtonState(ActionButtonsBox.getLastAction().text());
		thisProject.setPeriod(Simulation.getPeriodCurrent());

		Project.getEntityManager().getTransaction().commit();

		// retrieve the selected project record, and copy its various cursors and into the simulation cursors
		Simulation.timeStampIDCurrent = newProject.getTimeStamp();
		Simulation.timeStampDisplayCursor = newProject.getTimeStampDisplayCursor();
		Simulation.setTimeStampComparatorCursor(newProject.getTimeStampComparatorCursor());
		Simulation.setPeriodCurrent(newProject.getPeriod());
		actionButtonsBox.setActionStateFromLabel(newProject.getButtonState());
		Simulation.projectCurrent = newProjectID;
		DisplayControlsBox.setParameterComboPrompts();
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
			Stock.setComparators(timeStampID);
			Commodity.setComparators(timeStampID);
			Industry.setComparators(timeStampID);
			SocialClass.setComparators(timeStampID);
			Global.setComparators(timeStampID);

		} catch (Exception e) {
			Dialogues.alert(logger, "Database fubar. Sorry, please contact developer");
		}
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
	 * 
	 * 
	 * /**
	 * 
	 * @return the periodCurrent
	 */
	public static int getPeriodCurrent() {
		return periodCurrent;
	}

	/**
	 * @param periodCurrent
	 *            the periodCurrent to set
	 */
	public static void setPeriodCurrent(int periodCurrent) {
		Simulation.periodCurrent = periodCurrent;
	}



	/**
	 * @return the projectCurrent
	 */
	public static int getProjectCurrent() {
		return projectCurrent;
	}

	/**
	 * @return the timeStampIDCurrent
	 */
	public static int getTimeStampIDCurrent() {
		return timeStampIDCurrent;
	}

	/**
	 * @return the timeStampDisplayCursor
	 */
	public static int getTimeStampDisplayCursor() {
		return timeStampDisplayCursor;
	}

}
