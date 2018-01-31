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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.control.TableCell;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
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

	@FXML private TableView<UseValue> useValuesTable;
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
		doctorColumnHeaders(circuitsTable);
		doctorColumnHeaders(useValuesTable);
		doctorColumnHeaders(socialClassesTable);
	}

	private void setTooltips() {
		setTip(useValuesTable, "A commodity is anything that that society makes use of, and has established a quantitative measure for");
		setTip(circuitsTable, "A producer is a business, or group of businesses, who make one commodity with similar technologies. \n"
				+ "Two producers can make the same commodity, but would normally be distinguished apart because their technology differs."
				+ "\nThis can help study the effect of technological change");
		setTip(socialClassesTable,
				"A social class is a group of people with the same source of revenue, defined by the type of property that they specialise in");
	}

	private void setTip(TableView<?> tableView, String text) {
		Tooltip tip = new Tooltip();
		tip.setText(text);
		tip.setFont(new Font(15));
		tableView.setTooltip(tip);
	}

	/**
	 * Replace a column header with a custom header whose display can be modified. Only do this if the column already has an embedded graphic
	 * 
	 * @param column
	 *            the column to be doctored
	 */
	private void doctorColumnHeader(TableColumn<?, ?> column) {
		Node columnGraphic = column.getGraphic();
		if (columnGraphic == null)
			return;

		// Use jewelsea's method. Replace both text and graphic with a text, containing the text and the graphic
		// this gives more flexibility, because a Label has more functionality (such as switching between text and graphic display)

		Label label = new Label();
		label.setText(column.getText());
		label.setGraphic(columnGraphic);
		column.setGraphic(label);
		column.setText("");
		label.setTooltip(new Tooltip(label.getText())); // TODO this doesn't seem to work if there is a table tooltip active
		label.toFront();								// this doesn't have any effect on the tooltip either - needs to be investigated
		column.setUserData("HasGraphic"); 				// tells us that the heading has been doctored
	}

	/**
	 * replace all column headers in a table with custom headers whose display can be modified
	 * 
	 * @param tableView
	 *            the table whose columns are to be doctored
	 */
	private void doctorColumnHeaders(TableView<?> tableView) {
		for (TableColumn<?, ?> column : tableView.getColumns()) {
			doctorColumnHeader(column);
		}
	}

	/**
	 * flip the given table over so that its columnns display either graphics or text depending on the state of {@link ViewManager#graphicsState}. Columns that
	 * never had a graphic in the first place are unaffected
	 * 
	 * @param tableView
	 *            the table to be flipped
	 */
	public void switchTableDisplay(TableView<?> tableView) {
		for (TableColumn<?, ?> column : tableView.getColumns()) {
			if ("HasGraphic".equals(column.getUserData())) {
				Label columnlabel = (Label) column.getGraphic();
				columnlabel.setContentDisplay(ViewManager.graphicsState);
			}
		}
	}

	/**
	 * Set all columns in the viewer to display a graphic only, if the graphic has been set. This will only work for those columns which have been 'doctored'
	 * using {@link TabbedTableViewer#doctorColumnHeader(TableColumn)}. It applies to all doctored tables - individual tables are not(at present) singled out.
	 * <p>
	 * NOTE; the option text and graphics is not an option in the enum {@link ViewManager#graphicsState}, but we could possibly achieve the effect by
	 * 'undoctoring' the column headers.
	 * 
	 */
	public void switchHeaderDisplay() {
		switch (ViewManager.graphicsState) {
		case TEXT_ONLY:
			ViewManager.graphicsState = ContentDisplay.GRAPHIC_ONLY;
			break;
		case GRAPHIC_ONLY:
			ViewManager.graphicsState = ContentDisplay.TEXT_ONLY;
			break;
		default:
			ViewManager.graphicsState = ContentDisplay.TEXT_ONLY;
		}
		switchTableDisplay(circuitsTable);
		switchTableDisplay(useValuesTable);
		switchTableDisplay(socialClassesTable);
		switchTableDisplay(dynamicCircuitTable);
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
		productiveStockTable.setItems(olProvider.stocksByStockTypeObservable("Productive"));
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
		moneyStockTable.setItems(olProvider.stocksByStockTypeObservable("Money"));
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
		salesStockTable.setItems(olProvider.stocksByStockTypeObservable("Sales"));
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
		consumptionStockTable.setItems(olProvider.stocksByStockTypeObservable("Consumption"));
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
		useValuesTable.setItems(olProvider.useValuesObservable());
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
		circuitsTable.setItems(olProvider.circuitsObservable());
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
		socialClassesTable.setItems(olProvider.socialClassesObservable());
		makeSocialClassColumn(socialClassNameColumn, SocialClass.Selector.SOCIALCLASSNAME);
		makeSocialClassColumn(socialClassSalesStockColumn, SocialClass.Selector.SALES);
		makeSocialClassColumn(socialClassConsumptionGoodsColumn, SocialClass.Selector.CONSUMPTIONSTOCKS);
		makeSocialClassColumn(socialClassTotalColumn, SocialClass.Selector.TOTAL);
		makeSocialClassColumn(socialClassMoneyColumn, SocialClass.Selector.MONEY);
		makeSocialClassColumn(socialClassQuantityDemandedColumn, SocialClass.Selector.QUANTITYDEMANDED);
		makeSocialClassColumn(socialClassRevenueColumn, SocialClass.Selector.SPENDING);
		makeSocialClassColumn(socialClassSizeColumn, SocialClass.Selector.SIZE);
	}

	private void addDynamicCircuitColumn(String columnName, Circuit.Selector selector,String imageURL) {
		TableColumn<Circuit, String> newColumn = new TableColumn<Circuit, String>(columnName);
		newColumn.setCellFactory(new Callback<TableColumn<Circuit, String>, TableCell<Circuit, String>>() {
			@Override public TableCell<Circuit, String> call(TableColumn<Circuit, String> col) {
				return new CircuitTableCell(selector);
			}
		});
		newColumn.setCellValueFactory(cellData -> cellData.getValue().wrappedString(selector, TabbedTableViewer.displayAttribute));
		newColumn.getStyleClass().add("table-column-right");
		if (imageURL!=null) {
			Label label =new Label(columnName);
			newColumn.setGraphic(label);
			ImageView image=new ImageView(imageURL);
			image.setFitWidth(20);
			image.setFitHeight(20);
			label.setGraphic(image);
			newColumn.setText("");
			newColumn.setUserData("HasGraphic"); 				// tells us the heading has been doctored
		}
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
		
		//a bit of a botch here to put images in place for externally-supplied names
		//TODO allow the user to supply the image
		
		if (productiveStockName.equals("Labour Power")) {
			Label label =new Label(productiveStockName);
			newColumn.setGraphic(label);
			ImageView image=new ImageView("labourPower.png");
			image.setFitWidth(20);
			image.setFitHeight(20);
			label.setGraphic(image);
			newColumn.setText("");
			newColumn.setUserData("HasGraphic"); 				// tells us the heading has been doctored
		}		if (productiveStockName.equals("Consumption")) {
			Label label =new Label("Necessities");
			newColumn.setGraphic(label);
			ImageView image=new ImageView("necessities.png");
			image.setFitWidth(20);
			image.setFitHeight(20);
			label.setGraphic(image);
			newColumn.setText("");
			newColumn.setUserData("HasGraphic"); 				// tells us the heading has been doctored
		}
		if (productiveStockName.equals("Means of Production")) {
			Label label =new Label("Means of Production");
			newColumn.setGraphic(label);
			ImageView image=new ImageView("Means of Production.png");
			image.setFitWidth(20);
			image.setFitHeight(20);
			label.setGraphic(image);
			newColumn.setText("");
			newColumn.setUserData("HasGraphic"); 				// tells us the heading has been doctored
		}
		dynamicCircuitTable.getColumns().add(newColumn);
	}

	public void createDynamicCircuitsTable() {
		dynamicCircuitTable.getColumns().clear();
		dynamicCircuitTable.setItems(olProvider.circuitsObservable());
		
		addDynamicCircuitColumn("Producer", Circuit.Selector.PRODUCTUSEVALUETYPE,null);
		addDynamicCircuitColumn("Desired Output", Circuit.Selector.PROPOSEDOUTPUT,"maximum output.png");
		addDynamicCircuitColumn("Constrained Output", Circuit.Selector.CONSTRAINEDOUTPUT,"constrained output.png");
		addDynamicCircuitColumn("GrowthRate", Circuit.Selector.GROWTHRATE,"growthrate.png");
		for (Stock s : DataManager.productiveStocks()) {
			addDynamicCircuitProductiveStockColumn(s.getUseValueName());
		}
	}

	/**
	 * @param displayAttribute
	 *            the displayAttribute to set
	 */
	public void setDisplayAttribute(Stock.ValueExpression displayAttribute) {
		TabbedTableViewer.displayAttribute = displayAttribute;
	}

	/**
	 * @return the useValuesTable
	 */
	public TableView<UseValue> getUseValuesTable() {
		return useValuesTable;
	}

	/**
	 * @param useValuesTable
	 *            the useValuesTable to set
	 */
	public void setUseValuesTable(TableView<UseValue> useValuesTable) {
		this.useValuesTable = useValuesTable;
	}

	/**
	 * @return the circuitsTable
	 */
	public TableView<Circuit> getCircuitsTable() {
		return circuitsTable;
	}

	/**
	 * @param circuitsTable
	 *            the circuitsTable to set
	 */
	public void setCircuitsTable(TableView<Circuit> circuitsTable) {
		this.circuitsTable = circuitsTable;
	}

}