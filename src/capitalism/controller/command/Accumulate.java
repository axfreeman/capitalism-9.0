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
import capitalism.model.Commodity;
import capitalism.model.Industry;
import capitalism.utils.Reporter;
import capitalism.view.custom.ActionStates;
/**
 * Investment. One of several possible algorithms, to be generalised by allowing developers to write plugins
 * 
 * Starting point is to find out where there is a surplus of means of production of any type. Investment then
 * consists in allocating funds, so that this surplus can be used up by producing something. This surplus was
 * calculated in the Production phase, by deducting consumed productive stocks from output.
 * 
 * Next step is to accept, at face value, each industry's proposal for growth. We estimate the expansion demand
 * that would arise, if these proposals were accepted, and the cost of accepting them.
 * 
 * Expansion can be constrained if the total expansion demand exceeds the surplus. Alternatively, it may not
 * consume the entire surplus.
 * 
 * So, first, we compare the expansionDemand with the available surplus for each means of production. If there is
 * a shortfall, we reduce the proposed outputs of every industry in proportion. (If that doesn't work, the funds
 * will simply be allocated industry by industry in the (somewhat random) order that the industries are dealt with
 * by the simulation, and the industries at the bottom of the pecking order will lose out).
 * 
 * if there is a surplus (more usual if the data is not pathological) this goes to the consumption good industries. This procedure
 * follows Marx's expanded reproduction schemas, as I interpret them, though other interpretations are possible. In
 * particular it deals with the fact that growth, in these schemas, is NOT balanced as often supposed. It becomes
 * balanced after period 3, but this is a result of the calculation not a presupposition, as far as I can see.
 * 
 * TODO if consumer industries are overproducing, in the Demand stage they might reduce their output. But this is for later.
 * 
 * Finally, the question of funding: again, before allocating funds, we compare the cost with the available revenue and
 * reduce proportionately if not. Again, this should not occur unless the data is somewhat pathological at least for those
 * early and specific cases where the pricing mechanism is 'SIMPLE' and not 'DYNAMIC'. The allocation mechanism should then
 * be correspondingly restricted, because the purpose of such simpe examples is education in the general principles, not
 * yet the working simulation of an actual economy.
 * 
 */
public class Accumulate implements Command {
	private static final Logger logger = LogManager.getLogger(Accumulate.class);
	double surplusMeansOfProduction;

	public void execute() {
		Reporter.report(logger, 0, "ACCUMULATE");
		Simulation.advanceOneStep(ActionStates.C_M_Accumulate.text(), ActionStates.C_M_Distribute.text());
		allocateToProductionIndustries();
		allocateToConsumptionIndustries();
		Simulation.advanceOnePeriod();
	}

	/**
	 * See notes for the Accumulate class.
	 * 
	 */
	private void allocateToProductionIndustries() {
		Reporter.report(logger, 1, "Allocating investment to the production goods industries");

		for (Commodity u : Commodity.currentByFunction(Simulation.projectIDCurrent(),Simulation.timeStampIDCurrent(),Commodity.FUNCTION.PRODUCTIVE_INPUT)) {

			// Exclude socially-produced commodities
			if (u.getOrigin() == Commodity.ORIGIN.SOCIALLY_PRODUCED)
				continue;

			Reporter.report(logger, 2, "Processing commodity %s", u.name());
			for (Industry industry : u.industries()) {
				// simply grant the industry's proposed growth rate
				industry.expand(industry.getGrowthRate());
			}
		}
	}

	/**
	 * The consumption Industries can expand indefinitely, provided social classes can consume what they produce.
	 * Probably, this can only be taken care of when we introduce dynamic pricing and accumulation.
	 * For now we adopt an allocation system that says we simply accept the growth that the user proposes, 
	 * ie the same as for the production industries. This is unsatisfactory in that the simulation simply
	 * becomes a test of the consistency of what the user proposes, rather than an exploration of its consequences.
	 * 
	 * A further complication is that in Marx's first expanded reproduction schema, it is clear that the
	 * output of the consumption industries is limited by the means of production available, because in the 
	 * first period, the consumption industries don't expand as fast as in the second period.
	 * 
	 * Working on this...
	 */

	private void allocateToConsumptionIndustries() {
		double costs = 0;
		Reporter.report(logger, 1, "Allocating investment to the consumption goods industries");
		
		for (Commodity u : Commodity.currentByFunction(Simulation.projectIDCurrent(),Simulation.timeStampIDCurrent(),Commodity.FUNCTION.CONSUMER_GOOD)) {

			// Exclude socially-produced commodities
			if (u.getOrigin() == Commodity.ORIGIN.SOCIALLY_PRODUCED)
				continue;
			
			Reporter.report(logger, 2, "Processing commodity %s", u.name());
			for (Industry industry : u.industries()) {
				industry.expand(industry.computeGrowthRate());
			}
		}

		Reporter.report(logger, 1, "$%.0f will be allocated to finance accumulation in the production goods industries. $%.0f worth of Means of Production remain ",
				costs, surplusMeansOfProduction);
	}
}