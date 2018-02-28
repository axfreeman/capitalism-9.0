package capitalism.view.custom.command;

import capitalism.view.custom.DisplayControlsBox;
import capitalism.view.custom.ImageButton;
import capitalism.view.tables.TabbedTableViewer;

public class TogglePricesCommand implements DisplayCommand {

	@Override public void execute(ImageButton caller) {
		if (DisplayControlsBox.pricesExpressionDisplay == DisplayControlsBox.DISPLAY_AS_EXPRESSION.MONEY) {
			DisplayControlsBox.pricesExpressionDisplay = DisplayControlsBox.DISPLAY_AS_EXPRESSION.TIME;
			DisplayControlsBox.pricesExpressionSymbol = DisplayControlsBox.quantityExpressionSymbol;
			caller.setOffState();
		} else {
			DisplayControlsBox.pricesExpressionDisplay = DisplayControlsBox.DISPLAY_AS_EXPRESSION.MONEY;
			DisplayControlsBox.pricesExpressionSymbol = DisplayControlsBox.moneyExpressionSymbol;
			caller.setOnState();
		}
		TabbedTableViewer.refreshTables();
	}

}
