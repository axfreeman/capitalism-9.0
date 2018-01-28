package rd.dev.simulation.command;

import rd.dev.simulation.Simulation;
import rd.dev.simulation.custom.ActionStates;

public class OnePeriod  extends Simulation implements Command{
	public OnePeriod() {
	}
	
	/**
	 * One complete period
	 */
	public void execute() {
		ActionStates.M_C_PreTrade.getCommand().execute();
		ActionStates.C_P_Produce.getCommand().execute();
		ActionStates.C_M_Distribute.getCommand().execute();
	}
}
