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
package rd.dev.simulation.command;

import java.util.List;

import org.apache.commons.math3.util.Precision;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rd.dev.simulation.Simulation;
import rd.dev.simulation.custom.ActionStates;
import rd.dev.simulation.datamanagement.DataManager;
import rd.dev.simulation.model.Circuit;
import rd.dev.simulation.model.SocialClass;
import rd.dev.simulation.model.Stock;
import rd.dev.simulation.model.UseValue;
import rd.dev.simulation.utils.Reporter;

public class Constrain extends Simulation implements Command {
	private static final Logger logger = LogManager.getLogger(Constrain.class);

	public Constrain() {
	}

	/**
	 * An algorithm (eventually user-defined) to allocate the goods supplied between demanders. All circuits adjust their proposed outputs in accordance with
	 * what they can get their hands on. It is not possible to acquire goods that do not exist.Also, circuits cannot increase output above what they initially
	 * ask for, even if it is available. This is an investment function, and is to be dealt with under the distribution of the surplus.
	 * At present, just a simple share; there is space here for user-supplied algorithms corresponding to various models.
	 * 
	 * TODO there may be multiple suppliers of the same good, in which case it's also necessary to decide who gets to supply what.
	 */
	public void execute() {
		Reporter.report(logger, 0, "CONSTRAINING DEMAND ON THE BASIS OF AVAILABLE SUPPLY");

		advanceOneStep(ActionStates.M_C_Constrain.getText(), ActionStates.M_C_PreTrade.getText());

		// calculate what proportion of demand can actually be satisfied
		
		calculateAllocationShare();

		// Tell all stocks that are sources of demand (Productive and Consumption but not Sales or Money)
		// to lower their expectations

		allocateToStocks();

		// Then tell the capital circuits to constrain their output

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
		List<Stock> stockList = DataManager.stocksSourcesOfDemand();
		Reporter.report(logger, 1, " Constraining demand for stocks, on the basis of constraints on output levels");

		for (Stock s : stockList) {
			String useValueType = s.getUseValueName();
			UseValue u = s.getUseValue();
			double allocationShare = u.getAllocationShare();
			double newQuantityDemanded = s.getQuantityDemanded() * allocationShare;
			newQuantityDemanded = Precision.round(newQuantityDemanded, Simulation.getRoundingPrecision());
			Reporter.report(logger, 2, "  Demand for [%s] in circuit [%s] was %.2f and is now %.2f",
					useValueType, s.getCircuit(), s.getQuantityDemanded(), newQuantityDemanded);
			s.setQuantityDemanded(newQuantityDemanded);
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
		Reporter.report(logger, 1, " Adjusting the output of each industry");

		List<Circuit> circuits = DataManager.circuitsAll();
		for (Circuit c : circuits) {
			double desiredOutputLevel = c.getProposedOutput();
			Reporter.report(logger, 1, " Estimating supply-constrained output for industry [%s] with unconstrained output %.2f",
					c.getProductUseValueName(), desiredOutputLevel);
			List<Stock> managedStocks = DataManager.stocksProductiveByCircuit(timeStampIDCurrent, c.getProductUseValueName());
			for (Stock s : managedStocks) {
//				UseValue useValue = DataManager.useValueByName(timeStampIDCurrent, s.getUseValueName());
				double existingQuantity = s.getQuantity();
				double quantityDemanded = s.getQuantityDemanded();
				double quantityAvailable = existingQuantity + s.getQuantityDemanded();
				double coefficient = s.getCoefficient();
				double possibleOutput = Precision.round(quantityAvailable / coefficient ,roundingPrecision);
				if (possibleOutput < desiredOutputLevel) {
					Reporter.report(logger, 2, "  Constraining output to %.2f because stock [%s] has a supply of %.2f ",
							possibleOutput, s.getUseValueName(), quantityDemanded);
					desiredOutputLevel = possibleOutput;
				} else {
					Reporter.report(logger, 2, "  Output was not constrained by the stock of [%s] which can supply %.2f allowing for output of %.2f",
							s.getUseValueName(), quantityAvailable, possibleOutput);
				}
			}
			Reporter.report(logger, 1, " Output of [%s] has been constrained to %.2f; unconstrained output was %.2f",
					c.getProductUseValueName(), desiredOutputLevel, c.getProposedOutput());
			c.setConstrainedOutput(desiredOutputLevel);
		}
	}


	/**
	 * given supply and demand, calculate what proportion of demand can actually be satisfied.
	 * 
	 */
	public void calculateAllocationShare() {
		Reporter.report(logger, 0, "CALCULATE ALLOCATION SHARES");
	
		for (UseValue u : DataManager.useValuesAll()) {
			double totalDemand = u.getTotalDemand();
			double totalSupply = u.getTotalSupply();
			double allocationShare = totalSupply / totalDemand;
			allocationShare = (allocationShare > 1 ? 1 : allocationShare);
			Reporter.report(logger, 1, " Allocation share for commodity [%s] is %.2f", u.getUseValueName(), allocationShare);
			u.setAllocationShare(allocationShare);
		}
	}

	
	/**
	 * the classes also have to constrain their consumption corresponding to any shortages
	 * 
	 */

	public void constrainClasses() {
		Reporter.report(logger, 1, " Constraining consumption by classes");
		for (UseValue u:DataManager.useValuesOfType(UseValue.USEVALUETYPE.NECESSITIES)){
			double allocationShare = u.getAllocationShare();
			for (SocialClass sc : DataManager.socialClassesAll()) {
				double quantityDemanded = sc.consumptionQuantityDemanded();
				if (allocationShare != 1) {
					quantityDemanded = Precision.round(quantityDemanded, Simulation.getRoundingPrecision());
					Reporter.report(logger, 2, "  Consumption of class [%s] will be cut from %.2f to %.2f",
							sc.getSocialClassName(), quantityDemanded, quantityDemanded * allocationShare);
					sc.setConsumptionQuantityDemanded(quantityDemanded * allocationShare);
				} else {
					Reporter.report(logger, 2, "  Consumption of class [%s] is unchanged at %.2f", sc.getSocialClassName(), quantityDemanded);
				}
			}
		}
	}
}