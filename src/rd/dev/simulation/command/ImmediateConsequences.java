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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rd.dev.simulation.Simulation;
import rd.dev.simulation.custom.ActionStates;
import rd.dev.simulation.datamanagement.DataManager;
import rd.dev.simulation.model.Circuit;
import rd.dev.simulation.model.Global;
import rd.dev.simulation.model.Stock;
import rd.dev.simulation.model.UseValue;
import rd.dev.simulation.utils.Reporter;

public class ImmediateConsequences extends Simulation implements Command {
	private static final Logger logger = LogManager.getLogger(ImmediateConsequences.class);

	/**
	 * Calculate the unit value and price of each use value.
	 * Recalculate the MELT.
	 * Tell the stocks to adjust the total values and prices
	 * TODO CHECK INVARIANTS AT THIS POINT?
	 */
	public void execute() {
		Reporter.report(logger, 0, "VALIDATE STOCK AND COMMODITY AGGREGATES");
		// recalculate the use value aggregates from the stocks, because this does not happen automatically
		// (it could, but there would be a cost and it would open the door to bugs arising from failures to update usevalues following a change in stocks)
		calculateUseValueAggregates(true);

		Reporter.report(logger, 0, "RECALCULATE UNIT VALUES AND PRICES");
		advanceOneStep(ActionStates.C_P_ImmediateConsequences.getText(), ActionStates.C_P_Produce.getText());
		double globalTotalValue = 0.0;
		double globalTotalPrice = 0.0;
		Global global = DataManager.getGlobal(timeStampIDCurrent);
		List<UseValue> useValues = DataManager.useValuesAll(timeStampIDCurrent);

		// Note; the following step is nothing to do with price dynamics but corrects for an artefact of the simulation.
		// Because prices are calculated serially, for each branch of production independent of the others, we end up with
		// a price that may differ from value. Here, we merely correct for this error
		// in the revenue and distribution stages, we will introduce price dynamics, and this will require us to recalculat the MELT
		// Hopefully this clears up a fundamental confusion in earlier stages of this simulation
		// but not, interestingly enough, the 1992 simulation.

		// First, reset all unit values on the basis of the total value and total quantity of this commodity in existence
		// (at the same time, record the total value and total price in the system, in preparation for the next step)

		// TODO: the value of money has to be dealt with properly. 
		// for now, deal with by exempting money
		for (UseValue u : useValues) {
			Reporter.report(logger, 1, "Use value [%s]", u.getUseValueType());
			Reporter.report(logger, 2, "Unit value was %.2f, and unit price was %.2f", u.getUnitValue(), u.getUnitPrice());
			double quantity = u.getTotalQuantity();
			double value = u.getTotalValue();
			double price = u.getTotalPrice();
			Reporter.report(logger, 2, "Total value was %.2f, and total price was %.2f", value, price);
			double newUnitValue = value / quantity;
			Reporter.report(logger, 2, "Unit value will be reset to %.2f", newUnitValue);
			u.setUnitValue(newUnitValue);
			if (!u.getUseValueType().equals("Money")) {
				globalTotalValue += value;
				globalTotalPrice += price;
			}else {
				Reporter.report(logger, 2, "Note: Code to adjust the value of money not yet written");
			}
		}
		double adjustmentFactor = globalTotalValue / globalTotalPrice;
		global.setTotalPrice(globalTotalValue);
		Reporter.report(logger, 1, "Global total value was %.2f, and total price was %.2f; all prices will now be scaled down by %.2f",
				globalTotalValue, globalTotalPrice, adjustmentFactor);
		Reporter.report(logger, 2, "NOTE: this is to correct for an artefact of the simulation and has no theoretical significance");
		Reporter.report(logger, 2, "As recorded, value was %.2f, and price was %.2f. These should equal the last report line", global.getTotalValue(),
				global.getTotalPrice());

		// now, reset unit prices so that total price = total value
		// NOTE this is the extrinsic (monetary) expression that is being reset
		// We cannot deal with the intrinsic (labour time) expression yet because we have not dealt with the price dynamics

		for (UseValue u : useValues) {
			if (!u.getUseValueType().equals("Money")) {
				double adjustedUnitPrice = u.getUnitPrice() * adjustmentFactor;
				Reporter.report(logger, 2, "The unit price of [%s] will be set to %.2f", u.getUseValueType(), adjustedUnitPrice);
				u.setUnitPrice(adjustedUnitPrice);
			}else {
				Reporter.report(logger, 2, "Note: Code to adjust the value of money not yet written");
			}
		}

		// recalculate the values and prices of each stock on the basis of the new unit values and prices
		stockValuesRecalculate();

		// and recalculate the values and prices of the use values on the basis of the stock total values and prices we just re-calcualtex
		calculateUseValueAggregates(false);
		
		// finally, calculate the profits that resulted from the combination of production and revaluation.
		// NOTE these are the unmodified profits, before any equalization of profit rates or indeed,
		// any movement of profit rates caused by market operation and price formation.
		calculateProfits();
	}

	/**
	 * when unit values and prices change, the stocks have to be told to recalculate their total price and total value
	 */
	private void stockValuesRecalculate() {
		for (Stock s : DataManager.stocksAll(timeStampIDCurrent)) {
			s.reCalculateStockTotalValuesAndPrices();
		}
	}
	
	private void calculateProfits() {
		Global global = DataManager.getGlobal(timeStampIDCurrent);
		double globalProfit = 0.0;
		double globalInitialCapital = 0.0;
		double globalCurrentCapital = 0.0;
		Reporter.report(logger, 1, " Calculate profits and profit rates");
		for (Circuit c : DataManager.circuitsAll(timeStampIDCurrent)) {
			c.calculateCurrentCapital();
			double profit = c.getCurrentCapital() - c.getInitialCapital();
			globalInitialCapital += c.getInitialCapital();
			globalCurrentCapital += c.getCurrentCapital();
			globalProfit += profit;
			double profitRate = profit / c.getInitialCapital();
			Reporter.report(logger, 2, "  The initial capital of industry [%s] was %.2f; current capital is %.2f; profit is %.2f", c.getProductUseValueType(),
					c.getInitialCapital(), c.getCurrentCapital(), profit);
			c.setProfit(profit);
			c.setRateOfProfit(profitRate);
		}
		global.setCurrentCapital(globalCurrentCapital);
		global.setInitialCapital(globalInitialCapital);
		global.setProfit(globalProfit);
		global.setProfitRate(globalProfit / globalInitialCapital);
		Reporter.report(logger, 1, "Total profit %.2f, initial capital %.2f, global profit rate %.2f",
				globalProfit, globalInitialCapital, globalProfit / globalInitialCapital);
	}


}