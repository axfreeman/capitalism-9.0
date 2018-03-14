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

public class Simulation{
	private static final Logger logger = LogManager.getLogger("Simulation");
	/**
	 * A copy of the persistent timeStamp that defines the parameters of the current simulation.
	 * It also provide the methods for calculating global magnitudes such as the profit rate, the melt and so on.
	 * It is encapsulated in the Simulation class and its members and methods are accessed via
	 * the static helper methods defined in this class. This is so that if, in future, we wish to
	 * implement by means other than these persistent elements, as much as possible can be done by modifing the Simulation class.  
	 */
	private static TimeStamp timeStampCurrent;
	/**
	 * Application-wide persistent project that defines the current simulation.
	 * All other persistent entities are confined to records defined by it.
	 * TODO this should be the sole route into the global project parameters
	 */
	private static Project projectCurrent;

	public Simulation() {
	}

	/**
	 * startup. Initialise all variables that are derived from user data but not required explicitly
	 * TODO validate user data at this point
	 */
	public static void startup() {
		Reporter.report(logger, 0, "INITIALISE DATA FROM USER-DEFINED PROJECTS");

		// initialise the two key state variables - the current period and the current project.
		// all state variables are encapsulated in one or other of these variables.
		// here for convenience we keep a copy that has to be kept synchronised with the database copy
		// I am not sufficiently conversant with JPA to know if this happens automatically so
		// we do some belt and braces. TODO resolve this.
		timeStampCurrent = TimeStamp.single(1, 1);
		timeStampCurrent.setPeriod(1);
		projectCurrent = Project.get(1);
		for (Project p : Project.projectsAll()) {
			p.initialise();
		}
		
		// initialise all the cursors, which are held in the current project
		projectCurrent.setTimeStampID(1);
		projectCurrent.setTimeStampDisplayCursor(1);
		projectCurrent.setTimeStampComparatorCursor(1);
	}

	/**
	 * Convert the stock inputs, which provide production and consumption magnitudes, into coefficients.
	 * This is more transparent to the user and also permits higher precision, in general
	 * 
	 * @param projectID
	 *            the projectID of the stocks whose coefficients we wish to calculate
	 * @param timeStampID
	 *            the timeStampID of the stocks whose coefficients we wish to calculate
	 */

