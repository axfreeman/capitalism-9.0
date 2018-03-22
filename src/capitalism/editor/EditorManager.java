/*
 *  capitalism.view.editoreman 2017-2019
 *  
 *  This file is part of the Capitalism Simulation, abbreviated to CapSim
 *  in thEditorManagerf this project
 *
 *  Capsim is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either project 3 of the License, or
 *  (at your option) any later project.
*
*   Capsim is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU General Public License for more details.
*
*   You should have received a copy of the GNU General Public License
*   along with Capsim.  If not, see <http://www.gnu.org/licenses/>.
*/
package capitalism.editor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.utils.Reporter;
import capitalism.view.ViewManager;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class EditorManager {
	private final static Logger logger = LogManager.getLogger("EditorManager");

	private Stage editorStage = null;
	private Scene editorScene;
	private Editor editor;;

	public EditorManager() {
		Reporter.report(logger, 0, "Editor Window Opened");
		editorStage = new Stage();
		editor = new Editor();
		editorScene = new Scene(editor, ViewManager.windowWidth, ViewManager.windowHeight);
		String css = getClass().getResource("/SimulationTheme.css").toExternalForm();
		editorScene.getStylesheets().add(css);
		editorStage.setScene(editorScene);
		Editor.loadFromSimulation();
		Editor.buildTables();
		editorStage.setOnShown(collectHeights);
		editorStage.showAndWait();
	}
	
	private static EventHandler<WindowEvent> collectHeights= new EventHandler <WindowEvent> (){
		@Override public void handle(WindowEvent t) {
		Editor.getHeights();
		Editor.setHeights();
		}
	};


}
