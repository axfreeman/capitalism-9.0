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
package capitalism.view.custom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.Simulation;
import capitalism.model.Global;
import capitalism.model.Project;
import capitalism.view.custom.command.ColourHintsCommand;
import capitalism.view.custom.command.LoadCommand;
import capitalism.view.custom.command.OpenLogWindow;
import capitalism.view.custom.command.RestartCommand;
import capitalism.view.custom.command.SaveCommand;
import capitalism.view.custom.command.ToggleDecimals;
import capitalism.view.custom.command.ToggleGraphicsCommand;
import capitalism.view.custom.command.TogglePricesCommand;
import capitalism.view.custom.command.ToggleValuesCommand;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;

/**
 * The bar where all the visual display controls are put.
 * Fairly rough and ready. This class contains all the state variables such as the graphicsState,
 * and assigns DisplayCommands to ImageButtons. Both these are 'in-house' classes.
 */

public class DisplayControls extends HBox {
	final Logger logger = LogManager.getLogger("DisplayControlsBar");
	public static ContentDisplay graphicsState = ContentDisplay.TEXT_ONLY; // Whether to display graphics, text, or both

	public static enum DISPLAY_AS_EXPRESSION {
		MONEY, TIME;
	}

	public static DISPLAY_AS_EXPRESSION valuesExpressionDisplay = DISPLAY_AS_EXPRESSION.MONEY;
	public static DISPLAY_AS_EXPRESSION pricesExpressionDisplay = DISPLAY_AS_EXPRESSION.MONEY;

	public static String moneyExpressionSymbol = "$";
	public static String quantityExpressionSymbol = "#";
	public static String pricesExpressionSymbol = moneyExpressionSymbol;
	public static String valuesExpressionSymbol = moneyExpressionSymbol;
	public static boolean displayHints = false;

	private static ImageButton colourHintsButton= new ImageButton("hinton.png","hintoff.png",new ColourHintsCommand(),"No colour hints","Show colour hints");
	private static ImageButton restartButton= new ImageButton("restart.png",null,new RestartCommand(),"Restart this project","Restart this project");
	private static ImageButton toggleGraphicsButton= new ImageButton("graphics.png","text.png",new ToggleGraphicsCommand(),"Text column headers","Graphic column headers");
	private static ImageButton toggleValuesButton=new ImageButton("timevalue.png","dollarvalue.png",new ToggleValuesCommand(),"Money values","Labour Time Values");
	private static ImageButton togglePricesButton=new ImageButton("timeprice.png","dollarprice.png",new TogglePricesCommand(),"Money prices","Labour Time Prices"); 
	private static ImageButton logButton = new ImageButton("log.png", null,new OpenLogWindow(),"Hide Log Window","Show Log Window");
	private static ImageButton decimalsButton= new ImageButton("more.png", null,new ToggleDecimals(),"More digits after the decimal","Fewer digits after the decimal");
	private static ImageButton dataLoadButton= new ImageButton("load.png",null,new LoadCommand(),"Load data from your computer","");
	private static ImageButton dataDumpButton=new ImageButton("save2.png",null, new SaveCommand(),"Save the database to your computer","");

	// this is the projectCombo box
	private static ProjectCombo projectCombo = null;

	private static ButtonBar leftButtonBar =  new ButtonBar();
	private static ButtonBar rightButtonBar =new ButtonBar();
	private static Pane spacer = new Pane();
	
	public DisplayControls(){
		ObservableList<Project> projects = Project.observableProjects();
		Project currentProject = Project.projectSingle(Simulation.projectCurrent);
		String currentProjectDescription = currentProject.getDescription();
		projectCombo = new ProjectCombo(projects, currentProjectDescription);
		setMaxWidth(Double.MAX_VALUE);
		buildLeftBar();
		buildRightBar();
		spacer.setPrefHeight(50);
		spacer.setPrefWidth(50);
		setHgrow(spacer, Priority.ALWAYS);
		getChildren().addAll(projectCombo,leftButtonBar,spacer,rightButtonBar);
	}
	
	private void buildLeftBar() {
		leftButtonBar.getButtons().addAll(dataDumpButton,dataLoadButton,restartButton);
	}
	private void buildRightBar() {
		toggleValuesButton.setImageWidth(40);
		togglePricesButton.setImageWidth(40);
		logButton.setImageWidth(30);
		logButton.setImageHeight(30);

		rightButtonBar.getButtons().addAll(logButton,colourHintsButton, decimalsButton,toggleValuesButton,togglePricesButton,toggleGraphicsButton);
	}
	
	/**
	 * extract the per-project currency and quantity symbols from the globals record.
	 * NOTE these should perhaps be in the project record not the globals record.
	 * But if a simulation involves a currency reform, it could be in the right place after all.
	 */
	public static void setExpressionSymbols() {
		Global global = Global.getGlobal();
		moneyExpressionSymbol = global.getCurrencySymbol();
		quantityExpressionSymbol = global.getQuantitySymbol();
	}
	/**
	 * adds a tooltip to a Button. Overloads {@link setTip}
	 * 
	 * @param button
	 *            the table
	 * @param text
	 *            the tooltip
	 */
	public static void setTip(Button button, String text) {
		Tooltip tip = new Tooltip();
		tip.setText(text);
		tip.setFont(new Font(15));
		button.setTooltip(tip);
	}
	/**
	 * @param graphicsState the graphicsState to set
	 */
	public static void setGraphicsState(ContentDisplay graphicsState) {
		DisplayControls.graphicsState = graphicsState;
	}

	/**
	 * @return the graphicsState
	 */
	public static ContentDisplay getGraphicsState() {
		return graphicsState;
	}
}
