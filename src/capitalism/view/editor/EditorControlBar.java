package capitalism.view.editor;

import capitalism.view.command.CreateProjectCommand;
import capitalism.view.custom.ImageButton;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public class EditorControlBar extends HBox {
	private static ImageButton createProjectButton =new ImageButton("littlePlus.png",null, new CreateProjectCommand(),
			"Create a new project","Create a new project (under development)");
	private Button btnNew = new Button("New Record");

	EventHandler<ActionEvent> btnNewHandler = new EventHandler<ActionEvent>() {
		@Override public void handle(ActionEvent t) {
			Editor.setCommodityData(EditableCommodity.editableCommodities(1, 1));
			Editor.setIndustryData(EditableIndustry.editableIndustries(1,1));
			Editor.setSocialClassData(EditableSocialClass.editableSocialClasses(1,1));
		}
	};

	EditorControlBar(){
		btnNew.setOnAction(btnNewHandler);
		setPrefWidth(400);
		getChildren().add(btnNew);
		getChildren().add(createProjectButton);
	}
	
}
