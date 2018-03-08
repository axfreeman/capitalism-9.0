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
import capitalism.model.SocialClass;
import capitalism.utils.Reporter;
import capitalism.view.custom.ActionStates;

public class ClassesReproduce implements Command {
	private static final Logger logger = LogManager.getLogger(ClassesReproduce.class);

	/**
	 * Social consumption is underdeveloped. See notes in 'registerDemand'.
	 * Basically the workers are assumed to consume everything they have and the capitalists are assumed to
	 * consume a quantity proportional to their size (a primitive propensity to consume).
	 * The level of consumption of the capitalists in the event of shortage has already been fixed by registerAllocation.
	 * Hence we use quantityDemanded to ascertain what the capitalists actually consume.
	 */

	public void execute() {
		Reporter.report(logger, 0, "REPRODUCE CLASSES");
		Simulation.advanceOneStep(ActionStates.C_P_ClassesReproduce.text(), ActionStates.C_P_Produce.text());

		// NOTE: stockUsedUp has been initialised in the IndustriesProduce phase
		for (SocialClass sc : SocialClass.all()) {
			sc.consume();
			sc.regenerate();
		}
		
		Reporter.report(logger, 1, "Recompute values if necessary");
		Simulation.checkConsistency();

		// recalculate unit values, because these will have changed as a result of production
		Simulation.computeUnitValues();
	}
}