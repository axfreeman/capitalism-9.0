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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.model.Commodity;
import capitalism.model.Industry;
import capitalism.model.Project;
import capitalism.model.SocialClass;
import capitalism.model.Stock;
import capitalism.model.TimeStamp;
import capitalism.utils.Dialogues;
import capitalism.utils.MathStuff;
import capitalism.utils.Reporter;
import capitalism.view.custom.ActionButtonsBox;
import capitalism.view.custom.DisplayControlsBox;

public class Simulation extends Parameters {
	private static final Logger logger = LogManager.getLogger(Simulation.class);

	// Application-wide persistent project that defines a simulation. All other persistent entities are confined to records defined by it.

	public static int projectIDCurrent;

	// Application-wide persistent TimeStamp ID. This ID of the current timeStamp record in the simulation

	public static int timeStampIDCurrent;

	// Application-wide variable saying what is the latest period in the current simulation

	private static int periodCurrent;

	// Application-wide cursors. By changing these, the user views entities from earlier timestamps and compares them with
	// those from the current timeStamp. These cursors are independent of timeStampIDCurrent and operations that involve
	// them do not affect the database, or the simulation.

	public static int timeStampDisplayCursor; 				// determines which timeStamp is displayed
	private static int timeStampComparatorCursor; 			// the timeStamp with which the displayed data is to be compared

	// top copy of the timestamp record

	public static TimeStamp currentTimeStamp;

	/**
	 * A copy of the melt, for convenience and speed. MUST be updated by timeStamp.setMelt(). That way it will always synchronise.
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

		timeStampIDCurrent = 1;
		timeStampDisplayCursor = 1;
		timeStampComparatorCursor = 1;
		periodCurrent = 1;

		// Initialise all projects at the start
		for (Project p : Project.projectsAll()) {
			p.initialise();
		}
	}

	/**
	 * Convert the stock inputs, which provide production and consumption magnitudes, into coefficients.
	 * This is more transparent to the user and also permits higher precision, in general
	 */

