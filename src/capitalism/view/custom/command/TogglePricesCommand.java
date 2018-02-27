package capitalism.view.custom.command;

import capitalism.view.custom.DisplayControls;
import capitalism.view.custom.ImageButton;
import capitalism.view.tables.TabbedTableViewer;

public class TogglePricesCommand implements DisplayCommand {

	@Override public void execute(ImageButton caller) {
		if (DisplayControls.pricesExpressionDisplay == DisplayControls.DISPLAY_AS_EXPRESSION.MONEY) {
			DisplayControls.pricesExpressionDisplay = DisplayControls.DISPLAY_AS_EXPRESSION.TIME;
			DisplayControls.pricesExpressionSymbol = DisplayControls.quantityExpressionSymbol;
			caller.setOffState();
		} else {
			DisplayControls.pricesExpressionDisplay = DisplayControls.DISPLAY_AS_EXPRESSION.MONEY;
			DisplayControls.pricesExpressionSymbol = DisplayControls.moneyExpressionSymbol;
			caller.setOnState();
		}
		TabbedTableViewer.refreshTables();
	}

}
