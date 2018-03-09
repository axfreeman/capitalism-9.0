package capitalism.view.editor;

import capitalism.view.command.CreateProjectCommand;
import capitalism.view.custom.ImageButton;
import javafx.scene.layout.HBox;

public class EditorControlBar extends HBox {
	private static ImageButton createProjectButton =new ImageButton("littlePlus.png",null, new CreateProjectCommand(),
			"Create a new project","Create a new project (under development)");

	EditorControlBar(){
		setPrefWidth(400);
		getChildren().add(createProjectButton);
	}
	
}
