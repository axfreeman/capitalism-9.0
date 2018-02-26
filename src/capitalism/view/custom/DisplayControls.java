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
import capitalism.utils.Reporter;
import capitalism.view.ViewManager;
import capitalism.view.tables.TabbedTableViewer;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;

public class DisplayControls extends HBox {
	final Logger logger = LogManager.getLogger("DisplayControlsBar");

	// This enum and the following variables determine whether values, and prices, are displayed in units of money or time
	// They control the 'hints' colouring of the columns and the symbols (eg '$', '#') placed before price and value magnitudes in the tables display

	public enum DISPLAY_AS_EXPRESSION {
		MONEY, TIME;
	}

	public static DISPLAY_AS_EXPRESSION valuesExpressionDisplay = DISPLAY_AS_EXPRESSION.MONEY;
	public static DISPLAY_AS_EXPRESSION pricesExpressionDisplay = DISPLAY_AS_EXPRESSION.MONEY;

	public static String moneyExpressionSymbol = "$";
	public static String quantityExpressionSymbol = "#";

	public static String pricesExpressionSymbol = moneyExpressionSymbol;
	public static String valuesExpressionSymbol = moneyExpressionSymbol;
	
	public static boolean displayHints = false;

	private Font buttonFonts = new Font(10);
	private static Button dataDumpButton = new Button("Save");
	private static Button loadButton = new Button("Load");
	private static Button restartButton = new Button("Restart");
	
	private static Button colourHintButton= new Button("Hints");
	private static Button logButton= new Button ("Log");
	private static Button toggleDecimalButton= new Button(".>>");
	private static Button toggleValuesExpressionButton= new Button ("$");
	private static Button togglePricesExpressionButton= new Button ("$");
	private static Button graphicsToggle= new Button ("Graphics");

	// this is the projectCombo box

	private static ProjectCombo projectCombo = null;

	private ViewManager vm =null;
	
	ButtonBar leftButtonBar =  new ButtonBar();
	ButtonBar rightButtonBar =new ButtonBar();
	Pane spacer = new Pane();
	
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
		dataDumpButton.setFont(buttonFonts);
		loadButton.setFont(buttonFonts);
		restartButton.setFont(buttonFonts);
		setTip(dataDumpButton, "Save the current project including its history to a directory that you choose");
		setTip(restartButton, "Restart the current project from scratch");
		setTip(restartButton, "Restart the current project from scratch");
		restartButton.setOnAction((event) -> {
			vm.restart();
		});
		dataDumpButton.setOnAction((event) -> {
			Reporter.report(logger, 1, "User requested dump of entire database to CSV files");
			vm.dataDump();
		});
		loadButton.setOnAction((event) -> {
			Reporter.report(logger, 1, "User requested loading the database from a new location");
			vm.dataLoad();
		});
		
		leftButtonBar.getButtons().addAll(dataDumpButton,loadButton,restartButton);
	}
	private void buildRightBar() {
		colourHintButton.setFont(buttonFonts);
		logButton.setFont(buttonFonts);
		toggleDecimalButton.setFont(buttonFonts);
		toggleValuesExpressionButton.setFont(buttonFonts);
		togglePricesExpressionButton.setFont(buttonFonts);
		graphicsToggle.setFont(buttonFonts);
		setTip(colourHintButton, "Display colour hints to indicate quantity, value and price columns");
		setTip(logButton, "Open a window displaying a step-by-step report on the simulation");
		setTip(graphicsToggle, "Display icons instead of text labels for some of the columns, creating more space to view the results");
		setTip(toggleDecimalButton, "Display magnitudes to two decimal points more");
		setTip(toggleValuesExpressionButton, "Display Values as time instead of money");
		setTip(togglePricesExpressionButton, "Display Prices as time instead of money");
		colourHintButton.setOnAction((event) -> {
			toggleHints();
		});
		logButton.setOnAction((event) -> {
			Reporter.logWindow.showLoggerWindow();
		});
		toggleDecimalButton.setOnAction((event) -> {
			toggleDecimals();
		});

		toggleValuesExpressionButton.setOnAction((event) -> {
			toggleValueExpression();
		});

		togglePricesExpressionButton.setOnAction((event) -> {
			togglePriceExpression();
		});

		graphicsToggle.setOnAction((event) -> {
			ViewManager.getTabbedTableViewer().switchHeaderDisplays();
		});
		rightButtonBar.getButtons().addAll(logButton,toggleDecimalButton,toggleValuesExpressionButton,togglePricesExpressionButton,graphicsToggle);
	}
	
	/**
	 * toggle on and off colour hints for the columns
	 * 
	 */
	public void toggleHints() {
		if (displayHints) {
			setTip(colourHintButton, "Display colour hints to indicate quantity, value and price columns");
			colourHintButton.setText("Hints");
			displayHints = false;
			setTip(colourHintButton, "");
		} else {
			displayHints = true;
			setTip(colourHintButton, "Turn off colour hints for columns");
			colourHintButton.setText("No Hints");
		}
		TabbedTableViewer.refreshTables();
	}
	
	public void toggleDecimals() {
		if (ViewManager.largeNumbersFormatString.equals("%1$,.0f")) {
			ViewManager.largeNumbersFormatString = "%1$,.2f";
			ViewManager.smallNumbersFormatString = "%1$.4f";
			setTip(toggleDecimalButton, "Display all large magnitudes to zero decimal places and all small magnitudes to two decimal places");
			toggleDecimalButton.setText("<<..");
		} else {
			ViewManager.largeNumbersFormatString = "%1$,.0f";
			ViewManager.smallNumbersFormatString = "%1$.2f";
			setTip(toggleDecimalButton, "Display all large magnitudes to two decimal places and all small magnitudes to four decimal places");
			toggleDecimalButton.setText("..>>");
		}
		TabbedTableViewer.refreshTables();
	}
	public void registerViewManager(ViewManager vm) {
		this.vm=vm;
	}
	public static void togglePriceExpression() {
		if (pricesExpressionDisplay == DISPLAY_AS_EXPRESSION.MONEY) {
			pricesExpressionDisplay = DISPLAY_AS_EXPRESSION.TIME;
			pricesExpressionSymbol = quantityExpressionSymbol;
			togglePricesExpressionButton.setText("$Prices");
			setTip(togglePricesExpressionButton, "Display Prices as money instead of time");
		} else {
			pricesExpressionDisplay = DISPLAY_AS_EXPRESSION.MONEY;
			pricesExpressionSymbol = moneyExpressionSymbol;
			togglePricesExpressionButton.setText("#Prices");
			setTip(togglePricesExpressionButton, "Display Prices as time instead of money");
		}
		TabbedTableViewer.refreshTables();
	}

	public static void toggleValueExpression() {
		if (valuesExpressionDisplay == DISPLAY_AS_EXPRESSION.MONEY) {
			valuesExpressionDisplay = DISPLAY_AS_EXPRESSION.TIME;
			valuesExpressionSymbol = quantityExpressionSymbol;
			toggleValuesExpressionButton.setText("$Values");
			setTip(toggleValuesExpressionButton, "Display Prices as money instead of time");
		} else {
			valuesExpressionDisplay = DISPLAY_AS_EXPRESSION.MONEY;
			valuesExpressionSymbol = moneyExpressionSymbol;
			toggleValuesExpressionButton.setText("#Values");
			setTip(toggleValuesExpressionButton, "Display Prices as time instead of money");
		}
		TabbedTableViewer.refreshTables();
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

		// sneaky way to force the display options onto the new project

		togglePriceExpression();
		togglePriceExpression();
		toggleValueExpression();
		toggleValueExpression();

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




}
