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

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.controller.Simulation;
import capitalism.editor.EditorControlBar;
import capitalism.editor.EditorLoader;
import capitalism.model.Industry;
import capitalism.model.Stock;
import capitalism.model.Commodity.FUNCTION;
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

public class EditableIndustry {
	private static final Logger logger = LogManager.getLogger("EditableIndustry");
	private StringProperty name;
	private StringProperty commodityName;
	private DoubleProperty output;
	private EditableStock sales;
	private EditableStock money;
	private HashMap<String, EditableStock> productiveStocks;

	public enum EI_ATTRIBUTE {
		NAME("Name"), COMMODITY_NAME("Product"), OUTPUT("Output"), SALES("Sales Inventory"), MONEY("Money"), PRODUCTIVE_STOCK("Input");
		protected String text;

		EI_ATTRIBUTE(String text) {
			this.text = text;
		}
	}

	public EditableIndustry() {
		name = new SimpleStringProperty();
		output = new SimpleDoubleProperty();
		commodityName = new SimpleStringProperty();
		productiveStocks = new HashMap<String, EditableStock>();
	}

	/**
	 * Create a populated EditableSocialClass entity and also create its salesStock, moneyStock and consumptionStock
	 * 
	 * @param name
	 *            the name of the industry
	 * @param commodityName
	 *            the name of the commodity this industry produces
	 * @param output
	 *            the output that sells this industry is currently producing (i.e. at the time of project initiation)
	 * @return the populated EditableIndustry
	 */
	public static EditableIndustry makeIndustry(String name, String commodityName, double output) {
		EditableIndustry industry = new EditableIndustry();
		industry.setName(name);
		industry.setOutput(output);
		industry.setCommodityName(commodityName);
		industry.money = new EditableStock("Money");
		industry.sales = new EditableStock(commodityName);
		for (EditableCommodity e : EditorLoader.getCommodityData()) {
			if (e.getFunction().equals(FUNCTION.PRODUCTIVE_INPUT.text())) {
				industry.productiveStocks.put(e.getName(), new EditableStock(e.getName()));
			}
		}
		return industry;
	}

	/**
	 * Create an observable list of EditableIndustries (normally for display in the Industries Table) from the
	 * project identified by the current projectID and timeStampID. NOTE this is not just a wrapper. It will
	 * be edited by the user and eventually stored back to a modified version of the persistent entities
	 * from which it was created.
	 * 
	 * @return an observableList of EditableIndustries identified by the current projectID and timeStampID
	 */

	public static ObservableList<EditableIndustry> editableIndustries() {
		ObservableList<EditableIndustry> result = FXCollections.observableArrayList();
		for (Industry c : Industry.all(Simulation.projectIDCurrent(), Simulation.timeStampIDCurrent())) {
			EditableIndustry oneRecord = new EditableIndustry();
			oneRecord.setName(c.name());
			oneRecord.setCommodityName(c.getCommodityName());
			oneRecord.setOutput(c.getOutput());
			oneRecord.sales = new EditableStock(c.getCommodityName());
			oneRecord.money = new EditableStock("Money");
			result.add(oneRecord);
		}
		return result;
	}

	public void setDouble(EI_ATTRIBUTE attribute, double d) {
		switch (attribute) {
		case OUTPUT:
			output.set(d);
			break;
		case SALES:
			sales.getActualQuantityProperty().set(d);
			break;
		case MONEY:
			money.getActualQuantityProperty().set(d);
			break;
		default:
		}
	}

	public double getDouble(EI_ATTRIBUTE attribute) {
		switch (attribute) {
		case OUTPUT:
			return getOutput();
		case SALES:
			return sales.getActualQuantity();
		case MONEY:
			return money.getActualQuantity();
		default:
			return Double.NaN;
		}
	}

	public String getString(EI_ATTRIBUTE attribute) {
		switch (attribute) {
		case NAME:
			return getName();
		default:
			return "";
		}
	}

	public void setString(EI_ATTRIBUTE attribute, String newValue) {
		switch (attribute) {
		case NAME:
			name.set(newValue);
			break;
		default:
			break;
		}
	}

