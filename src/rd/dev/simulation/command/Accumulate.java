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
		global=DataManager.getGlobal(timeStampIDCurrent);
		Reporter.report(logger, 0, "ACCUMULATE");
		advanceOneStep(ActionStates.C_M_Accumulate.getText(), ActionStates.C_M_Distribute.getText());
		calculateSurplus();
		calculateCostsOfInvestment();
		allocateProfits();

		// TODO Investment.
		// One of several possible algorithms, to be improved on later if need be.
		// First (so as to implement Marx's schema of expanded reproduction) try to satisfy the investment demand of circuits
		// producing Means of Production
		// TODO write a named query to deliver these specific circuits, for now use 'if' clauses
		// to this end, give each circuit enough to satisfy what it asked for; if there is insufficient profit, 
		// first dole out enough to satisfy the requests for means of production.
		// if there is any left over, give it to the consumption circuits so as to satisfy their requests for MP
		// (The reason is that the aim is to purchase all the available MP)
		// Next, allocate resources for the additional Labour Power that is implied by this request
		// This allows us to incorporate changes in the technical structure of production, as in Marx's second case
		// What remains in capitalist hands is their revenue.
		// the final step is to set their anticipated consumption demand on the basis of this revenue.
		// TODO the latter should ideally happen in the next period in its Demand phase.
		
		Capitalism.simulation.advanceOnePeriod();
	}
	
	/**
	 * given the surplus of means of production and the available funds, allocate the funds to the industries in a systematic way.
	 * Initially while we are testing two-department models this takes the simple form of trying to invest
	 * at the desired rate and giving up if we can't.
	 */
	private void allocateProfits() {
		SocialClass capitalists = DataManager.socialClassByName(timeStampIDCurrent, "Capitalists");
		double meansOfProductionAccountedFor=0;
		
		// TODO the above should all be done in price terms; make sure it is so.
		
		for(Circuit c:DataManager.circuitsAll(timeStampIDCurrent)) {
			if (c.industryType()==Circuit.IndustryType.MEANSOFPRODUCTION) {
				double fundsRequired=c.getCostOfExpansion();
				Reporter.report(logger, 2, "  Industry [%s] needs to spend %.2s and will be allocated this from capitalist profits",c.getProductUseValueType(),fundsRequired);

				// transfer enough profits from the capitalist class to grow to the industry's proposed output
				// Give up if there isn't enough
				// Reduce revenue correspondingly

				Stock donor = capitalists.getMoneyStock();
				Stock recipient = c.getMoneyStock();
				recipient.modifyBy(fundsRequired);
				donor.modifyBy(-fundsRequired);
				Reporter.report(logger, 2, " The industry has received $%.2f from the capitalist class to accumulate",
						fundsRequired);
				capitalists.setRevenue(capitalists.getRevenue()-fundsRequired);
				meansOfProductionAccountedFor=c.getCostOfMPForExpansion();
			}
		}
		// TODO allocate remaining funds to consumption
		double meansOfProductionRemaining=global.getSurplusMeansOfProduction()-meansOfProductionAccountedFor;
		Reporter.report(logger, 2, " After allocating accumulation funds to Department I, %.2f remains to be allocated", meansOfProductionRemaining);
		
		//TODO do this
	}

	/**
	 * calculate the surplus of means of production that are available to invest in
	 */
	private void calculateSurplus() {
		
		// calculate the total surplus of means of production
		
		Reporter.report(logger, 1, " Calculating the surplus of means of production available for expansion");
		double surplusMeansOfProduction=0.0;
		for(UseValue u:DataManager.useValuesAll(timeStampIDCurrent)) {
			if (!u.getUseValueType().equals("Consumption")) {
				double thisSurplusMeansOfProduction=Precision.round(u.getSurplus()*u.getUnitPrice(),Simulation.roundingPrecision);
				Reporter.report(logger, 2, "  The surplus of commodity [%s] is %.2f and its price is $%.2f", u.getUseValueType(),u.getSurplus(),thisSurplusMeansOfProduction);
				surplusMeansOfProduction+=thisSurplusMeansOfProduction;
			}
		}	
		Reporter.report(logger, 2, "  Total investible surplus is $%.2f", surplusMeansOfProduction);
		global.setSurplusMeansOfProduction(surplusMeansOfProduction);
	}

	/**
	 * calculate the cost of the expansion proposed by the circuits
	 */
	
	private void calculateCostsOfInvestment() {
		Reporter.report(logger, 1, " Calculating the costs of investment", "");
		for(Circuit c:DataManager.circuitsAll(timeStampIDCurrent)) {
			double proposedOutput=c.getOutput()*(1+c.getGrowthRate());
			c.calculateOutputCosts(proposedOutput);
		}
	}
}