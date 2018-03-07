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
*/package capitalism.view.custom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.view.TabbedTableViewer;
import capitalism.view.ViewManager;
import capitalism.controller.Simulation;
import capitalism.model.Stock;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

/**
 * Controls that regulate how changes are displayed
 *
 */
public class TrackingControlsBox extends VBox {
	static final Logger logger = LogManager.getLogger("TrackingControls");

	private GridPane grid = new GridPane();
	private static Label timeStampCursorLabel= new Label ();
	private static Label projectCursorLabel= new Label ();
	private Label stocksAsDescription = new Label("Show stocks as ...");
	private Label compareWith=new Label("Compare With");

	private RadioButton showPricesButton=  new RadioButton("Prices");
	private RadioButton showValuesButton=  new RadioButton("Values");
	private RadioButton showQuantitiesButton=  new RadioButton("Quantities");
	
	private RadioButton startSelectorButton = new RadioButton("Start");
	private RadioButton endSelectorButton =  new RadioButton("End");
	private RadioButton customSelectorButton = new RadioButton("Custom");
	private RadioButton previousSelectorButton = new RadioButton("Previous");
	
	private HBox deltaContainer =new HBox();
	private CheckBox deltasCheckBox = new CheckBox();
	public static boolean displayDeltas = false;

	private ToggleGroup comparatorToggle = new ToggleGroup();
	private ToggleGroup magnitudeToggle = new ToggleGroup();

	public enum COMPARATOR_STATE {
		END("End"), START("Start"), PREVIOUS("Previous"), CUSTOM("Custom");
		String text;

		private COMPARATOR_STATE(String text) {
			this.text = text;
		}

		public String text() {
			return text;
		}
	}
	static COMPARATOR_STATE comparatorState = COMPARATOR_STATE.PREVIOUS;

	public TrackingControlsBox(){
		setMaxWidth(Double.MAX_VALUE);
		setMinWidth(150);
		setPrefHeight(750);
		setPrefWidth(150);
		
		ViewManager.setTip(showQuantitiesButton, "Switch to display of quantities in the overview table");
		ViewManager.setTip(showPricesButton, "Switch to display of prices in the overview table");
		ViewManager.setTip(showValuesButton, "Switch to display of values in the overview table");
		ViewManager.setTip(showQuantitiesButton, "Switch to display of quantities in the overview table");
		ViewManager.setTip(showPricesButton, "Switch to display of prices in the overview table");
		ViewManager.setTip(showValuesButton, "Switch to display of values in the overview table");
		// setTip(fullRepricing, "Not yet operational: when this is checked, money and labour power are included in repricing at the end of the period");
		
		ColumnConstraints topConstraint= new ColumnConstraints();
		ColumnConstraints bottomConstraint = new ColumnConstraints();
		topConstraint.setMinWidth(10);
		topConstraint.setPrefWidth(100);
		bottomConstraint.setMinWidth(10);
		bottomConstraint.setPrefWidth(100);
		topConstraint.setHgrow(Priority.NEVER);
		bottomConstraint.setHgrow(Priority.SOMETIMES);
		grid.getColumnConstraints().addAll(topConstraint,bottomConstraint);
		setVgrow(grid, Priority.NEVER);
		RowConstraints rowConstraint = new RowConstraints();
		rowConstraint.setPrefHeight(30);
		rowConstraint.setVgrow(Priority.NEVER);
		grid.getRowConstraints().add(rowConstraint);
		grid.add(timeStampCursorLabel, 0, 1);
		grid.add(projectCursorLabel, 1, 1);
		Insets gridInset  =  new Insets(0, 2, 0, 2);
		setMargin(grid, gridInset);
		Separator separator = new Separator();
		separator.setPrefWidth(200);
		getChildren().addAll(grid,separator);
		
		showValuesButton.minHeight(0);
		showValuesButton.prefHeight(17);
		showValuesButton.setPrefWidth(100);
		showValuesButton.setPadding(new Insets(0,2,0,0));
		showPricesButton.minHeight(0);
		showPricesButton.prefHeight(17);
		showPricesButton.setPrefWidth(100);
		showPricesButton.setPadding(new Insets(0,2,0,0));
		showQuantitiesButton.minHeight(0);
		showQuantitiesButton.prefHeight(17);
		showQuantitiesButton.setPrefWidth(100);
		showQuantitiesButton.setPadding(new Insets(0,2,0,0));
		
		separator= new Separator();
		
		getChildren().addAll(stocksAsDescription, showValuesButton,showPricesButton,showQuantitiesButton,separator);
		
		separator= new Separator();
		getChildren().addAll(compareWith, startSelectorButton,endSelectorButton,previousSelectorButton,customSelectorButton,separator);
		
		deltaContainer.setPrefWidth(150);
		deltaContainer.setPrefHeight(19);
		
		deltasCheckBox.setText("Deltas");
		deltasCheckBox.setTextAlignment(TextAlignment.RIGHT);

		deltaContainer.getChildren().add(deltasCheckBox);
		getChildren().add(deltaContainer);
		
		initializeTrackingControls();
	}

