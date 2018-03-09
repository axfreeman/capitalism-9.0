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
package capitalism.controller.command;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.controller.Simulation;
import capitalism.model.Industry;
import capitalism.model.SocialClass;
import capitalism.model.Stock;
import capitalism.utils.Reporter;
import capitalism.view.custom.ActionStates;


/**
 * Distribute profit
 * This is the residual money in the industries, after deducting the money they owned at the start.
 * NOTE this may not be the same as the value profit;see Maldonado-Filho on release and tie-up of capital.
 * In fact one purpose of the simulation is to track what happens to the value in distribution.
 * The current basis is that the capitalists will take what they need for consumption.
 * As a result, additional capital will remain in the industries for investment.
 * Essentially, the various classes place demands on industry profits (tax, rent, interest etc) which are the revenues of the classes
 * What is left is invested, in the {@link Accumulate} phase
 */

public class Revenue extends Simulation implements Command {
	private static final Logger logger = LogManager.getLogger(Revenue.class);

	public Revenue() {
	}

	public void execute() {
		Reporter.report(logger, 0, "REVENUE");
		advanceOneStep(ActionStates.C_M_Revenue.text(), ActionStates.C_M_Distribute.text());

		// Revenue properly speaking.
		// Capitalists receive profits.
		// Workers' revenue is already known by know because it was a price, paid for in trade. We calculated it in that phase of the simulation
		// Main principle is thus that working class revenue is spent in the current period, but capitalist revenue is spent in the next period.
		// Everything becomes clear once this is grasped.
		// TODO bankers landlords and merchants will receive revenue at this point, that is, before accumulation.

		// first, give the capitalists their profits
		// they will accumulate some of it and consume the rest. Accumualation is taken care of in the Accumulate phase
		// consumption will take place in the next period

		allocateProfits();
	}

	private void allocateProfits() {
		SocialClass capitalists = SocialClass.currentWithName("Capitalists");
		double capitalistRevenue = 0.0;
		Stock recipient = capitalists.moneyStock();
		for (Industry c : Industry.currentProjectAndTimeStamp()) {
			double profit = c.profit();

			// transfer all profits to the capitalist class. In the Accumulate phase, part of this revenue
			// will be put back into the industries to invest

			Stock donor = c.moneyStock();
			recipient.modifyBy(profit);
			donor.modifyBy(-profit);
			Reporter.report(logger, 1, "Capitalist class has received $%.0f from industry [%s]",
					profit, c.name());

			// Note: revenue is in effect a memo item, not a stock.
			// its use is to determine what classes consume.
			// TODO allow any class to receive property revenue so we allow for mixed classes
			// TODO a stricter approach to the relation between money and revenue

			capitalistRevenue += profit;
		}
		Reporter.report(logger, 1, "Capitalist revenue (disposable income) is now $%.0f", capitalistRevenue);

		capitalists.setRevenue(capitalistRevenue);
	}
}