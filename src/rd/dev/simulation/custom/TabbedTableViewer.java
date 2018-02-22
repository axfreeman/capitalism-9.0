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
import javafx.scene.layout.VBox;
import rd.dev.simulation.model.Industry;
import rd.dev.simulation.model.SocialClass;
import rd.dev.simulation.model.Stock;
import rd.dev.simulation.model.Commodity;
import rd.dev.simulation.view.ViewManager;

public class TabbedTableViewer extends VBox {
	static final Logger logger = LogManager.getLogger("TableViewer");

	// selects whether to display quantities, values or prices, where appropriate

	public static Stock.ValueExpression displayAttribute = Stock.ValueExpression.PRICE;

	// Stock Tables and header coulumns

	@FXML private TableView<Stock> productiveStockTable;
	@FXML private TableColumn<Stock, String> productiveStockHeaderColumn;
	@FXML private TableView<Stock> moneyStockTable;
	@FXML private TableColumn<Stock, String> moneyStockHeaderColumn;
	@FXML private TableView<Stock> salesStockTable;
	@FXML private TableColumn<Stock, String> salesStockHeaderColumn;
	@FXML private TableView<Stock> consumptionStockTable;
	@FXML private TableColumn<Stock, String> consumptionStockHeaderColumn;

	// The Commodities table and its header columns

	@FXML protected TableView<Commodity> commoditiesTable;
	private TableColumn<Commodity, String> commodityDemandSupplySuperColumn;
	private TableColumn<Commodity, String> commodityCapitalProfitSuperColumn;
	private TableColumn<Commodity, String> commodityValuePriceSuperColumn;
	private TableColumn<Commodity, String> commodityBasicsSuperColumn;

	private TableColumn<Commodity, String> commodityTotalPriceColumn;
	private TableColumn<Commodity, String> commodityAllocationShareColumn;
	private TableColumn<Commodity, String> commodityProfitRateColumn;
	private TableColumn<Commodity, String> commodityQuantityColumn;
	private TableColumn<Commodity, String> commodityNameColumn;

	// Industry Tables and their header columns

	@FXML private TableView<Industry> industriesTable;
	@FXML private TableView<Industry> dynamicIndustryTable;
	@FXML private TableView<SocialClass> socialClassesTable;

	/**
	 * Simple static lists of tables, so utilities can get at them
	 */

	private static ArrayList<TableView<?>> tabbedTables = new ArrayList<TableView<?>>();

	/**
	 * This enum contains most of the tooltips applied to table columns displayed in the viewer.
	 * It is a convenience rather than a hard and fast rule; its main purpose is to ensure that
	 * if two fields appear in different entity classes that describe the same thing, we can
	 * provide for them to get the same tooltip.
	 */

	public static enum HEADER_TOOL_TIPS {
		// @formatter:off
		COMMODITY("A commodity is anything that that society makes use of, and has established a quantitative measure for\n. "
				+ "In general it is anything produced to be sold. Some tradeable goods such as paper money are included\n "
				+ "in this list even though they are not produced."), 
		INDUSTRY("An industry is a business, or group of businesses, who make one commodity with similar technologies. \n" +
				 "Two producers can make the same commodity, but would normally be distinguished apart because their technology differs.\n" +
				 "This can help study the effect of technological change"), 
		SOCIALCLASS("A social class is a group of people with the same source of revenue, defined by the type of property that they specialise in"),
		OUTPUT("The current output of an industry is the quantity of its normal product \n"
				+ "that it produced in the last period. Its output in this period may be less \n"
				+ "if there are money or supply constraints, or more if there is fresh investment\n "
				+ "in the relevant industries"),
		INITIALCAPITAL("The initial capital, in money price terms, of a commodity or of the industries producing \n"
				+ "this commodity, at the start of the period"),
		CAPITAL("Capital is defined as productive stocks plus sales stocks, plus money stocks"),
		PROFIT("Profit is the difference, in money price terms, between the current capital\n"
				+ " and the initial capital of all the industries producing this commodity"),
		PROFITRATE("The profit rate is the Profit divided by the initial capital");
		// @formatter:on

