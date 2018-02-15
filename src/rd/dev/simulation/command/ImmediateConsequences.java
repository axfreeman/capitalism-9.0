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

import org.apache.commons.math3.util.Precision;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rd.dev.simulation.Simulation;
import rd.dev.simulation.custom.ActionStates;
import rd.dev.simulation.datamanagement.DataManager;
import rd.dev.simulation.datamanagement.SelectionsProvider;
import rd.dev.simulation.model.Industry;
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
		currentProject=SelectionsProvider.projectSingle(projectCurrent);		
		Reporter.report(logger, 0, "Recompute unit values and prices and hence the Monetary Expression of Value");
		Reporter.report(logger, 0, "Price dynamics are set to %s ", currentProject.getPriceDynamics());
		advanceOneStep(ActionStates.C_P_ImmediateConsequences.getText(), ActionStates.C_P_Produce.getText());
		Reporter.report(logger, 0, "VALIDATE STOCK AND COMMODITY AGGREGATES");

		// TODO: the value of money has to be dealt with properly.
		// for now, deal with by exempting money

		// A little consistency check...

		double globalTotalValue = 0.0;
		double globalTotalPrice = 0.0;
		Global global = DataManager.getGlobal();
		for (UseValue u :  DataManager.useValuesAll()) {
			Reporter.report(logger, 1, "Commodity [%s] Total value is %.0f, and total price is %.0f", u.getUseValueName(),u.totalValue(), u.totalPrice());
			globalTotalValue += u.totalValue();
			globalTotalPrice += u.totalPrice();
		}
		Reporter.report(logger, 1, "Global total value is %.0f, and total price is %.0f", globalTotalValue, globalTotalPrice);

		logger.debug("Recorded global total value is {}, and total value calculated from use values is {}", global.totalValue(), globalTotalValue);
		logger.debug("Recorded global total price is {}, and total price calculated from use values is {}", global.totalPrice(), globalTotalPrice);
		
		if (!Precision.equals(globalTotalValue,global.totalValue(),Simulation.roundingPrecision))
			Dialogues.alert(logger, "The total value of stocks is out of sync");
		if (!Precision.equals(globalTotalPrice,global.totalPrice(),Simulation.roundingPrecision))
			Dialogues.alert(logger, "The total price of stocks is out of sync");

		// ... end of the consistency check

		// adjust prices depending on the price adjustment mechanism specific to the project (no change, equalization, or dynamic)
		adjustPrices();
		
		// Reset the MELT.
		// NOTE: values and prices are recorded as a monetary expression. Therefore, if the MELT changes, values also have to change

		double oldMelt = global.getMelt();
		double adjustmentFactor = globalTotalPrice / globalTotalValue;
		double newMelt = oldMelt * (adjustmentFactor);

		Reporter.report(logger, 1, "MELT was %.4f and will be reset to %.4f", oldMelt, newMelt);
		global.setMelt(newMelt);

		// Reset all unit values on the basis of the total value and total quantity of this commodity in existence

		for (UseValue u : DataManager.useValuesAll()) {
			if (u.getUseValueType() != UseValue.USEVALUETYPE.MONEY) {
				double quantity = u.totalQuantity();
				double newUnitValue = Precision.round(adjustmentFactor * u.totalValue() / quantity, Simulation.roundingPrecision);
				Reporter.report(logger, 2, "The unit value of commodity [%s] was %.4f, and will be reset to %.4f", u.getUseValueName(),u.getUnitValue(), newUnitValue);
				u.setUnitValue(newUnitValue);
			}
		}

		// recalculate the values and prices of each stock on the basis of the new unit values and prices

		stockValuesRecalculate();
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
			Reporter.report(logger, 0, "Setting prices to equalise profit rates");
			Global global =DataManager.getGlobal();
			Reporter.report(logger, 1, "Average Profit Rate is currently recorded as %.4f", global.profitRate());

			// there may be more than one producer of the same commodity.
			// we can only set the profit rate for the sector as a whole,which means we work from the per-useValue profit rates
	
			for (UseValue u:DataManager.useValuesByIndustryType(UseValue.USEVALUEINDUSTRYTYPE.CAPITALIST)) {
				Reporter.report(logger, 1, " Setting profit-equalizing price for use value [%s]", u.getUseValueName());
				for (Industry c:DataManager.industriesByProductUseValue(u.getUseValueName())) {
					Reporter.report(logger, 2, " Note: industry %s produces this use value", c.getProductUseValueName());
				}
				double newUnitPrice=u.initialCapital()*(1+global.profitRate())/u.totalQuantity();
				Reporter.report(logger, 2, "  Unit price changed from %.4f to %.4f", u.getUnitPrice(),newUnitPrice);
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
}