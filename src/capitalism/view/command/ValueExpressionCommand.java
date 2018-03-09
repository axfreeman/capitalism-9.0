package capitalism.view.command;

import capitalism.view.TabbedTableViewer;
import capitalism.view.custom.DisplayControlsBox;
import capitalism.view.custom.ImageButton;

public class ValueExpressionCommand implements DisplayCommand {

	@Override public void execute(ImageButton caller) {
		if (DisplayControlsBox.expressionDisplay == DisplayControlsBox.EXPRESSION_DISPLAY.MONEY) {
			DisplayControlsBox.expressionDisplay = DisplayControlsBox.EXPRESSION_DISPLAY.TIME;
			DisplayControlsBox.expressionSymbol = DisplayControlsBox.quantityExpressionSymbol;
			caller.setOffState();
		} else {
			DisplayControlsBox.expressionDisplay = DisplayControlsBox.EXPRESSION_DISPLAY.MONEY;
			DisplayControlsBox.expressionSymbol = DisplayControlsBox.moneyExpressionSymbol;
			caller.setOnState();
		}
		TabbedTableViewer.refreshTables();
	}

}
