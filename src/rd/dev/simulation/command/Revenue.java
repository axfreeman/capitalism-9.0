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
import rd.dev.simulation.datamanagement.DataManager;
import rd.dev.simulation.model.Circuit;
import rd.dev.simulation.model.SocialClass;
import rd.dev.simulation.model.Stock;
import rd.dev.simulation.utils.Reporter;

public class Revenue extends Simulation implements Command {
	private static final Logger logger = LogManager.getLogger(Revenue.class);

	public Revenue() {
	}

	public void execute() {
		Reporter.report(logger, 0, "REVENUE");
		advanceOneStep(ActionStates.C_M_Revenue.getText(), ActionStates.C_M_Distribute.getText());

		// Revenue properly speaking.
		// Capitalists receive profits.
		// Workers' revenue is *already* known because it was a price, paid for in trade
		// Main principle is thus that working class revenue is spent in the current period, but capitalist revenue is spent in the next period. 
		// Everything becomes clear once this is grasped.
		// TODO bankers landlords and merchants will receive revenue at this point
		// that is, before accumulation.

	
		// first, give the capitalists their profits
		// they will accumulate some of it and consume the rest. this happens in the Accumulate phase
		
		allocateProfits();
	}

	private void allocateProfits() {
		SocialClass capitalists = DataManager.socialClassByName(timeStampIDCurrent, "Capitalists");
		double capitalistRevenue=0.0;
		for (Circuit c : DataManager.circuitsAll()) {
			double profit = c.getProfit();

			// transfer all profits to the capitalist class. In the Accumulate phase, part of this revenue
			// will be put back into the circuits to invest

			Stock recipient = capitalists.getMoneyStock();
			Stock donor = c.getMoneyStock();
			recipient.modifyBy(profit);
			donor.modifyBy(-profit);
			Reporter.report(logger, 1, " Capitalist class has received $%.2f from circuit [%s]",
					profit, c.getProductUseValueType());

			// Note: revenue is in effect a memo item, not a stock.
			// its use is to determine what classes consume

			capitalistRevenue+=profit;
			c.setProfit(0);
		}
		Reporter.report(logger, 1, "  Capitalist revenue (disposable income) set to $%.2f", capitalistRevenue);
		capitalists.setRevenue(capitalistRevenue);
	}
}