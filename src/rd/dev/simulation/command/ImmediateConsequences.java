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

import rd.dev.simulation.Capitalism;
import rd.dev.simulation.Simulation;
import rd.dev.simulation.custom.ActionStates;
import rd.dev.simulation.datamanagement.DataManager;
import rd.dev.simulation.model.Circuit;
import rd.dev.simulation.model.Global;
import rd.dev.simulation.model.Project;
import rd.dev.simulation.model.Stock;
import rd.dev.simulation.model.UseValue;
import rd.dev.simulation.utils.Dialogues;
import rd.dev.simulation.utils.Reporter;

public class ImmediateConsequences extends Simulation implements Command {
	private static final Logger logger = LogManager.getLogger(ImmediateConsequences.class);
	Project currentProject=null;

	/**
	 * Calculate the unit value and price of each use value.
	 * Recalculate the MELT.
	 * Tell the stocks to adjust the total values and prices
	 * TODO CHECK INVARIANTS AT THIS POINT?
	 */
	public void execute() {
		currentProject=Capitalism.selectionsProvider.projectSingle(projectCurrent);		
		Reporter.report(logger, 0, "RECOMPUTE THE MELT, UNIT VALUES AND PRICES, AND HENCE THE MONETARY EXPRESSION OF TOTAL VALUE");
		Reporter.report(logger, 0, "Price dynamics are set to %s ", currentProject.getPriceDynamics());
		advanceOneStep(ActionStates.C_P_ImmediateConsequences.getText(), ActionStates.C_P_Produce.getText());
		Reporter.report(logger, 0, "VALIDATE STOCK AND COMMODITY AGGREGATES");

		// TODO: the value of money has to be dealt with properly.
		// for now, deal with by exempting money

		// A little consistency check

		double globalTotalValue = 0.0;
		double globalTotalPrice = 0.0;
		Global global = DataManager.getGlobal(timeStampIDCurrent);
		List<UseValue> useValues = DataManager.useValuesAll(timeStampIDCurrent);

		for (UseValue u : useValues) {
			Reporter.report(logger, 1, "Commodity [%s]", u.getUseValueName());
			Reporter.report(logger, 2, "Total value is %.2f, and total price is %.2f", u.getTotalValue(), u.getTotalPrice());
			globalTotalValue += u.getTotalValue();
			globalTotalPrice += u.getTotalPrice();
		}
		Reporter.report(logger, 1, "Global total value is %.2f, and total price is %.2f", globalTotalValue, globalTotalPrice);

		logger.debug("Recorded global total value is %.2f, and calculated total value is %.2f", global.getTotalValue(), globalTotalValue);
		logger.debug("Recorded global total price is %.2f, and calculated total price is %.2f", global.getTotalPrice(), globalTotalPrice);
		
		if (globalTotalValue != global.getTotalValue())
			Dialogues.alert(logger, "The computed global total value is out of sync with recorded total value");
		if (globalTotalPrice != global.getTotalPrice())
			Dialogues.alert(logger, "The computed global total price is is out of sync with recorded total price");

		adjustPrices();
		
		// Reset the MELT.
		// NOTE: values and prices are recorded as a monetary expression. Therefore, if the MELT changes, values also have to change

		double oldMelt = global.getMelt();
		double adjustmentFactor = globalTotalPrice / globalTotalValue;
		double newMelt = oldMelt * (adjustmentFactor);

		Reporter.report(logger, 1, "MELT was %.2f and will be reset to %.2f", oldMelt, newMelt);
		global.setMelt(newMelt);

		// Reset all unit values on the basis of the total value and total quantity of this commodity in existence

		for (UseValue u : useValues) {
			if (u.getUseValueType() != UseValue.USEVALUETYPE.MONEY) {
				double quantity = u.getTotalQuantity();
				double newUnitValue = Precision.round(adjustmentFactor * u.getTotalValue() / quantity, Simulation.roundingPrecision);
				Reporter.report(logger, 2, "The unit value of commodity [%s] was %.2f, and will be reset to %.2f", u.getUseValueName(),u.getUnitValue(), newUnitValue);
				u.setUnitValue(newUnitValue);
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
	}
	
	/**
	 * adjust prices, depending on the setting of the price dynamics in the current project
	 */

	private void adjustPrices() {
		switch(currentProject.getPriceDynamics()) {
		case SIMPLE:
			// for the simple case do nothing
			return;
		case DYNAMIC:
			Dialogues.alert(logger, "Dynamic price adjustment not available yet, sorry");
		case EQUALISE:
			Reporter.report(logger, 1, "Setting prices to equalise profit rates");
			Global global =DataManager.getGlobal(timeStampIDCurrent);
			Reporter.report(logger, 1, "Average Profit Rate is %.2f", global.getProfitRate());

			// there may be more than one producer of the same commodity.
			// we can only set the profit rate for the sector as a whole,which means we work from the per-useValue profit rates
	
			for (UseValue u:DataManager.useValuesProductive(timeStampIDCurrent)) {
				Reporter.report(logger, 1, "Setting profit-equalizing price for use value [%s]", u.getUseValueName());
				for (Circuit c:DataManager.circuitsByProductUseValue(u.getUseValueName())) {
					Reporter.report(logger, 2, " Note: circuit %s is produces this use value", c.getProductUseValueType());
				}
				double newUnitPrice=u.getCapital()*(1+global.getProfitRate())/u.getTotalQuantity();
				Reporter.report(logger, 2, "  Unit price changed from %.2f to %.2f", u.getUnitPrice(),newUnitPrice);
				u.setUnitPrice(newUnitPrice);
			}
		}
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
		
		// initialise the capitals and sv of each use value
		// we are going to set the both to be the total of the circuits that produce this useValue
		
		for (UseValue u:DataManager.useValuesAll(timeStampIDCurrent)) {
			u.setCapital(0);
			u.setSurplusValue(0);
		}

		Reporter.report(logger, 1, " Calculate profits and profit rates");
		for (Circuit c : DataManager.circuitsAll()) {
			UseValue useValue=c.getUseValue();
			useValue.setCapital(useValue.getCapital()+c.getInitialCapital());
			c.calculateCurrentCapital();
			double profit = c.getCurrentCapital() - c.getInitialCapital();
			useValue.setSurplusValue(useValue.getSurplusValue()+profit);
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