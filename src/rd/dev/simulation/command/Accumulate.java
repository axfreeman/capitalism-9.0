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

import rd.dev.simulation.Capitalism;
import rd.dev.simulation.Simulation;
import rd.dev.simulation.custom.ActionStates;
import rd.dev.simulation.datamanagement.DataManager;
import rd.dev.simulation.model.Circuit;
import rd.dev.simulation.model.Global;
import rd.dev.simulation.model.SocialClass;
import rd.dev.simulation.model.Stock;
import rd.dev.simulation.model.UseValue;
import rd.dev.simulation.utils.Reporter;

public class Accumulate extends Simulation implements Command {
	private static final Logger logger = LogManager.getLogger(Accumulate.class);

	Global global;

	public Accumulate() {
	}

	public void execute() {
		global = DataManager.getGlobal();
		Reporter.report(logger, 0, "ACCUMULATE");
		advanceOneStep(ActionStates.C_M_Accumulate.getText(), ActionStates.C_M_Distribute.getText());
		calculateSurplus();
		allocateProfits();
		setCapitals(); // starting another period so recompute the initial capital

		// Investment.
		// One of several possible algorithms, to be generalised later.
		// First (so as to implement Marx's schema of expanded reproduction) try to satisfy the investment demand of circuits producing Means of Production
		// TODO write a named query to deliver these specific circuits, for now use 'if' clauses
		// to this end, give each circuit enough to satisfy what it asked for; if there is insufficient profit,
		// first dole out enough to satisfy the requests for means of production.
		// if there are any means of production left over, give money to the consumption circuits to buy them and also the additional labour power required to
		// use them.
		// (The reason is that the aim is to purchase all the available MP)
		// What remains in capitalist hands is their revenue.
		// Anticipated consumption demand on the basis of this revenue is finally reset.

		Capitalism.simulation.advanceOnePeriod();
	}

	/**
	 * calculate the surplus of means of production that are available to invest in
	 */
	private void calculateSurplus() {

		// calculate the total surplus of means of production

		Reporter.report(logger, 1, " Calculating the surplus of means of production available for expansion");
		double surplusMeansOfProduction = 0.0;
		for (UseValue u : DataManager.useValuesByType(UseValue.USEVALUETYPE.PRODUCTIVE)) {
				double thisSurplusMeansOfProduction = Precision.round(u.getSurplusProduct() * u.getUnitPrice(), Simulation.roundingPrecision);
				Reporter.report(logger, 2, "  The surplus of commodity [%s] is %.2f and its price is $%.2f", u.getUseValueName(), u.getSurplusProduct(),
						thisSurplusMeansOfProduction);
				surplusMeansOfProduction += thisSurplusMeansOfProduction;
		}
		Reporter.report(logger, 1, " Total surplus of means of production available for investment is $%.2f", surplusMeansOfProduction);
		global.setSurplusMeansOfProduction(surplusMeansOfProduction);
	}

	/**
	 * given the surplus of means of production and the available funds, allocate the funds to the industries in a systematic way.
	 * Initially while we are testing two-department models this takes the simple form of trying to invest
	 * at the desired rate and giving up if we can't.
	 */
	private void allocateProfits() {
		Reporter.report(logger, 1, " Allocating capitalist profits to industries in order to expand production");
		double meansOfProductionRemaining = global.getSurplusMeansOfProduction();
		double meansOfProductionAccountedFor = allocateToCircuitsOfType(meansOfProductionRemaining, Circuit.INDUSTRYTYPE.MEANSOFPRODUCTION);
		meansOfProductionRemaining -= meansOfProductionAccountedFor;
		Reporter.report(logger, 2, " After allocating accumulation funds to Department I, %.2f remains to be allocated", meansOfProductionRemaining);
		meansOfProductionAccountedFor -= allocateToCircuitsOfType(meansOfProductionRemaining, Circuit.INDUSTRYTYPE.NECESSITIES);
	}

	/**
	 * 
	 * @param meansOfProductionRemaining
	 *            the remaining surplus of means of production available for productive investment, after allocating funds to other circuits
	 * @param type
	 *            the industry type of the circuit
	 * @return the amount that was allocated
	 */
	private double allocateToCircuitsOfType(double meansOfProductionRemaining, Circuit.INDUSTRYTYPE type) {
		SocialClass capitalists = DataManager.socialClassByName( "Capitalists");
		Stock donor = capitalists.getMoneyStock();
		Stock recipient = null;
		double fundsAllocated = 0;

		// TODO write custom queries to get these specific circuits (or sets of circults in the general case)

		switch (type) {
		case MEANSOFPRODUCTION:

			// in this case, allocate funds to finance proposed growth

			for (Circuit c : DataManager.circuitsAll()) {
				if (c.industryType() == type) {
					double proposedOutput = c.getConstrainedOutput() * (1 + c.getGrowthRate());
					c.setProposedOutput(proposedOutput);
					c.calculateOutputCosts();
					fundsAllocated = c.getCostOfExpansion(); // Allocate the money to the total expansion requested
					Reporter.report(logger, 2, "  Industry [%s] has been allocated $%.2f of which %.2f for means of production ",
							c.getProductUseValueName(), fundsAllocated, c.getCostOfMPForExpansion());
					recipient = c.getMoneyStock();
					global.setSurplusMeansOfProduction(global.getSurplusMeansOfProduction() - c.getCostOfMPForExpansion());
				}
			}
			break;
		case NECESSITIES:

			// In this case, allocate all remaining funds to expansion and adjust output accordingly

			for (Circuit c : DataManager.circuitsAll()) {
				if (c.industryType() == type) {
					c.computePossibleOutput(global.getSurplusMeansOfProduction());
					fundsAllocated = c.getCostOfExpansion();
					Reporter.report(logger, 2, "Industry [%s] has been allocated %.2f of which %.2f for means of production",
							c.getProductUseValueName(), fundsAllocated, c.getCostOfMPForExpansion());
					recipient = c.getMoneyStock();
				}
			}
			break;
		default:
			return 0;
		}
		// transfer enough profits from the capitalist class to grow to the industry's proposed output
		// Give up if there isn't enough
		// Reduce revenue correspondingly

		recipient.modifyBy(fundsAllocated);
		donor.modifyBy(-fundsAllocated);
		Reporter.report(logger, 2, " Industry [%s] has received $%.2f from the capitalist class to accumulate", recipient.getUseValueName(), fundsAllocated);
		capitalists.setRevenue(capitalists.getRevenue() - fundsAllocated);
		return fundsAllocated;
	}
}