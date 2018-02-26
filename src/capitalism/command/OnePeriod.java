package capitalism.command;

import capitalism.Simulation;
import capitalism.view.custom.ActionStates;

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
