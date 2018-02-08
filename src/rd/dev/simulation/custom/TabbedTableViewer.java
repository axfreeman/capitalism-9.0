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
import rd.dev.simulation.Simulation;
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
	@FXML private TableColumn<UseValue, String> useValueProfitColumn;
	@FXML private TableColumn<UseValue, String> useValueProfitRateColumn;
	
	@FXML private TableColumn<UseValue,String> useValueDemandSupplySuperColumn;
	@FXML private TableColumn<UseValue,String> useValueCapitalProfitSuperColumn;
	@FXML private TableColumn<UseValue,String> useValueValuePriceSuperColumn;

	@FXML private TableView<Circuit> circuitsTable;
	@FXML private TableView<Circuit> dynamicCircuitTable;
	@FXML private TableView<SocialClass> socialClassesTable;

	/**
	 * a simple static list of all the tables, so utilities can get at them
	 */

	private static ArrayList<TableView<?>> tabbedTables = new ArrayList<TableView<?>>();
	
	private static ArrayList<TableView<?>> dynamicTables = new ArrayList<TableView<?>>();
	

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
		buildTables();

	}

	/** 
	 * completely reconstruct all the tables from scratch
	 * Called at startup, and when switching a project (because the dynamic columns may change)
	 */
	
	public void buildTables() {
		makeProductiveStocksViewTable();
		makeMoneyStocksViewTable();
		makeSalesStocksViewTable();
		makeConsumptionStocksViewTable();
		makeUseValuesViewTable();
		makeCircuitsViewTable();
		makeDynamicCircuitsTable();	
		makeSocialClassesViewTable();
		tabbedTables.clear();
		tabbedTables.add(productiveStockTable);
		tabbedTables.add(moneyStockTable);
		tabbedTables.add(salesStockTable);
		tabbedTables.add(salesStockTable);
		tabbedTables.add(consumptionStockTable);
		tabbedTables.add(useValuesTable);
		tabbedTables.add(circuitsTable);
		tabbedTables.add(socialClassesTable);
		tabbedTables.add(dynamicCircuitTable);
		dynamicTables.add(dynamicCircuitTable);
		dynamicTables.add(socialClassesTable);
		TableUtilities.doctorColumnHeaders(tabbedTables);  // doctor all the tables so the graphics can be switched
		TableUtilities.setSuperColumnHandler(useValueValuePriceSuperColumn,useValueTotalPriceColumn);
		TableUtilities.setSuperColumnHandler(useValueDemandSupplySuperColumn,useValueAllocationShareColumn);
		TableUtilities.setSuperColumnHandler(useValueCapitalProfitSuperColumn,useValueProfitRateColumn);
	}
	
	/** 
	 * rebuild the tables with variable numbers of columns (for example when switching projects)
	 */
	public void reBuildDynamicTables() {
		dynamicCircuitTable.getColumns().clear();
		socialClassesTable.getColumns().clear();
		makeDynamicCircuitsTable();	
		makeSocialClassesViewTable();
		TableUtilities.doctorColumnHeaders(dynamicTables);
	}
	
	public void setTooltips() {
		TableUtilities.setTip(useValuesTable, "A commodity is anything that that society makes use of, and has established a quantitative measure for");
		TableUtilities.setTip(circuitsTable, "A producer is a business, or group of businesses, who make one commodity with similar technologies. \n"
				+ "Two producers can make the same commodity, but would normally be distinguished apart because their technology differs."
				+ "\nThis can help study the effect of technological change");
		TableUtilities.setTip(socialClassesTable,
				"A social class is a group of people with the same source of revenue, defined by the type of property that they specialise in");
	}

	private void addStockColumn(TableColumn<Stock, String> column, Stock.Selector selector) {
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
	public void makeProductiveStocksViewTable() {
		addStockColumn(productiveStockCircuitColumn, Stock.Selector.CIRCUIT);
		addStockColumn(productiveStockUseValueColumn, Stock.Selector.USEVALUE);
		addStockColumn(productiveStockQuantityColumn, Stock.Selector.QUANTITY);
		addStockColumn(productiveStockValueColumn, Stock.Selector.VALUE);
		addStockColumn(productiveStockPriceColumn, Stock.Selector.PRICE);
		addStockColumn(productiveStockCoefficientColumn, Stock.Selector.COEFFICIENT);
		addStockColumn(productiveStockDemandColumn, Stock.Selector.QUANTITYDEMANDED);
	}

	/**
	 * Initialize the Money Stocks tableView and cellFactories
	 */
	public void makeMoneyStocksViewTable() {
		addStockColumn(moneyOwnerTypeColumn, Stock.Selector.OWNERTYPE);
		addStockColumn(moneyStockOwnerColumn, Stock.Selector.CIRCUIT);
		addStockColumn(moneyStockQuantityColumn, Stock.Selector.QUANTITY);
		addStockColumn(moneyStockValueColumn, Stock.Selector.VALUE);
		addStockColumn(moneyStockPriceColumn, Stock.Selector.PRICE);
	}

	/**
	 * Initialize the Sales Stocks tableView and cellFactories
	 */
	public void makeSalesStocksViewTable() {
		addStockColumn(salesStockOwnerTypeColumn, Stock.Selector.OWNERTYPE);
		addStockColumn(salesStockOwnerColumn, Stock.Selector.CIRCUIT);
		addStockColumn(salesStockUseValueColumn, Stock.Selector.USEVALUE);
		addStockColumn(salesStockQuantityColumn, Stock.Selector.QUANTITY);
		addStockColumn(salesStockValueColumn, Stock.Selector.VALUE);
		addStockColumn(salesStockPriceColumn, Stock.Selector.PRICE);
	}

	/**
	 * Initialize the Consumption Stocks tableView and cellFactories
	 */
	public void makeConsumptionStocksViewTable() {
		addStockColumn(consumptionStockSocialClassColumn, Stock.Selector.CIRCUIT);
		addStockColumn(consumptionStockQuantityColumn, Stock.Selector.QUANTITY);
		addStockColumn(consumptionStockValueColumn, Stock.Selector.VALUE);
		addStockColumn(consumptionStockPriceColumn, Stock.Selector.PRICE);
		addStockColumn(consumptionStockDemandColumn, Stock.Selector.QUANTITYDEMANDED);
	}

	private void addUseValueColumn(TableColumn<UseValue, String> column, UseValue.USEVALUE_SELECTOR useValueSelector) {
		column.setCellFactory(new Callback<TableColumn<UseValue, String>, TableCell<UseValue, String>>() {
			@Override public TableCell<UseValue, String> call(TableColumn<UseValue, String> col) {
				return new UseValueTableCell(useValueSelector);
			}
		});
		column.setCellValueFactory(cellData -> cellData.getValue().wrappedString(useValueSelector));
	}

	/**
	 * Initialize the UseValues tableView and cellFactories
	 */
	public void makeUseValuesViewTable() {
		addUseValueColumn(useValueNameColumn, UseValue.USEVALUE_SELECTOR.USEVALUENAME);
		addUseValueColumn(useValueTypeColumn, UseValue.USEVALUE_SELECTOR.USEVALUETYPE);
		addUseValueColumn(useValueTotalValueColumn, UseValue.USEVALUE_SELECTOR.TOTALVALUE);
		addUseValueColumn(useValueTotalPriceColumn, UseValue.USEVALUE_SELECTOR.TOTALPRICE);
		addUseValueColumn(useValueUnitValueColumn, UseValue.USEVALUE_SELECTOR.UNITVALUE);
		addUseValueColumn(useValueUnitPriceColumn, UseValue.USEVALUE_SELECTOR.UNITPRICE);
		addUseValueColumn(useValueTurnoverTimeColumn, UseValue.USEVALUE_SELECTOR.TURNOVERTIME);
		addUseValueColumn(useValueTotalSupplyColumn, UseValue.USEVALUE_SELECTOR.TOTALSUPPLY);
		addUseValueColumn(useValueTotalQuantityColumn, UseValue.USEVALUE_SELECTOR.TOTALQUANTITY);
		addUseValueColumn(useValueTotalDemandColumn, UseValue.USEVALUE_SELECTOR.TOTALDEMAND);
		addUseValueColumn(useValueSurplusColumn, UseValue.USEVALUE_SELECTOR.SURPLUS);
		addUseValueColumn(useValueAllocationShareColumn, UseValue.USEVALUE_SELECTOR.ALLOCATIONSHARE);
		addUseValueColumn(useValueCapitalColumn, UseValue.USEVALUE_SELECTOR.INITIALCAPITAL);
		addUseValueColumn(useValueProfitColumn, UseValue.USEVALUE_SELECTOR.PROFIT);
		addUseValueColumn(useValueProfitRateColumn, UseValue.USEVALUE_SELECTOR.PROFITRATE);
	}

	/**
	 * Initialize the Social Classes tableView and cellFactories
	 */
	public void makeSocialClassesViewTable() {
		
		socialClassesTable.getColumns().add(new SocialClassColumn(SocialClass.Selector.SOCIALCLASSNAME));
		socialClassesTable.getColumns().add(new SocialClassColumn(SocialClass.Selector.SIZE));
		socialClassesTable.getColumns().add(new SocialClassColumn(SocialClass.Selector.SALES));
		socialClassesTable.getColumns().add(new SocialClassColumn(SocialClass.Selector.MONEY));
		socialClassesTable.getColumns().add(new SocialClassColumn(SocialClass.Selector.TOTAL));
		socialClassesTable.getColumns().add(new SocialClassColumn(SocialClass.Selector.REVENUE));
		for (UseValue u:DataManager.useValuesByType(UseValue.USEVALUETYPE.CONSUMPTION)) {
			socialClassesTable.getColumns().add(new SocialClassColumn(u.getUseValueName()));		}
	}

	/**
	 * Build the Circuits tableView and cellFactories.
	 * Only call this when we want to rebuild the display from scratch, for example when starting up or switching projects
	 */
	
	public void makeCircuitsViewTable() {
		circuitsTable.getColumns().add(new CircuitColumn(Circuit.Selector.PRODUCTUSEVALUENAME));
		circuitsTable.getColumns().add(new CircuitColumn(Circuit.Selector.INITIALCAPITAL));
		circuitsTable.getColumns().add(new CircuitColumn(Circuit.Selector.SALESSTOCK));
		circuitsTable.getColumns().add(new CircuitColumn(Circuit.Selector.PRODUCTIVESTOCKS));
		circuitsTable.getColumns().add(new CircuitColumn(Circuit.Selector.MONEYSTOCK));
		circuitsTable.getColumns().add(new CircuitColumn(Circuit.Selector.CURRENTCAPITAL));
		circuitsTable.getColumns().add(new CircuitColumn(Circuit.Selector.PROFIT));
		circuitsTable.getColumns().add(new CircuitColumn(Circuit.Selector.PROFITRATE));
	}

	
	/**
	 * Build the DynamicCircuitTable and cellFactories.
	 * Only call this when we want to rebuild the display from scratch, for example when starting up or switching projects
	 */
	
	private void makeDynamicCircuitsTable() {
		dynamicCircuitTable.getColumns().add(new CircuitColumn(Circuit.Selector.PRODUCTUSEVALUENAME));
		dynamicCircuitTable.getColumns().add(new CircuitColumn(Circuit.Selector.CONSTRAINEDOUTPUT));
		dynamicCircuitTable.getColumns().add(new CircuitColumn(Circuit.Selector.PROPOSEDOUTPUT));
		dynamicCircuitTable.getColumns().add(new CircuitColumn(Circuit.Selector.GROWTHRATE));

		for (UseValue u : DataManager.useValuesByType(UseValue.USEVALUETYPE.PRODUCTIVE)) {
			dynamicCircuitTable.getColumns().add(new CircuitColumn(u.getUseValueName()));
		}
		for (UseValue u : DataManager.useValuesByType(UseValue.USEVALUETYPE.LABOURPOWER)) {
			dynamicCircuitTable.getColumns().add(new CircuitColumn(u.getUseValueName()));
		}
	}

	/**
	 * refresh the data in all the tabbed tables. Do not rebuild them.
	 * 
	 */

	public void repopulateTabbedTables() {
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
	 * we have to force a refresh of the display because if the data has not changed, it may not be observed by the table
	 * see https://stackoverflow.com/questions/11065140/javafx-2-1-tableview-refresh-items
	 * i have kept this method separate from {@code populateTabbedTables()} because the issue merits further study.
	 */

	public void refreshTables() {
		for (TableView<?>table:tabbedTables) {
			table.refresh();
		}
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

	/**
	 * @return the useValuesTable
	 */
	public TableView<UseValue> getUseValuesTable() {
		return useValuesTable;
	}
}