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
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class EditorManager {
	private final static Logger logger = LogManager.getLogger("EditorManager");

	private static Stage editorStage = null;
	private static Scene editorScene;
	private static Editor editor;;

	public static void buildEditorWindow() {
		Reporter.report(logger, 0, "Create Editor Window");
		editorStage = new Stage();
		editor = new Editor();
		editorScene = new Scene(editor, ViewManager.windowWidth, ViewManager.windowHeight-25);
		
		// style the Editor window (and in particular, the table columns)
		String css = Editor.class.getResource("/SimulationTheme.css").toExternalForm();
		editorScene.getStylesheets().add(css);

		// get ready to show the window
		editorStage.setScene(editorScene);
		
		// make the window modal so we can't do anything else until it is closed
		editorStage.initModality(Modality.WINDOW_MODAL);
        editorStage.initOwner(ViewManager.getPrimaryStage());

        // at present, when the editor fires up, we load the simple reproduction project from the simulation.
		EditorLoader.loadFromSimulation(1);
	}
	
	/**
	 * Show the editor window
	 */
	
	public static void showEditorWindow() {
		editorStage.showAndWait();
	}
	
	/**
	 * Close the editor window
	 */
	public static void closeEditorWindow() {
		editorStage.close();
	}
}
