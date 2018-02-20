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
import rd.dev.simulation.model.SocialClass;
import rd.dev.simulation.utils.Reporter;

public class ClassesReproduce extends Simulation implements Command {
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
		advanceOneStep(ActionStates.C_P_ClassesReproduce.getText(), ActionStates.C_P_Produce.getText());

		for (SocialClass sc : SocialClass.socialClassesAll()) {
			sc.consume();
			sc.regenerate();
		}
	}
}