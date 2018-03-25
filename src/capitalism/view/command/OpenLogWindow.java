package capitalism.view.command;

import capitalism.reporting.Reporter;
import capitalism.view.custom.ImageButton;

public class OpenLogWindow implements DisplayCommand{

	@Override public void execute(ImageButton caller) {
		Reporter.logWindow.showLoggerWindow();
	}

}
