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

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rd.dev.simulation.Simulation;
import rd.dev.simulation.custom.ActionStates;
import rd.dev.simulation.datamanagement.DataManager;
import rd.dev.simulation.model.UseValue;
import rd.dev.simulation.utils.Reporter;

public class Supply extends Simulation implements Command {
	private static final Logger logger = LogManager.getLogger(Supply.class);

	public Supply() {
	}
	public void execute() {

	/**
	 * Initialise the supply of every use value to zero. Then ask the sales stock of every productive circuit and every social class to add its quantity to the
	 * total supply of the relevant usevalue.
	 * <p>
	 * NOTE Labour Power is contributed in the current test dataset only by the Workers class; this could be varied, for example if there is a small business or
	 * peasant class, or differentiated labour power.
	 */

		Reporter.report(logger, 0, "REGISTERING SUPPLY");

		advanceOneStep(ActionStates.M_C_Supply.getText(),ActionStates.M_C_PreTrade.getText());
		
		List<UseValue> results = DataManager.useValuesAll(timeStampIDCurrent);
		for (UseValue u : results) {
			u.registerSupply();
		}
		
		setCapitals();
	}

}