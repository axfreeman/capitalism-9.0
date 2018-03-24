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
import capitalism.editor.model.EditableCommodity;
import capitalism.editor.model.EditableIndustry;
import capitalism.editor.model.EditableIndustry.EI_ATTRIBUTE;
import capitalism.editor.model.EditableSocialClass;
import capitalism.editor.model.EditableSocialClass.ESC_ATTRIBUTE;
import capitalism.editor.model.EditableStock;
import capitalism.editor.model.EditableTimeStamp;
import capitalism.model.Commodity;
import capitalism.model.Commodity.FUNCTION;
import capitalism.model.Commodity.ORIGIN;
import capitalism.model.Industry;
import capitalism.model.OneProject;
import capitalism.model.Project;
import capitalism.model.SocialClass;
import capitalism.model.Stock;
import capitalism.model.Stock.OWNERTYPE;
import capitalism.model.TimeStamp;
import capitalism.utils.Reporter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * This class moves data between the Editor, the simulation, and the user's saved project files
 */
public class EditorLoader {
	private final static Logger logger = LogManager.getLogger("EditorLoader");

	protected static ObservableList<EditableCommodity> commodityData = null;
	protected static ObservableList<EditableIndustry> industryData = null;
	protected static ObservableList<EditableSocialClass> socialClassData = null;
	private static EditableTimeStamp editableTimeStamp = null;

	/**
	 * Load the project with ID projectID into the editor
	 * 
	 * @param projectID
	 *            the ID of the project to load
	 */
	public static void loadFromSimulation(int projectID) {
		logger.debug("Loading the project with ID {}", projectID);
		// start from scratch every time
		commodityData = FXCollections.observableArrayList();
		industryData = FXCollections.observableArrayList();
		socialClassData = FXCollections.observableArrayList();

		// First fetch the commodities from the project into the commodity table
		commodityData = EditableCommodity.editableCommodities(projectID);

		// Next, load the industries
		industryData = EditableIndustry.editableIndustries(projectID);

		// Now add the productive stocks that these industries own
		for (EditableIndustry industry : industryData) {
			for (EditableCommodity commodity : commodityData) {
				if (commodity.getFunction().equals("Productive Inputs"))
					industry.addProductiveStock(commodity.getName());
			}
		}
		// Populate the EditableStocks from the simulation.
		// The money and sales stocks were created by the EditableIndustry constructor
		// We just added the productive stocks
		EditableIndustry.loadAllStocksFromSimulation(projectID);

		// load the social classes
		socialClassData = EditableSocialClass.editableSocialClasses(projectID);
		// Add the consumption stocks that these industries classes own
		for (EditableSocialClass socialClass : socialClassData) {
			for (EditableCommodity commodity : commodityData) {
				if (commodity.getFunction().equals("Consumer Goods"))
					socialClass.addConsumptionStock(commodity.getName());
			}
		}
		EditableSocialClass.loadAllStocksFromSimulation(projectID);
		Editor.makeAllTables();
	}

	/**
	 * Create a skeleton project, which contains the minimum necessary for a viable project
	 */
	public static void createSkeletonProject() {
		Reporter.report(logger, 0, "CREATING A PROJECT");
		commodityData = FXCollections.observableArrayList();
		industryData = FXCollections.observableArrayList();
		socialClassData = FXCollections.observableArrayList();

		// Create the commodities money, necessities, labour power, and means of production
		Reporter.report(logger, 1, "Creating the basic commodities");
		EditableCommodity moneyCommodity = EditableCommodity.makeCommodity("Money", ORIGIN.MONEY, FUNCTION.MONEY);
		EditableCommodity necessityCommodity = EditableCommodity.makeCommodity("Necessities", ORIGIN.INDUSTRIALLY_PRODUCED, FUNCTION.CONSUMER_GOOD);
		EditableCommodity meandOfProductionCommodity = EditableCommodity.makeCommodity("Means of Production", ORIGIN.INDUSTRIALLY_PRODUCED,
				FUNCTION.PRODUCTIVE_INPUT);
		EditableCommodity labourPowerCommodity = EditableCommodity.makeCommodity("Labour Power", ORIGIN.SOCIALLY_PRODUCED, FUNCTION.PRODUCTIVE_INPUT);
		commodityData.addAll(moneyCommodity, necessityCommodity, meandOfProductionCommodity, labourPowerCommodity);

		// Create the social classes Capitalists, Workers and their stocks
		Reporter.report(logger, 1, "Creating the minimum social Classes");
		EditableSocialClass capitalists = EditableSocialClass.makeSocialClass("Capitalists",0, 0);
		EditableSocialClass workers = EditableSocialClass.makeSocialClass("Workers",0, 1);
		socialClassData.addAll(workers, capitalists);

		// Create the two industries means of production and necessities and their stocks
		Reporter.report(logger, 1, "Creating the minimum industries");
		EditableIndustry dI = EditableIndustry.makeIndustry("Department I", "Means of production", 0);
		EditableIndustry dII = EditableIndustry.makeIndustry("Departmment II", "Necessities", 0);
		industryData.addAll(dI, dII);
		Editor.makeAllTables();
	}

