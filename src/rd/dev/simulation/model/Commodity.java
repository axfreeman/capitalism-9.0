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

package rd.dev.simulation.model;

import java.io.Serializable;
import java.util.List;
import javax.persistence.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import rd.dev.simulation.Simulation;
import rd.dev.simulation.utils.MathStuff;
import rd.dev.simulation.utils.Reporter;
import rd.dev.simulation.view.ViewManager;

/**
 * Commodity is the persistent class for the commodities database table. The embedded primary key is the associated class CommodityPK. 
 * All members of the primary key can be accessed via getters and setters in this, the main Commodity class
 */
@Entity
@Table(name = "commodities")
@NamedQueries({
		@NamedQuery(name = "Primary", query = "SELECT u FROM Commodity u where u.pk.project= :project AND u.pk.timeStamp= :timeStamp and u.pk.name=:name"),
		@NamedQuery(name = "All", query = "SELECT u FROM Commodity u where u.pk.project= :project and u.pk.timeStamp = :timeStamp"),
		@NamedQuery(name = "CommodityOriginType", query = "SELECT u FROM Commodity u where u.pk.project= :project and u.pk.timeStamp = :timeStamp and u.commodityOriginType=:commodityOriginType"),
		@NamedQuery(name = "CommodityFunctionType", query = "SELECT u FROM Commodity u where u.pk.project= :project and u.pk.timeStamp = :timeStamp and u.commodityFunctionType=:commodityFunctionType order by u.displayOrder")
})
@Embeddable
public class Commodity implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger("Commodity");

	// The primary key (composite key containing project, timeStamp and industryName)
	@EmbeddedId protected CommodityPK pk;

	@Column(name = "originType") private ORIGIN_TYPE commodityOriginType; // whether this is produced by an enterprise or a class
	@Column(name = "functionType") private FUNCTION_TYPE commodityFunctionType;// see enum FUNCTION_TYPE for list of possible types
	@Column(name = "turnoverTime") private double turnoverTime;
	@Column(name = "unitValue") private double unitValue;
	@Column(name = "unitPrice") private double unitPrice;
	@Column(name = "surplusProduct") private double surplusProduct; // if after production there is an excess of inventory over use, it is recorded here
	@Column(name = "allocationShare") private double allocationShare;// proportion of total demand that can actually be supplied
	@Column(name = "stockUsedUp") private double stockUsedUp; // stock used up in production in the current period
	@Column(name = "stockProduced") private double stockProduced; // stock produced in the current period
	@Column(name = "imageName") private String imageName; // a graphical image that can be used in column headers in place of text
	@Column(name = "displayOrder") private int displayOrder; // used to determine which order to display columns

	// Comparators
	@Transient private Commodity comparator;
	@Transient private Commodity previousComparator;
	@Transient private Commodity startComparator;
	@Transient private Commodity customComparator;
	@Transient private Commodity endComparator;

	// Data Management fields
	private static EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("DB_COMMODITIES");
	private static EntityManager entityManager;
	private static TypedQuery<Commodity> commodityByPrimaryKeyQuery;
	private static TypedQuery<Commodity> commoditiesAllQuery;
	private static TypedQuery<Commodity> commoditiesByOriginTypeQuery;
	private static TypedQuery<Commodity> commoditiesByFunctionQuery;

	static {
		entityManager = entityManagerFactory.createEntityManager();
		commodityByPrimaryKeyQuery = entityManager.createNamedQuery("Primary", Commodity.class);
		commoditiesAllQuery = entityManager.createNamedQuery("All", Commodity.class);
		commoditiesByOriginTypeQuery = entityManager.createNamedQuery("CommodityOriginType", Commodity.class);
		commoditiesByFunctionQuery = entityManager.createNamedQuery("CommodityFunctionType", Commodity.class);
	}
	
	//Enums
	/**
	 * Basic classification of commodity types: how are they used?
	 * 
	 */
	public enum FUNCTION_TYPE {
		MONEY("Money"), PRODUCTIVE_INPUT("Productive Inputs"), CONSUMER_GOOD("Consumer Goods");
		String text;

		FUNCTION_TYPE(String text) {
			this.text = text;
		}

		/**
		 * @return the text associated with this type - normally, so it can be displayed for the user
		 */

		public String getText() {
			return text;
		}
	};

	/**
	 * Second Basic classification of commodity types (overlaps FUNCTION_TYPE): how do they come into being?
	 *
	 */
	public enum ORIGIN_TYPE {
		SOCIALlY_PRODUCED("Social"), INDUSTRIALLY_PRODUCED("Capitalist"), MONEY("Money");
		String text;
		ORIGIN_TYPE(String text) {
			this.text = text;
		}
		public String text() {
			return text;
		}
	}

	/**
	 * Readable constants to refer to the methods which provide information about the persistent members of the class
	 */
	public enum SELECTOR {
		// @formatter:off
		NAME("Commodity",null,null), 
		OWNERTYPE("Owner Type",null,null), 
		TURNOVERTIME("Turnover Time","Turnover.png",null), 
		UNITVALUE("Unit Value","unitValueTransparent.png",null), 
		UNITPRICE("Unit Price","unitPrice.png",null), 
		TOTALSUPPLY("Supply","supply.png",null), 
		TOTALQUANTITY("Quantity","Quantity.png",null), 
		REPLENISHMENT_DEMAND("Replenishment","demand.png",null), 
		EXPANSION_DEMAND("Expansion","expansiondemand.png",null), 
		SURPLUS("Surplus","surplus.png",null), 
		TOTALVALUE("Total Value","Value.png",null), 
		TOTALPRICE("Total Price","price.png",null), 
		ALLOCATIONSHARE("Share","Allocation.png",null), 
		COMMODITY_FUNCTION_TYPE("Commodity Type",null,null), 
		INITIALCAPITAL("Initial Capital","capital  2.png",null), 
		PROFIT("Profit","profit.png",null), 
		PROFITRATE("Profit Rate","profitRate.png" ,null);
		// @formatter:on
		String text;
		String imageName;
		String toolTip;

		SELECTOR(String text, String imageName, String toolTip) {
			this.text = text;
			this.imageName = imageName;
			this.toolTip = toolTip;
		}
		public String text() {
			return text;
		}
		public String imageName() {
			return imageName;
		}
		public String tooltip() {
			return toolTip;
		}
	}
	
	public Commodity() {
		this.pk=new CommodityPK();
	}

	/**
	 * make a carbon copy of the commodity in the template entity with a new primary key record
	 * 
	 * @param template
	 *            the commodity to copy - usually the one from the previous timeStamp
	 */
	public Commodity(Commodity template) {
		this.pk=new CommodityPK();
		this.pk.timeStamp = template.pk.timeStamp;
		this.pk.name = template.pk.name;
		this.pk.project = template.pk.project;
		this.commodityOriginType = template.commodityOriginType;
		this.turnoverTime = template.turnoverTime;
		this.unitValue = template.unitValue;
		this.unitPrice = template.unitPrice;
		this.surplusProduct = template.surplusProduct;
		this.allocationShare = template.allocationShare;
		this.commodityFunctionType = template.commodityFunctionType;
		this.stockUsedUp = template.stockUsedUp;
		this.stockProduced = template.stockProduced;
		this.imageName = template.imageName;
		this.displayOrder = template.displayOrder;
	}
	
	/**
	 * an observable list of type Commodity for display by ViewManager, at the current project and timeStampDisplayCursor. timeStampDisplayCursor, which
	 * may diverge from timeStamp, identifies the row that the user last clicked on.
	 * 
	 * @return a list of Observable UseValues for the current project and timeStamp
	 */

	public static ObservableList<Commodity> commoditiesObservable() {
		Commodity.commoditiesAllQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp",
				Simulation.timeStampDisplayCursor);
		ObservableList<Commodity> result = FXCollections.observableArrayList();
		for (Commodity u : Commodity.commoditiesAllQuery.getResultList()) {
			result.add(u);
		}
		return result;
	}
	
	/**
	 * provides a wrapped version of the selected member which the display will recognise, as a ReadOnlyStringWrapper.
	 * 
	 * We don't mind the hardwiring because we don't really intend this code to be re-usable, it's not hard to modify, and it results in compact
	 * and readable usage code (see (@link TabbedTableViewer#populateCommoditiesViewTable})
	 * 
	 * @param SELECTOR
	 *            chooses which member to evaluate
	 * @return a String representation of the members, formatted according to the relevant format string
	 */

	public ReadOnlyStringWrapper wrappedString(SELECTOR SELECTOR) {
		switch (SELECTOR) {
		case NAME:
			return new ReadOnlyStringWrapper(pk.name);
		case OWNERTYPE:
			return new ReadOnlyStringWrapper(commodityOriginType.text());
		case UNITPRICE:
			return new ReadOnlyStringWrapper(String.format(ViewManager.smallNumbersFormatString, unitPrice));
		case UNITVALUE:
			return new ReadOnlyStringWrapper(String.format(ViewManager.smallNumbersFormatString, unitValue));
		case TOTALVALUE:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, totalValue()));
		case TOTALPRICE:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, totalPrice()));
		case TOTALQUANTITY:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, totalQuantity()));
		case TOTALSUPPLY:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, totalSupply()));
		case REPLENISHMENT_DEMAND:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, replenishmentDemand()));
		case EXPANSION_DEMAND:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, expansionDemand()));
		case SURPLUS:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, surplusProduct));
		case TURNOVERTIME:
			return new ReadOnlyStringWrapper(String.format(ViewManager.smallNumbersFormatString, turnoverTime));
		case ALLOCATIONSHARE:
			return new ReadOnlyStringWrapper(String.format(ViewManager.smallNumbersFormatString, allocationShare));
		case COMMODITY_FUNCTION_TYPE:
			return new ReadOnlyStringWrapper(commodityFunctionType.text);
		case INITIALCAPITAL:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, initialCapital()));
		case PROFIT:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, profit()));
		case PROFITRATE:
			return new ReadOnlyStringWrapper(String.format(ViewManager.smallNumbersFormatString, profitRate()));
		default:
			return null;
		}
	}

	/**
	 * informs the display whether the selected member of this entity has changed, compared with the 'comparator' Commodity which normally
	 * comes from a different timeStamp.
	 * 
	 * We don't mind the hardwiring because we don't really intend this code to be re-usable, it's not hard to modify, and it results in compact
	 * and readable usage code (see (@link TabbedTableViewer#populateCommoditiesViewTable})
	 * 
	 * @param sELECTOR
	 *            chooses which member to evaluate
	 * @return whether this member has changed or not. False if selector is unavailable here
	 */

	public boolean changed(SELECTOR sELECTOR) {
		chooseComparison();
		switch (sELECTOR) {
		case NAME:
			return false;
		case OWNERTYPE:
			return false;
		case UNITPRICE:
			return unitPrice != comparator.getUnitPrice();
		case UNITVALUE:
			return unitValue != comparator.getUnitValue();
		case TOTALVALUE:
			return totalValue() != comparator.totalValue();
		case TOTALPRICE:
			return totalPrice() != comparator.totalPrice();
		case TOTALQUANTITY:
			return totalQuantity() != comparator.totalQuantity();
		case TOTALSUPPLY:
			return totalSupply() != comparator.totalSupply();
		case REPLENISHMENT_DEMAND:
			return replenishmentDemand() != comparator.replenishmentDemand();
		case SURPLUS:
			return surplusProduct != comparator.surplusProduct;
		case TURNOVERTIME:
			return turnoverTime != comparator.getTurnoverTime();
		case ALLOCATIONSHARE:
			return allocationShare != comparator.allocationShare;
		case INITIALCAPITAL:
			return initialCapital() != comparator.initialCapital();
		case PROFIT:
			return profit() != comparator.profit();
		case PROFITRATE:
			return profitRate() != comparator.profitRate();
		default:
			return false;
		}
	}

	/**
	 * If the selected field has changed, return the difference between the current value and the former value
	 * 
	 * @param selector
	 *            chooses which field to evaluate
	 * 
	 * @param item
	 *            the original item - returned as the result if there is no change
	 * 
	 * @return the original item if nothing has changed, otherwise the change, as an appropriately formatted string
	 */

	public String showDelta(String item, SELECTOR selector) {
		chooseComparison();
		if (!changed(selector))
			return item;
		switch (selector) {
		case NAME:
		case OWNERTYPE:
			return item;
		case UNITPRICE:
			return String.format(ViewManager.smallNumbersFormatString, (unitPrice - comparator.unitPrice));
		case UNITVALUE:
			return String.format(ViewManager.smallNumbersFormatString, (unitValue - comparator.unitValue));
		case TOTALVALUE:
			return String.format(ViewManager.largeNumbersFormatString, (totalValue() - comparator.totalValue()));
		case TOTALPRICE:
			return String.format(ViewManager.largeNumbersFormatString, (totalPrice() - comparator.totalPrice()));
		case TOTALQUANTITY:
			return String.format(ViewManager.largeNumbersFormatString, (totalQuantity() - comparator.totalQuantity()));
		case TOTALSUPPLY:
			return String.format(ViewManager.largeNumbersFormatString, (totalSupply() - comparator.totalSupply()));
		case REPLENISHMENT_DEMAND:
			return String.format(ViewManager.largeNumbersFormatString, (replenishmentDemand() - comparator.replenishmentDemand()));
		case SURPLUS:
			return String.format(ViewManager.largeNumbersFormatString, (surplusProduct - comparator.surplusProduct));
		case TURNOVERTIME:
			return String.format(ViewManager.smallNumbersFormatString, (turnoverTime - comparator.getTurnoverTime()));
		case ALLOCATIONSHARE:
			return String.format(ViewManager.smallNumbersFormatString, (allocationShare - comparator.allocationShare));
		case PROFIT:
			return String.format(ViewManager.largeNumbersFormatString, (profit() - comparator.profit()));
		case PROFITRATE:
			return String.format(ViewManager.largeNumbersFormatString, (profitRate() - comparator.profitRate()));
		default:
			return item;
		}
	}

	/**
	 * chooses the comparator depending on the state set in the {@code ViewManager.comparatorToggle} radio buttons
	 */

	private void chooseComparison() {
		switch (ViewManager.getComparatorState()) {
		case CUSTOM:
			comparator = customComparator;
			break;
		case END:
			comparator = endComparator;
			break;
		case PREVIOUS:
			comparator = previousComparator;
			break;
		case START:
			comparator = startComparator;
		}
	}
	
	/**
	 * Calculate the total quantity, value and price of this commodity, from the stocks of it
	 * Validate against existing total if requested
	 * 
	 * @param validate
	 *            report if the result differs from what is already there.
	 */

	public void calculateAggregates(boolean validate) {
		double totalQuantity = 0;
		double totalValue = 0;
		double totalPrice = 0;
		for (Stock s : Stock.stocksByCommodity(this.pk.timeStamp, pk.name)) {
			totalQuantity += s.getQuantity();
			totalValue += s.getValue();
			totalPrice += s.getPrice();
			logger.debug(String.format("  Stock of type [%s] with name [%s] has added quantity %.2f; value %.2f, and price %.2f. ",
					s.getStockType(), s.getOwner(), s.getQuantity(), s.getPrice(), s.getValue()));
		}
		totalQuantity = MathStuff.round(totalQuantity);
		totalValue = MathStuff.round(totalValue);
		totalPrice = MathStuff.round(totalPrice);
		Reporter.report(logger, 2, "  Total quantity of the commodity [%s] is %.2f (value %.2f, price %.2f). ",
				pk.name, totalQuantity, totalPrice, totalValue);
	}

	/**
	 * @return a list of industries that produce this commodity
	 */
	public List<Industry> industries() {
		return Industry.industriesByCommodityName(pk.timeStamp, pk.name);
	}

	/**
	 * 
	 * sets a comparator use value, which comes from a different timestamp. This informs the 'change' method which
	 * communicates to the GUI interface so it knows to colour changed magnitudes differently.
	 * 
	 * @param comparator
	 * 
	 *            the comparator use value Bean
	 */
	public void setComparator(Commodity comparator) {
		this.comparator = comparator;
	}

	/**
	 * 
	 * @return the function of this commodity, as given by the {@code FUNCTION_TYPE} enum
	 */

	public FUNCTION_TYPE getCommodityFunctionType() {
		return commodityFunctionType;
	}

	public String commodityName() {
		return pk.name;
	}

	public int getTimeStamp() {
		return pk.timeStamp;
	}

	public int getProject() {
		return pk.project;
	}

	/**
	 * The total supply of this commodity from all sales stocks of it
	 * @return the total supply of this commmodity
	 */
	public double totalSupply() {
		double supply = 0.0;
		for (Stock s : Stock.stocksSalesByCommodity(pk.timeStamp, pk.name)) {
			supply += s.getQuantity();
		}
		return supply;
	}

	/**
	 * The total replenishment demand from all stocks of this commodity
	 * 
	 * @return replenishment demand from all stocks of this commodity
	 */

	public double replenishmentDemand() {
		double demand = 0.0;
		for (Stock s : Stock.stocksByCommodity(pk.timeStamp, pk.name)) {
			demand += s.getReplenishmentDemand();
		}
		return demand;
	}

	/**
	 * The total expansion demand from all stocks of this commodity
	 * 
	 * @return expansion demand from all stocks of this commodity
	 */

	public double expansionDemand() {
		double demand = 0.0;
		for (Stock s : Stock.stocksByCommodity(pk.timeStamp, pk.name)) {
			demand += s.getExpansionDemand();
		}
		return demand;
	}
	
	
	//QUERIES

	/**
	 * get the single commodity with the primary key given by all the parameters, including the timeStamp
	 * 
	 * @param project
	 *            the given project
	 * @param timeStamp
	 *            the timeStamp to report on
	 * @param name
	 *            the given commodity name
	 * @return the singlecommodity given by this primary key, null if it does not exist
	 */
	public static Commodity commodityByPrimaryKey(int project, int timeStamp, String name) {
		commodityByPrimaryKeyQuery.setParameter("project", project).setParameter("timeStamp", timeStamp).setParameter("name", name);
		try {
			return commodityByPrimaryKeyQuery.getSingleResult();
		} catch (NoResultException r) {
			return null;
		}
	}

	/**
	 * retrieve a Commodity by its name for the current project and a given timestamp
	 * 
	 * @param timeStamp
	 *            the given timeStamp
	 * 
	 * @param name
	 *            the name of the commodity
	 * @return the commodity called name, unless it doesn't exist, in which case null
	 */
	public static Commodity commodityByPrimaryKey(int timeStamp, String name) {
		commodityByPrimaryKeyQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp).setParameter("name",
				name);
		try {
			return commodityByPrimaryKeyQuery.getSingleResult();
		} catch (NoResultException r) {
			return null;
		}
	}

	/**
	 * a list of all commodities at the current project and timeStamp
	 * 
	 * 
	 * @return a list of all commodities at the current timeStamp and the current project
	 */
	public static List<Commodity> commoditiesAll() {
		commoditiesAllQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", Simulation.timeStampIDCurrent);
		return commoditiesAllQuery.getResultList();
	}

	/**
	 * a list of all commodities of the given Origintype at the current timeStamp and project
	 * 
	 * @param originType
	 *            the origin type of the Commodity (SOCIALLY_PRODUCED, INDUSTRIALLY_PRODUCED)
	 * @return a list of industries of the specified origin type, at the latest timeStamp that has been persisted.
	 * 
	 */
	public static List<Commodity> commoditiesByOriginType(Commodity.ORIGIN_TYPE originType) {
		commoditiesByOriginTypeQuery.setParameter("project", Simulation.projectCurrent);
		commoditiesByOriginTypeQuery.setParameter("timeStamp", Simulation.timeStampIDCurrent);
		commoditiesByOriginTypeQuery.setParameter("commodityOriginType", originType);
		return commoditiesByOriginTypeQuery.getResultList();
	}

	/**
	 * a list of all use values of the given commodityFunctionType at the current timeStamp and project
	 * 
	 * @param functionType
	 *            the function type of the use value (PRODUCTIVE INPUT, CONSUMER GOOD, MONEY)
	 * @return a list all use values of the given commodityFunctionType at the current timeStamp and project
	 */
	public static List<Commodity> commoditiesByFunction(Commodity.FUNCTION_TYPE functionType) {
		commoditiesByFunctionQuery.setParameter("timeStamp", Simulation.timeStampIDCurrent).setParameter("project", Simulation.projectCurrent);
		commoditiesByFunctionQuery.setParameter("commodityFunctionType", functionType);
		return commoditiesByFunctionQuery.getResultList();
	}

	/**
	 * return a single commodity of origin type SOCIALLY_PRODUCED, which will be labour power
	 * TODO but not immediately; there could conceivably be more than one such
	 * @return the single use value of origin type SOCIALLY_PRODUCED, which will be labour poweer
	 */
	public static Commodity labourPower() {
		commoditiesByOriginTypeQuery.setParameter("project", Simulation.projectCurrent);
		commoditiesByOriginTypeQuery.setParameter("timeStamp", Simulation.timeStampIDCurrent);
		commoditiesByOriginTypeQuery.setParameter("commodityOriginType", Commodity.ORIGIN_TYPE.SOCIALlY_PRODUCED);
		try {
			return commoditiesByOriginTypeQuery.getSingleResult();
		} catch (NoResultException r) {
			return null;
		}
	}
	
	/**
	 * Set the comparators for the current commodities at the given timeStamp and for the current project
	 * @param timeStampID the timeStamp of the commodities 
	 */
	public static void setComparators(int timeStampID) {
		commoditiesAllQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStampID);
		for (Commodity u : Commodity.commoditiesAllQuery.getResultList()) {
			u.setPreviousComparator(commodityByPrimaryKey(Simulation.projectCurrent, Simulation.getTimeStampComparatorCursor(), u.commodityName()));
			u.setStartComparator(commodityByPrimaryKey(Simulation.projectCurrent, 1, u.commodityName()));
			u.setEndComparator(commodityByPrimaryKey(Simulation.projectCurrent, Simulation.timeStampIDCurrent, u.commodityName()));
			u.setCustomComparator(commodityByPrimaryKey(Simulation.projectCurrent, Simulation.timeStampIDCurrent, u.commodityName()));
		}
	}
	

	/**
	 * @return the entityManager
	 */
	public static EntityManager getEntityManager() {
		return Commodity.entityManager;
	}
	
	public double getTurnoverTime() {
		return this.turnoverTime;
	}

	public void setTurnoverTime(double turnoverTime) {
		this.turnoverTime = turnoverTime;
	}

	public double getUnitPrice() {
		return this.unitPrice;
	}

	public void setUnitPrice(double unitPrice) {
		this.unitPrice = unitPrice;
	}

	public double getUnitValue() {
		return this.unitValue;
	}

	public void setUnitValue(double unitValue) {
		this.unitValue = unitValue;
	}

	public ORIGIN_TYPE getCommodityOriginType() {
		return this.commodityOriginType;
	}

	public void setCommodityOriginType(ORIGIN_TYPE originType) {
		this.commodityOriginType = originType;
	}

	public double getAllocationShare() {
		return allocationShare;
	}

	public void setAllocationShare(double allocationShare) {
		this.allocationShare = allocationShare;
	}

	public void setTimeStamp(int timeStamp) {
		pk.timeStamp = timeStamp;
	}

	/**
	 * @return the surplusProduct
	 */
	public double getSurplusProduct() {
		return surplusProduct;
	}

	/**
	 * @param surplus
	 *            the surplus to set
	 */
	public void setSurplusProduct(double surplus) {
		this.surplusProduct = surplus;
	}

	/**
	 * @return the total value of this use value in the economy at this time
	 */

	public double totalValue() {
		double totalValue = 0;
		for (Stock s : Stock.stocksByCommodity(pk.timeStamp, pk.name)) {
			totalValue += s.getValue();
		}
		return totalValue;
	}

	/**
	 * @return the total price of this use value in the economy at this time
	 */

	public double totalPrice() {
		double totalPrice = 0;
		for (Stock s : Stock.stocksByCommodity(pk.timeStamp, pk.name)) {
			totalPrice += s.getPrice();
		}
		return totalPrice;
	}

	/**
	 * @return the total quantity of this use value in the economy at this time
	 */

	public double totalQuantity() {
		double totalQuantity = 0;
		for (Stock s : Stock.stocksByCommodity(pk.timeStamp, pk.name)) {
			totalQuantity += s.getQuantity();
		}
		return totalQuantity;
	}

	/**
	 * @return total profit so far in the industries that produce this use value
	 */

	public double profit() {
		double profit = 0;
		for (Industry c : industries()) {
			profit += c.profit();
		}
		return profit;
	}

	/**
	 * @return the profit rate so far in the industries that produce this use value
	 */
	public double profitRate() {
		return profit() / initialCapital();
	}

	/**
	 * @return the total capital invested in producing this use value
	 */
	public double initialCapital() {
		double capital = 0;
		for (Industry c : industries()) {
			capital += c.getInitialCapital();
		}
		return capital;
	}

	/**
	 * @return the stockUsedUp
	 */
	public double getStockUsedUp() {
		return stockUsedUp;
	}

	/**
	 * @param stockUsedUp
	 *            the stockUsedUp to set
	 */
	public void setStockUsedUp(double stockUsedUp) {
		this.stockUsedUp = stockUsedUp;
	}

	/**
	 * @return the stockProduced
	 */
	public double getStockProduced() {
		return stockProduced;
	}

	/**
	 * @param stockProduced
	 *            the stockProduced to set
	 */
	public void setStockProduced(double stockProduced) {
		this.stockProduced = stockProduced;
	}

	/**
	 * @return the imageName
	 */
	public String getImageName() {
		return imageName;
	}

	/**
	 * @return the previousComparator
	 */
	public Commodity getPreviousComparator() {
		return previousComparator;
	}

	/**
	 * @param previousComparator
	 *            the previousComparator to set
	 */
	public void setPreviousComparator(Commodity previousComparator) {
		this.previousComparator = previousComparator;
	}

	/**
	 * @return the startComparator
	 */
	public Commodity getStartComparator() {
		return startComparator;
	}

	/**
	 * @param startComparator
	 *            the startComparator to set
	 */
	public void setStartComparator(Commodity startComparator) {
		this.startComparator = startComparator;
	}

	/**
	 * @return the customComparator
	 */
	public Commodity getCustomComparator() {
		return customComparator;
	}

	/**
	 * @param customComparator
	 *            the customComparator to set
	 */
	public void setCustomComparator(Commodity customComparator) {
		this.customComparator = customComparator;
	}

	/**
	 * @return the endComparator
	 */
	public Commodity getEndComparator() {
		return endComparator;
	}

	/**
	 * @param endComparator
	 *            the endComparator to set
	 */
	public void setEndComparator(Commodity endComparator) {
		this.endComparator = endComparator;
	}
}
