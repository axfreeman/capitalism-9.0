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
package capitalism.controller.command;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.controller.Simulation;
import capitalism.model.Commodity;
import capitalism.model.Industry;
import capitalism.model.Stock;
import capitalism.utils.MathStuff;
import capitalism.utils.Reporter;
import capitalism.view.custom.ActionStates;

public class Constrain implements Command {
	private static final Logger logger = LogManager.getLogger(Constrain.class);

	public Constrain() {
	}

	/**
	 * An algorithm (eventually user-defined) to allocate the goods supplied between demanders. All industries adjust their proposed outputs in accordance with
	 * what they can get their hands on. It is not possible to acquire goods that do not exist.Also, industries cannot increase output above what they initially
	 * ask for, even if it is available. This is an investment function, and is to be dealt with under the distribution of the surplus.
	 * At present, just a simple share; there is space here for user-supplied algorithms corresponding to various models.

	 */
	public void execute() {
		Reporter.report(logger, 0, "CONSTRAINTS");

		Simulation.advanceOneStep(ActionStates.M_C_Constrain.text(), ActionStates.M_C_Exchange.text());

		// calculate what proportion of demand can actually be satisfied

		calculateAllocationShare();

		// Tell all stocks that are sources of demand (Productive and Consumption but not Sales or Money)
		// to lower their expectations

		allocateToStocks();

		// Then tell the industries to constrain their output

		constrainOutput();

		// There is no call to separately ask the classes to constrain themselves. They just tighten their belts.
		// TODO introduce some demographics - at some point of course restricted demand will impact population
		// though in possibly unexpected ways, eg by converting capitalists into workers, or paupers, or both.
	}

	/**
	 * 
	 * Lower the quantity demanded for each stock to reconcile it with what is available
	 * Do not increase it above what is already proposed
	 * This is the separate function of investment, which comes under distribution
	 * 
	 */

	public void allocateToStocks() {
		List<Stock> stockList = Stock.sourcesOfDemand(Simulation.projectIDCurrent(),Simulation.timeStampIDCurrent());
		Reporter.report(logger, 1, "Constraining demand for stocks, on the basis of constraints on output levels");

		for (Stock s : stockList) {
			String commodityType = s.name();
			Commodity u = s.getCommodity();
			double allocationShare = u.getAllocationShare();
			double newQuantityDemanded = s.getReplenishmentDemand() * allocationShare;
			Reporter.report(logger, 2, "Demand for [%s] in industry [%s] was %.0f and is now %.0f",
					commodityType, s.getOwner(), s.getReplenishmentDemand(), newQuantityDemanded);
			s.setReplenishmentDemand(newQuantityDemanded);
		}
	}

	/**
	 * 
	 * Lower the output of each producer to reconcile it with what is available
	 * Do not increase it above what is already proposed
	 * This is the separate function of investment, which comes under distribution
	 * 
	 */

	public void constrainOutput() {
		List<Industry> industries = Industry.all(Simulation.projectIDCurrent(),Simulation.timeStampIDCurrent());
		for (Industry c : industries) {
			double desiredOutputLevel = c.getOutput();
			Reporter.report(logger, 1, "Estimating supply-constrained output for industry [%s] with unconstrained output %.0f",
					c.name(), desiredOutputLevel);
			List<Stock> managedStocks = Stock.allProductiveInIndustry(Simulation.projectIDcurrent(),Simulation.timeStampIDCurrent(), c.name());
			for (Stock s : managedStocks) {
				double existingQuantity = s.getQuantity();
				double quantityDemanded = s.getReplenishmentDemand();
				double quantityAvailable = existingQuantity + s.getReplenishmentDemand();
				double coefficient = s.getProductionCoefficient();
				if (coefficient > 0) {
					double possibleOutput = quantityAvailable / coefficient;
					if (possibleOutput < desiredOutputLevel-MathStuff.epsilon) {
						Reporter.report(logger, 2, "Constraining output to %.0f because stock [%s] has a supply of %.0f ",
								possibleOutput, s.name(), quantityDemanded);
						desiredOutputLevel = possibleOutput;
					} else {
						Reporter.report(logger, 2, "Output was not constrained by the stock of [%s] which can supply %.0f allowing for output of %.0f",
								s.name(), quantityAvailable, possibleOutput);
					}
				}
			}
			Reporter.report(logger, 2, "Output of [%s] has been set to %.0f; unconstrained output was %.0f",
					c.name(), desiredOutputLevel, c.getOutput());
			c.setOutput(desiredOutputLevel);
		}
	}

	/**
	 * given supply and demand, calculate what proportion of demand can actually be satisfied.
	 * 
	 */
	public void calculateAllocationShare() {
		Reporter.report(logger, 1, "Computing the proportion of demand that can be satisfied by supply, for each commodity type");
		for (Commodity u : Commodity.all(Simulation.projectIDcurrent(),Simulation.timeStampIDCurrent())) {
			double totalDemand = u.replenishmentDemand();
			double totalSupply = u.totalSupply();
			double allocationShare = totalSupply / totalDemand;
			allocationShare = (allocationShare > 1 ? 1 : allocationShare);
			Reporter.report(logger, 2, "Allocation share for commodity [%s] is %.4f", u.name(), allocationShare);
			u.setAllocationShare(allocationShare);
		}
	}
}