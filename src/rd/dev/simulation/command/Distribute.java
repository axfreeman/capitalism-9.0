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

import rd.dev.simulation.Simulation;
import rd.dev.simulation.custom.ActionStates;


public class Distribute extends Simulation implements Command {

	public Distribute() {
	}

	/**
	 * distribute profit.
	 * This is the residual money in the industries, after deducting the money they owned at the start.
	 * NOTE this may not be the same as the value profit;see Maldonado-Filho on release and tie-up of capital.
	 * In fact one purpose of the simulation is to track what happens to the value in distribution.
	 * The current basis is that the capitalists will take what they need for consumption.
	 * As a result, additional capital will remain in the industries for investment.
	 * Essentially, the various classes place demands on industry profits (tax, rent, interest etc) which are the revenues of the classes
	 * What is left is invested.
	 * 
	 */
	public void execute() {
		ActionStates.C_M_Revenue.getCommand().execute();
		ActionStates.C_M_Prices.getCommand().execute();
		ActionStates.C_M_Accumulate.getCommand().execute();
	}
}