	public void addProductiveStock(String commodityName) {
		EditableStock stock = new EditableStock(commodityName);
		logger.debug("Adding the editable productive Stock {} to the industry {}", commodityName, name.get());
		productiveStocks.put(commodityName, stock);
	}

	public static TableColumn<EditableIndustry, Double> makeStockColumn(String commodityName) {
		TableColumn<EditableIndustry, Double> col = new TableColumn<EditableIndustry, Double>(commodityName);
		col.getStyleClass().add("table-column-right");

		Callback<TableColumn<EditableIndustry, Double>, TableCell<EditableIndustry, Double>> cellFactory;
		cellFactory = new Callback<TableColumn<EditableIndustry, Double>, TableCell<EditableIndustry, Double>>() {
			public TableCell<EditableIndustry, Double> call(TableColumn<EditableIndustry, Double> p) {
				return new EditableIndustryCell();
			}
		};
		col.setCellValueFactory(cellData -> cellData.getValue().stockDoubleProperty(commodityName));
		col.setCellFactory(cellFactory);
		col.setOnEditCommit(
				new EventHandler<TableColumn.CellEditEvent<EditableIndustry, Double>>() {
					@Override public void handle(TableColumn.CellEditEvent<EditableIndustry, Double> t) {
						((EditableIndustry) t.getTableView().getItems().get(
								t.getTablePosition().getRow())).setStockDouble(commodityName, t.getNewValue());
					}
				});
		return col;
	}

	public static TableColumn<EditableIndustry, Double> makeDoubleColumn(EI_ATTRIBUTE attribute) {
		TableColumn<EditableIndustry, Double> col = new TableColumn<EditableIndustry, Double>(attribute.text);
		col.getStyleClass().add("table-column-right");
		Callback<TableColumn<EditableIndustry, Double>, TableCell<EditableIndustry, Double>> cellFactory;
		cellFactory = new Callback<TableColumn<EditableIndustry, Double>, TableCell<EditableIndustry, Double>>() {
			public TableCell<EditableIndustry, Double> call(TableColumn<EditableIndustry, Double> p) {
				return new EditableIndustryCell();
			}
		};
		col.setCellValueFactory(
				cellData -> cellData.getValue().doubleProperty(attribute));
		col.setCellFactory(cellFactory);
		col.setOnEditCommit(
				new EventHandler<TableColumn.CellEditEvent<EditableIndustry, Double>>() {
					@Override public void handle(TableColumn.CellEditEvent<EditableIndustry, Double> t) {
						((EditableIndustry) t.getTableView().getItems().get(
								t.getTablePosition().getRow())).setDouble(attribute, t.getNewValue());
					}
				});
		return col;
	}

	public static TableColumn<EditableIndustry, String> makeStringColumn(EI_ATTRIBUTE attribute) {
		TableColumn<EditableIndustry, String> col = new TableColumn<EditableIndustry, String>(attribute.text);
		col.getStyleClass().add("table-column-right");
		Callback<TableColumn<EditableIndustry, String>, TableCell<EditableIndustry, String>> cellFactory;
		cellFactory = new Callback<TableColumn<EditableIndustry, String>, TableCell<EditableIndustry, String>>() {
			public TableCell<EditableIndustry, String> call(TableColumn<EditableIndustry, String> p) {
				return new EditableIndustryStringCell();
			}
		};
		col.setCellValueFactory(cellData -> cellData.getValue().stringProperty(attribute));
		col.setCellFactory(cellFactory);
		col.setOnEditCommit(
				new EventHandler<TableColumn.CellEditEvent<EditableIndustry, String>>() {
					@Override public void handle(TableColumn.CellEditEvent<EditableIndustry, String> t) {
						((EditableIndustry) t.getTableView().getItems().get(
								t.getTablePosition().getRow())).setString(attribute, t.getNewValue());
					}
				});
		return col;
	}

