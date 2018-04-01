package capitalism.view.command;

import capitalism.view.TabbedTableViewer;
import capitalism.view.ViewManager;
import capitalism.view.custom.ImageButton;

public class GraphicsCommand implements DisplayCommand {
	public void execute(ImageButton caller) {
		ViewManager.getTabbedTableViewer().switchHeaderDisplays();
		caller.switchStates();
		TabbedTableViewer.refreshTables();
	}
}
