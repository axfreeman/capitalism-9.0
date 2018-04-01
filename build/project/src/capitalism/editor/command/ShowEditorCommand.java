package capitalism.editor.command;

import capitalism.editor.EditorManager;
import capitalism.view.command.DisplayCommand;
import capitalism.view.custom.ImageButton;
import javafx.stage.Stage;

public class ShowEditorCommand implements DisplayCommand {
	Stage editorStage;
	EditorManager editorManager;

	@Override public void execute(ImageButton caller) {
		EditorManager.showEditorWindow();
	}
}
