package capitalism.view.command;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.editor.EditorManager;
import capitalism.view.custom.DisplayControlsBox;
import capitalism.view.custom.ImageButton;
import javafx.stage.Stage;

public class ShowEditorCommand implements DisplayCommand {
	final Logger logger = LogManager.getLogger("DisplayControlsBar");
	Stage editorStage;
	EditorManager editorManager;

	@Override public void execute(ImageButton caller) {
		if(DisplayControlsBox.editorIsOpen())
			return;
		editorManager = new EditorManager();
	}
}
