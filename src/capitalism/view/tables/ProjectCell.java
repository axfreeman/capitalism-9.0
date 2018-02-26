package capitalism.view.tables;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.model.Project;
import javafx.scene.control.ListCell;

public class ProjectCell extends ListCell<Project> {
	static final Logger logger = LogManager.getLogger("ProjectCell");
	public ProjectCell(){
	}
	@Override
	public void updateItem(Project item, boolean empty) {
		super.updateItem(item,empty);
		setPrefWidth(100);
		if (item==null)	return;
		setText(item.getDescription());
	}
}
