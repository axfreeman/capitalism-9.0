package capitalism.view.command;

import capitalism.editor.Editor;
import capitalism.editor.EditorManager;
import capitalism.view.custom.ImageButton;
import javafx.stage.Stage;

public class AddRowCommand implements DisplayCommand {
	Stage editorStage;
	EditorManager editorManager;

	@Override public void execute(ImageButton caller) {
		switch (Editor.selectedTab()) {
		case COMMODITY:
			Editor.addCommodity();
			break;
		case INDUSTRY:
			Editor.showIndustryDialogue();
			break;
		case SOCIALCLASS:
			Editor.addSocialClass();
			break;
		default:
		}
	}
}
