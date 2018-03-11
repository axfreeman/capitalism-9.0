package capitalism.view.command;

import capitalism.editor.EditorManager;
import capitalism.view.custom.DisplayControlsBox;
import capitalism.view.custom.ImageButton;
import javafx.stage.Stage;

public class ShowEditorCommand implements DisplayCommand {
	Stage editorStage;
	EditorManager editorManager;

	@Override public void execute(ImageButton caller) {
		if(DisplayControlsBox.editorIsOpen())
			return;
		editorManager = new EditorManager();
	}
}
