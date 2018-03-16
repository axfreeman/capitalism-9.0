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

package capitalism.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.model.Commodity;
import capitalism.model.Commodity.FUNCTION;
import capitalism.model.Stock.OWNERTYPE;
import capitalism.model.Stock.STOCKTYPE;
import capitalism.model.Industry;
import capitalism.model.Project;
import capitalism.model.SocialClass;
import capitalism.model.Stock;
import capitalism.model.TimeStamp;

/**
 * Portmanteau class for validation tests
 */
public class Validate {

	private static final Logger logger = LogManager.getLogger("Validate");

	/**
	 * TODO:
	 * If a commodity is consumed, it must be produced.
	 * Every social class must have a sales stock for labour power even if it doesn't have any to sell (!)
	 * If a commodity type is productive, we should disallow stocks of it being consumption goods, and vice versa
	 * Every class should have a sales stock of labour power even if it doesn't produce it.
	 */

	private static Project project;

	/**
	 * carry out the global validation tests on the timeStamp and project records.
	 * Carry out the project-specific tests
	 * 
	 * @return true if all tests are passed, false otherwise
	 */

	public static boolean validate() {
		Reporter.report(logger, 0, "Validating");
		boolean valid = true;
		if (Validate.timeStampIntegrity()) {
			Reporter.report(logger, 1, "Passed time stamp integrity test");
		} else {
			valid = false;
		}
		for (Project p : Project.all()) {
			if (!Validate.validate(p.getProjectID()))
				valid = false;
		}
		return valid;
	}

	/**
	 * Conduct the per-project validation tests for the project with ID projectID
	 * 
	 * @param projectID
	 *            the ID of the project to be validated
	 * @return true if all tests are passed, false otherwise
	 */

	public static boolean validate(int projectID) {
		project = Project.get(projectID);
		boolean valid = true;
		Reporter.report(logger, 1, "Validating %d called %s", projectID, project.getDescription());
		if (timeStampIntegrity(projectID)) {
			Reporter.report(logger, 2, "Passed per-project time Stamp Integrity test");
		} else {
			valid = false;
		}

		if (consumerGoodTest(projectID)) {
			Reporter.report(logger, 2, "Passed consumer good existence test");
		} else {
			Reporter.report(logger, 2, "Validation error: there should be a commodity of origin 'CONSUMER_GOOD' called either 'Consumption' or 'Necessities'");
			valid = false;
		}

		if (labourPowerTest(projectID)) {
			Reporter.report(logger, 2, "Passed labour power existence test");
		} else {
			valid = false;
		}

		if (stockCommodityExistsTest(projectID)) {
			Reporter.report(logger, 2, "Passed stock commodity existence test");
		} else {
			valid = false;
		}

		if (stockOwnerExists(projectID)) {
			Reporter.report(logger, 2, "Passed stock owner test");
		}

		if (validStockType(projectID)) {
			Reporter.report(logger, 2, "Passed valid stock type test");
		}

		if (industryProduct(projectID)) {
			Reporter.report(logger, 2, "Passed industry product test");
		} else {
			valid = false;
		}
		
		if (inputCompleteness(projectID)) {
			Reporter.report(logger, 2, "Passed industry product test");
		} else {
			valid = false;
		}

		return valid;
	}

	/**
	 * TimeStamp integrity test
	 * No timeStamp should exist in the database that refers to a project which does not exist.
	 * Every timeStamp that is referenced by a project record should exist
	 * This is a global integrity test, and is not project-specific
	 * 
	 * @return true if the database passes this test, false otherwise
	 * 
	 */
	public static boolean timeStampIntegrity() {
		boolean valid = true;
		for (TimeStamp ts : TimeStamp.all()) {
			if (Project.get(ts.getTimeStampID()) == null) {
				Reporter.report(logger, 2, "A timeStamp with ID %d refers to a project with ID %d, which does not exist",
						ts.getTimeStampID(), ts.getProjectID());
				valid = false;
			}
		}
		for (Project p : Project.all()) {
			if (TimeStamp.single(p.getProjectID(), p.getTimeStampID()) == null) {
				Reporter.report(logger, 2, "The project with ID %d refers to a timeStamp with ID %d, which does not exist",
						p.getProjectID(), p.getTimeStampID());
				valid = false;
			}
		}
		return valid;
	}

