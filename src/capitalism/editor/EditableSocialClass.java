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
package capitalism.editor;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import capitalism.controller.Simulation;
import capitalism.model.SocialClass;
import capitalism.model.Stock;
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

public class EditableSocialClass {
	private static final Logger logger = LogManager.getLogger("EditableSocialClass");
	
	private StringProperty name;
	private DoubleProperty participationRatio;
	private DoubleProperty revenue;
	private EditableStock money;
	private EditableStock sales;
	private HashMap<String, EditableStock> consumptionStocks;

	enum ESC_ATTRIBUTE {
		NAME("Class Name"), PR("Participation Ratio"), REVENUE("Revenue"), MONEY("Money"), SALES("Sales Inventory"),CONSUMPTION("Consumer Goods");
		protected String text;

		private ESC_ATTRIBUTE(String text) {
			this.text = text;
		}
	}

	public EditableSocialClass() {
		name = new SimpleStringProperty();
		revenue = new SimpleDoubleProperty();
		participationRatio = new SimpleDoubleProperty();
		money = new EditableStock();
		sales = new EditableStock();
		consumptionStocks = new HashMap<String, EditableStock>();

	}

	/**
	 * Create an observable list of EditableSocialClass entities (normally for display in the SocialClass Table) from the
	 * project identified by the current projectID and timeStamp.
	 * 
	 * @return an observableList of EditableSocialClass entities identified by the current projectID and timeStampID
	 */

	public static ObservableList<EditableSocialClass> editableSocialClasses() {
		ObservableList<EditableSocialClass> result = FXCollections.observableArrayList();
		for (SocialClass c : SocialClass.allCurrent()) {
			EditableSocialClass oneRecord = new EditableSocialClass();
			oneRecord.setName(c.name());
			oneRecord.setParticipationRatio(c.getparticipationRatio());
			oneRecord.setRevenue(c.getRevenue());
			result.add(oneRecord);
		}
		return result;
	}

	public void setDouble(ESC_ATTRIBUTE attribute, double d) {
		switch (attribute) {
		case PR:
			participationRatio.set(d);
			break;
		case REVENUE:
			revenue.set(d);
			break;
		case MONEY:
			money.getQuantityProperty().set(d);
			break;
		case SALES:
			sales.getQuantityProperty().set(d);
			break;
		default:
		}
	}

	public double getDouble(ESC_ATTRIBUTE attribute) {
		switch (attribute) {
		case PR:
			return participationRatio.get();
		case REVENUE:
			return participationRatio.get();
		case MONEY:
			return money.getQuantityProperty().get();
		case SALES:
			return sales.getQuantityProperty().get();
		default:
			return Double.NaN;
		}
	}

	public String getString(ESC_ATTRIBUTE attribute) {
		switch (attribute) {
		case NAME:
			return getName();
		default:
			return "";
		}
	}

	public void setString(ESC_ATTRIBUTE attribute, String newValue) {
		switch (attribute) {
		case NAME:
			name.set(newValue);
			break;
		default:
			break;
		}
	}

	public void addConsumptionStock(String commodityName) {
		EditableStock stock = new EditableStock();
		consumptionStocks.put(commodityName, stock);
	}
	
	public static TableColumn<EditableSocialClass, Double> makeStockColumn(String commodityName) {
		TableColumn<EditableSocialClass, Double> col = new TableColumn<EditableSocialClass, Double>(commodityName);

		Callback<TableColumn<EditableSocialClass, Double>, TableCell<EditableSocialClass, Double>> cellFactory;
		cellFactory = new Callback<TableColumn<EditableSocialClass, Double>, TableCell<EditableSocialClass, Double>>() {
			public TableCell<EditableSocialClass, Double> call(TableColumn<EditableSocialClass, Double> p) {
				return new EditableSocialClassCell();
			}
		};
		col.setCellValueFactory(cellData -> cellData.getValue().stockDoubleProperty(commodityName));
		col.setCellFactory(cellFactory);
		col.setOnEditCommit(
				new EventHandler<TableColumn.CellEditEvent<EditableSocialClass, Double>>() {
					@Override public void handle(TableColumn.CellEditEvent<EditableSocialClass, Double> t) {
						((EditableSocialClass) t.getTableView().getItems().get(
								t.getTablePosition().getRow())).setStockDouble(commodityName, t.getNewValue());
					}
				});
		return col;
	}

