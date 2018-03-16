/*
 * Copyright (C) Alan Freeman 2017-2019
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

import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.controller.Parameters;
import capitalism.editor.EditableCommodity.EC_ATTRIBUTE;
import capitalism.editor.EditableIndustry.EI_ATTRIBUTE;
import capitalism.editor.EditableSocialClass.ESC_ATTRIBUTE;
import capitalism.editor.EditorManager.EditorControlBar;
import capitalism.model.Commodity;
import capitalism.model.Industry;
import capitalism.model.OneProject;
import capitalism.model.Project;
import capitalism.model.SocialClass;
import capitalism.model.Stock;
import capitalism.model.Stock.OWNERTYPE;
import capitalism.model.TimeStamp;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.VBox;

/*
 * with acknowledgements to http://java-buddy.blogspot.ca/2012/04/javafx-2-editable-tableview.html
 */

public class Editor extends VBox {
	private static final Logger logger = LogManager.getLogger("Editor");

	private static EditorControlBar ecb = new EditorControlBar();

	private static ObservableList<EditableCommodity> commodityData = null;
	private static TableView<EditableCommodity> commodityTable = new TableView<EditableCommodity>();
	private static ObservableList<EditableIndustry> industryData = null;
	private static TableView<EditableIndustry> industryTable = new TableView<EditableIndustry>();
	private static ObservableList<EditableSocialClass> socialClassData = null;
	private static TableView<EditableSocialClass> socialClassTable = new TableView<EditableSocialClass>();
	private static EditableTimeStamp editableTimeStamp = new EditableTimeStamp();

	public Editor() {

		// start from scratch every time
		commodityData = FXCollections.observableArrayList();
		industryData = FXCollections.observableArrayList();
		socialClassData = FXCollections.observableArrayList();
		commodityTable.getColumns().clear();
		industryTable.getColumns().clear();
		socialClassTable.getColumns().clear();

		makeCommodityTable();
		makeIndustryTable();
		makeSocialClassTable();

		// box for the commodity table
		VBox commodityBox = new VBox();
		commodityBox.setPrefHeight(300);
		commodityBox.setPrefWidth(7600);
		commodityBox.getChildren().add(commodityTable);

		// box for the industry table
		VBox industryBox = new VBox();
		industryBox.setPrefHeight(300);
		industryBox.setPrefWidth(7600);
		industryBox.getChildren().add(industryTable);

		// box for the social class table
		VBox socialClassBox = new VBox();
		socialClassBox.setPrefHeight(300);
		socialClassBox.setPrefWidth(7600);
		socialClassBox.getChildren().add(socialClassTable);

		// the tabbed pane
		TabPane tabPane = new TabPane();
		tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		Tab commodityTab = new Tab("Commodities");
		commodityTab.setContent(commodityBox);
		Tab industryTab = new Tab("Industries");
		industryTab.setContent(industryBox);
		Tab socialClassTab = new Tab("Classes");
		socialClassTab.setContent(socialClassBox);

		tabPane.getTabs().addAll(commodityTab, industryTab, socialClassTab);
		getChildren().addAll(ecb, tabPane);
	}

	private void makeCommodityTable() {
		commodityTable.setEditable(true);
		commodityTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		commodityTable.getColumns().add(EditableCommodity.makeStringColumn(EC_ATTRIBUTE.NAME));
		commodityTable.getColumns().add(EditableCommodity.makeStringColumn(EC_ATTRIBUTE.FUNCTION));
		commodityTable.getColumns().add(EditableCommodity.makeStringColumn(EC_ATTRIBUTE.ORIGIN));
		commodityTable.getColumns().add(EditableCommodity.makeDoubleColumn(EC_ATTRIBUTE.UNIT_VALUE));
		commodityTable.getColumns().add(EditableCommodity.makeDoubleColumn(EC_ATTRIBUTE.UNIT_PRICE));
		commodityTable.getColumns().add(EditableCommodity.makeDoubleColumn(EC_ATTRIBUTE.TURNOVER));
		commodityTable.setItems(commodityData);
	}

	private void makeIndustryTable() {
		industryTable.setEditable(true);
		industryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		industryTable.getColumns().add(EditableIndustry.makeStringColumn(EI_ATTRIBUTE.NAME));
		industryTable.getColumns().add(EditableIndustry.makeStringColumn(EI_ATTRIBUTE.COMMODITY_NAME));
		industryTable.getColumns().add(EditableIndustry.makeDoubleColumn(EI_ATTRIBUTE.OUTPUT));
		industryTable.getColumns().add(EditableIndustry.makeDoubleColumn(EI_ATTRIBUTE.SALES));
		industryTable.getColumns().add(EditableIndustry.makeDoubleColumn(EI_ATTRIBUTE.MONEY));
		industryTable.setItems(industryData);
	}

	public static void addIndustryStockColumns() {
		for (EditableCommodity commodity : commodityData) {
			// TODO bit of a developer leak here: need to make sure the
			// data exists. May be Better to drive from the data table than just
			// assume that just because there is a commodity, the data has been supplied.
			if (commodity.getFunction().equals("Productive Inputs"))
				industryTable.getColumns().add(EditableIndustry.makeStockColumn(commodity.getName()));
		}
	}