		String text;

		HEADER_TOOL_TIPS(String text) {
			this.text = text;
		}

		public String text() {
			return text;
		}
	}

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
		makeCommoditiesViewTable();
		makeIndustriesViewTable();
		makeDynamicIndustriesTable();
		makeSocialClassesViewTable();

		tabbedTables.clear();
		tabbedTables.add(productiveStockTable);
		tabbedTables.add(moneyStockTable);
		tabbedTables.add(salesStockTable);
		tabbedTables.add(salesStockTable);
		tabbedTables.add(consumptionStockTable);
		tabbedTables.add(commoditiesTable);
		tabbedTables.add(industriesTable);
		tabbedTables.add(socialClassesTable);
		tabbedTables.add(dynamicIndustryTable);

		TableUtilities.setSuperColumnHandler(commodityValuePriceSuperColumn, commodityTotalPriceColumn);
		TableUtilities.setSuperColumnHandler(commodityDemandSupplySuperColumn, commodityAllocationShareColumn);
		TableUtilities.setSuperColumnHandler(commodityCapitalProfitSuperColumn, commodityProfitRateColumn);
		TableUtilities.setSuperColumnHandler(commodityBasicsSuperColumn, commodityQuantityColumn);
	}

	/**
	 * Initialize the Productive Stocks tableView and cellFactories
	 */
	public void makeProductiveStocksViewTable() {
		productiveStockHeaderColumn.getColumns().clear();
		productiveStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.OWNER, true));
		productiveStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.COMMODITY, true));
		productiveStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.QUANTITY, false));
		productiveStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.VALUE, false));
		productiveStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.PRICE, false));
		productiveStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.PRODUCTION_COEFFICIENT, false));
		productiveStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.REPLENISHMENTDEMAND, false));
		productiveStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.EXPANSIONDEMAND, false));
	}

	/**
	 * Initialize the Money Stocks tableView and cellFactories
	 */
	public void makeMoneyStocksViewTable() {
		moneyStockHeaderColumn.getColumns().clear();
		moneyStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.OWNERTYPE, true));
		moneyStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.OWNER, true));
		moneyStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.QUANTITY, false));
		moneyStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.VALUE, false));
		moneyStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.PRICE, false));
	}

	/**
	 * Initialize the Sales Stocks tableView and cellFactories
	 */
	public void makeSalesStocksViewTable() {
		salesStockHeaderColumn.getColumns().clear();
		salesStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.OWNERTYPE, true));
		salesStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.OWNER, true));
		salesStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.COMMODITY, false));
		salesStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.QUANTITY, false));
		salesStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.VALUE, false));
		salesStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.PRICE, false));
	}

	/**
	 * Initialize the Consumption Stocks tableView and cellFactories
	 */
	public void makeConsumptionStocksViewTable() {
		consumptionStockHeaderColumn.getColumns().clear();
		consumptionStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.OWNER, true));
		consumptionStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.COMMODITY, true));
		consumptionStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.QUANTITY, false));
		consumptionStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.VALUE, false));
		consumptionStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.PRICE, false));
		consumptionStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.REPLENISHMENTDEMAND, false));
		consumptionStockHeaderColumn.getColumns().add(new StockColumn(Stock.Selector.CONSUMPTION_COEFFICIENT, false));
	}

	/**
	 * Initialize the Commodities tableView and cellFactories
	 */
	public void makeCommoditiesViewTable() {
		commoditiesTable.getColumns().clear();

		commodityNameColumn = new CommodityColumn(Commodity.SELECTOR.NAME, true);
		commodityNameColumn.setMinWidth(80); // because some commodity names are quite long
		commoditiesTable.getColumns().add(commodityNameColumn);

		commodityBasicsSuperColumn = new TableColumn<Commodity, String>("Basics");
		commoditiesTable.getColumns().add(commodityBasicsSuperColumn);
		commodityBasicsSuperColumn.getColumns().add(new CommodityColumn(Commodity.SELECTOR.FUNCTION_TYPE, true));
		commodityQuantityColumn=new CommodityColumn(Commodity.SELECTOR.TOTALQUANTITY, false);
		commodityBasicsSuperColumn.getColumns().add(commodityQuantityColumn);
		commodityBasicsSuperColumn.getColumns().add(new CommodityColumn(Commodity.SELECTOR.TURNOVERTIME, false));

		commodityValuePriceSuperColumn = new TableColumn<Commodity, String>("Values and Prices");
		commodityValuePriceSuperColumn.setResizable(true);
		commoditiesTable.getColumns().add(commodityValuePriceSuperColumn);
		commodityValuePriceSuperColumn.getColumns().add(new CommodityColumn(Commodity.SELECTOR.UNITVALUE, false));
		commodityValuePriceSuperColumn.getColumns().add(new CommodityColumn(Commodity.SELECTOR.UNITPRICE, false));
		commodityValuePriceSuperColumn.getColumns().add(new CommodityColumn(Commodity.SELECTOR.TOTALVALUE, false));
		commodityTotalPriceColumn = new CommodityColumn(Commodity.SELECTOR.TOTALPRICE, false);
		commodityValuePriceSuperColumn.getColumns().add(commodityTotalPriceColumn);

		commodityDemandSupplySuperColumn = new TableColumn<Commodity, String>("Demand and Supply");
		commodityDemandSupplySuperColumn.setResizable(true);
		commoditiesTable.getColumns().add(commodityDemandSupplySuperColumn);
		commodityDemandSupplySuperColumn.getColumns().add(new CommodityColumn(Commodity.SELECTOR.TOTALSUPPLY, false));
		commodityDemandSupplySuperColumn.getColumns().add(new CommodityColumn(Commodity.SELECTOR.REPLENISHMENT_DEMAND, false));
		commodityDemandSupplySuperColumn.getColumns().add(new CommodityColumn(Commodity.SELECTOR.EXPANSION_DEMAND, false));
		commodityAllocationShareColumn = new CommodityColumn(Commodity.SELECTOR.ALLOCATIONSHARE, false);
		commodityDemandSupplySuperColumn.getColumns().add(commodityAllocationShareColumn);

		commodityCapitalProfitSuperColumn = new TableColumn<Commodity, String>("CapitalAndProfit");
		commodityCapitalProfitSuperColumn.setResizable(true);
		commoditiesTable.getColumns().add(commodityCapitalProfitSuperColumn);
		commodityProfitRateColumn = new CommodityColumn(Commodity.SELECTOR.PROFITRATE, false);
		commodityCapitalProfitSuperColumn.getColumns().add(new CommodityColumn(Commodity.SELECTOR.INITIALCAPITAL, false));
		commodityCapitalProfitSuperColumn.getColumns().add(new CommodityColumn(Commodity.SELECTOR.PROFIT, false));
		commodityCapitalProfitSuperColumn.getColumns().add(commodityProfitRateColumn);
		commodityCapitalProfitSuperColumn.getColumns().add(new CommodityColumn(Commodity.SELECTOR.SURPLUS, false));
	}

	/**
	 * Initialize the Social Classes tableView and cellFactories
	 */
	public void makeSocialClassesViewTable() {
		socialClassesTable.getColumns().clear();
		socialClassesTable.getColumns().add(new SocialClassColumn(SocialClass.Selector.SOCIALCLASSNAME, true));
		socialClassesTable.getColumns().add(new SocialClassColumn(SocialClass.Selector.SIZE, false));
		socialClassesTable.getColumns().add(new SocialClassColumn(SocialClass.Selector.SALES, false));
		for (Commodity u : Commodity.commoditiesByFunction(Commodity.FUNCTION.CONSUMER_GOOD)) {
			socialClassesTable.getColumns().add(new SocialClassColumn(u));
		}
		socialClassesTable.getColumns().add(new SocialClassColumn(SocialClass.Selector.MONEY, false));
		socialClassesTable.getColumns().add(new SocialClassColumn(SocialClass.Selector.REVENUE, false));
		socialClassesTable.getColumns().add(new SocialClassColumn(SocialClass.Selector.TOTAL, false));
	}

	/**
	 * Build the Industries tableView and cellFactories.
	 * Only call this when we want to rebuild the display from scratch, for example when starting up or switching projects
	 */
	public void makeIndustriesViewTable() {
		industriesTable.getColumns().clear();
		industriesTable.getColumns().add(new IndustryColumn(Industry.Selector.INDUSTRYNAME, true));
		industriesTable.getColumns().add(new IndustryColumn(Industry.Selector.INITIALCAPITAL, false));
		industriesTable.getColumns().add(new IndustryColumn(Industry.Selector.SALESSTOCK, false));
		industriesTable.getColumns().add(new IndustryColumn(Industry.Selector.PRODUCTIVESTOCKS, false));
		industriesTable.getColumns().add(new IndustryColumn(Industry.Selector.MONEYSTOCK, false));
		industriesTable.getColumns().add(new IndustryColumn(Industry.Selector.CURRENTCAPITAL, false));
		industriesTable.getColumns().add(new IndustryColumn(Industry.Selector.PROFIT, false));
		industriesTable.getColumns().add(new IndustryColumn(Industry.Selector.PROFITRATE, false));
	}

	/**
	 * Build the DynamicIndustriesTable and cellFactories.
	 * Only call this when we want to rebuild the display from scratch, for example when starting up or switching projects
	 */
	private void makeDynamicIndustriesTable() {
		dynamicIndustryTable.getColumns().clear();
		TableColumn<Industry, String> industryNameColumn = new IndustryColumn(Industry.Selector.INDUSTRYNAME, true);
		industryNameColumn.setPrefWidth(100); // because some industry names are quite long
		dynamicIndustryTable.getColumns().add(industryNameColumn);
		dynamicIndustryTable.getColumns().add(new IndustryColumn(Industry.Selector.COMMODITYNAME, true));
		for (Commodity u : Commodity.commoditiesByFunction(Commodity.FUNCTION.PRODUCTIVE_INPUT)) {
			dynamicIndustryTable.getColumns().add(new IndustryColumn(u));
		}
		dynamicIndustryTable.getColumns().add(new IndustryColumn(Industry.Selector.PROFIT, false));
		dynamicIndustryTable.getColumns().add(new IndustryColumn(Industry.Selector.SALESSTOCK, false));
		dynamicIndustryTable.getColumns().add(new IndustryColumn(Industry.Selector.OUTPUT, false));
		dynamicIndustryTable.getColumns().add(new IndustryColumn(Industry.Selector.PROPOSEDOUTPUT, false));
		dynamicIndustryTable.getColumns().add(new IndustryColumn(Industry.Selector.GROWTHRATE, false));
	}

	/**
	 * refresh the data in all the tabbed tables. Do not rebuild them.
	 * 
	 */
	public void repopulateTabbedTables() {
		productiveStockTable.setItems(Stock.stocksByStockTypeObservable("Productive"));
		moneyStockTable.setItems(Stock.stocksByStockTypeObservable("Money"));
		salesStockTable.setItems(Stock.stocksByStockTypeObservable("Sales"));
		consumptionStockTable.setItems(Stock.stocksByStockTypeObservable("Consumption"));
		commoditiesTable.setItems(Commodity.commoditiesObservable());
		industriesTable.setItems(Industry.industriesObservable());
		socialClassesTable.setItems(SocialClass.socialClassesObservable());
		dynamicIndustryTable.setItems(Industry.industriesObservable());
	}

	/**
	 * we have to force a refresh of the display because if the data has not changed, it may not be observed by the table
	 * see https://stackoverflow.com/questions/11065140/javafx-2-1-tableview-refresh-items
	 * i have kept this method separate from {@code populateTabbedTables()} because the issue merits further study.
	 */

	public void refreshTables() {
		for (TableView<?> table : tabbedTables) {
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

}