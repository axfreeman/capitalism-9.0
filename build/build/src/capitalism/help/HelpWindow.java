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
package capitalism.help;

import capitalism.editor.EditorManager;
import capitalism.view.ViewManager;
import javafx.beans.value.ChangeListener;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Experimental class to be developed, possibly, into a docking help window
 * Currently (25/3/18) not in use, but tested to hold a fixed position in the calling window
 */

public class HelpWindow {
	private static Stage helpStage;
	final static WebView browser = new WebView();
	final static WebEngine webEngine = browser.getEngine();
	private static Stage editorStage;

	static ChangeListener<Number> widthListener = (observable, oldValue, newValue) -> {
		helpStage.setX(editorStage.getX() + editorStage.getHeight() / 2);
	};
	static ChangeListener<Number> heightListener = (observable, oldValue, newValue) -> {
		helpStage.setY(editorStage.getY() + editorStage.getHeight() / 2);
	};
	static ChangeListener<Number> XListener= (observable, oldValue, newValue) -> {
		helpStage.setX(editorStage.getX() + editorStage.getHeight() / 2);
	};
	static ChangeListener<Number> YListener= (observable, oldValue, newValue) -> {
		helpStage.setY(editorStage.getY() + editorStage.getHeight() / 2);
	};

	public static void buildHelpWindow() {
		helpStage = new Stage();
		editorStage = EditorManager.getEditorStage();

		editorStage.widthProperty().addListener(widthListener);
		editorStage.heightProperty().addListener(heightListener);		
		editorStage.xProperty().addListener(XListener);
		editorStage.yProperty().addListener(YListener);		
		
		helpStage.setTitle("Help");
		helpStage.setWidth(ViewManager.windowWidth / 3);
		helpStage.setHeight(ViewManager.windowHeight / 3);

		helpStage.initOwner(EditorManager.getEditorStage());
		helpStage.initStyle(StageStyle.UNDECORATED);

		Scene scene = new Scene(new Group());

		VBox root = new VBox();

		ScrollPane scrollPane = new ScrollPane();
		scrollPane.setContent(browser);

		// User can scroll by panning, only
		scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
		scrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);

		webEngine.loadContent("<p>welcome</p><p>Help facility under development</p>");

		root.getChildren().addAll(scrollPane);
		scene.setRoot(root);

		helpStage.setScene(scene);
	}

	/**
	 * Show the help window
	 */
	public static void showHelpWindow() {
		helpStage.setX(editorStage.getX() + editorStage.getHeight() / 2);
		helpStage.setY(editorStage.getY() + editorStage.getHeight() / 2);
		helpStage.setAlwaysOnTop(true);
		helpStage.show();
	}

	/**
	 * Hide the help window
	 */
	public static void hideHelpWindow() {
		helpStage.hide();
	}
}