	private ObservableValue<Double> doubleProperty(EI_ATTRIBUTE attribute) {
		switch (attribute) {
		case OUTPUT:
			return output.asObject();
		case SALES:
			return sales.getActualQuantityProperty().asObject();
		case MONEY:
			return money.getActualQuantityProperty().asObject();
		default:
			return new SimpleDoubleProperty(Double.NaN).asObject();
		}
	}

	private ObservableValue<String> stringProperty(EI_ATTRIBUTE attribute) {
		switch (attribute) {
		case NAME:
			return name;
		case COMMODITY_NAME:
			return commodityName;
		default:
			return new SimpleStringProperty("");
		}
	}

	private ObservableValue<Double> stockDoubleProperty(String commodityName) {
		EditableStock stock = productiveStocks.get(commodityName);
		if (stock==null) {
			logger.debug("The stock of {} in industry {} does not exist", commodityName, name.get());
			return new SimpleDoubleProperty(-99).asObject();
		}
		if (EditorControlBar.displayActuals()) {
			return stock.getActualQuantityProperty().asObject();
		} else {
			return stock.getDesiredQuantityProperty().asObject();
		}
	}

	private void setStockDouble(String commodityName, Double newValue) {
		EditableStock stock = productiveStocks.get(commodityName);
		if (EditorControlBar.displayActuals()) {
			stock.setActualQuantity(newValue);
		} else {
			stock.setDesiredQuantity(newValue);
		}
	}

	private static class EditableIndustryStringCell extends TableCell<EditableIndustry, String> {
		private TextField textField;

		public EditableIndustryStringCell() {
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

	private static class EditableIndustryCell extends TableCell<EditableIndustry, Double> {
		private TextField textField;

		public EditableIndustryCell() {
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
			return value;
		}
	}

	public void loadStocksFromSimulation() {
		Industry persistentIndustry = Industry.single(Simulation.projectIDCurrent(), Simulation.timeStampIDCurrent(), name.get());
		Stock moneyStock = persistentIndustry.moneyStock();

		// A simple dodge: for money and sales stocks, the desired and actual quantity are the same
		money.setActualQuantity(moneyStock.getQuantity());
		money.setDesiredQuantity(moneyStock.getQuantity());
		Stock salesStock = persistentIndustry.salesStock();
		sales.setActualQuantity(salesStock.getQuantity());
		sales.setDesiredQuantity(moneyStock.getQuantity());
		for (Stock productiveStock : persistentIndustry.productiveStocks()) {
			logger.debug("Loading the productiveStock called {} belonging to the industry called {}", productiveStock.name(), name.get());
			logger.debug("Actual quantity is {} and desired quantity is {}", productiveStock.getQuantity(), productiveStock.getProductionQuantity());
			EditableStock s = productiveStocks.get(productiveStock.name());
			s.setActualQuantity(productiveStock.getQuantity());
			s.setDesiredQuantity(productiveStock.getProductionQuantity());
			logger.debug("Actual quantity set to {} and desired quantity set to{}", s.getActualQuantity(), s.getDesiredQuantity());
		}
	}

	public static void loadAllStocksFromSimulation() {
		for (EditableIndustry industry : EditorLoader.getIndustryData()) {
			industry.loadStocksFromSimulation();
		}
	}

	public String getName() {
		return name.get();
	}

	public void setName(String name) {
		this.name.set(name);
	}

	/**
	 * @return the commodityName
	 */
	public String getCommodityName() {
		return commodityName.get();
	}

	/**
	 * @param commodityName
	 *            the commodityName to set
	 */
	public void setCommodityName(String commodityName) {
		this.commodityName.set(commodityName);
	}

	/**
	 * @return the output
	 */
	public Double getOutput() {
		return output.get();
	}

	/**
	 * @param output
	 *            the output to set
	 */
	public void setOutput(Double output) {
		this.output.set(output);
	}

	/**
	 * @return the productiveStocks
	 */
	public HashMap<String, EditableStock> getProductiveStocks() {
		return productiveStocks;
	}

	/**
	 * @param productiveStocks
	 *            the productiveStocks to set
	 */
	public void setProductiveStocks(HashMap<String, EditableStock> productiveStocks) {
		this.productiveStocks = productiveStocks;
	}

}
