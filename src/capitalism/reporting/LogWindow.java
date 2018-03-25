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

package capitalism.reporting;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.view.ViewManager;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * The logWindow is used by the simulation (see the userGuide) to report on the flows of value, price and 
 * quantity in a manner that is accessible to the user, and complements the other reporting mechanisms
 * such as the timeStampView and the tables. Also see {@link Reporter#createLogWindow()} and related code
 *
 */

public class LogWindow {
	private static final Logger logger = LogManager.getLogger(Reporter.class);
	// this scene used for the logger window.

	private static Scene logScene;
	private Stage loggingStage=null;
	private HBox hBox=null;
	private TreeView<Label> treeView= null;
	private double logWindowWidth = ViewManager.windowWidth* 0.6;
	private double logWindowHeight = ViewManager.windowHeight;
	private TreeItem<Label> lastLevel1;
	private TreeItem<Label> lastLevel2;
	private TreeItem<Label> lastLevel3;

	public LogWindow() {
		loggingStage=new Stage();
		hBox=new HBox();
		TreeItem<Label> rootItem = new TreeItem<Label>(new Label("Log"));
		TreeItem<Label> firstLevel1Item=new TreeItem<Label>(new Label("STARTUP"));
		TreeItem<Label> secondLevelItem=new TreeItem<Label>(new Label("Log Window Startup"));
		ZoneId zonedId = ZoneId.systemDefault();
		ZonedDateTime zdt = ZonedDateTime.now( zonedId );
		TreeItem<Label> thirdLevelItem=new TreeItem<Label>(new Label(zdt.toString()));
		rootItem.setExpanded(true);

		treeView = new TreeView<Label>(rootItem);
		
		rootItem.getChildren().add(firstLevel1Item);
		lastLevel1=firstLevel1Item;
		firstLevel1Item.getChildren().add(secondLevelItem);
		lastLevel2=secondLevelItem;
		secondLevelItem.getChildren().add(thirdLevelItem);
		lastLevel3=thirdLevelItem;
		

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
				Dialogues.alert(logger, "Something went wrong with the logging. Please contact the developer");
			}
			lastLevel1.getChildren().add(childItem);
			label.setText(" "+label.getText());
			label.setTextFill(Color.RED);
			label.setWrapText(true);
			lastLevel2=childItem;
			break;
		case 2:
			if(lastLevel2==null) {
				Dialogues.alert(logger, "Something went wrong with the logging. Please contact the developer");
			}
			lastLevel2.getChildren().add(childItem);
			label.setWrapText(false);
			label.setText("  "+label.getText());
			label.setTextFill(Color.DARKGREEN);
			lastLevel3=childItem;
			break;
		case 3:
			if(lastLevel3==null) {
				Dialogues.alert(logger, "Something went wrong with the logging. Please contact the developer");
			}
			lastLevel3.getChildren().add(childItem);
			label.setText("   "+label.getText());
			label.setWrapText(false);
			label.setTextFill(Color.BLACK);
			break;
			
		default:
			treeView.getRoot().getChildren().add(childItem);
			lastLevel1=childItem;
		}
	}
}