	/**
	 * Wrap the editor's observable entities in an instance of oneProject, for exporting or importing
	 * 
	 * @return a OneProject entity with all the editor entities stored in it as 'floating' JPA entities
	 *         that have not been persisted. These are purely transient objects which mediate between the editor
	 *         and the database, and should not be managed or persisted
	 */
	public static OneProject wrappedOneProject() {
		OneProject oneProject = new OneProject();
		editableTimeStamp = new EditableTimeStamp();
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
		for (EditableCommodity c : EditorLoader.commodityData) {
			Commodity pc = new Commodity();
			pc.setProjectID(0);
			pc.setTimeStampID(1);
			pc.setName(c.getName());
			pc.setTurnoverTime(c.getTurnoverTime());
			pc.setUnitValue(c.getUnitValue());
			pc.setUnitPrice(c.getUnitPrice());
			pc.setFunction(Commodity.FUNCTION.function(c.getFunction()));
			pc.setOrigin(Commodity.ORIGIN.origin(c.getOrigin()));
			logger.debug("Stashing the commodity called {} ", pc.name());
			commodities.add(pc);
		}
		oneProject.setCommodities(commodities);

		// The industries
		for (EditableIndustry ind : EditorLoader.industryData) {
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
		for (EditableSocialClass sc : EditorLoader.socialClassData) {
			SocialClass psc = new SocialClass();
			psc.setProjectID(0);
			psc.setTimeStamp(1);
			psc.setName(sc.getName());
			psc.setparticipationRatio(sc.getParticipationRatio());
			psc.setRevenue(sc.getRevenue());
			psc.setSize(sc.getSize());
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

	/**
	 * Create and populate one persistent money Stock entity
	 * 
	 * @param owner
	 *            the owner of the stock
	 * @param actualQuantity
	 *            the actual quantity of this commodity in existence
	 * @param ownerType
	 *            the ownerType of this stock (CLASS, INDUSTRY)
	 * @return one persistent Stock entity
	 */
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

	/**
	 * Create and populate one persistent sales Stock entity
	 * 
	 * @param owner
	 *            the owner of the stock, which may be a class or an industry
	 * @param commodityName
	 *            the name of its commodity
	 * @param actualQuantity
	 *            the actual quantity of this commodity in existence
	 * @param ownerType
	 *            the type of the owner (CLASS or INDUSTRY)
	 * @return one persistent Stock entity
	 */
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

	/**
	 * Create and populate one persistent consumption Stock entity
	 * 
	 * @param owner
	 *            the owner of the stock(which will be a social class)
	 * @param commodityName
	 *            the name of its commodity
	 * @param desiredQuantity
	 *            the quantity of this commodity required, given the size of the class that owns it
	 * @param actualQuantity
	 *            the actual quantity of this commodity in existence
	 * @return one persistent Stock entity
	 */
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
	 * Create and populate one persistent production Stock entity
	 * 
	 * @param owner
	 *            the owner of the stock (which will be an industry)
	 * @param commodityName
	 *            the name of its commodity
	 * @param desiredQuantity
	 *            the quantity of this commodity required, given the output level
	 * @param actualQuantity
	 *            the actual quantity of this commodity in existence
	 * @return one persistent Stock entity
	 */
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

	/**
	 * @return the commodityData
	 */
	public static ObservableList<EditableCommodity> getCommodityData() {
		return commodityData;
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
