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
import rd.dev.simulation.model.Industry;
import rd.dev.simulation.model.SocialClass;
import rd.dev.simulation.model.Stock;
import rd.dev.simulation.model.UseValue;
import rd.dev.simulation.utils.Reporter;

public class Accumulate extends Simulation implements Command {
	private static final Logger logger = LogManager.getLogger(Accumulate.class);

	double surplusMeansOfProduction;

	public Accumulate() {
	}

	/**
	 * Investment. One of several possible algorithms, to be generalised by allowing developers to write plugins
	 * 
	 * Starting point is to find out where there is a surplus of means of production of any type. Investment then
	 * consists in allocating funds, so that this surplus can be used up by producing something.
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
	public void execute() {
		Reporter.report(logger, 0, "ACCUMULATE: MOSES AND THE PROPHETS");
		advanceOneStep(ActionStates.C_M_Accumulate.getText(), ActionStates.C_M_Distribute.getText());
		calculateSurplus();
		allocateProfits();
		setCapitals(); // starting another period so recompute the initial capital
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
		double costsInDepartmentI = allocateToIndustriesOfType(Industry.OUTPUTTYPE.PRODUCTIONGOODS);
		Reporter.report(logger, 1, " $%.0f will be allocated to department I to finance accumulation. $%.0f worth of Means of Production remain ", 
				costsInDepartmentI, surplusMeansOfProduction);

		// when done, allocate any remaining funds to industries that are creating means of consumption
		double costsInDepartmentII= allocateToIndustriesOfType(Industry.OUTPUTTYPE.CONSUMPTIONGOODS);
		Reporter.report(logger, 1, " $%.0f will be allocated to department II to finance accumulation. $%.0f worth of Means of Production remain ", 
				costsInDepartmentII, surplusMeansOfProduction);
	}

	/**
	 * TODO write specific queries to deliver industries of the required industry type and then split this method into two,
	 * since it's really a syncretic amalgam of two algorithms, once for Department I and the other for Department II
	 * @param type
	 *            the industry type of the industry
	 * @return the amount that was allocated
	 */
	private double allocateToIndustriesOfType(Industry.OUTPUTTYPE type) {
		SocialClass capitalists = DataManager.socialClassByName( "Capitalists");
		Stock donor = capitalists.getMoneyStock();
		Stock recipient = null;
		double fundsAllocated = 0;
		
		switch (type) {
		case PRODUCTIONGOODS:

			// in this case, allocate funds to finance proposed growth
			for (Industry c : DataManager.industriesAll()) {
				if (c.outputType() == type) {
					double fundsAllocatedToThisIndustry=0;
					double proposedOutput = c.getConstrainedOutput() * (1 + c.getGrowthRate());
					c.setProposedOutput(proposedOutput);
					
					// The existing costs will be taken care of next period without allocating any additional funds
					Industry.DemandComponents existingCosts=c.computeOutputCosts(c.getConstrainedOutput());
					
					// This is what the industry will need if it is going to be able to expand to a higher output level
					Industry.DemandComponents proposedCosts=c.computeOutputCosts(proposedOutput);
					
					// This is the additional funding required
					Industry.DemandComponents accumulationCosts=Industry.extraCosts(existingCosts,proposedCosts);
					fundsAllocatedToThisIndustry=accumulationCosts.costOfExpansionOutput();// Allocate the money to the total expansion requested
					fundsAllocated +=fundsAllocatedToThisIndustry; 
					Reporter.report(logger, 2, "  Industry [%s] has received $%.0f from the capitalist class to accumulate, of which $%.0f for means of production ",
							c.getProductUseValueName(), fundsAllocatedToThisIndustry, accumulationCosts.costOfExpansionMP);
					recipient = c.getMoneyStock();
					recipient.modifyBy(fundsAllocatedToThisIndustry);
					donor.modifyBy(-fundsAllocatedToThisIndustry);
					capitalists.setRevenue(capitalists.getRevenue() - fundsAllocatedToThisIndustry);
					surplusMeansOfProduction -= accumulationCosts.costOfExpansionMP;
				}
			}
			break;
		case CONSUMPTIONGOODS:

			// In this case, allocate all remaining funds to expansion and adjust output accordingly

			for (Industry c : DataManager.industriesAll()) {
				if (c.outputType() == type) {
					double fundsAllocatedToThisIndustry=0;
					double proposedOutput=c.computePossibleOutput(surplusMeansOfProduction);
					c.setProposedOutput(proposedOutput);
					
					// The existing costs will be taken care of next period without allocating any additional funds
					Industry.DemandComponents existingCosts=c.computeOutputCosts(c.getConstrainedOutput());
					
					// This is what the industry will need if it is going to be able to expand to a higher output level
					Industry.DemandComponents proposedCosts=c.computeOutputCosts(proposedOutput);
					
					// This is the additional funding required
					Industry.DemandComponents accumulationCosts=Industry.extraCosts(existingCosts,proposedCosts);
				
					fundsAllocatedToThisIndustry = accumulationCosts.costOfExpansionOutput();
					fundsAllocated+=fundsAllocatedToThisIndustry;
					Reporter.report(logger, 2, "Industry [%s] has received $%.0f from the capitalist class to accumulate, of which $%.0f for means of production",
							c.getProductUseValueName(), fundsAllocatedToThisIndustry, accumulationCosts.costOfExpansionMP);
					recipient = c.getMoneyStock();
					recipient.modifyBy(fundsAllocatedToThisIndustry);
					donor.modifyBy(-fundsAllocatedToThisIndustry);
					capitalists.setRevenue(capitalists.getRevenue() - fundsAllocatedToThisIndustry);
					surplusMeansOfProduction -= accumulationCosts.costOfExpansionMP;
				}
			}
			break;
		default:
			return 0;
		}
		return fundsAllocated;
	}
}