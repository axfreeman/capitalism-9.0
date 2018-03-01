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

package capitalism.command;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.Simulation;
import capitalism.model.Commodity;
import capitalism.model.Global;
import capitalism.model.Industry;
import capitalism.model.Project;
import capitalism.model.Stock;
import capitalism.utils.Dialogues;
import capitalism.utils.MathStuff;
import capitalism.utils.Reporter;
import capitalism.view.custom.ActionStates;

/**
 * Implements the project- and user-determined pricing policies. This is the traditional 'end of the period'
 * when new prices and new values are established. It appears (confusingly, perhaps) in the middle of the
 * period as we currently present it because it is followed by distribution.
 * 
 * In reality, as established in 'axiomatic foundations', the aim is to account separately for the effects
 * of production and distribution, not to establish a particular time within the calculation when prices
 * change. It's nevertheless a confusing issue and requires thought and documentation.
 * 
 * Be that as it may, the process is
 * 
 * (1) establish the new prices (2) calculate the MELT (3) recompute unit and total values and prices
 * 
 * The recomputed values are the values of the 'next' period in the traditional sense. Thus, we suppose
 * that distribution and accumulation take place on the basis of the new prices arising from production.
 * 
 * We could possible place distribution at the start of the period, which would allow the new prices
 * to be established at the end of it.
 */

public class PriceDynamics implements Command {
	private static final Logger logger = LogManager.getLogger(PriceDynamics.class);
	Project currentProject = null;

	private static Global global;

	public void execute() {
		global = Global.getGlobal();
		Reporter.report(logger, 0, "PRICE DYNAMICS (%s)", global.getPriceResponse());
		Simulation.advanceOneStep(ActionStates.C_P_Prices.text(), ActionStates.C_P_Produce.text());

		// adjust prices depending on the price adjustment mechanism specific to the project (no change, equalization, or dynamic)
		computeRelativePrices();
		computeAbsolutePrices();

		// recalculate the values and prices of each stock on the basis of the new unit values and prices
		for (Stock s : Stock.all(Simulation.timeStampIDCurrent)) {
			s.reCalculateStockTotalValuesAndPrices();
		}
	}

	/**
	 * adjust prices, depending on the setting of the price dynamics in the current project
	 */

	private static void computeRelativePrices() {
		switch (global.getPriceResponse()) {
		case VALUES:
			// for the simple case do not adjust relative prices.
			// however, absolute prices may be adjusted in the next stage
			break;
		case DYNAMIC:
			Dialogues.alert(logger, "Dynamic price adjustment not available yet, sorry");
			break;
		case EQUALIZED:
			Reporter.report(logger, 1, "Setting prices to equalise profit rates");
			Reporter.report(logger, 2, "Average Profit Rate is currently recorded as %.4f", global.profitRate());

			// we can only set the profit rate for the sector as a whole, which means we work from the per-commodity profit rates
			for (Commodity u : Commodity.commoditiesByOrigin(Commodity.ORIGIN.INDUSTRIALLY_PRODUCED)) {
				Reporter.report(logger, 2, "Setting profit-equalizing price for commodity [%s] in which profit rate is %.4f",
						u.commodityName(), u.profitRate());
				for (Industry c : u.industries()) {
					Reporter.report(logger, 3, "Note: industry %s produces this commodity", c.getName());
				}
				double profitRate = global.profitRate();
				double profit = u.profit();
				double initialCapital = u.initialProductiveCapital();
				double totalPrice = initialCapital * (1 + global.profitRate());
				double totalValue = initialCapital + profit;
				double priceValueRatio = totalPrice / totalValue;
				double newUnitPrice = priceValueRatio * u.getUnitValue();
				Reporter.report(logger, 2,
						"Initial Capital $%.0f, profit rate %.4f, total price $%.0f, total value $%.0f, price-value ratio %.4f, new unit price $%.4f",
						initialCapital, profitRate, totalPrice, totalValue, priceValueRatio, newUnitPrice);
				u.setUnitPrice(newUnitPrice);
			}
			break;
		default:
			break;
		}
	}

	/**
	 * Depending on the rules governing it, this method now adjusts the MELT.
	 * 
	 * If the rule is Value-Driven, and total price is higher than total value, then prices have to adjust to fit the values
	 */
	private static void computeAbsolutePrices() {
		double oldMelt = global.getMelt();
		double newMelt = oldMelt;
		double adjustmentFactor = global.totalPrice() / global.totalValue();
		if (!MathStuff.equals(adjustmentFactor, 1)) {
			switch (global.getMeltResponse()) {
			case VALUE_DRIVEN: // just accept the existing MELT; prices will then be adjusted to fit
				Reporter.report(logger, 1, "Value-driven MELT remains unchanged at $%.4f. Prices will be recomputed",oldMelt);
				break;
			case PRICE_DRIVEN: // the prices have established a new MELT; values will be adjusted to fit
				Reporter.report(logger, 1, "Price-driven MELT was %.4f and will be reset to %.4f. Values will be recomputed", oldMelt, newMelt);
				newMelt = oldMelt / adjustmentFactor;
				break;

			}
			global.setMelt(newMelt);
		}else {
			Reporter.report(logger, 1, "Prices and values have the same monetary expression. The MELT was not reset");
		}
	}

	/**
	 * Not used yet. If 'full pricing' is selected, we need to re-price labour power because wage
	 * goods are cheaper. However this is beyond the scope of the simple illustrations, so we
	 * don't do this yet.
	 * 
	 * Optionally we may also include, in here, the computation of the value of money.
	 */
	@SuppressWarnings("unused") private void resetWage() {
		// if the prices of consumption goods have changed, the wage will change.
		// first calculate what it would have cost, at the new prices, for what workers consumed

		double totalWage = 0;
		for (Stock s : Stock.consumedByClass(Simulation.timeStampIDCurrent, "Workers")) {
			Commodity u = s.getCommodity();
			double price = u.getUnitPrice();
			Reporter.report(logger, 3, "Wage earners just consumed %.0f of [%s] which would add $%.0f to the price of their labour power at the new prices",
					s.getStockUsedUp(), u.commodityName(), s.getStockUsedUp() * price);
			totalWage += s.getStockUsedUp() * price;
		}

		// now divide this by the number of workers.
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