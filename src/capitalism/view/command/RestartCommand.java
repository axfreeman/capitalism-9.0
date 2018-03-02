package capitalism.view.command;

import capitalism.view.ViewManager;
import capitalism.view.custom.ImageButton;

public class RestartCommand implements DisplayCommand{

	@Override public void execute(ImageButton caller) {
		ViewManager.restart();
	}

}
