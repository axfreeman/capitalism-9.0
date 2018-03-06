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

package capitalism.model;

import java.io.Serializable;
import java.util.List;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.controller.Simulation;
import capitalism.utils.MathStuff;
import capitalism.utils.Reporter;
import capitalism.view.TabbedTableViewer;
import capitalism.view.ViewManager;
import capitalism.view.custom.DisplayControlsBox;
import capitalism.view.custom.TrackingControlsBox;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Commodity is the persistent class for the commodities database table. The embedded primary key is the associated class CommodityPK.
 * All members of the primary key can be accessed via getters and setters in this, the main Commodity class
 */
@Entity
@Table(name = "commodities")
@NamedQueries({
		@NamedQuery(name = "Primary", query = "SELECT u FROM Commodity u where u.pk.project= :project AND u.pk.timeStamp= :timeStamp and u.pk.name=:name"),
		@NamedQuery(name = "All", query = "SELECT u FROM Commodity u where u.pk.project= :project and u.pk.timeStamp = :timeStamp"),
		@NamedQuery(name = "Origin", query = "SELECT u FROM Commodity u where u.pk.project= :project and u.pk.timeStamp = :timeStamp and u.origin=:origin"),
		@NamedQuery(name = "Function", query = "SELECT u FROM Commodity u where u.pk.project= :project and u.pk.timeStamp = :timeStamp and u.function=:function order by u.displayOrder")
})
@Embeddable
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public class Commodity implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger("Commodity");

	// The primary key (composite key containing project, timeStamp and industryName)
	@XmlElement @EmbeddedId protected CommodityPK pk;

	@XmlElement @Column(name = "originType") private ORIGIN origin; // whether this is produced by an enterprise or a class
	@XmlElement @Column(name = "functionType") private FUNCTION function;// see enum FUNCTION for list of possible types
	@XmlElement @Column(name = "turnoverTime") private double turnoverTime;
	@XmlElement @Column(name = "unitValue") private double unitValue;
	@XmlElement @Column(name = "unitPrice") private double unitPrice;
	@XmlElement @Column(name = "surplusProduct") private double surplusProduct; // if after production there is an excess of inventory over use, it is recorded here
	@XmlElement @Column(name = "allocationShare") private double allocationShare;// proportion of total demand that can actually be supplied
	@XmlElement @Column(name = "stockUsedUp") private double stockUsedUp; // stock used up in production in the current period
	@XmlElement @Column(name = "stockProduced") private double stockProduced; // stock produced in the current period
	@XmlElement @Column(name = "imageName") private String imageName; // a graphical image that can be used in column headers in place of text
	@XmlElement @Column(name = "tooltip") private String toolTip;// an optional user-supplied description of the commodity
	@XmlElement @Column(name = "displayOrder") private int displayOrder; // used to determine which order to display columns

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
	private static TypedQuery<Commodity> commoditiesByOriginQuery;
	private static TypedQuery<Commodity> commoditiesByFunctionQuery;

	// initialise the entitManagers and queries statically once only, hopefully to reduce expensive requests for connections and query-building
	// TODO test with the EclipseLink profiler
	static {
		entityManager = entityManagerFactory.createEntityManager();
		commodityByPrimaryKeyQuery = entityManager.createNamedQuery("Primary", Commodity.class);
		commoditiesAllQuery = entityManager.createNamedQuery("All", Commodity.class);
		commoditiesByOriginQuery = entityManager.createNamedQuery("Origin", Commodity.class);
		commoditiesByFunctionQuery = entityManager.createNamedQuery("Function", Commodity.class);
	}

	// Enums
	/**
	 * Basic classification of commodity types: how are they used?
	 * 
	 */
	public enum FUNCTION {
		MONEY("Money"), PRODUCTIVE_INPUT("Productive Inputs"), CONSUMER_GOOD("Consumer Goods");
		String text;

		FUNCTION(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}
	};

	/**
	 * Second Basic classification of commodity types (overlaps FUNCTION_TYPE): how do they come into being?
	 *
	 */
	public enum ORIGIN {
		SOCIALlY_PRODUCED("Social"), INDUSTRIALLY_PRODUCED("Capitalist"), MONEY("Money");
		String text;

		ORIGIN(String text) {
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
		NAME("Commodity",null,TabbedTableViewer.HEADER_TOOL_TIPS.COMMODITY.text()), 
		PRODUCERTYPE("Producer Type",null,"Whether this commodity was produced by an industry or by a social classes. "), 
		TURNOVERTIME("Turnover Time","Turnover.png","The turnover time is the number of periods over which the commodity is used up"), 
		UNITVALUE("Unit Value","unitValueTransparent.png","The value per unit of this commodity"), 
		UNITPRICE("Unit Price","unitPrice.png","The price per unit of this commodity"), 
		TOTALSUPPLY("Supply","supply.png","The total supply of this commodity that is availlable for sale"), 
		TOTALQUANTITY("Quantity","Quantity.png","The total quantity of this commodity that is available for sale"), 
		REPLENISHMENT_DEMAND("Replenishment","demand.png","The quantity, value or price of this commodity required for all industries that use it to continue functioning at their current level"), 
		EXPANSION_DEMAND("Expansion","expansiondemand.png","The quantity, value or price of this commodity that the industries which use it were given funds to purchase by the end of the last period"), 
		SURPLUS("Surplus","surplus.png","The quantity of this commodity that was produced in excess of what was consumed"), 
		TOTALVALUE("Total Value","Value.png","The total value of this commodity in existence. Should be equal to quantity X unit value"), 
		TOTALPRICE("Total Price","price.png","The total price of this commodity in existence. Should be equal to quantity X unit price"), 
		ALLOCATIONSHARE("Share","Allocation.png","The proportion of replenishment demand that can be satisfied from existing supply"), 
		FUNCTION_TYPE("Commodity Type",null,"Whether this is a productive input,a consumption good, or money"), 
		INITIALCAPITAL("Initial Capital","capital  2.png",TabbedTableViewer.HEADER_TOOL_TIPS.INITIALCAPITAL.text()),
		INITIALPRODUCTIVECAPITAL("Productive Capital","capital  2.png",TabbedTableViewer.HEADER_TOOL_TIPS.PRODUCTIVECAPITAL.text()),
		CURRENTCAPITAL("Capital","capital 1.png",TabbedTableViewer.HEADER_TOOL_TIPS.CAPITAL.text()),
		PROFIT("Profit","profit.png",TabbedTableViewer.HEADER_TOOL_TIPS.PROFIT.text()), 
		PROFITRATE("Profit Rate","profitRate.png" ,TabbedTableViewer.HEADER_TOOL_TIPS.PROFITRATE.text());
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

	/**
	 * Controls whether value properties are reported as intrinsic or extrinsic
	 * This is managed distinctly from whether the value magnitude is returned to the caller as intrinsic or extrinsic
	 * It's really only for display purposes and not any other
	 */

	private enum ATTRIBUTE {
		VALUE, PRICE, UNIT_VALUE, UNIT_PRICE, INITIAL_CAPITAL, INITIAL_PRODUCTIVE_CAPITAL, CURRENT_CAPITAL, PROFIT;
	}

	public Commodity() {
		this.pk = new CommodityPK();
	}

	/**
	 * make a carbon copy of the commodity in the template entity with a new primary key record
	 * 
	 * @param template
	 *            the commodity to copy - usually the one from the previous timeStamp
	 */
	public Commodity(Commodity template) {
		this.pk = new CommodityPK();
		this.pk.timeStamp = template.pk.timeStamp;
		this.pk.name = template.pk.name;
		this.pk.project = template.pk.project;
		this.origin = template.origin;
		this.turnoverTime = template.turnoverTime;
		this.unitValue = template.unitValue;
		this.unitPrice = template.unitPrice;
		this.surplusProduct = template.surplusProduct;
		this.allocationShare = template.allocationShare;
		this.function = template.function;
		this.stockUsedUp = template.stockUsedUp;
		this.stockProduced = template.stockProduced;
		this.imageName = template.imageName;
		this.displayOrder = template.displayOrder;
	}

	/**
	 * an observable list of type Commodity for display by ViewManager, at the current project and timeStampDisplayCursor. timeStampDisplayCursor, which
	 * may diverge from timeStamp, identifies the row that the user last clicked on.
	 * 
	 * @return a list of Observable Commodities for the current project and timeStamp
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
		case PRODUCERTYPE:
			return new ReadOnlyStringWrapper(origin.text());
		case UNITPRICE:
			return new ReadOnlyStringWrapper(String.format(ViewManager.getSmallFormat(), expressionOf(ATTRIBUTE.UNIT_PRICE)));
		case UNITVALUE:
			return new ReadOnlyStringWrapper(String.format(ViewManager.getSmallFormat(), expressionOf(ATTRIBUTE.UNIT_VALUE)));
		case TOTALVALUE:
			return new ReadOnlyStringWrapper(String.format(ViewManager.getLargeFormat(), expressionOf(ATTRIBUTE.VALUE)));
		case TOTALPRICE:
			return new ReadOnlyStringWrapper(String.format(ViewManager.getLargeFormat(), expressionOf(ATTRIBUTE.PRICE)));
		case TOTALQUANTITY:
			return new ReadOnlyStringWrapper(String.format(ViewManager.getLargeFormat(), totalQuantity()));
		case TOTALSUPPLY:
			return new ReadOnlyStringWrapper(String.format(ViewManager.getLargeFormat(), totalSupply()));
		case REPLENISHMENT_DEMAND:
			return new ReadOnlyStringWrapper(String.format(ViewManager.getLargeFormat(), replenishmentDemand()));
		case EXPANSION_DEMAND:
			return new ReadOnlyStringWrapper(String.format(ViewManager.getLargeFormat(), expansionDemand()));
		case SURPLUS:
			return new ReadOnlyStringWrapper(String.format(ViewManager.getLargeFormat(), surplusProduct));
		case TURNOVERTIME:
			return new ReadOnlyStringWrapper(String.format(ViewManager.getSmallFormat(), turnoverTime));
		case ALLOCATIONSHARE:
			return new ReadOnlyStringWrapper(String.format(ViewManager.getSmallFormat(), allocationShare));
		case FUNCTION_TYPE:
			return new ReadOnlyStringWrapper(function.text);
		case INITIALCAPITAL:
			return new ReadOnlyStringWrapper(String.format(ViewManager.getLargeFormat(), expressionOf(ATTRIBUTE.INITIAL_CAPITAL)));
		case INITIALPRODUCTIVECAPITAL:
			return new ReadOnlyStringWrapper(String.format(ViewManager.getLargeFormat(), expressionOf(ATTRIBUTE.INITIAL_PRODUCTIVE_CAPITAL)));
		case CURRENTCAPITAL:
			return new ReadOnlyStringWrapper(String.format(ViewManager.getLargeFormat(), expressionOf(ATTRIBUTE.CURRENT_CAPITAL)));
		case PROFIT:
			return new ReadOnlyStringWrapper(String.format(ViewManager.getLargeFormat(), expressionOf(ATTRIBUTE.PROFIT)));
		case PROFITRATE:
			return new ReadOnlyStringWrapper(String.format(ViewManager.getSmallFormat(), profitRate()));
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
		case PRODUCERTYPE:
			return false;
		case UNITPRICE:
			return unitPrice != comparator.getUnitPrice();// no need to convert to intrinsic/extrinsic. The result will be the same
		case UNITVALUE:
			return unitValue != comparator.getUnitValue();// no need to convert to intrinsic/extrinsic. The result will be the same
		case TOTALVALUE:
			return expressionOf(ATTRIBUTE.VALUE) != expressionOf(ATTRIBUTE.VALUE);
		case TOTALPRICE:
			return expressionOf(ATTRIBUTE.PRICE) != expressionOf(ATTRIBUTE.PRICE);
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
			return initialCapital() != comparator.initialCapital();// no need to convert to intrinsic/extrinsic. The result will be the same
		case CURRENTCAPITAL:
			return currentCapital() != comparator.currentCapital();// no need to convert to intrinsic/extrinsic. The result will be the same
		case PROFIT:
			return profit() != comparator.profit();// no need to convert to intrinsic/extrinsic. The result will be the same
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
		case PRODUCERTYPE:
			return item;
		case UNITPRICE:
			return String.format(ViewManager.getSmallFormat(), (expressionOf(ATTRIBUTE.UNIT_PRICE) - comparator.expressionOf(ATTRIBUTE.UNIT_PRICE)));
		case UNITVALUE:
			return String.format(ViewManager.getSmallFormat(), (expressionOf(ATTRIBUTE.UNIT_VALUE) - comparator.expressionOf(ATTRIBUTE.UNIT_VALUE)));
		case TOTALVALUE:
			return String.format(ViewManager.getLargeFormat(), (expressionOf(ATTRIBUTE.VALUE) - comparator.expressionOf(ATTRIBUTE.VALUE)));
		case TOTALPRICE:
			return String.format(ViewManager.getLargeFormat(), (expressionOf(ATTRIBUTE.PRICE) - comparator.expressionOf(ATTRIBUTE.PRICE)));
		case TOTALQUANTITY:
			return String.format(ViewManager.getLargeFormat(), (totalQuantity() - comparator.totalQuantity()));
		case TOTALSUPPLY:
			return String.format(ViewManager.getLargeFormat(), (totalSupply() - comparator.totalSupply()));
		case REPLENISHMENT_DEMAND:
			return String.format(ViewManager.getLargeFormat(), (replenishmentDemand() - comparator.replenishmentDemand()));
		case SURPLUS:
			return String.format(ViewManager.getLargeFormat(), (surplusProduct - comparator.surplusProduct));
		case TURNOVERTIME:
			return String.format(ViewManager.getSmallFormat(), (turnoverTime - comparator.getTurnoverTime()));
		case ALLOCATIONSHARE:
			return String.format(ViewManager.getSmallFormat(), (allocationShare - comparator.allocationShare));
		case PROFIT:
			return String.format(ViewManager.getLargeFormat(), ((expressionOf(ATTRIBUTE.PROFIT) - comparator.expressionOf(ATTRIBUTE.PROFIT))));
		case INITIALCAPITAL:
			return String.format(ViewManager.getLargeFormat(),
					((expressionOf(ATTRIBUTE.INITIAL_CAPITAL) - comparator.expressionOf(ATTRIBUTE.INITIAL_CAPITAL))));
		case CURRENTCAPITAL:
			return String.format(ViewManager.getLargeFormat(),
					((expressionOf(ATTRIBUTE.CURRENT_CAPITAL) - comparator.expressionOf(ATTRIBUTE.CURRENT_CAPITAL))));
		case PROFITRATE:
			return String.format(ViewManager.getLargeFormat(), (profitRate() - comparator.profitRate()));
		default:
			return item;
		}
	}

	/**
	 * chooses the comparator depending on the state set in the {@code ViewManager.comparatorToggle} radio buttons
	 */

	private void chooseComparison() {
		switch (TrackingControlsBox.getComparatorState()) {
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
	 * generic selector which returns a numerical property of the commodity depending on the calling valueProperty.
	 * used exclusively in displaying the magnitudes involved, though I haven't worked out how to stop it
	 * being used for something else.
	 * 
	 * @param valueProperty
	 *            selects the property of this commodity that we wish to display (VALUE, PRICE, UNIT_VALUE,UNIT_Price)
	 * @return the value property, either as an intrinsic or a monetary magnitude depending on
	 *         {@link DisplayControlsBox#expressionDisplay} and {@link DisplayControlsBox#expressionDisplay}
	 */
	public double expressionOf(ATTRIBUTE valueProperty) {
		Global global = Global.getGlobal();
		double melt = global.getMelt();
		double expression;
		switch (valueProperty) {
		case VALUE:
			expression = totalValue();
			break;
		case PRICE:
			expression = totalPrice();
			break;
		case UNIT_VALUE:
			expression = unitValue;
			break;
		case UNIT_PRICE:
			expression = unitPrice;
			break;
		case CURRENT_CAPITAL:
			expression = currentCapital();
			break;
		case INITIAL_CAPITAL:
			expression = initialCapital();
			break;
		case INITIAL_PRODUCTIVE_CAPITAL:
			expression = initialProductiveCapital();
			break;
		case PROFIT:
			expression = profit();
			break;
		default:
			return Double.NaN;
		}
		if (DisplayControlsBox.expressionDisplay == DisplayControlsBox.DISPLAY_AS_EXPRESSION.MONEY) {
			return expression;
		} else {
			return (expression==0)?0:expression / melt;
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
		double quantity = 0;
		double value = 0;
		double price = 0;
		for (Stock s : Stock.comoditiesCalled(this.pk.timeStamp, pk.name)) {
			quantity += s.getQuantity();
			value += s.getValue();
			price += s.getPrice();
			logger.debug(String.format("  Stock of type [%s] with name [%s] has added quantity %.2f; value %.2f, and price %.2f. ",
					s.getStockType(), s.getOwner(), s.getQuantity(), s.getPrice(), s.getValue()));
		}
		quantity = MathStuff.round(quantity);
		value = MathStuff.round(value);
		price = MathStuff.round(price);
		Reporter.report(logger, 2, "  Total quantity of the commodity [%s] is %.2f (value %.2f, price %.2f). ",
				pk.name, quantity, price, value);
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

	// aggregators
	// TODO get aggregator queries working

	/**
	 * nothing more than a testbed so far here
	 * 
	 * @return stockUsedUp
	 */
	public double stockUsedUp() {
		Reporter.report(logger, 1, "Starting Computation");
		Query query = Commodity.getEntityManager().createQuery("Select SUM(u.stockUsedUp) from Commodity u");
		double stockUsed = (double) query.getSingleResult();
		Reporter.report(logger, 0, "Computed stock used up was %.4f", stockUsed);
		return stockUsed;
	}

	/**
	 * @return the total value of this use value in the economy at this time
	 */

	public double totalValue() {
		double totalValue = 0;
		for (Stock s : Stock.comoditiesCalled(pk.timeStamp, pk.name)) {
			totalValue += s.getValue();
		}
		return totalValue;
	}

	/**
	 * @return the total price of this use value in the economy at this time
	 */

	public double totalPrice() {
		double totalPrice = 0;
		for (Stock s : Stock.comoditiesCalled(pk.timeStamp, pk.name)) {
			totalPrice += s.getPrice();
		}
		return totalPrice;
	}

	/**
	 * @return the total quantity of this use value in the economy at this time
	 */

	public double totalQuantity() {
		double totalQuantity = 0;
		for (Stock s : Stock.comoditiesCalled(pk.timeStamp, pk.name)) {
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
	 * @return the total capital invested in producing this commodity
	 */
	public double initialCapital() {
		double capital = 0;
		for (Industry c : industries()) {
			capital += c.initialCapital();
		}
		return capital;
	}

	/**
	 * Returns the initial productive capital invested in producing this commodity.
	 * This is equal to the initialCapital less the money stocks of the industry.
	 * It is chiefly used in the pricing stage, if the user opts to ignore money as a store of value
	 * 
	 * @return the initial productive capital invested in producing this commodity
	 */
	public double initialProductiveCapital() {
		double productiveCapital = 0.0;
		for (Industry c : industries()) {
			productiveCapital += c.productiveCapital();
		}
		return productiveCapital;
	}

	/**
	 * The total supply of this commodity from all sales stocks of it
	 * 
	 * @return the total supply of this commmodity
	 */
	public double totalSupply() {
		double supply = 0.0;
		for (Stock s : Stock.salesByCommodity(pk.timeStamp, pk.name)) {
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
		for (Stock s : Stock.comoditiesCalled(pk.timeStamp, pk.name)) {
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
		for (Stock s : Stock.comoditiesCalled(pk.timeStamp, pk.name)) {
			demand += s.getExpansionDemand();
		}
		return demand;
	}

	/**
	 * @return the current capital of all the industries that produce this commodity
	 */

	public double currentCapital() {
		double currentCapital = 0.0;
		for (Industry c : industries()) {
			currentCapital += c.currentCapital();
		}
		return currentCapital;
	}

	// QUERIES

	/**
	 * @return a list of industries that produce this commodity
	 */
	public List<Industry> industries() {
		return Industry.industriesByCommodityName(pk.timeStamp, pk.name);
	}

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
	 * a list of all commodities at the current project and the given timeStamp
	 * 
	 * @param timeStamp
	 *            the timeStamp of the commodities returned
	 * @return a list of all commodities at the given timeStamp and the current project
	 */
	public static List<Commodity> commoditiesAll(int timeStamp) {
		commoditiesAllQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp);
		return commoditiesAllQuery.getResultList();
	}

	/**
	 * a list of all commodities of the given origin at the current timeStamp and project
	 * 
	 * @param origin
	 *            the origin type of the Commodity (SOCIALLY_PRODUCED, INDUSTRIALLY_PRODUCED)
	 * @return a list of industries of the specified origin type, at the latest timeStamp that has been persisted.
	 * 
	 */
	public static List<Commodity> commoditiesByOrigin(Commodity.ORIGIN origin) {
		commoditiesByOriginQuery.setParameter("project", Simulation.projectCurrent);
		commoditiesByOriginQuery.setParameter("timeStamp", Simulation.timeStampIDCurrent);
		commoditiesByOriginQuery.setParameter("origin", origin);
		return commoditiesByOriginQuery.getResultList();
	}

	/**
	 * a list of all use values of the given function at the current timeStamp and project
	 * 
	 * @param function
	 *            the function type of the use value (PRODUCTIVE INPUT, CONSUMER GOOD, MONEY)
	 * @return a list all use values with the given function at the current timeStamp and project
	 */
	public static List<Commodity> commoditiesByFunction(Commodity.FUNCTION function) {
		commoditiesByFunctionQuery.setParameter("timeStamp", Simulation.timeStampIDCurrent).setParameter("project", Simulation.projectCurrent);
		commoditiesByFunctionQuery.setParameter("function", function);
		return commoditiesByFunctionQuery.getResultList();
	}

	/**
	 * return a single commodity of origin type SOCIALLY_PRODUCED, which will be labour power
	 * TODO but not immediately; there could conceivably be more than one such
	 * 
	 * @return the single use value of origin type SOCIALLY_PRODUCED, which will be labour poweer
	 */
	public static Commodity labourPower() {
		commoditiesByOriginQuery.setParameter("project", Simulation.projectCurrent);
		commoditiesByOriginQuery.setParameter("timeStamp", Simulation.timeStampIDCurrent);
		commoditiesByOriginQuery.setParameter("origin", Commodity.ORIGIN.SOCIALlY_PRODUCED);
		try {
			return commoditiesByOriginQuery.getSingleResult();
		} catch (NoResultException r) {
			return null;
		}
	}

	/**
	 * Set the comparators for the current commodities at the given timeStamp and for the current project
	 * 
	 * @param timeStampID
	 *            the timeStamp of the commodities
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
	 * 
	 * @return the function of this commodity, as given by the {@code FUNCTION_TYPE} enum
	 */

	public FUNCTION getFunction() {
		return function;
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
		this.unitPrice = MathStuff.round(unitPrice);
	}

	public double getUnitValue() {
		return this.unitValue;
	}

	public void setUnitValue(double unitValue) {
		this.unitValue = MathStuff.round(unitValue);
	}

	public ORIGIN getOrigin() {
		return this.origin;
	}

	public void setOrigin(ORIGIN origin) {
		this.origin = origin;
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

	/**
	 * @return the toolTip
	 */
	public String getToolTip() {
		return toolTip;
	}

	/**
	 * @param toolTip
	 *            the toolTip to set
	 */
	public void setToolTip(String toolTip) {
		this.toolTip = toolTip;
	}
}