	public static void convertMagnitudesToCoefficients(int projectID, int timeStampID) {
		for (Industry industry : Industry.all(projectID, timeStampID)) {
			for (Stock stock : Stock.allProductiveInIndustry(projectID, timeStampID, industry.name())) {
				double coefficient = stock.getProductionQuantity() / industry.getOutput();
				logger.debug("Industry {} stock {} has coefficient {} and magnitude {} for an output of {}. Coefficient will be changed to {}",
						industry.name(), stock.name(), stock.getProductionCoefficient(), stock.getProductionQuantity(), industry.getOutput(),
						coefficient);
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
		for (Commodity u : Commodity.all(projectIDCurrent(), timeStampIDCurrent())) {
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

		checkMoneySufficiency(projectIDcurrent(),timeStampIDCurrent());

		// move the timeStamp forward in the current project persistent record and save it.
		// do not create a new project record - modify the existing one.
		// NOTE we move forward the persistent project record, and then copy it to
		// the top copy that is held in the simulation.
		// TODO check empirically whether if we update the top copy we in fact update the persistent entity
		// probably we do, but I haven't studied JPA internals enough to be sure, so this is belt and braces

		int oldTimeStampID=timeStampIDCurrent();
		Project.getEntityManager().getTransaction().begin();
		Project.get(projectCurrent.getProjectID()).setTimeStampComparatorCursor(oldTimeStampID);
		Project.get(projectCurrent.getProjectID()).setTimeStampDisplayCursor(oldTimeStampID+ 1);
		Project.get(projectCurrent.getProjectID()).setTimeStampID(oldTimeStampID+ 1);
		Project.getEntityManager().getTransaction().commit();
		projectCurrent=Project.get(projectCurrent.getProjectID()); // retrieve the newly-persisted record and take a fresh copy
		
		logger.debug("Move One Step in project {} at period {} by creating a new timeStamp {} called {}",
				projectCurrent.getProjectID(), getPeriodCurrent(), projectCurrent.getTimeStampID(), description);

		// persist a new version of all simulation entities, with the same project, and the new timeStamp...
		TimeStamp.getEntityManager().getTransaction().begin();
		// Create a new timeStamp that moves on by one from the present timeStamp, but has the same project and period
		// set its description and superState from the parameters in the call to advanceOneStep
		int tempPID=projectIDCurrent();
		TimeStamp oldTimeStamp = TimeStamp.single(tempPID, oldTimeStampID);
		timeStampCurrent = new TimeStamp(oldTimeStamp);
		timeStampCurrent.setTimeStampID(projectCurrent.getTimeStampID());
		timeStampCurrent.setSuperState(superState);
		timeStampCurrent.setDescription(description);
		timeStampCurrent.setPeriod(getPeriodCurrent());
		TimeStamp.getEntityManager().persist(timeStampCurrent);

		TimeStamp.getEntityManager().getTransaction().commit();

		Commodity.getEntityManager().getTransaction().begin();
		Stock.getEntityManager().getTransaction().begin();
		Industry.getEntityManager().getTransaction().begin();
		SocialClass.getEntityManager().getTransaction().begin();

		// Commodities
		logger.debug(" Persisting a new set of commodities with timeStamp {} and period {}", timeStampIDCurrent(), getPeriodCurrent());
		Commodity commodity;
		for (Commodity u : Commodity.all(projectIDCurrent(), oldTimeStampID)) {
			commodity = new Commodity(u);
			commodity.setTimeStampID(timeStampIDCurrent());
			Commodity.getEntityManager().persist(commodity);
		}

		// Stocks
		logger.debug(" Persisting a new set of stocks with timeStamp {} ", timeStampIDCurrent());
		Stock newStock;
		for (Stock s : Stock.all(projectIDCurrent(), oldTimeStampID)) {
			newStock = new Stock(s);
			newStock.setTimeStamp(timeStampIDCurrent());
			Stock.getEntityManager().persist(newStock);
		}

		// industries
		logger.debug("Persisting a new set of industries with timeStamp {} ", timeStampIDCurrent());
		Industry newIndustry;
		for (Industry c : Industry.all(projectIDCurrent(), oldTimeStampID)) {
			logger.debug("Persisting an industry that produces commodity" + c.name());
			newIndustry = new Industry(c);
			newIndustry.setTimeStamp(timeStampIDCurrent());
			Industry.getEntityManager().persist(newIndustry);
		}

		// Social Classes
		logger.debug("Persisting a new set of social classes with timeStamp {}", timeStampIDCurrent());
		SocialClass newSocialClass;
		for (SocialClass sc : SocialClass.all(projectIDCurrent(), oldTimeStampID)) {
			logger.debug("  Persisting a social class whose name is " + sc.name());
			newSocialClass = new SocialClass();
			newSocialClass.copy(sc);
			newSocialClass.setTimeStamp(timeStampIDCurrent());
			SocialClass.getEntityManager().persist(newSocialClass);
		}

		setComparators(projectIDCurrent(), timeStampIDCurrent());

		// now commit all the modified records
		SocialClass.getEntityManager().getTransaction().commit();
		Industry.getEntityManager().getTransaction().commit();
		Stock.getEntityManager().getTransaction().commit();
		Commodity.getEntityManager().getTransaction().commit();

		// some diagnostics - switch off if not needed for debug
		// for (Industry i:Industry.all()) {
		// logger.debug("Industry {} has project {} and timeStamp {}",i.getName(),i.getProject(),i.getTimeStamp());
		// }

		logger.debug("Done Persisting: exit AdvanceOneStep");
	}

	/**
	 * A consistency check: does everyone have at least some money?
	 * 
	 * @param projectID
	 *            the projectID of the stocks to be checked
	 * @param timeStampID
	 *            the timeStampID of the stocks to be checked
	 */
	private static void checkMoneySufficiency(int projectID, int timeStampID) {
		// a little consistency check
		for (Stock s : Stock.all(projectID, timeStampID)) {
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
	 * @param projectID
	 *            the projectID for which the reporting and aggregation is to be done
	 * @param timeStampID
	 *            the timeStampID for which the reporting and aggregation is to be done
	 *            tell every stock to record its value and price, based on the quantity of the stock, its unit value and its price
	 */
	public static void calculateStockAggregates(int projectID, int timeStampID) {
		Reporter.report(logger, 2, "Calculating stock values and prices from stock quantities, unit values and unit prices");
		List<Stock> allStocks = Stock.all(projectID, timeStampID);
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
		for (Commodity u : Commodity.all(projectIDCurrent(), timeStampIDCurrent())) {
			if (u.getFunction() != Commodity.FUNCTION.MONEY) {
				double quantity = u.totalQuantity();
				double newUnitValue = u.totalValue() / quantity;
				Reporter.report(logger, 2, "The unit value of commodity [%s] was %.4f, and will be reset to %.4f",
						u.name(), u.getUnitValue(), newUnitValue);
				u.setUnitValue(newUnitValue);
			}
		}
		for (Stock s : Stock.all(projectIDCurrent(), timeStampIDCurrent())) {
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
			for (Commodity u : Commodity.all(projectIDCurrent(), timeStampIDCurrent())) {
				Reporter.report(logger, 2, "Commodity [%s] Total value is %.0f, and total price is %.0f", u.name(), u.totalValue(), u.totalPrice());
				totalValue += u.totalValue();
				totalPrice += u.totalPrice();
			}
		} else {
			for (Commodity u : Commodity.currentByFunction(projectIDCurrent(), timeStampIDCurrent(), Commodity.FUNCTION.PRODUCTIVE_INPUT)) {
				Reporter.report(logger, 2, "Commodity [%s] Total value is %.0f, and total price is %.0f", u.name(), u.totalValue(), u.totalPrice());
				totalValue += u.totalValue();
				totalPrice += u.totalPrice();

			}
			for (Commodity u : Commodity.currentByFunction(projectIDCurrent(), timeStampIDCurrent(), Commodity.FUNCTION.CONSUMER_GOOD)) {
				Reporter.report(logger, 2, "Commodity [%s] Total value is %.0f, and total price is %.0f", u.name(), u.totalValue(), u.totalPrice());
				totalValue += u.totalValue();
				totalPrice += u.totalPrice();
			}
		}
		Reporter.report(logger, 1, "Total value is %.0f, and Total price is %.0f", totalValue, totalPrice);

		logger.debug("Recorded total value is {}, and calculated total value is {}", timeStampCurrent.totalValue(), totalValue);
		logger.debug("Recorded total price is {}, and calculated total price is {}", timeStampCurrent.totalPrice(), totalPrice);

		if (!MathStuff.equals(totalValue, timeStampCurrent.totalValue()))
			Dialogues.alert(logger, "The total value of stocks is out of sync");
		if (!MathStuff.equals(totalPrice, timeStampCurrent.totalPrice()))
			Dialogues.alert(logger, "The total price of stocks is out of sync");
	}

	/**
	 * @param projectID
	 *            the projectID for which the capitals are being calculated
	 * @param timeStampID
	 *            the timeStampID for which the capitals are being calculated
	 *            initialise the initialCapital of each industry to be the price of its stocks when the period starts.
	 *            Initialise the currentCapital to be the same.
	 *            Called at startup and thereafter afterAccumulate (i.e. at the very end of the whole industry and start of the next)
	 */
	public static void setCapitals(int projectID, int timeStampID) {
		TimeStamp ts = TimeStamp.single(projectID, timeStampID);
		for (Industry c : Industry.all(projectID, timeStampID)) {
			double initialCapital = c.currentCapital();
			Reporter.report(logger, 3, "The initial capital of the industry[%s] is now $%.0f (intrinsic %.0f)", c.name(), initialCapital,
					initialCapital / ts.getMelt());
			c.setInitialCapital(initialCapital);
		}
		Reporter.report(logger, 2, "Total initial capital is now $%.0f (intrinsic %.0f)", ts.initialCapital(),
				ts.initialCapital() / ts.getMelt());
		Reporter.report(logger, 2, "The profit of the previous period has been erased from the record; it was stored during the price calculation");
		// Erase the public record of the profit of the previous period
		for (Industry c : Industry.all(projectID, timeStampID)) {
			c.persistProfit();
		}
	}

	/**
	 * initialise the initial productive capital of the industry, to be the price of its productive stocks
	 * Chiefly used for profit-rate equalization when we don't do full repricing
	 */

	public static void setInitialProductiveCapitals() {

		for (Industry c : Industry.all(projectIDCurrent(), timeStampIDCurrent())) {
			double productiveCapital = 0.0;
			for (Stock s : c.productiveStocks()) {
				productiveCapital += s.getPrice();
			}
			productiveCapital += c.salesPrice();
			c.setProductiveCapital(productiveCapital);
		}
	}

	public static void advanceOnePeriod() {
		setPeriodCurrent(getPeriodCurrent()+1);
		Reporter.report(logger, 0, "ADVANCING ONE PERIOD TO %d", getPeriodCurrent());
		// start another period so recompute the initial capitals and profits
		setCapitals(projectIDCurrent(), timeStampIDCurrent());
	}

	/**
	 * Switch from one project to another.
	 * <p>
	 * (1)copy the current timeStamp and timeStampDisplayCursor into the current Project record
	 * (2)retrieve the timeStamp and timeStampDisplayCursor from the new Project
	 * (3)save the current Project record to the database
	 * (4)set 'currentProject' to be the new project
	 * (5)the calling method must refresh the display
	 * 
	 * @param newProjectID
	 *            the ID of the project to switch to
	 * @param actionButtonsBox
	 *            the actionButtonsBox which has invoked the switch (and which knows the buttonState of the current project)
	 */

	public static void switchProjects(int newProjectID, ActionButtonsBox actionButtonsBox) {
		if (newProjectID == projectIDCurrent()) {
			logger.debug("The user switched to project {} which  is already current. No action was taken", newProjectID);
			return;
		}

		Project.getEntityManager().getTransaction().begin();

		// save the state of the present project so it knows how to return to the same point
		projectCurrent.setButtonState(ActionButtonsBox.getLastAction().text());

		Project.getEntityManager().getTransaction().commit();

		// retrieve the selected project record, and copy its various cursors and into the simulation cursors
		projectCurrent = Project.get(newProjectID);
		actionButtonsBox.setActionStateFromLabel(projectCurrent.getButtonState());
		DisplayControlsBox.setParameterComboPrompts();
		timeStampCurrent = TimeStamp.singleInProjectAndTimeStamp(newProjectID, timeStampIDCurrent());
		Reporter.report(logger, 0, "SWITCHED TO PROJECT %s (%s)", newProjectID, projectCurrent.getDescription());
		// ViewManager.getTabbedTableViewer().buildTables();
	}

	/**
	 * for all persistent entities at the given timeStamp, set comparators that refer to the timeStampComparatorCursor
	 * TODO previousComparator not yet properly implemented.
	 * 
	 * @param projectID
	 *            all persistent records at this timeStampID will be given comparators equal to the timeStampComparatorCursor
	 * @param timeStampID
	 *            all persistent records at this timeStampID will be given comparators equal to the timeStampComparatorCursor
	 */

	public static void setComparators(int projectID, int timeStampID) {
		try {
			Stock.setComparators(projectID, timeStampID);
			Commodity.setComparators(projectID, timeStampID);
			Industry.setComparators(projectID, timeStampID);
			SocialClass.setComparators(projectID, timeStampID);
			TimeStamp.setComparators(projectID, timeStampID);
		} catch (Exception e) {
			Dialogues.alert(logger, "Could not set comparators. Sorry, please contact developer");
		}
	}

	/**
	 * Delete all entities in the project with ID projectID (wipe project from persistent memory)
	 * 
	 * @param projectID
	 *            the projectID of the entire project to be deleted
	 */
	public static void deleteAllFromProject(int projectID) {
		Commodity.deleteFromProject(projectID);
		// TODO etcetera
	}

	/**
	 * @return the timeStampComparatorCursor
	 */
	public static int getTimeStampComparatorCursor() {
		return projectCurrent.getTimeStampComparatorCursor();
	}

	/**
	 * @param timeStampComparatorCursor
	 *            the timeStampComparatorCursor to set
	 */
	public static void setTimeStampComparatorCursor(int timeStampComparatorCursor) {
		projectCurrent.setTimeStampComparatorCursor(timeStampComparatorCursor);
	}

	/**
	 * 
	 * 
	 * /**
	 * 
	 * @return the periodCurrent
	 */
	public static int getPeriodCurrent() {
		return timeStampCurrent.getPeriod();
	}

	/**
	 * @param period
	 *            the periodCurrent to set
	 */
	public static void setPeriodCurrent(int period) {
		timeStampCurrent.setPeriod(period);
	}

	/**
	 * @return the projectIDCurrent
	 */
	public static int projectIDCurrent() {
		return projectCurrent.getProjectID();
	}

	/**
	 * @return the timeStampDisplayCursor
	 */
	public static int timeStampDisplayCursor() {
		return projectCurrent.getTimeStampDisplayCursor();
	}


	/**
	 * @param currentTimeStamp
	 *            the currentTimeStamp to set
	 */
	public static void setTimeStampCurrent(TimeStamp currentTimeStamp) {
		Simulation.timeStampCurrent = currentTimeStamp;
	}

	/**
	 * @return the currentProject
	 */
	public static Project getProjectCurrent() {
		return projectCurrent;
	}

	/**
	 * @return the current projectID
	 */
	public static int projectIDcurrent() {
		return projectCurrent.getProjectID();
	}

	/**
	 * @return the current timeStampID
	 */
	public static int timeStampIDCurrent() {
		return projectCurrent.getTimeStampID();
	}

	/**
	 * @param currentProject
	 *            the currentProject to set
	 */
	public static void setProjectCurrent(Project currentProject) {
		Simulation.projectCurrent = currentProject;
	}

	public static void setProjectID(int projectID) {
		projectCurrent.setProjectID(projectID);
	}

	/**
	 * @return the melt
	 */
	public static double melt() {
		return timeStampCurrent.getMelt();
	}

	/**
	 * @param melt
	 *            the melt to set
	 */
	public static void setMelt(double melt) {
		timeStampCurrent.setMelt(melt);
	}

	/**
	 * @param timeStampDisplayCursor
	 *            the timeStampDisplayCursor to set
	 */
	public static void setTimeStampDisplayCursor(int timeStampDisplayCursor) {
		projectCurrent.setTimeStampDisplayCursor(timeStampDisplayCursor);
	}
	
	/**
	 * @return the labourSupplyResponse stored in timeStampCurrent
	 */
	public static Parameters.LABOUR_RESPONSE labourSupplyResponse(){
		return timeStampCurrent.getLabourSupplyResponse();
	}

	/**
	 * @return the result of the totalPrice() method of timeStampCurrent
	 */
	public static double totalPrice() {
		return timeStampCurrent.totalPrice();
	}
	/**
	 * @return the result of the totalValue() method of timeStampCurrent
	 */
	public static double totalValue() {
		return timeStampCurrent.totalValue();
	}
	
	/**
	 * @return the result of the profitRate() method of timeStampCurrent
	 */
	public static double profitRate() {
		return timeStampCurrent.profitRate();
	}
	
	/**
	 * @return the meltResponse stored in timeStampCurrent
	 */
	public static Parameters.MELT_RESPONSE meltResponse(){
		return timeStampCurrent.getMeltResponse();
	}
	/**
	 * set the meltResponse stored in timeStampCurrent
	 * @param meltResponse the meltResponse to set
	 */
	public static void setMeltResponse(Parameters.MELT_RESPONSE meltResponse) {
		timeStampCurrent.setMeltResponse(meltResponse);
	}
	/**
	 * set the labourSupplyResponse stored in timeStampCurrent
	 * @param labourSupplyResponse the meltResponse to set
	 */
	public static void setLabourSupplyResponse(Parameters.LABOUR_RESPONSE labourSupplyResponse) {
		timeStampCurrent.setLabourSupplyResponse(labourSupplyResponse);
	}
	/**
	 * set the priceResponse stored in timeStampCurrent
	 * @param meltResponse the priceResponse to set
	 */
	public static void setPriceResponse(Parameters.PRICE_RESPONSE priceResponse) {
		timeStampCurrent.setPriceResponse(priceResponse);
	}
	
	/**
	 * return the priceResponse stored in timeStampCurrent
	 */
	public static Parameters.PRICE_RESPONSE priceResponse(){
		return timeStampCurrent.getPriceResponse();
	}
}
