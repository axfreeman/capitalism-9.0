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

package capitalism.view;

import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.model.Commodity;
import capitalism.model.Industry;
import capitalism.model.SocialClass;
import capitalism.model.Stock;
import capitalism.view.custom.DisplayControlsBox;
import capitalism.view.tables.CommodityColumn;
import capitalism.view.tables.IndustryColumn;
import capitalism.view.tables.SocialClassColumn;
import capitalism.view.tables.StockColumn;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.VBox;

public class TabbedTableViewer extends VBox {
	static final Logger logger = LogManager.getLogger("TableViewer");

	// selects whether to display quantities, values or prices, where appropriate

	public static Stock.VALUE_EXPRESSION displayAttribute = Stock.VALUE_EXPRESSION.PRICE;

	// Stock Tables and header columns

	private static TableView<Stock> productiveStockTable= new TableView<Stock> ();
	private static TableColumn<Stock, String> productiveStockHeaderColumn = new TableColumn<Stock, String> ("Productive Stocks");

	private static TableView<Stock> moneyStockTable =  new TableView<Stock> ();
	private static TableColumn<Stock, String> moneyStockHeaderColumn =new TableColumn<Stock, String> ("Money");

	private static TableView<Stock> salesStockTable = new TableView<Stock> ();
	private static TableColumn<Stock, String> salesStockHeaderColumn =new TableColumn<Stock, String>("Sales Stocks") ;
	
	private static TableView<Stock> consumptionStockTable = new TableView<Stock> ();
	private static TableColumn<Stock, String> consumptionStockHeaderColumn = new TableColumn<Stock, String> ("Stocks of Consumption Goods");

	// The Commodities table and its header columns

	protected static TableView<Commodity> commoditiesTable = new TableView<Commodity> ();
	private static TableColumn<Commodity, String> commodityDemandSupplySuperColumn;
	private static TableColumn<Commodity, String> commodityCapitalProfitSuperColumn;
	private static TableColumn<Commodity, String> commodityValuePriceSuperColumn;
	private static TableColumn<Commodity, String> commodityBasicsSuperColumn;

	private static TableColumn<Commodity, String> commodityTotalPriceColumn;
	private static TableColumn<Commodity, String> commodityAllocationShareColumn;
	private static TableColumn<Commodity, String> commodityProfitRateColumn;
	private static TableColumn<Commodity, String> commodityQuantityColumn;
	private static TableColumn<Commodity, String> commodityNameColumn;

	// Industry Tables and their header columns

	private static TableView<Industry> industryCapitalAccountTable =new TableView<Industry> ();
	private static TableView<Industry> industryProductionAccountsTable= new TableView<Industry> ();
	private static TableView<SocialClass> socialClassesTable=  new TableView<SocialClass> ();
	private static TableColumn<Industry, String> inputSuperColumn;
	private static TableColumn<Industry,String> productiveInputsColumn;
	private static TableColumn<Industry, String> outputSuperColumn;
	private static TableColumn<Industry,String> outputColumn;

	/**
	 * Simple static lists of tables, so utilities can get at them
	 */

	private static ArrayList<TableView<?>> mainTables = new ArrayList<TableView<?>>();
	private static ArrayList<TableView<?>> stockTables = new ArrayList<TableView<?>>();
	private static ArrayList<TableView<?>> allTables = new ArrayList<TableView<?>>();

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
		PRODUCTIVECAPITAL("The initial capital excluding money"),
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

	static {
		stockTables.add(salesStockTable);
		stockTables.add(moneyStockTable);
		stockTables.add(consumptionStockTable);
		stockTables.add(productiveStockTable);
		mainTables.add(industryProductionAccountsTable);
		mainTables.add(industryCapitalAccountTable);
		mainTables.add(socialClassesTable);
		mainTables.add(commoditiesTable);
		// TODO there must be a better way...
		for (TableView<?> table:stockTables) {
			allTables.add(table);
		}
		for (TableView<?> table:mainTables) {
			allTables.add(table);
		}
	}

