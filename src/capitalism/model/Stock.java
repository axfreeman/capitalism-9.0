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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.controller.Simulation;
import capitalism.utils.Dialogues;
import capitalism.utils.MathStuff;
import capitalism.utils.Reporter;
import capitalism.view.ViewManager;
import capitalism.view.custom.DisplayControlsBox;
import capitalism.view.custom.TrackingControlsBox;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * The persistent class for the stocks database table. It extends the Observable Class so it can provide base data for TableViews.
 * TODO The additional functionality provided by the Observable Class is not used, but it probably should be.
 */

@Entity
@Table(name = "stocks")
@NamedQueries({
		// select all stocks of a given project and timestamp, whose stockType is one of two specified types (used with Productive and Cnsumption to yield
		// sources of demand)
		@NamedQuery(name = "Demand", query = "SELECT s FROM Stock s where s.pk.projectID = :project and s.pk.timeStampID=:timeStamp "
				+ "and (s.pk.stockType = :stockType1 or s.pk.stockType=:stockType2)")
})
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "Stock")
public class Stock implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(Stock.class);

	@XmlElement @EmbeddedId protected StockPK pk;
	@XmlElement @Column(name = "ownertype") private OWNERTYPE ownerType;
	@XmlElement @Column(name = "quantity") private double quantity;
	@XmlElement @Column(name = "value") private double value;
	@XmlElement @Column(name = "price") private double price;
	@XmlElement @Column(name = "replenishmentDemand") private double replenishmentDemand;
	@XmlElement @Column(name = "expansionDemand") private double expansionDemand;

	// the proportion of this stock used up in producing one unit of output.
	// ONLY relevant if this is of stockType PRODUCTIVE_INPUT (in which case the owner will be an industry)
	@XmlElement @Column(name = "productionCoefficient") private double productionCoefficient;

	// the amount of this stock required to produce the designated output.
	// this is loaded at the start and used to calculate the productionCoefficient
	// like productionCoefficient, only relevant if this is of stockType PRODUCTIVE_INPUT
	@XmlElement @Column(name = "productionQuantity") private double productionQuantity;

	// the proportion of the revenue of a class that will be spent on this stock in one period.
	// ONLY relevant if this is of stockType CONSUMER_GOOD (in which case the owner will be a social class)
	@XmlElement @Column(name = "consumptionCoefficient") private double consumptionCoefficient;

	// the amount of this stock required to produce the designated output.
	// this is loaded at the start and used to calculate the consumptionCoefficient
	// like consumptionCoefficient, only relevant if this is of stockType CONSUMER_GOOD
	@XmlElement @Column(name = "consumptionQuantity") private double consumptionQuantity;

	// how much of this was used up in production or reproduction
	@XmlElement @Column(name = "stockUsedUp") private double stockUsedUp;

	// Comparators
	@Transient private Stock comparator;
	@Transient private Stock previousComparator;
	@Transient private Stock startComparator;
	@Transient private Stock customComparator;
	@Transient private Stock endComparator;

	// Data Management
	private static EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("DB_STOCKS");
	private static EntityManager entityManager;
	private static TypedQuery<Stock> primaryQuery;
	private static TypedQuery<Stock> allQuery;
	private static TypedQuery<Stock> allInProjectAndTimeStampQuery;
	private static TypedQuery<Stock> allInProjectQuery;
	private static TypedQuery<Stock> ofCommodityQuery;
	private static TypedQuery<Stock> ofCommodityAndTypeQuery;
	private static TypedQuery<Stock> withOwnerAndTypeQuery;
	private static TypedQuery<Stock> sourcesOfDemandQuery;
	private static TypedQuery<Stock> withStockTypeQuery;
	private static TypedQuery<Stock> productiveQuery;

	static {
		entityManager = entityManagerFactory.createEntityManager();
		primaryQuery = entityManager.createQuery(
				"SELECT s FROM Stock s WHERE s.pk.projectID=:project and s.pk.timeStampID =:timeStamp and s.pk.owner =:owner and s.pk.commodity= :commodity and s.pk.stockType=:stockType",
				Stock.class);
		allQuery=entityManager.createQuery("Select s from Stock s",Stock.class);
		allInProjectAndTimeStampQuery = entityManager.createQuery(
				"SELECT s FROM Stock s where s.pk.projectID= :project and s.pk.timeStampID = :timeStamp", Stock.class);
		allInProjectQuery = entityManager.createQuery(
				"SELECT s FROM Stock s where s.pk.projectID= :project", Stock.class);
		withStockTypeQuery = entityManager.createQuery(
				"SELECT s FROM Stock s where s.pk.projectID = :project and s.pk.timeStampID=:timeStamp and s.pk.stockType=:stockType",
				Stock.class);
		withOwnerAndTypeQuery = entityManager.createQuery(
				"SELECT s FROM Stock s where s.pk.projectID= :project and s.pk.timeStampID = :timeStamp and s.pk.owner= :owner and s.pk.stockType=:stockType",
				Stock.class);
		ofCommodityQuery = entityManager.createQuery(
				"SELECT s FROM Stock s where s.pk.projectID = :project and s.pk.timeStampID = :timeStamp and s.pk.commodity= :commodity",
				Stock.class);
		ofCommodityAndTypeQuery = entityManager.createQuery(
				"SELECT s FROM Stock s where s.pk.projectID = :project and s.pk.timeStampID = :timeStamp and s.pk.stockType=:stockType and s.pk.commodity= :commodity",
				Stock.class);
		sourcesOfDemandQuery = entityManager.createQuery(
				"SELECT s FROM Stock s where s.pk.projectID = :project and s.pk.timeStampID =:timeStamp and (s.pk.stockType = :stockType1 or s.pk.stockType=:stockType2)",
				Stock.class);
		productiveQuery = entityManager.createQuery(
				"Select s from Stock s where s.pk.projectID =:project and s.pk.stockType ='PRODUCTIVE'", Stock.class);
	}

	/**
	 * Readable constants to refer to the methods which provide information about the persistent members of the class
	 */
	public enum STOCK_ATTRIBUTE {
		// @formatter:off
		OWNER("Owner",null,null), 
		OWNERTYPE("Owner Type",null,null), 
		COMMODITY("Commodity Produced",null,null), 
		STOCKTYPE("Stock Type",null,null), 
		QUANTITY("Quantity",null,"quantity.png"), 
		PRODUCTION_COEFFICIENT("Coefficient",null,null), 
		CONSUMPTION_COEFFICIENT("Coefficient",null,null),
		REPLENISHMENTDEMAND("Demand",null,"demand.png"), 
		EXPANSIONDEMAND("Expansion",null,"expansiondemand.png"),
		VALUE("Value",null,"value.png"), 
		PRICE("Price",null,"price.png");
		// @formatter:on
		String text;
		String imageName;
		String toolTip;

		STOCK_ATTRIBUTE(String text, String imageName, String toolTip) {
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
	 * 
	 * SQL type ENUM ('PRODUCTIVE_INPUT', 'CONSUMER_GOOD', 'SALES', 'MONEY')
	 * NOTE: here is a bug in H2 which prevents an enum type being used in a primary key;
	 * in consequence, the type of the persistent field 'stockType' is, confusingly, String and not StockType.
	 * For code transparency, this enum provides the text that is used in SQL queries, via its 'text' method
	 * See for example {@link Stock#singleProductive(int, int, String, String)}
	 * TODO we should use the built in 'values()' method for this. But since we will drop this enum
	 * if and when the bug is fixed, probably a low priority
	 */
	public static enum STOCKTYPE {
		PRODUCTIVE("Productive"), CONSUMPTION("Consumption"), SALES("Sales"), MONEY("Money");
		private String text;

		STOCKTYPE(String text) {
			this.text = text;
		}

		public String text() {
			return text;
		}
	}

	/**
	 * Owners can be of two main types: industries, which produce things by employing labour
	 * and classes, which provide labour, and consume revenue.
	 */
	public enum OWNERTYPE {
		CLASS("Social Class"), INDUSTRY("Industry"), UNKNOWN("Unknown");
		private String text;

		OWNERTYPE(String text) {
			this.text = text;
		}

		public String text() {
			return text;
		}
	}

	/**
	 * Fundamental.
	 * 
	 * The magnitude of any stock or flow may be expressed in three ways: (1) an intrinsic expression of its magnitude such as tons, yards, units, tickets, etc.
	 * ((2) its value (3) its price.
	 * 
	 * The need to express a stock magnitude in one of three different ways is basic to the axiomatic reconstruction of value theory. It confronts us, in the
	 * simulation,
	 * through the issue of aggregation or omposition.
	 * 
	 * We can conceive of an individual stock as having a quantity, a value and a price that are separate from each other, much as a human may have a height or
	 * a weight.
	 * 
	 * Once we aggregate or compose this distincton takes a different form, because no meaning can be assigned to the quantity of a composite. That is to say,
	 * it is not totally ordered.
	 * 
	 * We must therefore decide how its composite magnitude is 'expressed', that is to say, how it is aggregated. This requires a function that obeys the
	 * additive law f(a+b)=f(a)+f(b)
	 * 
	 * price and value are two such expressions of aggregate/composite magnitude, each with its own distinct laws. Value is functionally dependent on
	 * production, price is not.
	 * 
	 * So, we must allow composites (for example total inputs, total capital) to be expressed in any of these forms. This also however applies for magnitudes
	 * that are not composite, since an individual entity is merely the simplest possible composite.
	 * 
	 * The user chooses how to display any stock or flow magnitude whose form of expression is not explicit. This class provides for that choice to be
	 * displayed.
	 * 
	 * @author afree
	 *
	 */
	public enum VALUE_EXPRESSION {
		QUANTITY, VALUE, PRICE
	}

	/**
	 * Constructor for a Stock Entity. Also creates a 'hollow' primary key which must be consistently populated by the caller
	 */
	public Stock() {
		this.pk = new StockPK();
	}

	/**
	 * the Commodity entity of this Stock
	 * 
	 * @return the Commodity entity of this Stock
	 */
	public Commodity getCommodity() {
		return Commodity.single(pk.projectID, pk.timeStampID, pk.commodity);
	}

	/**
	 * Make a carbon copy of the stock.
	 * At present, this is used to construct a comparator stock, so that changes can be highlighted ('differencing') in the display tables.
	 * It may have other uses, but I am not aware of them.
	 * 
	 * @param template
	 *            the stock from which to copy
	 */
	public Stock(Stock template) {
		this.pk = new StockPK();
		pk.timeStampID = template.pk.timeStampID;
		pk.projectID = template.pk.projectID;
		pk.owner = template.pk.owner;
		pk.commodity = template.pk.commodity;
		pk.stockType = template.pk.stockType;
		price = template.price;
		value = template.value;
		ownerType = template.ownerType;
		productionCoefficient = template.productionCoefficient;
		consumptionCoefficient = template.consumptionCoefficient;
		quantity = template.quantity;
		replenishmentDemand = template.replenishmentDemand;
		expansionDemand = template.expansionDemand;
		stockUsedUp = template.stockUsedUp;
		productionQuantity = template.productionQuantity;
		consumptionQuantity = template.consumptionQuantity;
	}

	/**
	 * generic selector which returns a numerical attribute depending on the {@link VALUE_EXPRESSION}
	 * 
	 * @param a
	 *            (QUANTITY, VALUE OR PRICE) selects whether to return the quantity, the value or the price of this stock
	 * @return the quantity if a=QUANTITY, etc.
	 */
	public double get(VALUE_EXPRESSION a) {
		switch (a) {
		case QUANTITY:
			return quantity;
		case VALUE:
			return ViewManager.valueExpression(value, DisplayControlsBox.expressionDisplay);
		case PRICE:
			return ViewManager.valueExpression(price, DisplayControlsBox.expressionDisplay);
		default:
			throw new RuntimeException("ERROR: unknown attribute selector");
		}
	}

	/**
	 * provides a wrapped version of the selected member which the display will recognise, as a ReadOnlyStringWrapper.
	 * 
	 * We don't mind the hardwiring because we don't really intend this code to be re-usable, it's not hard to modify, and it results in compact
	 * and readable usage code (@see for example TabbedTableViewer#makeProductiveStocksViewTable})
	 * 
	 * @param attribute
	 *            chooses which member to evaluate
	 * @return a String representation of the members, formatted according to the relevant format string
	 */
	public ReadOnlyStringWrapper wrappedString(STOCK_ATTRIBUTE attribute) {
		chooseComparison();
		switch (attribute) {
		case OWNER:
			return new ReadOnlyStringWrapper(pk.owner);
		case OWNERTYPE:
			return new ReadOnlyStringWrapper(ownerType.text());
		case COMMODITY:
			return new ReadOnlyStringWrapper(pk.commodity);
		case STOCKTYPE:
			return new ReadOnlyStringWrapper(pk.stockType);
		case QUANTITY:
			return new ReadOnlyStringWrapper(String.format(ViewManager.getLargeFormat(), quantity));
		case VALUE:
			return new ReadOnlyStringWrapper(
					String.format(ViewManager.getLargeFormat(), ViewManager.valueExpression(value, DisplayControlsBox.expressionDisplay)));
		case PRICE:
			return new ReadOnlyStringWrapper(
					String.format(ViewManager.getLargeFormat(), ViewManager.valueExpression(price, DisplayControlsBox.expressionDisplay)));
		case REPLENISHMENTDEMAND:
			return new ReadOnlyStringWrapper(String.format(ViewManager.getLargeFormat(), replenishmentDemand));
		case PRODUCTION_COEFFICIENT:
			return new ReadOnlyStringWrapper(String.format(ViewManager.getSmallFormat(), productionCoefficient));
		case CONSUMPTION_COEFFICIENT:
			return new ReadOnlyStringWrapper(String.format(ViewManager.getSmallFormat(), consumptionCoefficient));
		default:
			return null;
		}
	}

	/**
	 * generic selector which returns a boolean depending on the {@link VALUE_EXPRESSION}
	 * 
	 * @param a
	 *            (QUANTITY, VALUE OR PRICE) selects whether to inspect the quantity, the value or the price of this stock
	 * @return true if the selected attribute is different from the corresponding attribute of the comparator stock.
	 */
	public boolean changed(VALUE_EXPRESSION a) {
		chooseComparison();
		switch (a) {
		case QUANTITY:
			return quantity != comparator.quantity;
		case VALUE:
			return value != comparator.value;
		case PRICE:
			return price != comparator.price;
		default:
			throw new RuntimeException("ERROR: unknown attribute selector");
		}
	}

	/**
	 * informs the display whether the selected member of this entity has changed, compared with the 'comparator' Stock which normally
	 * comes from a different timeStamp.
	 * 
	 * We don't mind the hardwiring because we don't really intend this code to be re-usable, it's not hard to modify, and it results in compact
	 * and readable usage code for example (@see TabbedTableViewer#makeSalesStocksViewTable})
	 * 
	 * @param attribute
	 *            chooses which member to evaluate
	 * @return whether this member has changed or not. False if selector is unavailable here
	 */
	public boolean changed(STOCK_ATTRIBUTE attribute) {
		chooseComparison();
		switch (attribute) {
		case OWNER:
		case COMMODITY:
		case STOCKTYPE:
			return false;
		case QUANTITY:
			return quantity != comparator.quantity;
		case VALUE:
			return value != comparator.value;
		case PRICE:
			return price != comparator.price;
		case REPLENISHMENTDEMAND:
			return replenishmentDemand != comparator.replenishmentDemand;
		case PRODUCTION_COEFFICIENT:
			return productionCoefficient != comparator.productionCoefficient;
		case CONSUMPTION_COEFFICIENT:
			return consumptionCoefficient != comparator.consumptionCoefficient;
		default:
			return false;
		}
	}

	/**
	 * If the selected field has changed, return the difference between the current value and the former value
	 * 
	 * @param item
	 *            the original item - returned as the result if there is no change
	 * 
	 * @param valueExpression
	 *            selects the display attribute where relevant (QUANTITY, VALUE, PRICE)
	 * 
	 * @return the original item if nothing has changed, otherwise the change, as an appropriately formatted string
	 */
	public String showDelta(String item, VALUE_EXPRESSION valueExpression) {
		chooseComparison();
		if (!changed(valueExpression))
			return item;
		switch (valueExpression) {
		case QUANTITY:
			return String.format(ViewManager.getLargeFormat(), quantity - comparator.quantity);
		case VALUE:
			return String.format(ViewManager.getLargeFormat(), value - comparator.value);
		case PRICE:
			return String.format(ViewManager.getLargeFormat(), price - comparator.price);
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
	 * an observable list of stocks of a particular stock type, for display by ViewManager, at the given project and timeStamp.
	 * 
	 * @param projectID
	 *            the given projectID
	 * 
	 * @param timeStampID
	 *            the given timeStampID
	 * @param stockType
	 *            the stockType (Productive, Sales, Consumption, Money) of this stock
	 * 
	 * @return an observableList of stocks
	 */
	public static ObservableList<Stock> ofStockTypeObservable(int projectID, int timeStampID, String stockType) {
		Stock.withStockTypeQuery.setParameter("project", projectID).setParameter("stockType", stockType).setParameter("timeStamp", timeStampID);
		ObservableList<Stock> result = FXCollections.observableArrayList();
		for (Stock s : Stock.withStockTypeQuery.getResultList()) {
			result.add(s);
		}
		return result;
	}

	/**
	 * When the unit price or the unit value of a commodity changes, the price and value of each stock changes accordingly
	 * This method enacts the change.
	 */

	public void reCalculateStockTotalValuesAndPrices() {
		price = quantity * unitPrice();
		value = quantity * unitValue();
	}

	/**
	 * Change the size of the stock by quantity and adjust the value and price accordingly. Throw runtime error if the result would be less than zero
	 * 
	 * @param extraQuantity
	 *            the quantity to be added to the size of the stock (negative if subtracted)
	 */
	public void modifyBy(double extraQuantity) {
		double melt = Simulation.melt();

		double unitValue = unitValue();
		double unitPrice = unitPrice();
		double extraValue = extraQuantity * unitValue;
		double extraPrice = extraQuantity * unitPrice;
		double newValue = value + extraValue;
		double newPrice = price + extraPrice;
		double newQuantity = quantity + extraQuantity;
		quantity = MathStuff.round(newQuantity);
		value = MathStuff.round(newValue);
		price = MathStuff.round(newPrice);
		Reporter.report(logger, 3,
				"Commodity [%s], of type [%s], owned by [%s]: is now %.0f. Its value is now $%.0f (intrinsic %.0f), and its price is %.0f (intrinsic %.0f)",
				pk.commodity, pk.stockType, pk.owner, quantity, value, value / melt, price, price / melt);
	}

	/**
	 * overloaded version of changeBy which sets the value separately
	 * 
	 * @param quantity
	 *            the increase in size
	 * @param valueAdded
	 *            the increase in value
	 */
	public void modifyBy(double quantity, double valueAdded) {
		double oldValue = value;
		modifyBy(quantity);
		setValue(MathStuff.round(oldValue + valueAdded)); // overwrite what was done by the simple call to changeBy
	}

	/**
	 * Set the size of the stock to the Quantity and adjust the value and price accordingly. Throw runtime error if the result would be less than zero
	 * 
	 * @param newQuantity
	 *            the quantity to be added to the size of the stock (negative if subtracted)
	 */
	public void modifyTo(double newQuantity) {
		double melt = Simulation.melt();
		try {
			double unitValue = unitValue();
			double unitPrice = unitPrice();
			double newValue = newQuantity * unitValue;
			double newPrice = newQuantity * unitPrice;
			quantity = MathStuff.round(newQuantity);
			value = MathStuff.round(newValue);
			price = MathStuff.round(newPrice);
			Reporter.report(logger, 3,
					"Size of commodity [%s], of type [%s], owned by [%s]: is %.0f. Value set to $%.0f (intrinsic %.0f), and price to %.0f (intrinsic %.0f)",
					pk.commodity, pk.stockType, pk.owner, quantity, value, value / melt, price, price / melt);
		} catch (Exception e) {
			Dialogues.alert(logger, "Something went wrong pre-processing the stock called %s. Please check your data.", pk.commodity);
		}
	}

	/**
	 * Helper function transfers quantityTransferred from this Stock to toStock. Also transfers the value of the stock and the price. Carries out checks and
	 * throws an exception if conditions are violated
	 * 
	 * @param to
	 *            the stock that is gaining the value
	 * @param quantityTransferred
	 *            the amount to transfer
	 */
	public void transferStock(Stock to, double quantityTransferred) throws RuntimeException {
		Commodity commodity = getCommodity();
		if (quantityTransferred == 0) {
			return;			// Nothing to transfer
		}

		// a little consistency check

		if (!pk.commodity.equals(to.name())) {
			Dialogues.alert(logger,
					"The simulation tried to transfer stock between commodities of different types.This is a programme error. Please contact the developer");
		}

		double unitValue = commodity.getUnitValue();
		double unitPrice = commodity.getUnitPrice();
		double toValue = to.getValue();
		double fromValue = value;
		double toPrice = to.getPrice();
		double fromPrice = price;
		double fromQuantity = quantity;
		double toQuantity = to.getQuantity();

		// another little consistency check

		if (toQuantity != 0) {
			if (!MathStuff.equals(toPrice / toQuantity, unitPrice)) {
				Dialogues.alert(logger, "The unit price of the [%s] is %.2f and the unit price of its use value is  %.2f",
						to.name(), toPrice / toQuantity, unitPrice);
			}
			if (!MathStuff.equals(toValue / toQuantity, unitValue)) {
				Dialogues.alert(logger, "The unit price of the stock [%s] is %.2f and the unit price of its use value is  %.2f",
						to.name(), toPrice / toQuantity, unitPrice);
			}
		}
		if (fromQuantity != 0) {
			if (!MathStuff.equals(fromPrice / fromQuantity, unitPrice)) {
				Dialogues.alert(logger, "The unit price of the target stock [%s] is %.2f and the unit price of its use value is  %.2f",
						pk.commodity, fromPrice / fromQuantity, unitPrice);
			}
			if (!MathStuff.equals(fromValue / fromQuantity, unitValue)) {
				Dialogues.alert(logger, "The unit price of the target stock [%s] is %.2f and the unit price of its use value is  %.2f",
						pk.commodity, fromPrice / fromQuantity, unitPrice);
			}
		}
		logger.debug(String.format("   Transfer %.2f from [%s] in [%s] to [%s] in [%s]",
				quantityTransferred, pk.commodity, pk.owner, to.name(), to.getOwner()));
		logger.debug(String.format("   Recipient [%s] size is: %.2f", to.name(), to.getQuantity()));
		logger.debug(String.format("   Donor [%s] size is: %.2f ", pk.commodity, quantity));

		to.modifyBy(quantityTransferred);
		modifyBy(-quantityTransferred);

		logger.debug(String.format("   Recipient [%s] size is now: %.2f ", to.name(), to.getQuantity()));
		logger.debug(String.format("   Donor [%s] size is now: %.2f ", pk.commodity, quantity));
	}

	/**
	 * set the comparators for all stock entities in the given project, for the given timeStampID
	 * 
	 * @param timeStampID
	 *            the timeStampID of the Stock entities whose comparators will be set
	 * @param projectID
	 *            the projectID of the Stock entities whose comparators will be set
	 */
	public static void setComparators(int projectID, int timeStampID) {
		logger.debug("Setting comparators for stocks in project {} with timeStamp {}", projectID, timeStampID);
		Project project = Project.get(projectID);
		for (Stock s : all(projectID, timeStampID)) {
			s.setPreviousComparator(single(projectID, project.getTimeStampComparatorCursor(), s.getOwner(), s.name(), s.getStockType()));
			s.setStartComparator(single(projectID, 1, s.getOwner(), s.name(), s.getStockType()));
			s.setEndComparator(single(projectID, project.getTimeStampID(), s.getOwner(), s.name(), s.getStockType()));
			s.setCustomComparator(single(projectID, project.getTimeStampID(), s.getOwner(), s.name(), s.getStockType()));
		}
	}

	/**
	 * get the single stock with the primary key given by all the parameters
	 * 
	 * @param projectID
	 *            the given project
	 * @param timeStampID
	 *            the given timeStamp
	 * @param industry
	 *            the name of the owning industry, as a String
	 * @param commodity
	 *            the name of the use value of this stock, as a String
	 * @param stockType
	 *            the type of this stock (money, productive, sales, consumption) as a String
	 * @return the single stock defined by this primary key, null if it does not exist
	 */
	public static Stock single(int projectID, int timeStampID, String industry, String commodity, String stockType) {
		primaryQuery.setParameter("project", projectID).setParameter("timeStamp", timeStampID).setParameter("owner", industry)
				.setParameter("commodity", commodity).setParameter("stockType", stockType);
		try {
			return primaryQuery.getSingleResult();
		} catch (NoResultException r) {
			return null;
		}
	}

	/**
	 * the money stock of a Industry defined by the name of the industry and the use value it produces, for the given project and timeStamp
	 * 
	 * @param projectID
	 *            ID the given projectID
	 * @param timeStampID
	 *            the given timeStampID
	 * @param industry
	 *            the Industry to which the stock belongs
	 * 
	 * @return the single stock of money owned by the industry
	 */
	public static Stock moneyByOwner(int projectID, int timeStampID, String industry) {
		primaryQuery.setParameter("project", projectID).setParameter("timeStamp", timeStampID).setParameter("owner", industry)
				.setParameter("stockType", Stock.STOCKTYPE.MONEY.text()).setParameter("commodity", "Money");
		try {
			return primaryQuery.getSingleResult();
		} catch (javax.persistence.NoResultException e) {
			return null;// because this query throws a fit if it doesn't find anything
		}
	}

	/**
	 * A list of all stocks at the database
	 * Mainly for validation purposes but could have other uses
	 * 
	 * @return a list of stocks in the database
	 */
	public static List<Stock> all() {
		return allQuery.getResultList();
	}

	
	/**
	 * a list of all stocks at the given projectID and a given timeStampID
	 * 
	 * @param projectID
	 *            the given projectID
	 * @param timeStampID
	 *            the given timeStamp
	 * 
	 * @return a list of stocks at the given projectID and timeStampID
	 */
	public static List<Stock> all(int projectID, int timeStampID) {
		allInProjectAndTimeStampQuery.setParameter("project", projectID).setParameter("timeStamp", timeStampID);
		return allInProjectAndTimeStampQuery.getResultList();
	}

	/**
	 * A list of all stocks in the given projectID
	 * Mainly for validation purposes but could have other uses
	 * 
	 * @param projectID
	 *            the given projectID
	 * @return a list of stocks in the given projectID
	 */
	public static List<Stock> all(int projectID) {
		allInProjectQuery.setParameter("project", projectID);
		return allInProjectQuery.getResultList();
	}

	/**
	 * a list of all stocks for the given commodity at the given project and timestamp.
	 * 
	 * @param projectID
	 *            the given projectID
	 * @param timeStampID
	 *            the given timeStampID
	 * @param commodityName
	 *            the commodity name of the stocks
	 * @return a list of stocks for the given commodity at the currently selected time and for the currently selected project
	 */
	public static List<Stock> stocksOfCommodity(int projectID, int timeStampID, String commodityName) {
		ofCommodityQuery.setParameter("project", projectID).setParameter("timeStamp", timeStampID).setParameter("commodity",
				commodityName);
		return ofCommodityQuery.getResultList();
	}

	/**
	 * a list of all stocks that constitute sources of demand (productive and consumption but not money or sales), for a given project and a given timeStamp
	 * 
	 * @param projectID
	 *            the given projectID
	 * @param timeStampID
	 *            the given timeStampID
	 * @return a list of all stocks that constitute sources of demand
	 */
	public static List<Stock> sourcesOfDemand(int projectID, int timeStampID) {
		sourcesOfDemandQuery.setParameter("project", projectID).setParameter("timeStamp", timeStampID);
		sourcesOfDemandQuery.setParameter("stockType1", Stock.STOCKTYPE.PRODUCTIVE.text()).setParameter("stockType2",
				Stock.STOCKTYPE.CONSUMPTION.text());
		return sourcesOfDemandQuery.getResultList();
	}

	/**
	 * a list of all the productive stocks that are managed by a given industry, at the given project and timeStamp
	 * 
	 * @param projectID
	 *            the given projectID
	 * @param timeStampID
	 *            the given timeStamp
	 * @param industry
	 *            the industry that manages these productive stocks
	 * @return a list of the productive stocks managed by this industry
	 */
	public static List<Stock> allProductiveInIndustry(int projectID, int timeStampID, String industry) {
		withOwnerAndTypeQuery.setParameter("project", projectID).setParameter("timeStamp", timeStampID);
		withOwnerAndTypeQuery.setParameter("owner", industry).setParameter("stockType", Stock.STOCKTYPE.PRODUCTIVE.text());
		return withOwnerAndTypeQuery.getResultList();
	}

	/**
	 * the single productive stock of a industry defined by the name of the industry, the use value it produces, for the given project and timeStamp
	 * 
	 * @param projectID
	 *            the given projectID
	 * @param timeStampID
	 *            the given timeStamp
	 * @param industry
	 *            the industry to which the stock belongs
	 * @param commodity
	 *            the commodity of the stock
	 * @return the single productive stock, with the given commodity, of the named industry
	 */
	public static Stock singleProductive(int projectID, int timeStampID, String industry, String commodity) {
		primaryQuery.setParameter("project", projectID).setParameter("timeStamp", timeStampID)
				.setParameter("owner", industry).setParameter("stockType", Stock.STOCKTYPE.PRODUCTIVE.text()).setParameter("commodity", commodity);
		try {
			return primaryQuery.getSingleResult();
		} catch (javax.persistence.NoResultException e) {
			return null;// because this query throws a fit if it doesn't find anything
		}
	}

	/**
	 * A list of all productive stocks belonging to a given project, regardless of owner and timeStamp
	 * 
	 * @param projectID
	 *            the ID of the project containing the stocks
	 * @return a list of all productive stocks belonging to a given project, regardless of owner and timeStamp
	 */
	public static List<Stock> productive(int projectID) {
		productiveQuery.setParameter("project", projectID);
		return productiveQuery.getResultList();
	}

	/**
	 * a list of the various consumer goods owned by a given social class, at the given project and timeStamp
	 * 
	 * @param projectID
	 *            the given projectID
	 * @param timeStampID
	 *            the given timeStampID
	 * @param socialClass
	 *            the socialClass that consumes these stocks
	 * @return a list of the consumption stocks owned by this social class
	 */
	public static List<Stock> consumedByClass(int projectID, int timeStampID, String socialClass) {
		withOwnerAndTypeQuery.setParameter("project", projectID).setParameter("timeStamp", timeStampID);
		withOwnerAndTypeQuery.setParameter("owner", socialClass).setParameter("stockType", Stock.STOCKTYPE.CONSUMPTION.text());
		return withOwnerAndTypeQuery.getResultList();
	}

	/**
	 * the single stock of a consumer good of the given use value owned by the given social class, at the given project and timeStampID
	 *
	 * @param projectID
	 *            the given projectID
	 * @param timeStampID
	 *            the given timeStampID
	 * @param socialClass
	 *            the socialClass that consumes these stocks
	 * @param commodity
	 *            the required use value
	 * @return the single consumption stocks of the given commodity that is owned by this social class
	 */
	public static Stock consumptionByCommodityAndClassSingle(int projectID, int timeStampID, String socialClass, String commodity) {
		primaryQuery.setParameter("project", projectID).setParameter("timeStamp", timeStampID);
		primaryQuery.setParameter("owner", socialClass).setParameter("stockType", Stock.STOCKTYPE.CONSUMPTION.text()).setParameter("commodity",
				commodity);
		try {
			return primaryQuery.getSingleResult();
		} catch (javax.persistence.NoResultException e) {
			return null;// because this query throws a fit if it doesn't find anything
		}
	}

	/**
	 * a list of sales Stock of a given use value for the given project and timeStamp.
	 * NOTE only the industry will vary, and at present only one of these industries will produce this use value. However in general more than one industry may
	 * produce it so we yield a list here.
	 * 
	 * @param projectID
	 *            the given projectID
	 * @param timeStampID
	 *            the given timeStampID
	 * 
	 * @param commodity
	 *            the use value that the sales stocks contain
	 * @return a list of the sales stocks that contain the given use value
	 *         Note: there can be more than one seller of the same use value
	 */
	public static List<Stock> salesByCommodity(int projectID, int timeStampID, String commodity) {
		ofCommodityAndTypeQuery.setParameter("project", projectID).setParameter("timeStamp", timeStampID).setParameter("commodity",
				commodity);
		ofCommodityAndTypeQuery.setParameter("stockType", Stock.STOCKTYPE.SALES.text());
		return ofCommodityAndTypeQuery.getResultList();
	}

	/**
	 * @return the entityManager
	 */
	public static EntityManager getEntityManager() {
		return entityManager;
	}

	/**
	 * Part of primitive typology of use values
	 * 
	 * @return the use value type of this stock
	 */
	public Commodity.FUNCTION functionType() {
		return getCommodity().getFunction();
	}

	/**
	 * a short report as a string showing the quantity, value and price of this stock
	 * 
	 * @return a string showing the quantity, value and price of this stock
	 */
	public String sizeAsString() {
		return String.format("quantity %.2f  (value %.2f, price %.2f)", quantity, value, price);
	}

	/**
	 * @return formatted primary key of the stock formatted to constant length, which identifies it visually
	 */
	public String primaryKeyAsString() {
		return String.format("[ %12.12s.%12.12s.%12.12s]", pk.stockType, pk.owner, pk.commodity);
	}

	/**
	 * get the unit price of the use value that this stock contains
	 * 
	 * @return the unit price of the use value that this stock contains
	 */
	private double unitPrice() {
		return getCommodity().getUnitPrice();
	}

	/**
	 * get the unit value of the use value that this stock contains
	 * 
	 * @return the unit value of the use value that this stock contains
	 */
	private double unitValue() {
		return getCommodity().getUnitValue();
	}

	/**
	 * 
	 * @return the production coefficient
	 */

	public double getProductionCoefficient() {
		return this.productionCoefficient;
	}

	/**
	 * set the production coefficient
	 * 
	 * @param productionCoefficient
	 *            the coefficient to set
	 */
	public void setProductionCoefficient(double productionCoefficient) {
		this.productionCoefficient = productionCoefficient;
	}

	/**
	 * 
	 * @return the consumption Coefficient
	 */
	public double getConsumptionCoefficient() {
		return consumptionCoefficient;
	}

	/**
	 * Set the consumption coefficient
	 * 
	 * @param consumptionCoefficient
	 *            the coefficient to set
	 */
	public void setConsumptionCoefficient(double consumptionCoefficient) {
		this.consumptionCoefficient = consumptionCoefficient;
	}

	/**
	 * 
	 * @return the quantity of this stock
	 */

	public double getQuantity() {
		return this.quantity;
	}

	/**
	 * Set the quantity of this stock
	 * 
	 * @param quantity
	 *            the quantity to set
	 */
	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}

	/**
	 * 
	 * @return replenishmentDemand
	 */
	public double getReplenishmentDemand() {
		return replenishmentDemand;
	}

	/**
	 * Set the replenishment Demand (the quantity of this productive stock required to continue producing at
	 * the same output level)
	 * 
	 * @param quantityDemanded
	 *            the quantity demanded for production to continue at the same level
	 */
	public void setReplenishmentDemand(double quantityDemanded) {
		this.replenishmentDemand = MathStuff.round(quantityDemanded);
	}

	/**
	 * Get the stock Type (productive, consumption, sales or money)
	 * 
	 * @return the stockType
	 */

	public String getStockType() {
		return pk.stockType;
	}

	/**
	 * Set the stockType (productive, consumption, sales or money)
	 * 
	 * @param stockType
	 *            the stockType to set
	 */

	public void setStockType(String stockType) {
		this.pk.stockType = stockType;
	}

	/**
	 * The name of this stock (which is the commodity that it consists of)
	 * 
	 * @return the name of the commodity that this stock consists of
	 */
	public String name() {
		return pk.commodity;
	}

	/**
	 * Set the name of this stock (which is the commodity that it consists of)
	 * 
	 * @param commodityName
	 *            the commodityName to set
	 */
	public void setCommodityName(String commodityName) {
		pk.commodity = commodityName;
	}

	/**
	 * Get the owner of this stock, which may be an Industry or a Social Class
	 * 
	 * @return the name of the owner of this stock
	 */
	public String getOwner() {
		return pk.owner;
	}

	/**
	 * Set the timeStampID of this stock
	 * 
	 * @param timeStampID
	 *            the timeStampID to set
	 */

	public void setTimeStamp(int timeStampID) {
		pk.timeStampID = timeStampID;
	}

	/**
	 * @return the value
	 */
	public double getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(double value) {
		this.value = value;
	}

	/**
	 * @return the price
	 */
	public double getPrice() {
		return price;
	}

	/**
	 * @param price
	 *            the price to set
	 */
	public void setPrice(double price) {
		this.price = price;
	}

	/**
	 * 
	 * @return the projectID of this stock
	 */
	public Integer getProjectID() {
		return pk.projectID;
	}

	/**
	 * 
	 * @return the timeStampID of this stock
	 */

	public int getTimeStampID() {
		return pk.timeStampID;
	}

	/**
	 * set the comparator stock.
	 * 
	 * @param comparator
	 *            A Stock Bean with which this stock will be compared
	 */
	public void setComparator(Stock comparator) {
		this.comparator = comparator;
	}

	/**
	 * @return the previousComparator
	 */
	public Stock getPreviousComparator() {
		return previousComparator;
	}

	/**
	 * @param previousComparator
	 *            the previousComparator to set
	 */
	public void setPreviousComparator(Stock previousComparator) {
		this.previousComparator = previousComparator;
	}

	/**
	 * @return the startComparator
	 */
	public Stock getStartComparator() {
		return startComparator;
	}

	/**
	 * @param startComparator
	 *            the startComparator to set
	 */
	public void setStartComparator(Stock startComparator) {
		this.startComparator = startComparator;
	}

	/**
	 * @return the customComparator
	 */
	public Stock getCustomComparator() {
		return customComparator;
	}

	/**
	 * @param customComparator
	 *            the customComparator to set
	 */
	public void setCustomComparator(Stock customComparator) {
		this.customComparator = customComparator;
	}

	/**
	 * @return the endComparator
	 */
	public Stock getEndComparator() {
		return endComparator;
	}

	/**
	 * @param endComparator
	 *            the endComparator to set
	 */
	public void setEndComparator(Stock endComparator) {
		this.endComparator = endComparator;
	}

	/**
	 * @return the expansionDemand
	 */
	public double getExpansionDemand() {
		return expansionDemand;
	}

	/**
	 * @param expansionDemand
	 *            the expansionDemand to set
	 */
	public void setExpansionDemand(double expansionDemand) {
		this.expansionDemand = expansionDemand;
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
	 * Set the projectID of this stock
	 * 
	 * @param projectID
	 *            the projectID to set
	 * 
	 */
	public void setProjectID(int projectID) {
		pk.projectID = projectID;
	}

	/**
	 * @return the productionQuantity
	 */
	public double getProductionQuantity() {
		return productionQuantity;
	}

	/**
	 * @param productionQuantity
	 *            the productionQuantity to set
	 */
	public void setProductionQuantity(double productionQuantity) {
		this.productionQuantity = productionQuantity;
	}

	/**
	 * @return the consumptionQuantity
	 */
	public double getConsumptionQuantity() {
		return consumptionQuantity;
	}

	/**
	 * @param consumptionQuantity
	 *            the consumptionQuantity to set
	 */
	public void setConsumptionQuantity(double consumptionQuantity) {
		this.consumptionQuantity = consumptionQuantity;
	}

	public void setOwner(String owner) {
		pk.owner = owner;
	}

	/**
	 * @return the ownerType
	 */
	public OWNERTYPE getOwnerType() {
		return ownerType;
	}

	/**
	 * @param ownerType
	 *            the ownerType to set
	 */
	public void setOwnerType(OWNERTYPE ownerType) {
		this.ownerType = ownerType;
	}

}