/*
 *  Copyright (C) Alan Freeman 2017-2019
 *  
 *  This file is part of the Capitalism Simulation, abbreviated to CapSim
 *  in the remainder of this project
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

package rd.dev.simulation.view;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class LogWindow {

	// this scene used for the logger window. Quite primitive

	private static Scene logScene;
	private Stage loggingStage = new Stage();
	private HBox hBox = new HBox();
	private TreeView<Label> treeView;
	private double logWindowWidth = ViewManager.screenBounds.getWidth() * 0.5;
	private double logWindowHeight = ViewManager.screenBounds.getHeight() * 0.9;
	private TreeItem<Label> lastLevel1;
	private TreeItem<Label> lastLevel2;

	public LogWindow() {
		// TODO think about all the sizing issues. Ultimately, the window should be dockable

		TreeItem<Label> rootItem = new TreeItem<Label>(new Label("Log"));
		TreeItem<Label> firstLevel1Item=new TreeItem<Label>(new Label("STARTUP"));
		TreeItem<Label> secondLevelItem=new TreeItem<Label>(new Label("Log Window Startup"));
		rootItem.setExpanded(true);

		treeView = new TreeView<Label>(rootItem);
		
		//TODO this code just to catch level errors in the calling methods
		//TODO error checking - essentially, no higher level item should be added until a 'completion' call has been made at the lower level.
		//or something like that.
		
		rootItem.getChildren().add(firstLevel1Item);
		lastLevel1=firstLevel1Item;
		firstLevel1Item.getChildren().add(secondLevelItem);
		lastLevel2=secondLevelItem;

		treeView.setPrefHeight(logWindowHeight);
		treeView.setMaxHeight(Region.USE_PREF_SIZE);
		treeView.setPrefWidth(logWindowWidth);
		logScene = new Scene(hBox, logWindowWidth, logWindowHeight);
		hBox.getChildren().add(treeView);
		HBox.setHgrow(treeView, Priority.ALWAYS);
		loggingStage.setScene(logScene);
		loggingStage.setX(0);
		loggingStage.setY(0);
	}

	/**
	 * display the window.
	 * TODO the window should be shut down when the app closes.
	 */
	public void showLoggerWindow() {
		loggingStage.show();
	}

	/**
	 * add a log message to the window. Messages are always added sequentially but can be displayed in tree form so as to abbreviate the display.
	 * 
	 * @param message
	 *            the message to add
	 * @param treeLevel
	 *            at which level in the hierarchy to add the message. The message is always added to the last root at the immediately higher level
	 */
	public void addItem(String message, int treeLevel) {
		TreeItem<Label> childItem = new TreeItem<Label>(new Label(message));
		Label label=(Label)childItem.getValue();
		switch (treeLevel) {
		case 0: //root
			treeView.getRoot().getChildren().add(childItem);
			label.setTextFill(Color.BLUE);
			label.setWrapText(true);
			lastLevel1=childItem;
			break;
		case 1:
			if(lastLevel1==null) {
				throw new RuntimeException("ERROR:LOG WINDOW CONFIGURATION FUBAR");
			}
			lastLevel1.getChildren().add(childItem);
			label.setTextFill(Color.RED);
			label.setWrapText(true);
			lastLevel2=childItem;
			break;
		case 2:
			if(lastLevel2==null) {
				throw new RuntimeException("ERROR:LOG WINDOW CONFIGURATION FUBAR");
			}
			lastLevel2.getChildren().add(childItem);
			label.setWrapText(true);
			label.setTextFill(Color.DARKGREEN);
			break;
		default:
			treeView.getRoot().getChildren().add(childItem);
			lastLevel1=childItem;
		}
	}
}