	/**
	 * Custom control handles the main ViewTables
	 */
	public TabbedTableViewer() {
		setMaxWidth(Double.MAX_VALUE);

		// box for the main tables
		VBox mainBox = new VBox();
		mainBox.setPrefHeight(700);
		mainBox.setPrefWidth(7600);

		// box for the stock tables
		VBox stockBox = new VBox();
		stockBox.setPrefHeight(700);
		stockBox.setPrefWidth(7600);

		// the tabbed pane
		TabPane tabPane = new TabPane();
		tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		Tab mainTab = new Tab("Main");
		mainTab.setContent(mainBox);
		Tab stockTab = new Tab("Stock");
		stockTab.setContent(stockBox);
		tabPane.getTabs().addAll(mainTab, stockTab);

		for (TableView<?> table : mainTables) {
			table.setPrefHeight(150);
			table.setPrefWidth(750);
			table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
			mainBox.getChildren().add(table);
		}
		for (TableView<?> table : stockTables) {
			table.setPrefHeight(150);
			table.setPrefWidth(750);
			table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
			stockBox.getChildren().add(table);
		}

		productiveStockTable.getColumns().add(productiveStockHeaderColumn);
		moneyStockTable.getColumns().add(moneyStockHeaderColumn);
		salesStockTable.getColumns().add(salesStockHeaderColumn);
		consumptionStockTable.getColumns().add(consumptionStockHeaderColumn);

		getChildren().add(tabPane);
		
		DisplayControlsBox.setGraphicsState(ContentDisplay.TEXT_ONLY);		// initialize so start state is text only
		setDisplayAttribute(Stock.VALUE_EXPRESSION.PRICE);			// start off displaying prices
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
		makeIndustriesCapitalAccountsTable();
		makeIndustriesProductionAccountsTable();
		makeSocialClassesViewTable();

		TableUtilities.setSuperColumnHandler(inputSuperColumn, productiveInputsColumn);
		TableUtilities.setSuperColumnHandler(outputSuperColumn, outputColumn);
		
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
		productiveStockHeaderColumn.getColumns().add(new StockColumn(Stock.STOCK_ATTRIBUTE.OWNER, true));
		productiveStockHeaderColumn.getColumns().add(new StockColumn(Stock.STOCK_ATTRIBUTE.COMMODITY, true));
		productiveStockHeaderColumn.getColumns().add(new StockColumn(Stock.STOCK_ATTRIBUTE.QUANTITY, false));
		productiveStockHeaderColumn.getColumns().add(new StockColumn(Stock.STOCK_ATTRIBUTE.VALUE, false));
		productiveStockHeaderColumn.getColumns().add(new StockColumn(Stock.STOCK_ATTRIBUTE.PRICE, false));
		productiveStockHeaderColumn.getColumns().add(new StockColumn(Stock.STOCK_ATTRIBUTE.PRODUCTION_COEFFICIENT, false));
		productiveStockHeaderColumn.getColumns().add(new StockColumn(Stock.STOCK_ATTRIBUTE.REPLENISHMENTDEMAND, false));
		productiveStockHeaderColumn.getColumns().add(new StockColumn(Stock.STOCK_ATTRIBUTE.EXPANSIONDEMAND, false));
	}

	/**
	 * Initialize the Money Stocks tableView and cellFactories
	 */
	public void makeMoneyStocksViewTable() {
		moneyStockHeaderColumn.getColumns().clear();
		moneyStockHeaderColumn.getColumns().add(new StockColumn(Stock.STOCK_ATTRIBUTE.OWNERTYPE, true));
		moneyStockHeaderColumn.getColumns().add(new StockColumn(Stock.STOCK_ATTRIBUTE.OWNER, true));
		moneyStockHeaderColumn.getColumns().add(new StockColumn(Stock.STOCK_ATTRIBUTE.QUANTITY, false));
		moneyStockHeaderColumn.getColumns().add(new StockColumn(Stock.STOCK_ATTRIBUTE.VALUE, false));
		moneyStockHeaderColumn.getColumns().add(new StockColumn(Stock.STOCK_ATTRIBUTE.PRICE, false));
	}

