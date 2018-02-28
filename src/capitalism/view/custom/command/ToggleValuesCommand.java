package capitalism.view.custom.command;

import capitalism.view.custom.DisplayControlsBox;
import capitalism.view.custom.ImageButton;
import capitalism.view.tables.TabbedTableViewer;

public class ToggleValuesCommand implements DisplayCommand {

	@Override public void execute(ImageButton caller) {
		if (DisplayControlsBox.valuesExpressionDisplay == DisplayControlsBox.DISPLAY_AS_EXPRESSION.MONEY) {
			DisplayControlsBox.valuesExpressionDisplay = DisplayControlsBox.DISPLAY_AS_EXPRESSION.TIME;
			DisplayControlsBox.valuesExpressionSymbol = DisplayControlsBox.quantityExpressionSymbol;
			caller.setOffState();
		} else {
			DisplayControlsBox.valuesExpressionDisplay = DisplayControlsBox.DISPLAY_AS_EXPRESSION.MONEY;
			DisplayControlsBox.valuesExpressionSymbol = DisplayControlsBox.moneyExpressionSymbol;
			caller.setOnState();
		}
		TabbedTableViewer.refreshTables();
	}

}
