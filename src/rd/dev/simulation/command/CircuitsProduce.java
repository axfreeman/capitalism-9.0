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
import rd.dev.simulation.model.Global;
import rd.dev.simulation.model.Stock;
import rd.dev.simulation.model.UseValue;
import rd.dev.simulation.utils.Dialogues;
import rd.dev.simulation.utils.Reporter;

public class CircuitsProduce extends Simulation implements Command {
	private static final Logger logger = LogManager.getLogger("Industry Production");

	/**
	 * For each circuit decrease the stocks and increase the sales stocks, using the coefficient to decide how much gets used up.
	 * For each social circuit decrease the stock of consumption goods and decide, on Malthusian principles, what happens to the classes
	 * (user algorithms could play a big role here). Recalculate the value produced (=C*MELT +L)
	 * 
	 * TODO should usevalues re-calculate the unit value when all producers are done?
	 */
	public void execute() {
		Reporter.report(logger, 0, "INDUSTRY PRODUCTION");
		advanceOneStep(ActionStates.C_P_CircuitsProduce.getText(), ActionStates.C_P_Produce.getText());
		Global global = DataManager.getGlobal(timeStampIDCurrent);
		double melt = global.getMelt();
		List<Circuit> circuits = DataManager.circuitsAll(timeStampIDCurrent);
		for (Circuit c : circuits) {
			String useValueType = c.getProductUseValueType();
			Stock salesStock = c.getSalesStock();
			UseValue useValue = c.getUseValue();
			double output = c.getOutput();
			double valueAdded = 0;
			Reporter.report(logger, 1, " Industry [%s] is producing output %.2f.; the melt is %.2f", useValueType, output, melt);
			List<Stock> stocks = DataManager.stocksProductiveByCircuit(timeStampIDCurrent, useValueType);
			for (Stock s : stocks) {
				if (!s.getStockType().equals("Productive")) {
					Dialogues.alert(logger,
							String.format("Non-productive stock of type [%s] called [%s] included as input ", s.getStockType(), s.getUseValueName()));
				}
				double coefficient = s.getCoefficient();
				double stockUsedUp = output * coefficient;
				stockUsedUp = Precision.round(stockUsedUp, getRoundingPrecision());
				if (s.getUseValueName().equals("Labour Power")) {
					valueAdded += stockUsedUp * melt;
					Reporter.report(logger, 2, "  Labour Power will add %.2f (instrinsic %.2f)", valueAdded, stockUsedUp);
				} else {
					double valueOfStockUsedUp = stockUsedUp * useValue.getUnitValue();
					Reporter.report(logger, 2, "  Stock [%s] has transferred value $%.2f (intrinsic %.2f) to commodity [%s] ",
							s.getUseValueName(), valueOfStockUsedUp, valueOfStockUsedUp / melt, c.getProductUseValueType());
					valueAdded += valueOfStockUsedUp;
				}
				s.modifyBy(-stockUsedUp);
			}

			// to set the value of the output, we use an overloaded version of modifyBy

			double extraSalesQuantity = output;
			extraSalesQuantity = Precision.round(extraSalesQuantity, getRoundingPrecision());
			salesStock.modifyBy(extraSalesQuantity, valueAdded);
			Reporter.report(logger, 2,
					"  The sales stock of [%s] has grown to %.2f, its value to $%.2f (intrinsic value %.2f) and its price to $%.2f (intrinsic value %.2f)",
					c.getProductUseValueType(), salesStock.getQuantity(), salesStock.getValue(), salesStock.getValue() / melt, salesStock.getPrice(),
					salesStock.getPrice() / melt);
			if (!c.getProductUseValueType().equals("Consumption")) {
				double surplus = Precision.round(salesStock.getQuantity() - useValue.getTotalDemand(), Simulation.getRoundingPrecision());
				Reporter.report(logger, 2, "  The surplus of production over use for [%s] was %.2f", useValue.getUseValueType(), surplus);
				useValue.setSurplus(surplus);
			}else {
				Reporter.report(logger, 2, "  The surplus of consumer goods has been set to zero, because they are not used in production", 0);
				useValue.setSurplus(0);// just in case...
			}

			// TODO has to be more sophisticated if we allow multiple producers of the same use value
		}

		// DO NOT recalculate the use value aggregates from the stocks, because the stocks are out of sync at this point
		// therefore, don't do this: calculateAggregates(true);
	}
}