	/**
	 * Initialize the Sales Stocks tableView and cellFactories
	 */
	public void makeSalesStocksViewTable() {
		salesStockHeaderColumn.getColumns().clear();
		salesStockHeaderColumn.getColumns().add(new StockColumn(Stock.STOCK_ATTRIBUTE.OWNERTYPE, true));
		salesStockHeaderColumn.getColumns().add(new StockColumn(Stock.STOCK_ATTRIBUTE.OWNER, true));
		salesStockHeaderColumn.getColumns().add(new StockColumn(Stock.STOCK_ATTRIBUTE.COMMODITY, false));
		salesStockHeaderColumn.getColumns().add(new StockColumn(Stock.STOCK_ATTRIBUTE.QUANTITY, false));
		salesStockHeaderColumn.getColumns().add(new StockColumn(Stock.STOCK_ATTRIBUTE.VALUE, false));
		salesStockHeaderColumn.getColumns().add(new StockColumn(Stock.STOCK_ATTRIBUTE.PRICE, false));
	}

	/**
	 * Initialize the Consumption Stocks tableView and cellFactories
	 */
	public void makeConsumptionStocksViewTable() {
		consumptionStockHeaderColumn.getColumns().clear();
		consumptionStockHeaderColumn.getColumns().add(new StockColumn(Stock.STOCK_ATTRIBUTE.OWNER, true));
		consumptionStockHeaderColumn.getColumns().add(new StockColumn(Stock.STOCK_ATTRIBUTE.COMMODITY, true));
		consumptionStockHeaderColumn.getColumns().add(new StockColumn(Stock.STOCK_ATTRIBUTE.QUANTITY, false));
		consumptionStockHeaderColumn.getColumns().add(new StockColumn(Stock.STOCK_ATTRIBUTE.VALUE, false));
		consumptionStockHeaderColumn.getColumns().add(new StockColumn(Stock.STOCK_ATTRIBUTE.PRICE, false));
		consumptionStockHeaderColumn.getColumns().add(new StockColumn(Stock.STOCK_ATTRIBUTE.REPLENISHMENTDEMAND, false));
		consumptionStockHeaderColumn.getColumns().add(new StockColumn(Stock.STOCK_ATTRIBUTE.CONSUMPTION_COEFFICIENT, false));
	}

	/**
	 * Initialize the Commodities tableView and cellFactories
	 */
	public void makeCommoditiesViewTable() {
		commoditiesTable.getColumns().clear();

		commodityNameColumn = new CommodityColumn(Commodity.COMMODITY_ATTRIBUTE.NAME, true);
		commodityNameColumn.setMinWidth(80); // because some commodity names are quite long
		commoditiesTable.getColumns().add(commodityNameColumn);

		commodityBasicsSuperColumn = new TableColumn<Commodity, String>("Basics");
		commoditiesTable.getColumns().add(commodityBasicsSuperColumn);
		commodityBasicsSuperColumn.getColumns().add(new CommodityColumn(Commodity.COMMODITY_ATTRIBUTE.FUNCTION_TYPE, true));
		commodityQuantityColumn = new CommodityColumn(Commodity.COMMODITY_ATTRIBUTE.TOTALQUANTITY, false);
		commodityBasicsSuperColumn.getColumns().add(commodityQuantityColumn);
		commodityBasicsSuperColumn.getColumns().add(new CommodityColumn(Commodity.COMMODITY_ATTRIBUTE.TURNOVERTIME, false));

		commodityValuePriceSuperColumn = new TableColumn<Commodity, String>("Values and Prices");
		commodityValuePriceSuperColumn.setResizable(true);
		commoditiesTable.getColumns().add(commodityValuePriceSuperColumn);
		commodityValuePriceSuperColumn.getColumns().add(new CommodityColumn(Commodity.COMMODITY_ATTRIBUTE.UNITVALUE, false));
		commodityValuePriceSuperColumn.getColumns().add(new CommodityColumn(Commodity.COMMODITY_ATTRIBUTE.UNITPRICE, false));
		commodityValuePriceSuperColumn.getColumns().add(new CommodityColumn(Commodity.COMMODITY_ATTRIBUTE.TOTALVALUE, false));
		commodityTotalPriceColumn = new CommodityColumn(Commodity.COMMODITY_ATTRIBUTE.TOTALPRICE, false);
		commodityValuePriceSuperColumn.getColumns().add(commodityTotalPriceColumn);

		commodityDemandSupplySuperColumn = new TableColumn<Commodity, String>("Demand and Supply");
		commodityDemandSupplySuperColumn.setResizable(true);
		commoditiesTable.getColumns().add(commodityDemandSupplySuperColumn);
		commodityDemandSupplySuperColumn.getColumns().add(new CommodityColumn(Commodity.COMMODITY_ATTRIBUTE.TOTALSUPPLY, false));
		commodityDemandSupplySuperColumn.getColumns().add(new CommodityColumn(Commodity.COMMODITY_ATTRIBUTE.REPLENISHMENT_DEMAND, false));
		commodityDemandSupplySuperColumn.getColumns().add(new CommodityColumn(Commodity.COMMODITY_ATTRIBUTE.EXPANSION_DEMAND, false));
		commodityAllocationShareColumn = new CommodityColumn(Commodity.COMMODITY_ATTRIBUTE.ALLOCATIONSHARE, false);
		commodityDemandSupplySuperColumn.getColumns().add(commodityAllocationShareColumn);

		commodityCapitalProfitSuperColumn = new TableColumn<Commodity, String>("CapitalAndProfit");
		commodityCapitalProfitSuperColumn.setResizable(true);
		commoditiesTable.getColumns().add(commodityCapitalProfitSuperColumn);
		commodityProfitRateColumn = new CommodityColumn(Commodity.COMMODITY_ATTRIBUTE.PROFITRATE, false);
		commodityCapitalProfitSuperColumn.getColumns().add(new CommodityColumn(Commodity.COMMODITY_ATTRIBUTE.INITIALCAPITAL, false));
		commodityCapitalProfitSuperColumn.getColumns().add(new CommodityColumn(Commodity.COMMODITY_ATTRIBUTE.PROFIT, false));
		commodityCapitalProfitSuperColumn.getColumns().add(commodityProfitRateColumn);
		commodityCapitalProfitSuperColumn.getColumns().add(new CommodityColumn(Commodity.COMMODITY_ATTRIBUTE.SURPLUS, false));
	}

