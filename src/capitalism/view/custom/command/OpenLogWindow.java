package capitalism.view.custom.command;

import capitalism.utils.Reporter;
import capitalism.view.custom.ImageButton;

public class OpenLogWindow implements DisplayCommand{

	@Override public void execute(ImageButton caller) {
		Reporter.logWindow.showLoggerWindow();
	}

}