	/**
	 * initialise all the tracking controls (set handlers, etc)
	 */
	
	private void initializeTrackingControls(){
		startSelectorButton.setToggleGroup(comparatorToggle);
		endSelectorButton.setToggleGroup(comparatorToggle);
		customSelectorButton.setToggleGroup(comparatorToggle);
		previousSelectorButton.setToggleGroup(comparatorToggle);
		startSelectorButton.setUserData(COMPARATOR_STATE.START);
		endSelectorButton.setUserData(COMPARATOR_STATE.END);
		customSelectorButton.setUserData(COMPARATOR_STATE.CUSTOM);
		previousSelectorButton.setUserData(COMPARATOR_STATE.PREVIOUS);
		previousSelectorButton.setSelected(true);
		comparatorToggle.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			@Override public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
				if (comparatorToggle.getSelectedToggle() != null) {
					comparatorState = (COMPARATOR_STATE) comparatorToggle.getSelectedToggle().getUserData();

					switch (comparatorState) {
					case END:
						Simulation.setTimeStampComparatorCursor(Simulation.timeStampIDCurrent);
						break;
					case START:
						Simulation.setTimeStampComparatorCursor(1);
						break;
					case PREVIOUS:
						int cursor = Simulation.timeStampDisplayCursor;
						cursor = cursor < 2 ? 1 : cursor - 1;
						Simulation.setTimeStampComparatorCursor(cursor);
						break;
					case CUSTOM:
						Simulation.setTimeStampComparatorCursor(Simulation.timeStampDisplayCursor);
						break;
					default:
						logger.error("Unknown radio button {} selected ");
						break;
					}
					TabbedTableViewer.refreshTables();
				}
			}
		});

		showValuesButton.setToggleGroup(magnitudeToggle);
		showPricesButton.setToggleGroup(magnitudeToggle);
		showQuantitiesButton.setToggleGroup(magnitudeToggle);
		showValuesButton.setUserData("VALUES");
		showPricesButton.setUserData("PRICES");
		showQuantitiesButton.setUserData("QUANTITIES");
		showPricesButton.setSelected(true);
		magnitudeToggle.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			@Override public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
				if (magnitudeToggle.getSelectedToggle() != null) {
					String selected = magnitudeToggle.getSelectedToggle().getUserData().toString();
					switch (selected) {
					case "VALUES":
						TabbedTableViewer.setDisplayAttribute(Stock.ValueExpression.VALUE);
						break;
					case "PRICES":
						TabbedTableViewer.setDisplayAttribute(Stock.ValueExpression.PRICE);
						break;
					case "QUANTITIES":
						TabbedTableViewer.setDisplayAttribute(Stock.ValueExpression.QUANTITY);
						break;
					default:
						logger.error("Unknown radio button {} selected ", selected);
						break;
					}
					TabbedTableViewer.refreshTables();
				}
			}
		});
		deltasCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {

			@Override public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				logger.debug("User chose to display deltas:" + newValue);
				displayDeltas = newValue;
				TabbedTableViewer.refreshTables();
			}
		});
	}
	
	
	/**
	 * @return the timeStampCursorLabel
	 */
	public static Label getTimeStampCursorLabel() {
		return timeStampCursorLabel;
	}

	/**
	 * @return the projectCursorLabel
	 */
	public static Label getProjectCursorLabel() {
		return projectCursorLabel;
	}

	/**
	 * @return the showPricesButton
	 */
	public RadioButton getShowPricesButton() {
		return showPricesButton;
	}

	/**
	 * @return the showValuesButton
	 */
	public RadioButton getShowValuesButton() {
		return showValuesButton;
	}

	/**
	 * @return the showQuantitiesButton
	 */
	public RadioButton getShowQuantitiesButton() {
		return showQuantitiesButton;
	}

	/**
	 * @return the startSelectorButton
	 */
	public RadioButton getStartSelectorButton() {
		return startSelectorButton;
	}

	/**
	 * @return the endSelectorButton
	 */
	public RadioButton getEndSelectorButton() {
		return endSelectorButton;
	}

	/**
	 * @return the customSelectorButton
	 */
	public RadioButton getCustomSelectorButton() {
		return customSelectorButton;
	}

	/**
	 * @return the previousSelectorButton
	 */
	public RadioButton getPreviousSelectorButton() {
		return previousSelectorButton;
	}

	/**
	 * @return the deltasCheckBox
	 */
	public CheckBox getDeltasCheckBox() {
		return deltasCheckBox;
	}

	/**
	 * @return the comparatorState
	 */
	public static COMPARATOR_STATE getComparatorState() {
		return comparatorState;
	}

}
