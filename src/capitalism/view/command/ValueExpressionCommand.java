package capitalism.view.command;

import capitalism.view.TabbedTableViewer;
import capitalism.view.custom.DisplayControlsBox;
import capitalism.view.custom.ImageButton;

public class ValueExpressionCommand implements DisplayCommand {

	@Override public void execute(ImageButton caller) {
		if (DisplayControlsBox.expressionDisplay == DisplayControlsBox.DISPLAY_AS_EXPRESSION.MONEY) {
			DisplayControlsBox.expressionDisplay = DisplayControlsBox.DISPLAY_AS_EXPRESSION.TIME;
			DisplayControlsBox.expressionSymbol = DisplayControlsBox.quantityExpressionSymbol;
			caller.setOffState();
		} else {
			DisplayControlsBox.expressionDisplay = DisplayControlsBox.DISPLAY_AS_EXPRESSION.MONEY;
			DisplayControlsBox.expressionSymbol = DisplayControlsBox.moneyExpressionSymbol;
			caller.setOnState();
		}
		TabbedTableViewer.refreshTables();
	}

}