	/**
	 * Initialize the Social Classes tableView and cellFactories
	 */
	public void makeSocialClassesViewTable() {
		socialClassesTable.getColumns().clear();
		socialClassesTable.getColumns().add(new SocialClassColumn(SocialClass.SOCIALCLASS_ATTRIBUTE.SOCIALCLASSNAME, true));
		socialClassesTable.getColumns().add(new SocialClassColumn(SocialClass.SOCIALCLASS_ATTRIBUTE.SIZE, false));
		socialClassesTable.getColumns().add(new SocialClassColumn(SocialClass.SOCIALCLASS_ATTRIBUTE.SALES, false));
		for (Commodity u : Commodity.currentByFunction(Commodity.FUNCTION.CONSUMER_GOOD)) {
			socialClassesTable.getColumns().add(new SocialClassColumn(u));
		}
		socialClassesTable.getColumns().add(new SocialClassColumn(SocialClass.SOCIALCLASS_ATTRIBUTE.MONEY, false));
		socialClassesTable.getColumns().add(new SocialClassColumn(SocialClass.SOCIALCLASS_ATTRIBUTE.REVENUE, false));
		socialClassesTable.getColumns().add(new SocialClassColumn(SocialClass.SOCIALCLASS_ATTRIBUTE.TOTAL, false));
	}

	/**
	 * Build the Industries tableView and cellFactories.
	 * Only call this when we want to rebuild the display from scratch, for example when starting up or switching projects
	 */
	public void makeIndustriesCapitalAccountsTable() {
		industryCapitalAccountTable.getColumns().clear();
		industryCapitalAccountTable.getColumns().add(new IndustryColumn(Industry.INDUSTRY_ATTRIBUTE.INDUSTRYNAME, true));
		industryCapitalAccountTable.getColumns().add(new IndustryColumn(Industry.INDUSTRY_ATTRIBUTE.INITIALCAPITAL, false));
		industryCapitalAccountTable.getColumns().add(new IndustryColumn(Industry.INDUSTRY_ATTRIBUTE.SALESSTOCK, false));
		industryCapitalAccountTable.getColumns().add(new IndustryColumn(Industry.INDUSTRY_ATTRIBUTE.PRODUCTIVESTOCKS, false));
		industryCapitalAccountTable.getColumns().add(new IndustryColumn(Industry.INDUSTRY_ATTRIBUTE.MONEYSTOCK, false));
		industryCapitalAccountTable.getColumns().add(new IndustryColumn(Industry.INDUSTRY_ATTRIBUTE.CURRENTCAPITAL, false));
		industryCapitalAccountTable.getColumns().add(new IndustryColumn(Industry.INDUSTRY_ATTRIBUTE.INITIALPRODUCTIVECAPITAL, false));
		industryCapitalAccountTable.getColumns().add(new IndustryColumn(Industry.INDUSTRY_ATTRIBUTE.PROFIT, false));
		industryCapitalAccountTable.getColumns().add(new IndustryColumn(Industry.INDUSTRY_ATTRIBUTE.PROFITRATE, false));
	}

