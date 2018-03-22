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
package capitalism.editor.model;

import capitalism.controller.Simulation;
import capitalism.model.Commodity;
import capitalism.model.Commodity.FUNCTION;
import capitalism.model.Commodity.ORIGIN;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;

public class EditableCommodity {
	private StringProperty name;
	private DoubleProperty turnoverTime;
	private DoubleProperty unitValue;
	private DoubleProperty unitPrice;
	private StringProperty origin; // whether this is produced by an enterprise or a class
	private StringProperty function;// see enum FUNCTION for list of possible types

	public enum EC_ATTRIBUTE {
		UNIT_VALUE("Unit Value"), UNIT_PRICE("Unit Price"), TURNOVER("Turnover Time"), NAME("Name"), FUNCTION("Function"), ORIGIN("Origin");
		protected String text;

		private EC_ATTRIBUTE(String text) {
			this.text = text;
		}
	}

	/**
	 * Create a completely empty Editable commodity
	 */
	public EditableCommodity() {
		name = new SimpleStringProperty();
		turnoverTime = new SimpleDoubleProperty();
		unitValue = new SimpleDoubleProperty();
		unitPrice = new SimpleDoubleProperty();
		origin = new SimpleStringProperty();
		function = new SimpleStringProperty();
	}

	/**
	 * Create a single Editable commodity with minimal properties
	 * 
	 * @param name
	 *            the name of the new commodity
	 * @param origin
	 *            the origin of the new commodity (SOCIALLY_PRODUCED, INDUSTRIALLY_PRODUCED, MONEY)
	 * @param function
	 *            the function of the new commodity (PRODUCTIVE_INPUT, CONSUMER_GOOD, MONEY)
	 * @return a populated EditableCommodity
	 */
	public static EditableCommodity makeCommodity(String name, ORIGIN origin, FUNCTION function) {
		EditableCommodity commodity = new EditableCommodity();
		commodity.setName(name);
		commodity.setTurnoverTime(1);
		commodity.setUnitValue(1);
		commodity.setUnitPrice(1);
		commodity.setFunction(function.text());
		commodity.setOrigin(origin.text());
		return commodity;
	}

	/**
	 * Create an observable list of EditableCommodity entities (normally for display in the Commodities Table) from the
	 * project identified by the current projectID and timeStamp.
	 * 
	 * @return an observableList of EditableCommodity entities identified by the current projectID and timeStampID
	 */
	public static ObservableList<EditableCommodity> editableCommodities() {
		ObservableList<EditableCommodity> result = FXCollections.observableArrayList();
		for (Commodity c : Commodity.all(Simulation.projectIDcurrent(), Simulation.timeStampIDCurrent())) {
			EditableCommodity oneRecord = new EditableCommodity();
			oneRecord.setName(c.name());
			oneRecord.setUnitValue(c.getUnitValue());
			oneRecord.setUnitPrice(c.getUnitPrice());
			oneRecord.setTurnoverTime(c.getTurnoverTime());
			oneRecord.setFunction(c.getFunction().text());
			oneRecord.setOrigin(c.getOrigin().text());
			result.add(oneRecord);
		}
		return result;
	}

	public void set(EC_ATTRIBUTE attribute, double d) {
		switch (attribute) {
		case UNIT_VALUE:
			unitValue.set(d);
			break;
		case UNIT_PRICE:
			unitPrice.set(d);
			break;
		case TURNOVER:
			turnoverTime.set(d);
			break;
		default:
		}
	}

	public double getDouble(EC_ATTRIBUTE attribute) {
		switch (attribute) {
		case UNIT_VALUE:
			return getUnitValue();
		case UNIT_PRICE:
			return getUnitPrice();
		case TURNOVER:
			return getTurnoverTime();
		default:
			return Double.NaN;
		}
	}

	public void set(EC_ATTRIBUTE attribute, String newValue) {
		switch (attribute) {
		case NAME:
			name.set(newValue);
			break;
		case FUNCTION:
			function.set(newValue);
		case ORIGIN:
			origin.set(newValue);
		default:
			break;
		}
	}