	private void makeSocialClassTable() {
		socialClassTable.setEditable(true);
		socialClassTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		socialClassTable.getColumns().add(EditableSocialClass.makeStringColumn(ESC_ATTRIBUTE.NAME));
		socialClassTable.getColumns().add(EditableSocialClass.makeDoubleColumn(ESC_ATTRIBUTE.PR));
		socialClassTable.getColumns().add(EditableSocialClass.makeDoubleColumn(ESC_ATTRIBUTE.REVENUE));
		socialClassTable.getColumns().add(EditableSocialClass.makeDoubleColumn(ESC_ATTRIBUTE.SALES));
		socialClassTable.getColumns().add(EditableSocialClass.makeDoubleColumn(ESC_ATTRIBUTE.MONEY));
		socialClassTable.setItems(socialClassData);
	}

	public static void addSocialClassStockColumns() {
		for (EditableCommodity commodity : commodityData) {
			logger.debug("Adding columns for consumption goods: trying {}", commodity.getName());
			// TODO bit of a developer leak here: need to make sure the
			// data exists. May be Better to drive from the data table than just
			// assume that just because there is a commodity, the data has been supplied.
			if (commodity.getFunction().equals("Consumer Goods"))
				socialClassTable.getColumns().add(EditableSocialClass.makeStockColumn(commodity.getName()));
		}
	}

	public static void refresh() {
		// TODO check empirically if this is really needed because these are observables
		// so in principle refresh should be automatic
		industryTable.refresh();
		socialClassTable.refresh();
	}

	/**
	 * Wrap the editor entities in an instance of oneProject, for exporting or importing
	 * 
	 * @return a OneProject entity with all the editor entities stored in it as 'floating' JPA entities
	 *         that have not been persisted. These are purely transient objects which mediate between the editor
	 *         and the database, and should not be managed or persisted
	 */
	public static OneProject wrap() {
		OneProject oneProject = new OneProject();

		ArrayList<Commodity> commodities = new ArrayList<Commodity>();
		ArrayList<Industry> industries = new ArrayList<Industry>();
		ArrayList<SocialClass> socialClasses = new ArrayList<SocialClass>();
		ArrayList<Stock> stocks = new ArrayList<Stock>();
		ArrayList<TimeStamp> timeStamps = new ArrayList<TimeStamp>();

		// The timeStamp - actually a list, even though in this case it only has one member
		TimeStamp timeStamp = new TimeStamp(1, 0, 1, "Revenue", 1, "Start");
		timeStamp.setPopulationGrowthRate(editableTimeStamp.getPopulationGrowthRate());
		timeStamp.setInvestmentRatio(editableTimeStamp.getInvestmentRatio());
		timeStamp.setLabourSupplyResponse(Parameters.LABOUR_RESPONSE.fromText(editableTimeStamp.getLabourSupplyResponse()));
		timeStamp.setPriceResponse(Parameters.PRICE_RESPONSE.fromText(editableTimeStamp.getPriceResponse()));
		timeStamp.setMeltResponse(Parameters.MELT_RESPONSE.fromText(editableTimeStamp.getMeltResponse()));
		timeStamp.setCurrencySymbol(editableTimeStamp.getCurrencySymbol());
		timeStamp.setQuantitySymbol(editableTimeStamp.getQuantitySymbol());
		timeStamps.add(timeStamp);
		oneProject.setTimeStamps(timeStamps);

		// the project
		Project project = new Project();
		project.setProjectID(0);
		project.setTimeStampID(1);
		project.setDescription("New Project");
		project.setTimeStampDisplayCursor(1);
		project.setTimeStampComparatorCursor(1);
		oneProject.setProject(project);

		// The commodities
		for (EditableCommodity c : commodityData) {
			Commodity pc = new Commodity();
			pc.setProjectID(0);
			pc.setTimeStampID(1);
			pc.setName(c.getName());
			pc.setTurnoverTime(c.getTurnoverTime());
			pc.setUnitValue(pc.getUnitValue());
			pc.setUnitPrice(pc.getUnitPrice());
			pc.setFunction(Commodity.FUNCTION.function(c.getFunction()));
			pc.setOrigin(Commodity.ORIGIN.origin(c.getOrigin()));
			logger.debug("Stashing the commodity called {} ", pc.name());
			commodities.add(pc);
		}
		oneProject.setCommodities(commodities);

		// The industries

		for (EditableIndustry ind : industryData) {
			Industry pind = new Industry();
			pind.setProjectID(0);
			pind.setTimeStamp(1);
			pind.setName(ind.getName());
			pind.setCommodityName(ind.getCommodityName());
			pind.setOutput(ind.getOutput());
			industries.add(pind);
			logger.debug("Saving the industry called {}, together with its money and sales stocks ", pind.name());
			// TODO retrieve the actual amounts
			Stock moneyStock = moneyStockBuilder(ind.getName(), ind.getDouble(EI_ATTRIBUTE.MONEY), OWNERTYPE.INDUSTRY);
			Stock salesStock = salesStockBuilder(ind.getName(), ind.getCommodityName(), ind.getDouble(EI_ATTRIBUTE.SALES), OWNERTYPE.INDUSTRY);
			stocks.add(salesStock);
			stocks.add(moneyStock);
			for (EditableStock ps : ind.getProductiveStocks().values()) {
				Stock pps = productiveStockBuilder(ind.getName(), ps.getName(), ps.getDesiredQuantity(), ps.getActualQuantity());
				logger.debug(" Stashing the  stock called {} belonging to industry {}", pps.name(), pind.name());
				stocks.add(pps);
			}
		}
		oneProject.setIndustries(industries);

		// the Social Classes
		for (EditableSocialClass sc : socialClassData) {
			SocialClass psc = new SocialClass();
			psc.setProjectID(0);
			psc.setTimeStamp(1);
			psc.setName(sc.getName());
			psc.setparticipationRatio(sc.getParticipationRatio());
			psc.setRevenue(psc.getRevenue());
			logger.debug("Stashing the social class called {} together with its money and sales stocks", psc.name());
			socialClasses.add(psc);
			Stock moneyStock = moneyStockBuilder(sc.getName(), sc.getDouble(ESC_ATTRIBUTE.MONEY), OWNERTYPE.CLASS);
			Stock salesStock = salesStockBuilder(sc.getName(), "Labour Power", sc.getDouble(ESC_ATTRIBUTE.SALES), OWNERTYPE.CLASS);
			stocks.add(salesStock);
			stocks.add(moneyStock);
			for (EditableStock ps : sc.getConsumptionStocks().values()) {
				Stock pps = consumptionStockBuilder(sc.getName(), ps.getName(), ps.getDesiredQuantity(), ps.getActualQuantity());
				logger.debug(" Stashing the  stock called {} belonging to class {} ", pps.name(), psc.name());
				stocks.add(pps);
			}
		}
		oneProject.setSocialClasses(socialClasses);
		// The stocks, which have all been created as we go along
		oneProject.setStocks(stocks);
		return oneProject;
	}

