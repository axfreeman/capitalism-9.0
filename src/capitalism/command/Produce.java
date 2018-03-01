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
package capitalism.command;

import capitalism.Simulation;
import capitalism.view.custom.ActionStates;

public class Produce extends Simulation implements Command {

	public Produce() {
	}
	
	/**
	 * combines {@link IndustriesProduce}, {@link PriceDynamics} and {@link ClassesReproduce} in one button press
	 */
	public void execute() {
		ActionStates.C_P_IndustriesProduce.getCommand().execute();
		ActionStates.C_P_Prices.getCommand().execute();
		ActionStates.C_P_ClassesReproduce.getCommand().execute();
	 }
}