	/**
	 * there should be a Project record for every project that is referenced by any other table in the database
	 * @return true if the project passes the test, false otherwise
	 */
	public static boolean projectIntegrity() {
		boolean valid = true;
		for (Commodity c : Commodity.all()) {
			if (Project.get(c.getProjectID()) == null) {
				Reporter.report(logger, 2, "A commodity called %s with timeStamp %d refers to project %d d which does not exist",
						c.name(), c.getTimeStampID(), c.getProjectID());
				valid = false;
			}
		}
		for (Industry ind : Industry.all()) {
			if (Project.get(ind.getProjectID()) == null) {
				Reporter.report(logger, 2, "An industry called %s with timeStamp %d refers to a project %d which does not exist",
						ind.name(), ind.getTimeStampID(), ind.getProjectID());
				valid = false;
			}
		}
		for (SocialClass sc : SocialClass.all()) {
			if (Project.get(sc.getProjectID()) == null) {
				Reporter.report(logger, 2, "A social Class called %s in project %d refers to a timeStamp %d which does not exist",
						sc.name(), sc.getProjectID(), sc.getTimeStampID());
				valid = false;
			}
		}
		for (Stock s : Stock.all()) {
			if (Project.get(s.getProjectID()) == null) {

				Reporter.report(logger, 2, "A stock of commodity %s, owned by %s, with timeStamp %d refers to a project %d which does not exist",
						s.name(), s.getOwner(), s.getTimeStampID(), s.getProjectID());
				valid = false;
			}
		}
		return valid;
	}

	/**
	 * Per project timeStamp integrity test
	 * There should be a TimeStamp record for every timeStamp that is referenced by any other table in the database
	 * 
	 * @param projectID
	 *            the ID of the project whose integrity is to be tested
	 * @return true if the project passes the test, false otherwise
	 */

	public static boolean timeStampIntegrity(int projectID) {
		boolean valid = true;
		for (Commodity c : Commodity.all(projectID)) {
			if (TimeStamp.singleInProjectAndTimeStamp(projectID, c.getTimeStampID()) == null) {
				Reporter.report(logger, 2, "A commodity called %s in project %d refers to a timeStamp %d which does not exist",
						c.name(), c.getProjectID(), c.getTimeStampID());
				valid = false;
			}
		}
		for (Industry ind : Industry.all(projectID)) {
			if (TimeStamp.singleInProjectAndTimeStamp(projectID, ind.getTimeStampID()) == null) {
				Reporter.report(logger, 2, "An industry called %s in project %d refers to a timeStamp %d which does not exist",
						ind.name(), ind.getProjectID(), ind.getTimeStampID());
				valid = false;
			}
		}
		for (SocialClass sc : SocialClass.all(projectID)) {
			if (TimeStamp.singleInProjectAndTimeStamp(projectID, sc.getTimeStampID()) == null) {

				Reporter.report(logger, 2, "A social Class called %s in project %d refers to a timeStamp %d which does not exist",
						sc.name(), sc.getProjectID(), sc.getTimeStampID());
				valid = false;
			}
		}
		for (Stock s : Stock.all(projectID)) {
			if (TimeStamp.singleInProjectAndTimeStamp(projectID, s.getTimeStampID()) == null) {

				Reporter.report(logger, 2, "A stock of commodity %s, owned by %s, in project %d, refers to a timeStamp %d which does not exist",
						s.name(), s.getOwner(), s.getProjectID(), s.getTimeStampID());
				valid = false;
			}
		}
		return valid;
	}

	/**
	 * test existence of consumer goods
	 * 
	 * @param projectID
	 *            the ID of the project we are testing
	 * @return true if the project passes this test, false otherwise
	 */
	public static boolean consumerGoodTest(int projectID) {
		/*
		 * A Commodity called 'consumption' must exist (Better: configure the name of the consumption good/s in the configuration file)
		 */
		boolean consumerGoodExists = false;
		for (Commodity commodity : Commodity.currentByFunction(projectID, 1, FUNCTION.CONSUMER_GOOD)) {
			if (commodity.name().equals("Consumption")) {
				consumerGoodExists = true;
			}
			if (commodity.name().equals("Necessities")) {
				consumerGoodExists = true;
			}
		}
		return consumerGoodExists;
	}

	/**
	 * A commodity called Labour Power must exist at all times
	 * @param projectID the ID of the project being tested
	 * @return true if a commodity called Labour Power exists, false otherwise
	 */
	private static boolean labourPowerTest(int projectID) {
		boolean valid = true;
		for (TimeStamp ts : TimeStamp.allInProject(projectID)) {
			if (Commodity.single(projectID, ts.getTimeStampID(), "Labour Power") == null) {
				Reporter.report(logger, 2, "Validation error: there is no commodity called Labour Power at time %d", ts);
				valid = false;
			}
		}
		return valid;
	}

	/**
	 * Every stock's commodity must exist
	 * 
	 * @param projectID
	 *            the ID of the project to which the test should be applied
	 * @return true if the project passes this test, false otherwise
	 */
	private static boolean stockCommodityExistsTest(int projectID) {
		for (Stock stock : Stock.all(projectID)) {
			String commodityName = stock.name();
			try {
				@SuppressWarnings("unused") Commodity commodity = Commodity.single(projectID, stock.getTimeStampID(), commodityName);
			} catch (Exception e) {
				Reporter.report(logger, 2, "Validation error: the stock called %s refers to a commodity which does not exist",
						commodityName);
				return false;
			}
		}
		return true;
	}

