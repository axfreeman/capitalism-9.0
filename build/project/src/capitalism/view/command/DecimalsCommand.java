package capitalism.view.command;

import capitalism.view.TabbedTableViewer;
import capitalism.view.ViewManager;
import capitalism.view.custom.ImageButton;

public class DecimalsCommand implements DisplayCommand {
	public void execute(ImageButton caller) {
		if (ViewManager.getLargeFormat().equals("%1$,.0f")) {
			ViewManager.setLargeFormat("%1$,.2f");
			ViewManager.setSmallFormat("%1$.4f");
			caller.setOnState();
		} else {
			ViewManager.setLargeFormat("%1$,.0f");
			ViewManager.setSmallFormat("%1$.2f");
			caller.setOffState();
		}
		TabbedTableViewer.refreshTables();
	}
}
