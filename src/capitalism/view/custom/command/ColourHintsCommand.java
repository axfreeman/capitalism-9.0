package capitalism.view.custom.command;

import capitalism.view.custom.DisplayControls;
import capitalism.view.custom.ImageButton;
import capitalism.view.tables.TabbedTableViewer;

public class ColourHintsCommand implements DisplayCommand{

	@Override public void execute(ImageButton caller) {
		if (DisplayControls.displayHints) {
			DisplayControls.displayHints = false;
		} else {
			DisplayControls.displayHints = true;
		}
		caller.switchStates();
		TabbedTableViewer.refreshTables();
	}

}
