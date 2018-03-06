package capitalism.view.command;

import capitalism.utils.XMLutils;
import capitalism.view.ViewManager;
import capitalism.view.custom.ImageButton;

public class DumpCommand implements DisplayCommand{

	@Override public void execute(ImageButton caller) {
		XMLutils.makeXML(1);
		ViewManager.dataDump();
	}

}
