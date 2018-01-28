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
import rd.dev.simulation.model.UseValue;
import rd.dev.simulation.utils.Reporter;

public class Accumulate extends Simulation implements Command {
	private static final Logger logger = LogManager.getLogger(Accumulate.class);

	public Accumulate() {
	}

	public void execute() {
		Reporter.report(logger, 0, "ACCUMULATE");
		advanceOneStep(ActionStates.C_M_Accumulate.getText(), ActionStates.C_M_Distribute.getText());
		calculateSurplus();
		calculateCostsOfInvestment();

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
	 * calculate the surplus of means of production that are available to invest in
	 */
	private void calculateSurplus() {
		// calculate the total surplus of means of production
		
		Reporter.report(logger, 1, " Calculating the surplus of means of production available to invest in");
		double surplusMeansOfProduction=0.0;
		for(UseValue u:DataManager.useValuesAll(timeStampIDCurrent)) {
			if (!u.getUseValueType().equals("Consumption")) {
				double thisSurplusMeansOfProduction=Precision.round(u.getSurplus()*u.getUnitPrice(),Simulation.roundingPrecision);
				Reporter.report(logger, 2, "  The surplus of commodity [%s] is %.2f and its price is $%.2f", u.getUseValueType(),u.getSurplus(),thisSurplusMeansOfProduction);
				surplusMeansOfProduction+=thisSurplusMeansOfProduction;
			}
		}	
		Reporter.report(logger, 2, "  Total investible surplus is $%.2f", surplusMeansOfProduction);
		Global global =DataManager.getGlobal(timeStampIDCurrent);
		global.setSurplusMeansOfProduction(surplusMeansOfProduction);
	}

	/**
	 * calculate the cost of the expansion proposed by the circuits
	 */
	
	private void calculateCostsOfInvestment() {
		for(Circuit c:DataManager.circuitsAll(timeStampIDCurrent)) {
			double proposedOutput=c.getOutput()*c.getGrowthRate();
			c.calculateOutputCosts(proposedOutput);
		}
	}
}