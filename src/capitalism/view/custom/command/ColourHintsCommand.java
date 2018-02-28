package capitalism.view.custom.command;

import capitalism.view.custom.DisplayControlsBox;
import capitalism.view.custom.ImageButton;
import capitalism.view.tables.TabbedTableViewer;

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