	public String getString(EC_ATTRIBUTE attribute) {
		switch (attribute) {
		case NAME:
			return getName();
		case FUNCTION:
			return getFunction();
		case ORIGIN:
			return getOrigin();
		default:
			return "";
		}
	}

	public static TableColumn<EditableCommodity, Double> makeDoubleColumn(EC_ATTRIBUTE attribute) {
		TableColumn<EditableCommodity, Double> col = new TableColumn<EditableCommodity, Double>(attribute.text);
		col.getStyleClass().add("table-column-right");
		Callback<TableColumn<EditableCommodity, Double>, TableCell<EditableCommodity, Double>> cellFactory = new Callback<TableColumn<EditableCommodity, Double>, TableCell<EditableCommodity, Double>>() {
			public TableCell<EditableCommodity, Double> call(TableColumn<EditableCommodity, Double> p) {
				return new EditableCommodityCell();
			}
		};
		col.setCellValueFactory(
				cellData -> cellData.getValue().doubleProperty(attribute)

		// TODO need to abstract here
		// new PropertyValueFactory<EditableCommodity, Double>(fieldName)
		);
		col.setCellFactory(cellFactory);
		col.setOnEditCommit(
				new EventHandler<TableColumn.CellEditEvent<EditableCommodity, Double>>() {
					@Override public void handle(TableColumn.CellEditEvent<EditableCommodity, Double> t) {
						((EditableCommodity) t.getTableView().getItems().get(
								t.getTablePosition().getRow())).set(attribute, t.getNewValue());
					}
				});
		return col;
	}

	public static TableColumn<EditableCommodity, String> makeStringColumn(EC_ATTRIBUTE attribute) {
		TableColumn<EditableCommodity, String> col = new TableColumn<EditableCommodity, String>(attribute.text);
		col.getStyleClass().add("table-column-right");

		Callback<TableColumn<EditableCommodity, String>, TableCell<EditableCommodity, String>> cellFactory = new Callback<TableColumn<EditableCommodity, String>, TableCell<EditableCommodity, String>>() {
			public TableCell<EditableCommodity, String> call(TableColumn<EditableCommodity, String> p) {
				return new EditableCommodityStringCell();
			}
		};
		col.setCellValueFactory(
				cellData -> cellData.getValue().stringProperty(attribute)

		// TODO need to abstract here
		// new PropertyValueFactory<EditableCommodity, String>(fieldName)
		);
		col.setCellFactory(cellFactory);
		col.setOnEditCommit(
				new EventHandler<TableColumn.CellEditEvent<EditableCommodity, String>>() {
					@Override public void handle(TableColumn.CellEditEvent<EditableCommodity, String> t) {
						((EditableCommodity) t.getTableView().getItems().get(
								t.getTablePosition().getRow())).set(attribute, t.getNewValue());
					}
				});
		return col;
	}

	private ObservableValue<Double> doubleProperty(EC_ATTRIBUTE attribute) {
		switch (attribute) {
		case TURNOVER:
			return turnoverTime.asObject();
		case UNIT_VALUE:
			return unitValue.asObject();
		case UNIT_PRICE:
			return unitValue.asObject();
		default:
			return new SimpleDoubleProperty(Double.NaN).asObject();
		}
	}

	private ObservableValue<String> stringProperty(EC_ATTRIBUTE attribute) {
		switch (attribute) {
		case NAME:
			return name;
		case FUNCTION:
			return function;
		case ORIGIN:
			return origin;
		default:
			return new SimpleStringProperty("");
		}

	}

	private static class EditableCommodityStringCell extends TableCell<EditableCommodity, String> {
		private TextField textField;

		public EditableCommodityStringCell() {
		}

		@Override public void startEdit() {
			super.startEdit();
			if (textField == null) {
				createTextField();
			}
			setGraphic(textField);
			setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			textField.selectAll();
		}