	public static Stock moneyStockBuilder(String owner, double actualQuantity, OWNERTYPE ownerType) {
		Stock moneyStock = new Stock();
		moneyStock.setTimeStamp(1);
		moneyStock.setProjectID(0);
		moneyStock.setCommodityName("Money");
		moneyStock.setStockType("Money");
		moneyStock.setOwner(owner);
		moneyStock.setOwnerType(ownerType);
		moneyStock.setQuantity(actualQuantity);
		return moneyStock;
	}

	public static Stock salesStockBuilder(String owner, String commodityName, double actualQuantity, OWNERTYPE ownerType) {
		Stock salesStock = new Stock();
		salesStock.setTimeStamp(1);
		salesStock.setProjectID(0);
		salesStock.setStockType("Sales");
		salesStock.setOwner(owner);
		salesStock.setQuantity(actualQuantity);
		salesStock.setCommodityName(commodityName);
		salesStock.setOwnerType(ownerType);
		return salesStock;
	}

	public static Stock productiveStockBuilder(String owner, String commodityName, double desiredQuantity, double actualQuantity) {
		Stock productiveStock = new Stock();
		productiveStock.setTimeStamp(1);
		productiveStock.setProjectID(0);
		productiveStock.setStockType("Productive");
		productiveStock.setOwner(owner);
		productiveStock.setOwnerType(OWNERTYPE.INDUSTRY);
		productiveStock.setCommodityName(commodityName);
		productiveStock.setQuantity(actualQuantity);
		productiveStock.setProductionQuantity(desiredQuantity);
		return productiveStock;
	}

	public static Stock consumptionStockBuilder(String owner, String commodityName, double desiredQuantity, double actualQuantity) {
		Stock consumptionStock = new Stock();
		consumptionStock.setTimeStamp(1);
		consumptionStock.setProjectID(0);
		consumptionStock.setStockType("Consumption");
		consumptionStock.setOwner(owner);
		consumptionStock.setOwnerType(OWNERTYPE.CLASS);
		consumptionStock.setCommodityName(commodityName);
		consumptionStock.setQuantity(actualQuantity);
		consumptionStock.setConsumptionQuantity(desiredQuantity);
		return consumptionStock;
	}

	/**
	 * @return the data
	 */
	public static ObservableList<EditableCommodity> getCommodityData() {
		return commodityData;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public static void setCommodityData(ObservableList<EditableCommodity> data) {
		Editor.commodityData = data;
		commodityTable.setItems(Editor.commodityData);
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public static void setIndustryData(ObservableList<EditableIndustry> data) {
		Editor.industryData = data;
		industryTable.setItems(Editor.industryData);
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public static void setSocialClassData(ObservableList<EditableSocialClass> data) {
		Editor.socialClassData = data;
		socialClassTable.setItems(Editor.socialClassData);
	}

	/**
	 * @return the industryData
	 */
	public static ObservableList<EditableIndustry> getIndustryData() {
		return industryData;
	}

	/**
	 * @return the socialClassData
	 */
	public static ObservableList<EditableSocialClass> getSocialClassData() {
		return socialClassData;
	}
}
