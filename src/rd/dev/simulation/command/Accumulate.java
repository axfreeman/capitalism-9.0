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
import rd.dev.simulation.model.SocialClass;
import rd.dev.simulation.model.Stock;
import rd.dev.simulation.model.UseValue;
import rd.dev.simulation.utils.Reporter;

public class Accumulate extends Simulation implements Command {
	private static final Logger logger = LogManager.getLogger(Accumulate.class);

	double surplusMeansOfProduction;

	public Accumulate() {
	}

	public void execute() {
		Reporter.report(logger, 0, "ACCUMULATE: MOSES AND THE PROPHETS");
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
		Reporter.report(logger, 1, " Calculating the surplus of means of production available for expansion");
		for (UseValue u : DataManager.useValuesByType(UseValue.USEVALUETYPE.PRODUCTIVE)) {
				double thisSurplusMeansOfProduction = Precision.round(u.getSurplusProduct() * u.getUnitPrice(), Simulation.roundingPrecision);
				Reporter.report(logger, 2, "  The surplus of commodity [%s] is %.0f and its price is $%.0f", u.getUseValueName(), u.getSurplusProduct(),
						thisSurplusMeansOfProduction);
				surplusMeansOfProduction += thisSurplusMeansOfProduction;
		}
		Reporter.report(logger, 1, " Total surplus of means of production available for investment is $%.0f", surplusMeansOfProduction);
	}

	/**
	 * given the surplus of means of production and the available funds, allocate the funds to the industries in a systematic way.
	 * Initially while we are testing two-department models this takes the simple form of trying to invest
	 * at the desired rate and giving up if we can't.
	 */
	private void allocateProfits() {
		Reporter.report(logger, 1, " Allocating capitalist profits to industries in order to expand production");
		
		// first allocate to the industries that are creating means of production 
		double costsInDepartmentI = allocateToCircuitsOfType(Circuit.INDUSTRYTYPE.MEANSOFPRODUCTION);
		Reporter.report(logger, 1, " After allocating $%.0f to department I, $%.0f worth of Means of Production remain ", 
				costsInDepartmentI, surplusMeansOfProduction);

		// when done, allocate any remaining funds to industries that are creating means of consumption
		double costsInDepartmentII= allocateToCircuitsOfType(Circuit.INDUSTRYTYPE.NECESSITIES);
		Reporter.report(logger, 2, " After allocating $%.0f to department II, $%.0f worth of Means of Production remain ", 
				costsInDepartmentII, surplusMeansOfProduction);
	}

	/**
	 * TODO write specific queries to deliver circuits of the required industry type and then split this method into two,
	 * since it's really a syncretic amalgam of two algorithms, once for Department I and the other for Department II
	 * @param type
	 *            the industry type of the circuit
	 * @return the amount that was allocated
	 */
	private double allocateToCircuitsOfType(Circuit.INDUSTRYTYPE type) {
		SocialClass capitalists = DataManager.socialClassByName( "Capitalists");
		Stock donor = capitalists.getMoneyStock();
		Stock recipient = null;
		double fundsAllocated = 0;
		
		switch (type) {
		case MEANSOFPRODUCTION:

			// in this case, allocate funds to finance proposed growth
			for (Circuit c : DataManager.circuitsAll()) {
				if (c.industryType() == type) {
					double fundsAllocatedToThisIndustry=0;
					double proposedOutput = c.getConstrainedOutput() * (1 + c.getGrowthRate());
					c.setProposedOutput(proposedOutput);
					
					// The existing costs will be taken care of next period without allocating any additional funds
					Circuit.ExpansionCosts existingCosts=c.computeOutputCosts(c.getConstrainedOutput());
					
					// This is what the circuit will need if it is going to be able to expand to a higher output level
					Circuit.ExpansionCosts proposedCosts=c.computeOutputCosts(proposedOutput);
					
					// This is the additional funding required
					Circuit.ExpansionCosts accumulationCosts=Circuit.extraCosts(existingCosts,proposedCosts);
					
					Reporter.report(logger, 1, " The additional funds required by [%s] for accumulation are $%.0f", 
							c.getProductUseValueName(), accumulationCosts.costOfOutput());
					fundsAllocatedToThisIndustry=accumulationCosts.costOfOutput();// Allocate the money to the total expansion requested
					fundsAllocated +=fundsAllocatedToThisIndustry; 
					Reporter.report(logger, 2, "  Industry [%s] has received $%.0f from the capitalist class to accumulate, of which $%.0f for means of production ",
							c.getProductUseValueName(), fundsAllocatedToThisIndustry, accumulationCosts.costOfMP);
					recipient = c.getMoneyStock();
					recipient.modifyBy(fundsAllocatedToThisIndustry);
					donor.modifyBy(-fundsAllocatedToThisIndustry);
					capitalists.setRevenue(capitalists.getRevenue() - fundsAllocatedToThisIndustry);
					surplusMeansOfProduction -= accumulationCosts.costOfMP;
				}
			}
			break;
		case NECESSITIES:

			// In this case, allocate all remaining funds to expansion and adjust output accordingly

			for (Circuit c : DataManager.circuitsAll()) {
				if (c.industryType() == type) {
					double fundsAllocatedToThisIndustry=0;
					double proposedOutput=c.computePossibleOutput(surplusMeansOfProduction);
					c.setProposedOutput(proposedOutput);
					
					// The existing costs will be taken care of next period without allocating any additional funds
					Circuit.ExpansionCosts existingCosts=c.computeOutputCosts(c.getConstrainedOutput());
					
					// This is what the circuit will need if it is going to be able to expand to a higher output level
					Circuit.ExpansionCosts proposedCosts=c.computeOutputCosts(proposedOutput);
					
					// This is the additional funding required
					Circuit.ExpansionCosts accumulationCosts=Circuit.extraCosts(existingCosts,proposedCosts);
					
					fundsAllocatedToThisIndustry = accumulationCosts.costOfOutput();
					fundsAllocated+=fundsAllocatedToThisIndustry;
					Reporter.report(logger, 2, "Industry [%s] has been allocated $%.0f of which $%.0f for means of production",
							c.getProductUseValueName(), fundsAllocatedToThisIndustry, accumulationCosts.costOfMP);
					recipient = c.getMoneyStock();
					recipient.modifyBy(fundsAllocatedToThisIndustry);
					donor.modifyBy(-fundsAllocatedToThisIndustry);
					capitalists.setRevenue(capitalists.getRevenue() - fundsAllocatedToThisIndustry);
					surplusMeansOfProduction -= accumulationCosts.costOfMP;
				}
			}
			break;
		default:
			return 0;
		}
		return fundsAllocated;
	}
}