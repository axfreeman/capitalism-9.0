package capitalism.view.command;

import capitalism.controller.Simulation;
import capitalism.view.ViewManager;
import capitalism.view.custom.ImageButton;

public class RestartCommand implements DisplayCommand{

	@Override public void execute(ImageButton caller) {
		Simulation.restart();
		ViewManager.getActionButtonsBox().setActionStateFromLabel("Accumulate");
		ViewManager.refreshTimeStampView();
		ViewManager.refreshDisplay();
	}

}