	public static void convertMagnitudesToCoefficients() {
		for (Industry industry : Industry.allCurrent()) {
			for (Stock stock : Stock.allProductiveInIndustry(Simulation.timeStampIDCurrent, industry.name())) {
				double coefficient = stock.getProductionQuantity() / industry.getOutput();
				logger.debug("Industry {} stock {} has coefficient {} and magnitude {} for an output of {}. Coefficient will be changed to {}",
						industry.name(), stock.name(), stock.getProductionCoefficient(), stock.getProductionQuantity(), industry.getOutput(),
						coefficient);
//				stock.setProductionCoefficient(coefficient);
			}
		}
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
		for (Commodity u : Commodity.allCurrent()) {
			double listedQuantity = u.totalQuantity();
			double unitValue = u.getUnitValue();
			double listedValue = u.totalValue();
			double calculatedValue = listedQuantity * unitValue;
			if (listedValue != calculatedValue) {
				logger.error("Listed price is {} and total price is {}", listedValue, calculatedValue);
			} else {
				logger.debug("Listed price of {} matches calculated price at  {}", u.name(), calculatedValue);
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
		logger.debug("Move One Step in project {} at period {} by creating a new timeStamp {} called {}",
				projectIDCurrent, periodCurrent, timeStampIDCurrent + 1, description);

		// persist a new version of all simulation entities, with the same project, and the new timeStamp...
		TimeStamp.getEntityManager().getTransaction().begin();
		Project.getEntityManager().getTransaction().begin();

		// Create a new timeStamp that moves on by one from the present timeStamp, but has the same project and period
		// set its description and superState from the parameters in the call to advanceOneStep
		TimeStamp oldTimeStamp = TimeStamp.singleInCurrentProject(timeStampIDCurrent);
		currentTimeStamp = new TimeStamp(oldTimeStamp);
		currentTimeStamp.setTimeStampID(timeStampIDCurrent + 1);
		currentTimeStamp.setSuperState(superState);
		currentTimeStamp.setDescription(description);
		currentTimeStamp.setPeriod(periodCurrent);
		TimeStamp.getEntityManager().persist(currentTimeStamp);

		// record the present timeStamp and cursor in the current project persistent record and save it.
		// do not create a new project record - modify the existing one.
		Project.setTimeStam(projectIDCurrent, timeStampIDCurrent + 1);
		Project.setTimeStampCursor(projectIDCurrent, timeStampDisplayCursor);

		TimeStamp.getEntityManager().getTransaction().commit();
		Project.getEntityManager().getTransaction().commit();

		Commodity.getEntityManager().getTransaction().begin();
		Stock.getEntityManager().getTransaction().begin();
		Industry.getEntityManager().getTransaction().begin();
		SocialClass.getEntityManager().getTransaction().begin();

		// Commodities
		logger.debug(" Persisting a new set of commodities with timeStamp {} and period {}", timeStampIDCurrent + 1, periodCurrent);
		Commodity commodity;
		for (Commodity u : Commodity.allCurrent()) {
			commodity = new Commodity(u);
			commodity.setTimeStampID(timeStampIDCurrent + 1);
			Commodity.getEntityManager().persist(commodity);
		}

		// Stocks
		logger.debug(" Persisting a new set of stocks with timeStamp {} ", timeStampIDCurrent + 1);
		Stock newStock;
		for (Stock s : Stock.allCurrent()) {
			newStock = new Stock(s);
			newStock.setTimeStamp(timeStampIDCurrent + 1);
			Stock.getEntityManager().persist(newStock);
		}

		// industries
		logger.debug("Persisting a new set of industries with timeStamp {} ", timeStampIDCurrent + 1);
		Industry newIndustry;
		for (Industry c : Industry.allCurrent()) {
			logger.debug("Persisting an industry that produces commodity" + c.name());
			newIndustry = new Industry(c);
			newIndustry.setTimeStamp(timeStampIDCurrent + 1);
			Industry.getEntityManager().persist(newIndustry);
		}

		// Social Classes
		logger.debug("Persisting a new set of social classes with timeStamp {}", timeStampIDCurrent + 1);
		SocialClass newSocialClass;
		for (SocialClass sc : SocialClass.allCurrent()) {
			logger.debug("  Persisting a social class whose name is " + sc.name());
			newSocialClass = new SocialClass();
			newSocialClass.copy(sc);
			newSocialClass.setTimeStamp(timeStampIDCurrent + 1);
			SocialClass.getEntityManager().persist(newSocialClass);
		}

		setComparators(timeStampIDCurrent + 1);

		// now commit all the modified records
		SocialClass.getEntityManager().getTransaction().commit();
		Industry.getEntityManager().getTransaction().commit();
		Stock.getEntityManager().getTransaction().commit();
		Commodity.getEntityManager().getTransaction().commit();

		// some diagnostics - switch off if not needed for debug
		// for (Industry i:Industry.all()) {
		// logger.debug("Industry {} has project {} and timeStamp {}",i.getName(),i.getProject(),i.getTimeStamp());
		// }

		// we can only advance the timeStampID now, because the old ID was used to persist all the other records
		timeStampIDCurrent++;

		logger.debug("Done Persisting: exit AdvanceOneStep");
	}

	/**
	 * A consistency check: does everyone have at least some money?
	 */
	private static void checkMoneySufficiency() {
		// a little consistency check
		for (Stock s : Stock.allCurrent()) {
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
	public static void calculateStockAggregates() {
		Reporter.report(logger, 2, "Calculating stock values and prices from stock quantities, unit values and unit prices");
		List<Stock> allStocks = Stock.allCurrent();
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
		for (Commodity u : Commodity.allCurrent()) {
			if (u.getFunction() != Commodity.FUNCTION.MONEY) {
				double quantity = u.totalQuantity();
				double newUnitValue = u.totalValue() / quantity;
				Reporter.report(logger, 2, "The unit value of commodity [%s] was %.4f, and will be reset to %.4f",
						u.name(), u.getUnitValue(), newUnitValue);
				u.setUnitValue(newUnitValue);
			}
		}
		for (Stock s : Stock.allCurrentProject(timeStampIDCurrent)) {
			s.reCalculateStockTotalValuesAndPrices();
		}
	}

	/**
	 * this helper method simply checks consistency
	 */

	public static void checkConsistency() {
		double totalValue = 0.0;
		double totalPrice = 0.0;

		// TODO this is somewhat hamfisted. Need queries to do this stuff.
		if (Parameters.isFullPricing()) {
			for (Commodity u : Commodity.allCurrent()) {
				Reporter.report(logger, 2, "Commodity [%s] Total value is %.0f, and total price is %.0f", u.name(), u.totalValue(), u.totalPrice());
				totalValue += u.totalValue();
				totalPrice += u.totalPrice();
			}
		} else {
			for (Commodity u : Commodity.currentByFunction(Commodity.FUNCTION.PRODUCTIVE_INPUT)) {
				Reporter.report(logger, 2, "Commodity [%s] Total value is %.0f, and total price is %.0f", u.name(), u.totalValue(), u.totalPrice());
				totalValue += u.totalValue();
				totalPrice += u.totalPrice();

			}
			for (Commodity u : Commodity.currentByFunction(Commodity.FUNCTION.CONSUMER_GOOD)) {
				Reporter.report(logger, 2, "Commodity [%s] Total value is %.0f, and total price is %.0f", u.name(), u.totalValue(), u.totalPrice());
				totalValue += u.totalValue();
				totalPrice += u.totalPrice();
			}
		}
		Reporter.report(logger, 1, "Total value is %.0f, and Total price is %.0f", totalValue, totalPrice);

		logger.debug("Recorded total value is {}, and calculated total value is {}", Simulation.currentTimeStamp.totalValue(), totalValue);
		logger.debug("Recorded total price is {}, and calculated total price is {}", Simulation.currentTimeStamp.totalPrice(), totalPrice);

		if (!MathStuff.equals(totalValue, Simulation.currentTimeStamp.totalValue()))
			Dialogues.alert(logger, "The total value of stocks is out of sync");
		if (!MathStuff.equals(totalPrice, Simulation.currentTimeStamp.totalPrice()))
			Dialogues.alert(logger, "The total price of stocks is out of sync");
	}

	/**
	 * initialise the initialCapital of each industry to be the price of its stocks when the period starts.
	 * Initialise the currentCapital to be the same.
	 * Called at startup and thereafter afterAccumulate (i.e. at the very end of the whole industry and start of the next)
	 */
	public static void setCapitals() {
		for (Industry c : Industry.allCurrent()) {
			double initialCapital = c.currentCapital();
			Reporter.report(logger, 3, "The initial capital of the industry[%s] is now $%.0f (intrinsic %.0f)", c.name(), initialCapital,
					initialCapital / Simulation.currentTimeStamp.getMelt());
			c.setInitialCapital(initialCapital);
		}
		Reporter.report(logger, 2, "Total initial capital is now $%.0f (intrinsic %.0f)", Simulation.currentTimeStamp.initialCapital(),
				Simulation.currentTimeStamp.initialCapital() / Simulation.currentTimeStamp.getMelt());
		Reporter.report(logger, 2, "The profit of the previous period has been erased from the record; it was stored during the price calculation");
		// Erase the public record of the profit of the previous period
		for (Industry c : Industry.allCurrent()) {
			c.persistProfit();
		}
	}

	/**
	 * initialise the initial productive capital of the industry, to be the price of its productive stocks
	 * Chiefly used for profit-rate equalization when we don't do full repricing
	 */

	protected static void setInitialProductiveCapitals() {

		for (Industry c : Industry.allCurrent()) {
			double productiveCapital = 0.0;
			for (Stock s : c.productiveStocks()) {
				productiveCapital += s.getPrice();
			}
			productiveCapital += c.salesPrice();
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
		if (newProjectID == Simulation.projectIDCurrent) {
			logger.debug("The user switched to project {} which  is already current. No action was taken", newProjectID);
			return;
		}
		Project newProject = Project.get(newProjectID);

		// record the current timeStamp, timeStampDisplayCursor and buttonState in the current project record, and persist it to the database
		Project thisProject = Project.get(Simulation.projectIDCurrent);

		Project.getEntityManager().getTransaction().begin();

		thisProject.setTimeStamp(Simulation.timeStampIDCurrent);
		thisProject.setTimeStampDisplayCursor(Simulation.timeStampDisplayCursor);
		thisProject.setTimeStampComparatorCursor(Simulation.getTimeStampComparatorCursor());
		thisProject.setButtonState(ActionButtonsBox.getLastAction().text());
		thisProject.setPeriod(Simulation.getPeriodCurrent());

		Project.getEntityManager().getTransaction().commit();

		// retrieve the selected project record, and copy its various cursors and into the simulation cursors
		Simulation.timeStampIDCurrent = newProject.getTimeStampID();
		Simulation.timeStampDisplayCursor = newProject.getTimeStampDisplayCursor();
		Simulation.setTimeStampComparatorCursor(newProject.getTimeStampComparatorCursor());
		Simulation.setPeriodCurrent(newProject.getPeriod());
		actionButtonsBox.setActionStateFromLabel(newProject.getButtonState());
		Simulation.projectIDCurrent = newProjectID;
		DisplayControlsBox.setParameterComboPrompts();
		currentTimeStamp = TimeStamp.singleInProjectAndTimeStamp(newProjectID, timeStampIDCurrent);
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
			TimeStamp.setComparators(timeStampID);
		} catch (Exception e) {
			Dialogues.alert(logger, "Could not set comparators. Sorry, please contact developer");
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
	public static int getProjectIDCurrent() {
		return projectIDCurrent;
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

	/**
	 * @return the currentTimeStamp
	 */
	public static TimeStamp getCurrentTimeStamp() {
		return currentTimeStamp;
	}

	/**
	 * @param currentTimeStamp the currentTimeStamp to set
	 */
	public static void setCurrentTimeStamp(TimeStamp currentTimeStamp) {
		Simulation.currentTimeStamp = currentTimeStamp;
	}

}
