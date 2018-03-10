package capitalism.view.command;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.editor.EditorManager;
import capitalism.utils.Reporter;
import capitalism.view.custom.ImageButton;
import javafx.stage.Stage;

public class CreateProjectCommand implements DisplayCommand {
	final Logger logger = LogManager.getLogger("DisplayControlsBar");

	Stage editorStage;
	EditorManager editorManager;

	@Override public void execute(ImageButton caller) {
		Reporter.report(logger, 0, "User asked to create project");
	}
}
