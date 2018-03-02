package capitalism.view.command;

import capitalism.view.TabbedTableViewer;
import capitalism.view.custom.DisplayControlsBox;
import capitalism.view.custom.ImageButton;

public class ColourHintsCommand implements DisplayCommand{

	@Override public void execute(ImageButton caller) {
		if (DisplayControlsBox.displayHints) {
			DisplayControlsBox.displayHints = false;
		} else {
			DisplayControlsBox.displayHints = true;
		}
		caller.switchStates();
		TabbedTableViewer.refreshTables();
	}

}
