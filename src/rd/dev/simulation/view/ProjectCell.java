package rd.dev.simulation.view;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.scene.control.ListCell;
import rd.dev.simulation.model.Project;

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