	/**
	 * Build the DynamicIndustriesTable and cellFactories.
	 * Only call this when we want to rebuild the display from scratch, for example when starting up or switching projects
	 */
	private void makeIndustriesProductionAccountsTable() {
		industryProductionAccountsTable.getColumns().clear();
		TableColumn<Industry, String> industryNameColumn = new IndustryColumn(Industry.INDUSTRY_ATTRIBUTE.INDUSTRYNAME, true);
		industryNameColumn.setPrefWidth(100); // because some industry names are quite long
		industryProductionAccountsTable.getColumns().add(industryNameColumn);
		industryProductionAccountsTable.getColumns().add(new IndustryColumn(Industry.INDUSTRY_ATTRIBUTE.COMMODITYNAME, true));

		inputSuperColumn= new TableColumn<Industry, String>("Inputs");
		inputSuperColumn.setResizable(true);
		industryProductionAccountsTable.getColumns().add(inputSuperColumn);
		productiveInputsColumn= new IndustryColumn(Industry.INDUSTRY_ATTRIBUTE.PRODUCTIVESTOCKS, false);
		for (Commodity u : Commodity.currentByFunction(Commodity.FUNCTION.PRODUCTIVE_INPUT)) {
			inputSuperColumn.getColumns().add(new IndustryColumn(u));
		}
		inputSuperColumn.getColumns().add(productiveInputsColumn);

		outputSuperColumn=new TableColumn<Industry, String>("Outputs");
		outputColumn=new IndustryColumn(Industry.INDUSTRY_ATTRIBUTE.OUTPUT, false);
		industryProductionAccountsTable.getColumns().add(outputSuperColumn);
		outputSuperColumn.getColumns().add(outputColumn);
		outputSuperColumn.getColumns().add(new IndustryColumn(Industry.INDUSTRY_ATTRIBUTE.PROPOSEDOUTPUT, false));
		outputSuperColumn.getColumns().add(new IndustryColumn(Industry.INDUSTRY_ATTRIBUTE.GROWTHRATE, false));
	}

	/**
	 * refresh the data in all the tabbed tables. Do not rebuild them.
	 * 
	 */
	public void repopulateTabbedTables() {
		productiveStockTable.setItems(Stock.ofStockTypeObservable("Productive"));
		moneyStockTable.setItems(Stock.ofStockTypeObservable("Money"));
		salesStockTable.setItems(Stock.ofStockTypeObservable("Sales"));
		consumptionStockTable.setItems(Stock.ofStockTypeObservable("Consumption"));
		commoditiesTable.setItems(Commodity.commoditiesObservable());
		industryCapitalAccountTable.setItems(Industry.industriesObservable());
		socialClassesTable.setItems(SocialClass.socialClassesObservable());
		industryProductionAccountsTable.setItems(Industry.industriesObservable());
	}

	/**
	 * we have to force a refresh of the display because if the data has not changed, it may not be observed by the table
	 * see https://stackoverflow.com/questions/11065140/javafx-2-1-tableview-refresh-items
	 * i have kept this method separate from {@code populateTabbedTables()} because the issue merits further study.
	 */

	public static void refreshTables() {
		for (TableView<?> table : mainTables) {
			table.refresh();
		}
		for (TableView<?> table : stockTables) {
			table.refresh();
		}
	}

	/**
	 * switch the header displays (between graphics and text) for all our tables
	 */

	public void switchHeaderDisplays() {
		TableUtilities.switchHeaderDisplays(allTables);
	}

	/**
	 * @param displayAttribute
	 *            the displayAttribute to set
	 */
	public static void setDisplayAttribute(Stock.VALUE_EXPRESSION displayAttribute) {
		TabbedTableViewer.displayAttribute = displayAttribute;
	}
}