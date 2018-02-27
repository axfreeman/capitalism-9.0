package capitalism.view.custom.command;

import capitalism.view.ViewManager;
import capitalism.view.custom.ImageButton;
import capitalism.view.tables.TabbedTableViewer;

public class ToggleDecimals implements DisplayCommand {
	public void execute(ImageButton caller) {
		if (ViewManager.getLargeNumbersFormatString().equals("%1$,.0f")) {
			ViewManager.setLargeNumbersFormatString("%1$,.2f");
			ViewManager.setSmallNumbersFormatString("%1$.4f");
			caller.setOnState();
		} else {
			ViewManager.setLargeNumbersFormatString("%1$,.0f");
			ViewManager.setSmallNumbersFormatString("%1$.2f");
			caller.setOffState();
		}
		TabbedTableViewer.refreshTables();
	}
}
