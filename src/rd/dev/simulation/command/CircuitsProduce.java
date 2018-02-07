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
import rd.dev.simulation.model.Circuit;
import rd.dev.simulation.model.Global;
import rd.dev.simulation.model.Stock;
import rd.dev.simulation.model.UseValue;
import rd.dev.simulation.model.UseValue.USEVALUECIRCUITTYPE;
import rd.dev.simulation.model.UseValue.USEVALUETYPE;
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
		Global global = DataManager.getGlobal();
		double melt = global.getMelt();

		// initialise the accounting for how much of this useValue is used up and how much is created in production in the current period
		// so we can calculate how much surplus of it resulted from production in this period.

		for (UseValue u : DataManager.useValuesByCircuitType(USEVALUECIRCUITTYPE.CAPITALIST)) {
			u.setStockUsedUp(0);
			u.setStockProduced(0);
		}
		
		// Now do the actual production: value process then production process
		// all productive stocks except a stock of type labour power now contribute value to the product of the circuit that owns them,
		// equal to their price at this time except stocks of type labour power, which contribute their magnitude, multiplied by their complexity, divided by the MELT
		// (TODO incorporate labour complexity)
		
		for (Circuit c : DataManager.circuitsAll()) {
			String useValueType = c.getProductUseValueName();
			Stock salesStock = c.getSalesStock();
			UseValue useValue = c.getUseValue();
			double output = c.getConstrainedOutput();
			double valueAdded = 0;
			Reporter.report(logger, 1, " Industry [%s] is producing output %.2f.; the melt is %.2f", useValueType, output, melt);

			for (Stock s : DataManager.stocksProductiveByCircuit(timeStampIDCurrent, useValueType)) {

				// a little consistency check ...
				if (!s.getStockType().equals("Productive")) {
					Dialogues.alert(logger,
							String.format("Non-productive stock of type [%s] called [%s] included as input ", s.getStockType(), s.getUseValueName()));
				}
				// .. end of little consistency check

				double coefficient = s.getCoefficient();
				double stockUsedUp = output * coefficient;
				stockUsedUp = Precision.round(stockUsedUp, getRoundingPrecision());
				if (s.useValueType() == USEVALUETYPE.LABOURPOWER) {
					valueAdded += stockUsedUp * melt;
					Reporter.report(logger, 2, "  Labour Power has added value amounting to %.0f (intrinsic %.0f) to commodity [%s]", valueAdded, stockUsedUp,c.getProductUseValueName());
				} else {
					double valueOfStockUsedUp = stockUsedUp * useValue.getUnitValue();
					Reporter.report(logger, 2, "  Stock [%s] has transferred value $%.0f (intrinsic %.0f) to commodity [%s] ",
							s.getUseValueName(), valueOfStockUsedUp, valueOfStockUsedUp / melt, c.getProductUseValueName());
					valueAdded += valueOfStockUsedUp;
				}

				// the stock is reduced by what was used up, and account of this is registered with its use value
				UseValue u = s.getUseValue();
				Reporter.report(logger, 2, "  %.0f of input [%s] was used up in producing the output [%s]", stockUsedUp, u.getUseValueName(),
						c.getProductUseValueName());
				double stockOfUSoFarUsedUp = u.getStockUsedUp();
				u.setStockUsedUp(stockOfUSoFarUsedUp + stockUsedUp);
				s.modifyBy(-stockUsedUp);
			}

			// to set the value of the output, we now use an overloaded version of modifyBy which only sets the value

			double extraSalesQuantity = output;
			extraSalesQuantity = Precision.round(extraSalesQuantity, getRoundingPrecision());
			salesStock.modifyBy(extraSalesQuantity, valueAdded);
			c.getUseValue().setStockProduced(c.getUseValue().getStockProduced() + extraSalesQuantity);
			Reporter.report(logger, 2,
					"  The sales stock of [%s] has grown to %.0f, its value to $%.0f (intrinsic value %.0f) and its price to $%.0f (intrinsic value %.0f)",
					c.getProductUseValueName(), salesStock.getQuantity(), salesStock.getValue(), salesStock.getValue() / melt, salesStock.getPrice(),
					salesStock.getPrice() / melt);
		}

		// now (and only now) we can calculate the surplus (if any) of each of the use values

		for (UseValue u : DataManager.useValuesByCircuitType(USEVALUECIRCUITTYPE.CAPITALIST)) {
			u.setSurplusProduct(u.getStockProduced() - u.getStockUsedUp());
		}
	}
}