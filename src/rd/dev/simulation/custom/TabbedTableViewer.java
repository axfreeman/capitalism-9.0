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

package rd.dev.simulation.custom;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableCell;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import rd.dev.simulation.Capitalism;
import rd.dev.simulation.datamanagement.DataManager;
import rd.dev.simulation.datamanagement.ObservableListProvider;
import rd.dev.simulation.model.Circuit;
import rd.dev.simulation.model.SocialClass;
import rd.dev.simulation.model.Stock;
import rd.dev.simulation.model.UseValue;
import rd.dev.simulation.view.CircuitTableCell;
import rd.dev.simulation.view.CircuitTableStockCell;
import rd.dev.simulation.view.SocialClassTableCell;
import rd.dev.simulation.view.StockTableCell;
import rd.dev.simulation.view.UseValueTableCell;
import rd.dev.simulation.view.ViewManager;

public class TabbedTableViewer extends VBox {
	static final Logger logger = LogManager.getLogger("TableViewer");
	private ObservableListProvider olProvider = Capitalism.olProvider;

	// selects whether to display quantities, values or prices, where appropriate

	public static Stock.ValueExpression displayAttribute = Stock.ValueExpression.PRICE;

	// Stock Tables

	@FXML private TableView<Stock> productiveStockTable;
	@FXML private TableColumn<Stock, String> productiveStockCircuitColumn;
	@FXML private TableColumn<Stock, String> productiveStockUseValueColumn;
	@FXML private TableColumn<Stock, String> productiveStockQuantityColumn;
	@FXML private TableColumn<Stock, String> productiveStockValueColumn;
	@FXML private TableColumn<Stock, String> productiveStockPriceColumn;
	@FXML private TableColumn<Stock, String> productiveStockCoefficientColumn;
	@FXML private TableColumn<Stock, String> productiveStockDemandColumn;

	@FXML private TableView<Stock> moneyStockTable;
	@FXML private TableColumn<Stock, String> moneyOwnerTypeColumn;
	@FXML private TableColumn<Stock, String> moneyStockOwnerColumn;
	@FXML private TableColumn<Stock, String> moneyStockQuantityColumn;
	@FXML private TableColumn<Stock, String> moneyStockValueColumn;
	@FXML private TableColumn<Stock, String> moneyStockPriceColumn;

	@FXML private TableView<Stock> salesStockTable;
	@FXML private TableColumn<Stock, String> salesStockOwnerTypeColumn;
	@FXML private TableColumn<Stock, String> salesStockOwnerColumn;
	@FXML private TableColumn<Stock, String> salesStockUseValueColumn;
	@FXML private TableColumn<Stock, String> salesStockQuantityColumn;
	@FXML private TableColumn<Stock, String> salesStockValueColumn;
	@FXML private TableColumn<Stock, String> salesStockPriceColumn;

	@FXML private TableView<Stock> consumptionStockTable;
	@FXML private TableColumn<Stock, String> consumptionStockSocialClassColumn;
	@FXML private TableColumn<Stock, String> consumptionStockQuantityColumn;
	@FXML private TableColumn<Stock, String> consumptionStockValueColumn;
	@FXML private TableColumn<Stock, String> consumptionStockPriceColumn;
	@FXML private TableColumn<Stock, String> consumptionStockDemandColumn;

	// The UseValues table

	@FXML protected TableView<UseValue> useValuesTable;
	@FXML private TableColumn<UseValue, String> useValueNameColumn;
	@FXML private TableColumn<UseValue, String> useValueTypeColumn;
	@FXML private TableColumn<UseValue, String> useValueCircuitTypeColumn;
	@FXML private TableColumn<UseValue, String> useValueUnitValueColumn;
	@FXML private TableColumn<UseValue, String> useValueUnitPriceColumn;
	@FXML private TableColumn<UseValue, String> useValueTurnoverTimeColumn;
	@FXML private TableColumn<UseValue, String> useValueTotalSupplyColumn;
	@FXML private TableColumn<UseValue, String> useValueTotalDemandColumn;
	@FXML private TableColumn<UseValue, String> useValueSurplusColumn;
	@FXML private TableColumn<UseValue, String> useValueTotalQuantityColumn;
	@FXML private TableColumn<UseValue, String> useValueTotalValueColumn;
	@FXML private TableColumn<UseValue, String> useValueTotalPriceColumn;
	@FXML private TableColumn<UseValue, String> useValueAllocationShareColumn;
	@FXML private TableColumn<UseValue, String> useValueCapitalColumn;
	@FXML private TableColumn<UseValue, String> useValueSurplusValueColumn;