	public static TableColumn<EditableSocialClass, Double> makeDoubleColumn(ESC_ATTRIBUTE attribute) {
		TableColumn<EditableSocialClass, Double> col = new TableColumn<EditableSocialClass, Double>(attribute.text);
		Callback<TableColumn<EditableSocialClass, Double>, TableCell<EditableSocialClass, Double>> cellFactory;
		cellFactory = new Callback<TableColumn<EditableSocialClass, Double>, TableCell<EditableSocialClass, Double>>() {
			public TableCell<EditableSocialClass, Double> call(TableColumn<EditableSocialClass, Double> p) {
				return new EditableSocialClassCell();
			}
		};
		col.setCellValueFactory(
				cellData -> cellData.getValue().doubleProperty(attribute));
		col.setCellFactory(cellFactory);
		col.setOnEditCommit(
				new EventHandler<TableColumn.CellEditEvent<EditableSocialClass, Double>>() {
					@Override public void handle(TableColumn.CellEditEvent<EditableSocialClass, Double> t) {
						((EditableSocialClass) t.getTableView().getItems().get(
								t.getTablePosition().getRow())).setDouble(attribute, t.getNewValue());
					}
				});
		return col;
	}

	public static TableColumn<EditableSocialClass, String> makeStringColumn(ESC_ATTRIBUTE attribute) {
		TableColumn<EditableSocialClass, String> col = new TableColumn<EditableSocialClass, String>(attribute.text);
		Callback<TableColumn<EditableSocialClass, String>, TableCell<EditableSocialClass, String>> cellFactory = new Callback<TableColumn<EditableSocialClass, String>, TableCell<EditableSocialClass, String>>() {
			public TableCell<EditableSocialClass, String> call(TableColumn<EditableSocialClass, String> p) {
				return new EditableSocialClassStringCell();
			}
		};
		col.setCellValueFactory(
				cellData -> cellData.getValue().stringProperty(attribute));
		col.setCellFactory(cellFactory);
		col.setOnEditCommit(
				new EventHandler<TableColumn.CellEditEvent<EditableSocialClass, String>>() {
					@Override public void handle(TableColumn.CellEditEvent<EditableSocialClass, String> t) {
						((EditableSocialClass) t.getTableView().getItems().get(
								t.getTablePosition().getRow())).setString(attribute, t.getNewValue());
					}
				});
		return col;
	}

	private ObservableValue<Double> doubleProperty(ESC_ATTRIBUTE attribute) {
		switch (attribute) {
		case PR:
			return participationRatio.asObject();
		case REVENUE:
			return revenue.asObject();
		case SALES:
			return sales.getQuantityProperty().asObject();
		case MONEY:
			return money.getQuantityProperty().asObject();
		default:
			return new SimpleDoubleProperty(Double.NaN).asObject();
		}
	}

	private ObservableValue<String> stringProperty(ESC_ATTRIBUTE attribute) {
		switch (attribute) {
		case NAME:
			return name;
		default:
			return new SimpleStringProperty("");
		}
	}

	private ObservableValue<Double> stockDoubleProperty(String commodityName) {
		EditableStock stock = consumptionStocks.get(commodityName);
		return stock.getQuantityProperty().asObject();
	}

	private void setStockDouble(String commodityName, Double newValue) {
		EditableStock stock = consumptionStocks.get(commodityName);
		stock.setQuantity(newValue);
	}
	private static class EditableSocialClassStringCell extends TableCell<EditableSocialClass, String> {
		private TextField textField;

		public EditableSocialClassStringCell() {
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
			String value = getItem() == null ? "" : getItem().toString();
			return value;
		}
	}

	private static class EditableSocialClassCell extends TableCell<EditableSocialClass, Double> {
		private TextField textField;

		public EditableSocialClassCell() {
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
			String value = getItem() == null ? "" : getItem().toString();
			logger.debug("getString {}", value);
			return value;
		}
	}
	
	
	public void loadStocksFromSimulation() {
		SocialClass persistentSocialClass=SocialClass.single(Simulation.projectIDCurrent, Simulation.timeStampIDCurrent, name.get());
		Stock moneyStock = persistentSocialClass.moneyStock(); 
				//Stock.moneyByOwner(Simulation.timeStampIDCurrent, name.get()); 
		money.setQuantity(moneyStock.getQuantity());
		Stock salesStock = persistentSocialClass.salesStock();
		sales.setQuantity(salesStock.getQuantity());
		for (Stock consumptionStock:persistentSocialClass.consumptionStocks()) {
			EditableStock s=consumptionStocks.get(consumptionStock.name());
			s.setQuantity(consumptionStock.getQuantity());
		}
	}
	
	public static void loadAllStocksFromSimulation() {
		for (EditableSocialClass socialClass:Editor.getSocialClassData()) {
			socialClass.loadStocksFromSimulation();
		}
	}

	public String getName() {
		return name.get();
	}

	public void setName(String name) {
		this.name.set(name);
	}

	/**
	 * @return the participationRatio
	 */
	public double getParticipationRatio() {
		return participationRatio.get();
	}

	/**
	 * @param participationRatio
	 *            the participationRatio to set
	 */
	public void setParticipationRatio(double participationRatio) {
		this.participationRatio.set(participationRatio);
	}

	/**
	 * @return the revenue
	 */
	public Double getRevenue() {
		return revenue.get();
	}

	/**
	 * @param revenue
	 *            the revenue to set
	 */
	public void setRevenue(double revenue) {
		this.revenue.set(revenue);
	}
}
