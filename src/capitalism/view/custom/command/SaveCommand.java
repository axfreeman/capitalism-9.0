package capitalism.view.custom.command;

import capitalism.view.ViewManager;
import capitalism.view.custom.ImageButton;

public class SaveCommand implements DisplayCommand{

	@Override public void execute(ImageButton caller) {
		ViewManager.dataDump();
	}

}