	/**
	 * Every stock must belong to either an industry or a social class that exists.
	 * @param projectID the ID of the project being tested
	 * @return true if a commodity called Labour Power exists, false otherwise
	 * 
	 */
	private static boolean stockOwnerExists(int projectID) {
		boolean valid = true;
		for (Stock stock : Stock.all(projectID)) {
			OWNERTYPE ownerType = stock.getOwnerType();
			String owner = stock.getOwner();
			switch (ownerType) {
			case CLASS:
				SocialClass ownerClass = SocialClass.single(projectID, stock.getTimeStampID(), owner);
				if (ownerClass == null) {
					Reporter.report(logger, 2,
							"Validation error: a stock of commodity %s belongs to socialClass %s, which does not exist", stock.name(), owner);
					valid = false;
				}
				break;
			case INDUSTRY:
				Industry ownerIndustry = Industry.single(projectID, stock.getTimeStampID(), owner);
				if (ownerIndustry == null) {
					Reporter.report(logger, 2,
							"Validation error: a stock of commodity %s belongs to industry %s, which does not exist", stock.name(), owner);
					valid = false;
				}
				break;
			default:
			case UNKNOWN:
				// this probably can't happen but no harm in trapping it
				Reporter.report(logger, 2,
						"Validation error: a stock of commodity %s has with owner %s an unknown owner type", stock.name(), owner);
				valid = false;
				break;
			}
		}
		return valid;
	}

	/**
	 * every stock must have a stock type in the enum STOCKTYPE
	 * @param projectID the ID of the project being tested
	 * @return true if a commodity called Labour Power exists, false otherwise

	 */
	private static boolean validStockType(int projectID) {
		boolean valid = true;
		for (Stock stock : Stock.all(projectID)) {
			String stockTypeText = stock.getStockType();
			try {
				@SuppressWarnings("unused") STOCKTYPE stockType = STOCKTYPE.valueOf(stockTypeText.toUpperCase());
			} catch (Exception e) {
				Reporter.report(logger, 2, "The stock of %s owned by %s has an invalid stock type %s",
						stock.name(), stock.getOwner(), stockTypeText);
				valid = false;
			}
		}
		return valid;
	}

	/**
	 * Every industry's product must be an existing commodity.
	 * The industry must have a sales stock and a money stock of this commodity.
	 * - we don't check that all the productive stocks of this industry are existing commodities, because
	 * that is taken care of by {@code stockCommodityExists}
	 * 
	 * @param projectID
	 *            the ID of the project to which the test should be applied
	 * @return true if the project passes this test, false otherwise
	 */
	private static boolean industryProduct(int projectID) {
		boolean valid = true;
		for (Industry industry : Industry.all(projectID)) {
			if (Commodity.single(projectID, industry.getTimeStampID(), industry.getCommodityName()) == null) {
				Reporter.report(logger, 2, "Validation error: the industry %s produces commodity %s that does not exist",
						industry.name(), industry.getCommodityName());
				valid = false;
			}
			// get the sales stock and see if it exists
			if (Stock.single(projectID, industry.getTimeStampID(), industry.name(), industry.getCommodityName(), STOCKTYPE.SALES.text()) == null) {
				Reporter.report(logger, 2, "Validation error: the industry %s has no sales stock", industry.name());
				valid = false;

			}
			// get the money stock and see if it exists
			if (Stock.single(projectID, industry.getTimeStampID(), industry.name(), industry.getCommodityName(), STOCKTYPE.SALES.text()) == null) {
				if (Stock.single(projectID, industry.getTimeStampID(), industry.name(), industry.getCommodityName(), STOCKTYPE.SALES.text()) == null) {
					Reporter.report(logger, 2, "Validation error: the industry %s has no money stock", industry.name());
					valid = false;
				}
			}
		}
		return valid;
	}
	
	/**
	 * Every Industry must have exactly one stock of every possible productive input
	 * @param projectID the ID of the project to check
	 * @return true if the test completes successfully, false if any errors are discovered
	 */

	public static boolean inputCompleteness(int projectID) {
		boolean valid = true;
		for (Industry ind:Industry.all(projectID)) {
			for (Commodity c: Commodity.currentByFunction(projectID, ind.getTimeStampID(), FUNCTION.PRODUCTIVE_INPUT)) {
				Stock s=Stock.single(projectID, ind.getTimeStampID(), ind.name(), c.name(), STOCKTYPE.PRODUCTIVE.text());
				if (s==null) {
					Reporter.report(logger, 2, "Validation error: the industry %s does not have a productive stock of the commodity %s at timeStamp %d",
							ind.name(),c.name(),ind.getTimeStampID());
					valid = false;
				}
			}
		}
		
		return valid;
	}

}
