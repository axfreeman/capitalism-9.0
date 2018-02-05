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
import rd.dev.simulation.model.Global;
import rd.dev.simulation.model.SocialClass;
import rd.dev.simulation.model.Stock;
import rd.dev.simulation.utils.Dialogues;
import rd.dev.simulation.utils.Reporter;

public class ClassesReproduce extends Simulation implements Command {
	private static final Logger logger = LogManager.getLogger(ClassesReproduce.class);

	/**
	 * Social circuit consumption is underdeveloped. See notes in 'registerDemand'.
	 * Basically the workers are assumed to consume everything they have and the capitalists are assumed to
	 * consume a quantity proportional to their size (a primitive propensity to consume).
	 * The level of consumption of the capitalists in the event of shortage has already been fixed by registerAllocation.
	 * Hence we use quantityDemanded to ascertain what the capitalists actually consume.
	 */

	public void execute() {
		Reporter.report(logger, 0, "REPRODUCE CLASSES");
		advanceOneStep(ActionStates.C_P_ClassesReproduce.getText(), ActionStates.C_P_Produce.getText());
		
// dump the code below when we are sure it is not needed		
//		UseValue consumptionUseValue=DataManager.useValueOfConsumptionGoods(timeStampIDCurrent);
//		double priceOfConsumptionGoods=consumptionUseValue.getUnitPrice();

		Global global = DataManager.getGlobal(Simulation.timeStampIDCurrent);
		double melt = global.getMelt();
		List<SocialClass> socialClasses = DataManager.socialClassesAll();
		for (SocialClass sc : socialClasses) {
			String socialClassName = sc.getSocialClassName();
			Reporter.report(logger, 1, " Reproducing the class [%s]", socialClassName);
			double currentSize = sc.getSize();
			double populationGrowth = DataManager.getGlobal(timeStampIDCurrent).getPopulationGrowthRate();

			// if the working class has grown it has more labour power to sell; if either class has grown its demand will increase

			double newSize = Precision.round(currentSize * (1 + populationGrowth), Simulation.getRoundingPrecision());
			Stock consumptionStock = sc.getConsumptionStock();
			if (socialClassName.equals("Workers")) {

				// workers regenerate labour power in proportion to the size of their class and the parameter 'reproductionTime'
				// conceptually this is different from the turnover time of labour power, so I created two separate parameters. I can't yet conceive of
				// a situation where they would be different. But logically, the possibility would seem to exist.

				Stock salesStock = sc.getSalesStock();

				double existingLabourPower = salesStock.getQuantity();
				double reproductionTime=sc.getReproductionTime();
				if (reproductionTime<=0) {
					Dialogues.alert(logger, "Reproduction Time of workers cannot be zero");
					break;
				}
				double newLabourPower = newSize / sc.getReproductionTime();
				double extraLabourPower = newLabourPower - existingLabourPower;
				if (extraLabourPower > 0) {
					Reporter.report(logger, 2, "  The working class has added %.2f to its existing stock of Labour Power which was %.2f", extraLabourPower,
							existingLabourPower);
					salesStock.modifyBy(extraLabourPower);
				}
				Reporter.report(logger, 2,
						"  Labour Power regenerated with size %.2f, value $%.2f (intrinsic value %.2f) and price $%.2f (intrinsic value %.2f)",
						salesStock.getQuantity(), salesStock.getValue(), salesStock.getValue() / melt, salesStock.getPrice(), salesStock.getPrice() / melt);
			}
			double existingStock=sc.getConsumptionQuantity();
			double quantityConsumed = existingStock;
			consumptionStock.modifyBy(-quantityConsumed);
			Reporter.report(logger, 2, "  Consumption stock of class [%s] reduced from %.2f to %.2f by consuming %.2f ",
					sc.getSocialClassName(), sc.getConsumptionQuantity(),quantityConsumed, existingStock);
		}
	}
}