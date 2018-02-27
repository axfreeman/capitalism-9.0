package capitalism.view.custom.command;

import capitalism.view.ViewManager;
import capitalism.view.custom.ImageButton;
import capitalism.view.tables.TabbedTableViewer;

public class ToggleGraphicsCommand implements DisplayCommand {
	public void execute(ImageButton caller) {
		ViewManager.getTabbedTableViewer().switchHeaderDisplays();
		caller.switchStates();
		TabbedTableViewer.refreshTables();
	}
}
