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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rd.dev.simulation.Simulation;
import rd.dev.simulation.custom.ActionStates;
import rd.dev.simulation.model.Industry;
import rd.dev.simulation.model.Global;
import rd.dev.simulation.model.Project;
import rd.dev.simulation.model.Stock;
import rd.dev.simulation.model.Commodity;
import rd.dev.simulation.utils.Dialogues;
import rd.dev.simulation.utils.MathStuff;
import rd.dev.simulation.utils.Reporter;

public class PriceDynamics extends Simulation implements Command {
	private static final Logger logger = LogManager.getLogger(PriceDynamics.class);
	Project currentProject = null;

	/**
	 * Calculate the unit value and price of each commodity.
	 * Recalculate the MELT.
	 * Tell the stocks to adjust the total values and prices
	 * TODO: the value of money has to be dealt with properly. For now, deal with by exempting money from the adjustment
	 */

	public void execute() {
		currentProject = Project.projectSingle(projectCurrent);
		Reporter.report(logger, 0, "PRICE DYNAMICS (%s)", currentProject.getPriceDynamics());
		advanceOneStep(ActionStates.C_M_Prices.getText(), ActionStates.C_M_Distribute.getText());

		// adjust prices depending on the price adjustment mechanism specific to the project (no change, equalization, or dynamic)
		computePrices();

		// recalculate the values and prices of each stock on the basis of the new unit values and prices
		for (Stock s : Stock.all(timeStampIDCurrent)) {
			s.reCalculateStockTotalValuesAndPrices();
		}
		advanceOnePeriod();
	}

	/**
	 * adjust prices, depending on the setting of the price dynamics in the current project
	 */

	private void computePrices() {
		Global global = Global.getGlobal();
		switch (currentProject.getPriceDynamics()) {
		case SIMPLE:
			// for the simple case do nothing
			return;
		case DYNAMIC:
			Dialogues.alert(logger, "Dynamic price adjustment not available yet, sorry");
			// For the dynamic case do nothing much at present except reset the MELT.
			// This can only change if the price dynamics are DYNAMIC, that is, if prices are
			// established by market forces external to production. However as yet we don't
			// have any code for price dynamics so this is here because eventually we will need it.

			double oldMelt = global.getMelt();
			double adjustmentFactor = global.totalPrice() / global.totalValue();
			if (!MathStuff.equals(adjustmentFactor, 1)) {
				double newMelt = oldMelt * (adjustmentFactor);
				Reporter.report(logger, 1, "MELT was %.4f and will be reset to %.4f", oldMelt, newMelt);
				global.setMelt(newMelt);
			}

			return;
		case EQUALISE:
			Reporter.report(logger, 1, "Setting prices to equalise profit rates");
			Reporter.report(logger, 2, "Average Profit Rate is currently recorded as %.4f", global.profitRate());

			// we can only set the profit rate for the sector as a whole, which means we work from the per-commodity profit rates
			for (Commodity u : Commodity.commoditiesByOrigin(Commodity.ORIGIN.INDUSTRIALLY_PRODUCED)) {
				Reporter.report(logger, 2, "Setting profit-equalizing price for commodity [%s] in which profit rate is %.4f",
						u.commodityName(), u.profitRate());
				for (Industry c : u.industries()) {
					Reporter.report(logger, 3, "Note: industry %s produces this commodity", c.getIndustryName());
				}
				double profitRate = global.profitRate();
				double profit = global.profit();
				double initialCapital = u.initialCapital();
				double currentCapital = u.currentCapital();
				double totalPrice = initialCapital * (1 + global.profitRate());
				double totalValue = initialCapital + profit;
				double priceValueRatio = totalPrice / totalValue;
				double newUnitPrice = priceValueRatio * u.getUnitValue();
				Reporter.report(logger, 2,
						"Initial Capital $%.0f, current Capital $%.0f, profit rate %.4f, total price $%.0f, total value $%.0f, price-value ratio %.4f, new unit price $%.4f",
						initialCapital, currentCapital, profitRate, totalPrice, totalValue, priceValueRatio, newUnitPrice);
				u.setUnitPrice(newUnitPrice);
			}
			// if the prices of consumption goods have changed, the wage will change
			double totalWage = 0;
			for (Commodity u : Commodity.commoditiesByFunction(Commodity.FUNCTION.CONSUMER_GOOD)) {
				double price = u.getUnitPrice();
				Reporter.report(logger, 3, "Wage earners just consumed %.0f of [%s] which would add $%.0f to the price of their labour power at the new prices",
						u.getStockUsedUp(), u.commodityName(), u.getStockUsedUp() * price);
				totalWage += u.getStockUsedUp() * price;
			}
			// multiple types of labour power are beyond us at this point because we cannot as yet attribute
			// specific types of consumption to specific sellers of labour power
			Commodity labourPower = Commodity.labourPower();
			double labourPowerSupplied = labourPower.getStockUsedUp();
			double wageRate = totalWage / labourPowerSupplied;
			Reporter.report(logger, 2, "%.0f of Labour Power was consumed and the current cost of feeding them is $%.0f, so the wage will be reset to $%.4f",
					labourPowerSupplied, totalWage, wageRate);
			labourPower.setUnitPrice(wageRate);
		}
	}
}