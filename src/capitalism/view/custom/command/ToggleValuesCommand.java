package capitalism.view.custom.command;

import capitalism.view.custom.DisplayControls;
import capitalism.view.custom.ImageButton;
import capitalism.view.tables.TabbedTableViewer;

public class ToggleValuesCommand implements DisplayCommand {

	@Override public void execute(ImageButton caller) {
		if (DisplayControls.valuesExpressionDisplay == DisplayControls.DISPLAY_AS_EXPRESSION.MONEY) {
			DisplayControls.valuesExpressionDisplay = DisplayControls.DISPLAY_AS_EXPRESSION.TIME;
			DisplayControls.valuesExpressionSymbol = DisplayControls.quantityExpressionSymbol;
			caller.setOffState();
		} else {
			DisplayControls.valuesExpressionDisplay = DisplayControls.DISPLAY_AS_EXPRESSION.MONEY;
			DisplayControls.valuesExpressionSymbol = DisplayControls.moneyExpressionSymbol;
			caller.setOnState();
		}
		TabbedTableViewer.refreshTables();
	}

}