	// the Circuit Table and associated columns

	@FXML private TableView<Circuit> circuitsTable;
	@FXML private TableColumn<Circuit, String> circuitUseValueTypeColumn;
	@FXML private TableColumn<Circuit, String> circuitInitialCapitalColumn;
	@FXML private TableColumn<Circuit, String> circuitSalesColumn;
	@FXML private TableColumn<Circuit, String> circuitMoneyColumn;
	@FXML private TableColumn<Circuit, String> circuitProductiveColumn;
	@FXML private TableColumn<Circuit, String> circuitCurrentCapitalColumn;
	@FXML private TableColumn<Circuit, String> circuitProfitColumn;
	@FXML private TableColumn<Circuit, String> circuitProfitRateColumn;

	// the Dynamic Circuit tableView and data table

	@FXML private TableView<Circuit> dynamicCircuitTable;

	// The social classes table and associated columns

	@FXML private TableView<SocialClass> socialClassesTable;
	@FXML private TableColumn<SocialClass, String> socialClassNameColumn;
	@FXML private TableColumn<SocialClass, String> socialClassDescriptionColumn;
	@FXML private TableColumn<SocialClass, String> socialClassSizeColumn;
	@FXML private TableColumn<SocialClass, String> socialClassSalesStockColumn;
	@FXML private TableColumn<SocialClass, String> socialClassMoneyColumn;
	@FXML private TableColumn<SocialClass, String> socialClassConsumptionGoodsColumn;
	@FXML private TableColumn<SocialClass, String> socialClassTotalColumn;
	@FXML private TableColumn<SocialClass, String> socialClassQuantityDemandedColumn;
	@FXML private TableColumn<SocialClass, String> socialClassRevenueColumn;

	/**
	 * a simple static list of all the tables, so utilities can get at them
	 */

	private ArrayList<TableView<?>> tabbedTables = new ArrayList<TableView<?>>();