		@Override public void cancelEdit() {
			super.cancelEdit();
			setText(String.valueOf(getItem()));
			setContentDisplay(ContentDisplay.TEXT_ONLY);
		}

		@Override public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);
			if (empty) {
				setText(null);
				setGraphic(null);
			} else {
				if (isEditing()) {
					if (textField != null) {
						textField.setText(getString());
					}
					setGraphic(textField);
					setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				} else {
					setText(getString());
					setContentDisplay(ContentDisplay.TEXT_ONLY);
				}
			}
		}

		private void createTextField() {
			textField = new TextField(getString());
			textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			textField.setOnKeyPressed(new EventHandler<KeyEvent>() {

				@Override public void handle(KeyEvent t) {
					if (t.getCode() == KeyCode.ENTER) {
						commitEdit(textField.getText());
					} else if (t.getCode() == KeyCode.ESCAPE) {
						cancelEdit();
					}
				}
			});
		}

		private String getString() {
			return getItem() == null ? "" : getItem().toString();
		}
	}

	private static class EditableCommodityCell extends TableCell<EditableCommodity, Double> {
		private TextField textField;

		public EditableCommodityCell() {
		}

		@Override public void startEdit() {
			super.startEdit();
			if (textField == null) {
				createTextField();
			}
			setGraphic(textField);
			setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			textField.selectAll();
		}

		@Override public void cancelEdit() {
			super.cancelEdit();
			setText(String.valueOf(getItem()));
			setContentDisplay(ContentDisplay.TEXT_ONLY);
		}

		@Override public void updateItem(Double item, boolean empty) {
			super.updateItem(item, empty);
			if (empty) {
				setText(null);
				setGraphic(null);
			} else {
				if (isEditing()) {
					if (textField != null) {
						textField.setText(getString());
					}
					setGraphic(textField);
					setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				} else {
					setText(getString());
					setContentDisplay(ContentDisplay.TEXT_ONLY);
				}
			}
		}

		private void createTextField() {
			textField = new TextField(getString());
			textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			textField.setOnKeyPressed(new EventHandler<KeyEvent>() {

				@Override public void handle(KeyEvent t) {
					if (t.getCode() == KeyCode.ENTER) {
						commitEdit(Double.parseDouble(textField.getText()));
					} else if (t.getCode() == KeyCode.ESCAPE) {
						cancelEdit();
					}
				}
			});
		}

		private String getString() {
			return getItem() == null ? "" : getItem().toString();
		}
	}

	public double getUnitValue() {
		return unitValue.get();
	}

	public void setUnitValue(double v) {
		unitValue.set(v);
	}

	public double getUnitPrice() {
		return unitPrice.get();
	}

	public void setUnitPrice(double v) {
		unitPrice.set(v);
	}

	public void setUnitPrice(int v) {
		unitValue.set(v);
	}

	/**
	 * @return the turnoverTime property
	 */
	public double getTurnoverTime() {
		return turnoverTime.get();
	}

	/**
	 * @param turnoverTime
	 *            the turnoverTime to set
	 */
	public void setTurnoverTime(double turnoverTime) {
		this.turnoverTime.set(turnoverTime);
	}

	public String getName() {
		return name.get();
	}

	public void setName(String name) {
		this.name.set(name);
	}

	/**
	 * @return the origin
	 */
	public String getOrigin() {
		return origin.get();
	}

	/**
	 * @param origin
	 *            the origin to set
	 */
	public void setOrigin(String origin) {
		this.origin.set(origin);
	}

	/**
	 * @return the function
	 */
	public String getFunction() {
		return function.get();
	}

	/**
	 * @param function
	 *            the function to set
	 */
	public void setFunction(String function) {
		this.function.set(function);
	}

	public String toString() {
		StringBuilder sb=new StringBuilder();
		sb.append(name.get()).append("(").append(function.get()).append(",").append(origin.get()).append(")\n");
		return  sb.toString();
	}
}