	/**
	 * Custom control handles the main ViewTables
	 */
	public TabbedTableViewer() {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("TabbedTableViewer.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		setTooltips();
		ViewManager.graphicsState = ContentDisplay.TEXT_ONLY;		// initialize so start state is text only
		setDisplayAttribute(Stock.ValueExpression.PRICE);			// start off displaying prices

		populateProductiveStocksViewTable();
		populateMoneyStocksViewTable();
		populateSalesStocksViewTable();
		populateConsumptionStocksViewTable();
		populateSocialClassesViewTable();
		populateUseValuesViewTable();
		populateCircuitsViewTable();

		createDynamicCircuitsTable();		// Dynamic table columns are defined from the data so it is constructed programmatically

		tabbedTables.add(productiveStockTable);
		tabbedTables.add(moneyStockTable);
		tabbedTables.add(salesStockTable);
		tabbedTables.add(salesStockTable);
		tabbedTables.add(consumptionStockTable);
		tabbedTables.add(useValuesTable);
		tabbedTables.add(circuitsTable);
		tabbedTables.add(socialClassesTable);
		tabbedTables.add(dynamicCircuitTable);

		TableUtilities.doctorColumnHeaders(tabbedTables);  // doctor all the tables so the graphics can be switched
	}

	public void setTooltips() {
		TableUtilities.setTip(useValuesTable, "A commodity is anything that that society makes use of, and has established a quantitative measure for");
		TableUtilities.setTip(circuitsTable, "A producer is a business, or group of businesses, who make one commodity with similar technologies. \n"
				+ "Two producers can make the same commodity, but would normally be distinguished apart because their technology differs."
				+ "\nThis can help study the effect of technological change");
		TableUtilities.setTip(socialClassesTable,
				"A social class is a group of people with the same source of revenue, defined by the type of property that they specialise in");
	}

	private void makeStockColumn(TableColumn<Stock, String> column, Stock.Selector selector) {
		column.setCellFactory(new Callback<TableColumn<Stock, String>, TableCell<Stock, String>>() {
			@Override public TableCell<Stock, String> call(TableColumn<Stock, String> col) {
				return new StockTableCell(selector);
			}
		});
		column.setCellValueFactory(cellData -> cellData.getValue().wrappedString(selector));
	}

	/**
	 * Initialize the Productive Stocks tableView and cellFactories
	 */
	public void populateProductiveStocksViewTable() {
		makeStockColumn(productiveStockCircuitColumn, Stock.Selector.CIRCUIT);
		makeStockColumn(productiveStockUseValueColumn, Stock.Selector.USEVALUE);
		makeStockColumn(productiveStockQuantityColumn, Stock.Selector.QUANTITY);
		makeStockColumn(productiveStockValueColumn, Stock.Selector.VALUE);
		makeStockColumn(productiveStockPriceColumn, Stock.Selector.PRICE);
		makeStockColumn(productiveStockCoefficientColumn, Stock.Selector.COEFFICIENT);
		makeStockColumn(productiveStockDemandColumn, Stock.Selector.QUANTITYDEMANDED);
	}

	/**
	 * Initialize the Money Stocks tableView and cellFactories
	 */
	public void populateMoneyStocksViewTable() {
		makeStockColumn(moneyOwnerTypeColumn, Stock.Selector.OWNERTYPE);
		makeStockColumn(moneyStockOwnerColumn, Stock.Selector.CIRCUIT);
		makeStockColumn(moneyStockQuantityColumn, Stock.Selector.QUANTITY);
		makeStockColumn(moneyStockValueColumn, Stock.Selector.VALUE);
		makeStockColumn(moneyStockPriceColumn, Stock.Selector.PRICE);
	}

	/**
	 * Initialize the Sales Stocks tableView and cellFactories
	 */
	public void populateSalesStocksViewTable() {
		makeStockColumn(salesStockOwnerTypeColumn, Stock.Selector.OWNERTYPE);
		makeStockColumn(salesStockOwnerColumn, Stock.Selector.CIRCUIT);
		makeStockColumn(salesStockUseValueColumn, Stock.Selector.USEVALUE);
		makeStockColumn(salesStockQuantityColumn, Stock.Selector.QUANTITY);
		makeStockColumn(salesStockValueColumn, Stock.Selector.VALUE);
		makeStockColumn(salesStockPriceColumn, Stock.Selector.PRICE);
	}

	/**
	 * Initialize the Consumption Stocks tableView and cellFactories
	 */
	public void populateConsumptionStocksViewTable() {
		makeStockColumn(consumptionStockSocialClassColumn, Stock.Selector.CIRCUIT);
		makeStockColumn(consumptionStockQuantityColumn, Stock.Selector.QUANTITY);
		makeStockColumn(consumptionStockValueColumn, Stock.Selector.VALUE);
		makeStockColumn(consumptionStockPriceColumn, Stock.Selector.PRICE);
		makeStockColumn(consumptionStockDemandColumn, Stock.Selector.QUANTITYDEMANDED);
	}

	private void makeUseValueColumn(TableColumn<UseValue, String> column, UseValue.Selector selector) {
		column.setCellFactory(new Callback<TableColumn<UseValue, String>, TableCell<UseValue, String>>() {
			@Override public TableCell<UseValue, String> call(TableColumn<UseValue, String> col) {
				return new UseValueTableCell(selector);
			}
		});
		column.setCellValueFactory(cellData -> cellData.getValue().wrappedString(selector));
	}

	/**
	 * Initialize the UseValues tableView and cellFactories
	 */
	public void populateUseValuesViewTable() {
		makeUseValueColumn(useValueNameColumn, UseValue.Selector.USEVALUENAME);
		makeUseValueColumn(useValueTypeColumn, UseValue.Selector.USEVALUETYPE);
		makeUseValueColumn(useValueTotalValueColumn, UseValue.Selector.TOTALVALUE);
		makeUseValueColumn(useValueTotalPriceColumn, UseValue.Selector.TOTALPRICE);
		makeUseValueColumn(useValueUnitValueColumn, UseValue.Selector.UNITVALUE);
		makeUseValueColumn(useValueUnitPriceColumn, UseValue.Selector.UNITPRICE);
		makeUseValueColumn(useValueTurnoverTimeColumn, UseValue.Selector.TURNOVERTIME);
		makeUseValueColumn(useValueTotalSupplyColumn, UseValue.Selector.TOTALSUPPLY);
		makeUseValueColumn(useValueTotalQuantityColumn, UseValue.Selector.TOTALQUANTITY);
		makeUseValueColumn(useValueTotalDemandColumn, UseValue.Selector.TOTALDEMAND);
		makeUseValueColumn(useValueSurplusColumn, UseValue.Selector.SURPLUS);
		makeUseValueColumn(useValueAllocationShareColumn, UseValue.Selector.ALLOCATIONSHARE);
		makeUseValueColumn(useValueCapitalColumn, UseValue.Selector.CAPITAL);
		makeUseValueColumn(useValueSurplusValueColumn, UseValue.Selector.SURPLUSVALUE);
		useValuesTable.setItems(olProvider.useValuesObservable());
	}

	private void makeCircuitColumn(TableColumn<Circuit, String> column, Circuit.Selector selector) {
		column.setCellFactory(new Callback<TableColumn<Circuit, String>, TableCell<Circuit, String>>() {
			@Override public TableCell<Circuit, String> call(TableColumn<Circuit, String> col) {
				return new CircuitTableCell(selector);
			}
		});
		column.setCellValueFactory(cellData -> cellData.getValue().wrappedString(selector, TabbedTableViewer.displayAttribute));
	}

	/**
	 * Initialize the Circuits tableView and cellFactories
	 */
	public void populateCircuitsViewTable() {
		makeCircuitColumn(circuitUseValueTypeColumn, Circuit.Selector.PRODUCTUSEVALUETYPE);
		makeCircuitColumn(circuitInitialCapitalColumn, Circuit.Selector.INITIALCAPITAL);
		makeCircuitColumn(circuitSalesColumn, Circuit.Selector.SALESSTOCK);
		makeCircuitColumn(circuitProductiveColumn, Circuit.Selector.PRODUCTIVESTOCKS);
		makeCircuitColumn(circuitMoneyColumn, Circuit.Selector.MONEYSTOCK);
		makeCircuitColumn(circuitCurrentCapitalColumn, Circuit.Selector.CURRENTCAPITAL);
		makeCircuitColumn(circuitProfitColumn, Circuit.Selector.PROFIT);
		makeCircuitColumn(circuitProfitRateColumn, Circuit.Selector.RATEOFPROFIT);
	}

	private void makeSocialClassColumn(TableColumn<SocialClass, String> column, SocialClass.Selector selector) {
		column.setCellFactory(new Callback<TableColumn<SocialClass, String>, TableCell<SocialClass, String>>() {
			@Override public TableCell<SocialClass, String> call(TableColumn<SocialClass, String> col) {
				return new SocialClassTableCell(selector);
			}
		});
		column.setCellValueFactory(cellData -> cellData.getValue().wrappedString(selector, TabbedTableViewer.displayAttribute));
	}

	/**
	 * Initialize the Social Classes tableView and cellFactories
	 */
	public void populateSocialClassesViewTable() {
		makeSocialClassColumn(socialClassNameColumn, SocialClass.Selector.SOCIALCLASSNAME);
		makeSocialClassColumn(socialClassSalesStockColumn, SocialClass.Selector.SALES);
		makeSocialClassColumn(socialClassConsumptionGoodsColumn, SocialClass.Selector.CONSUMPTIONSTOCKS);
		makeSocialClassColumn(socialClassTotalColumn, SocialClass.Selector.TOTAL);
		makeSocialClassColumn(socialClassMoneyColumn, SocialClass.Selector.MONEY);
		makeSocialClassColumn(socialClassQuantityDemandedColumn, SocialClass.Selector.QUANTITYDEMANDED);
		makeSocialClassColumn(socialClassRevenueColumn, SocialClass.Selector.SPENDING);
		makeSocialClassColumn(socialClassSizeColumn, SocialClass.Selector.SIZE);
	}

	private void addDynamicCircuitColumn(String columnName, Circuit.Selector selector, String imageURL) {
		TableColumn<Circuit, String> newColumn = new TableColumn<Circuit, String>(columnName);
		newColumn.setCellFactory(new Callback<TableColumn<Circuit, String>, TableCell<Circuit, String>>() {
			@Override public TableCell<Circuit, String> call(TableColumn<Circuit, String> col) {
				return new CircuitTableCell(selector);
			}
		});
		newColumn.setCellValueFactory(cellData -> cellData.getValue().wrappedString(selector, TabbedTableViewer.displayAttribute));
		newColumn.getStyleClass().add("table-column-right");
		TableUtilities.addGraphicToColummnHeader(newColumn, imageURL);
		dynamicCircuitTable.getColumns().add(newColumn);
	}

	private void addDynamicCircuitProductiveStockColumn(String productiveStockName) {
		TableColumn<Circuit, String> newColumn = new TableColumn<Circuit, String>(productiveStockName);// Use the stock name as a column heading
		newColumn.setCellFactory(new Callback<TableColumn<Circuit, String>, TableCell<Circuit, String>>() {
			@Override public TableCell<Circuit, String> call(TableColumn<Circuit, String> col) {
				return new CircuitTableStockCell(productiveStockName);
			}
		});
		newColumn.setCellValueFactory(cellData -> cellData.getValue().wrappedString(productiveStockName));
		newColumn.getStyleClass().add("table-column-right");

		// a bit of a botch here to put images in place for externally-supplied names
		// TODO allow the user to supply the image

		if (productiveStockName.equals("Labour Power")) {
			TableUtilities.addGraphicToColummnHeader(newColumn, "labourPower.png");
		}
		if (productiveStockName.equals("Consumption")) {
			TableUtilities.addGraphicToColummnHeader(newColumn, "necessities.png");
		}
		if (productiveStockName.equals("Means of Production")) {
			TableUtilities.addGraphicToColummnHeader(newColumn, "Means of Production.png");
		}
		dynamicCircuitTable.getColumns().add(newColumn);
	}

	private void createDynamicCircuitsTable() {
		addDynamicCircuitColumn("Producer", Circuit.Selector.PRODUCTUSEVALUETYPE, null);
		addDynamicCircuitColumn("Desired Output", Circuit.Selector.PROPOSEDOUTPUT, "maximum output.png");
		addDynamicCircuitColumn("Constrained Output", Circuit.Selector.CONSTRAINEDOUTPUT, "constrained output.png");
		addDynamicCircuitColumn("GrowthRate", Circuit.Selector.GROWTHRATE, "growthrate.png");
		for (Stock s : DataManager.productiveStocks()) {
			addDynamicCircuitProductiveStockColumn(s.getUseValueName());
		}
	}

	/**
	 * refresh all the tabbed tables
	 * 
	 */

	public void rePopulateTabbedTables() {
		productiveStockTable.setItems(olProvider.stocksByStockTypeObservable("Productive"));
		moneyStockTable.setItems(olProvider.stocksByStockTypeObservable("Money"));
		salesStockTable.setItems(olProvider.stocksByStockTypeObservable("Sales"));
		consumptionStockTable.setItems(olProvider.stocksByStockTypeObservable("Consumption"));
		useValuesTable.setItems(olProvider.useValuesObservable());
		circuitsTable.setItems(olProvider.circuitsObservable());
		socialClassesTable.setItems(olProvider.socialClassesObservable());
		dynamicCircuitTable.setItems(olProvider.circuitsObservable());
	}

	/**
	 * switch the header displays (between graphics and text) for all our tables
	 */

	public void switchHeaderDisplays() {
		TableUtilities.switchHeaderDisplays(tabbedTables);
	}

	/**
	 * @param displayAttribute
	 *            the displayAttribute to set
	 */
	public void setDisplayAttribute(Stock.ValueExpression displayAttribute) {
		TabbedTableViewer.displayAttribute = displayAttribute;
	}

	/**
	 * @return an array of all the tables, so the utilities and other providers can service the objects in them
	 */
	public ArrayList<TableView<?>> getTabbedTables() {
		return tabbedTables;
	}

}