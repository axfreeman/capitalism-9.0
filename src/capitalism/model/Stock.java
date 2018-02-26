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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.Simulation;
import capitalism.utils.Dialogues;
import capitalism.utils.MathStuff;
import capitalism.utils.Reporter;
import capitalism.view.ViewManager;
import capitalism.view.custom.DisplayControls;
import capitalism.view.custom.TrackingControls;
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
		// select a single stock by all elements of its primary key
		@NamedQuery(name = "Primary", query = "SELECT s FROM Stock s WHERE s.pk.project=:project and s.pk.timeStamp =:timeStamp and s.pk.owner =:owner and s.pk.commodity= :commodity and s.pk.stockType=:stockType"),

		// select all stocks with the given project and timeStamp
		@NamedQuery(name = "All", query = "SELECT s FROM Stock s where s.pk.project= :project and s.pk.timeStamp = :timeStamp"),

		// select all stocks with the given project, timeStamp and stockType
		@NamedQuery(name = "StockType", query = "SELECT s FROM Stock s where s.pk.project = :project and s.pk.timeStamp=:timeStamp and s.pk.stockType=:stockType"),

		// select all stocks with the given project, timeStamp, owner and stockType
		@NamedQuery(name = "Owner.StockType", query = "SELECT s FROM Stock s "
				+ "where s.pk.project= :project and s.pk.timeStamp = :timeStamp and s.pk.owner= :owner and s.pk.stockType=:stockType"),

		// select all stocks of a given project, timeStamp and commodity
		@NamedQuery(name = "Commodity", query = "SELECT s FROM Stock s where s.pk.project = :project and s.pk.timeStamp = :timeStamp and s.pk.commodity= :commodity"),

		// select all stocks of a given project, timeStamp, commodity and stocktype
		@NamedQuery(name = "Commodity.StockType", query = "SELECT s FROM Stock s where s.pk.project = :project and s.pk.timeStamp = :timeStamp and s.pk.stockType=:stockType and s.pk.commodity= :commodity"),

		// select all stocks of a given project and timestamp, whose stockType is one of two specified types (used with Productive and Cnsumption to yield
		// sources of demand)
		@NamedQuery(name = "Demand", query = "SELECT s FROM Stock s where s.pk.project = :project and s.pk.timeStamp=:timeStamp "
				+ "and (s.pk.stockType = :stockType1 or s.pk.stockType=:stockType2)")
})

public class Stock implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(Stock.class);

	@EmbeddedId protected StockPK pk;
	@Column(name = "ownertype") private OWNERTYPE ownerType;
	@Column(name = "quantity") private double quantity;
	@Column(name = "value") private double value;
	@Column(name = "price") private double price;
	@Column(name = "replenishmentDemand") private double replenishmentDemand;
	@Column(name = "expansionDemand") private double expansionDemand;

	// the proportion of this stock used up in producing one unit of output.
	// ONLY relevant if this is of stockType PRODUCTIVE_INPUT (in which case the owner will be an industry)

	@Column(name = "productionCoefficient") private double productionCoefficient;

	// the proportion of the revenue of a class that will be spent on this stock in one period.
	// ONLY relevant if this is of stockType CONSUMER_GOOD (in which case the owner will be a social class)

	@Column(name = "consumptionCoefficient") private double consumptionCoefficient;

	// how much of this was used up in production or reproduction
	@Column (name = "stockUsedUp") private double stockUsedUp; 
	
	// Comparators
	@Transient private Stock comparator;
	@Transient private Stock previousComparator;
	@Transient private Stock startComparator;
	@Transient private Stock customComparator;
	@Transient private Stock endComparator;

	// Data Management
	private static EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("DB_STOCKS");
	private static EntityManager entityManager;
	private static TypedQuery<Stock> stockByPrimaryKeyQuery;
	private static TypedQuery<Stock> stocksAllQuery;
	private static TypedQuery<Stock> stocksByCommodityQuery;
	private static TypedQuery<Stock> stocksByCommodityAndTypeQuery;
	private static TypedQuery<Stock> stocksByOwnerAndTypeQuery;
	private static TypedQuery<Stock> stocksByOneOfTwoStockTypesQuery;
	public static TypedQuery<Stock> stocksByStockTypeQuery;

	static {
		entityManager = entityManagerFactory.createEntityManager();
		stockByPrimaryKeyQuery = entityManager.createNamedQuery("Primary", Stock.class);
		stocksAllQuery = entityManager.createNamedQuery("All", Stock.class);
		stocksByStockTypeQuery = entityManager.createNamedQuery("StockType", Stock.class);
		stocksByOwnerAndTypeQuery = entityManager.createNamedQuery("Owner.StockType", Stock.class);
		stocksByCommodityQuery = entityManager.createNamedQuery("Commodity", Stock.class);
		stocksByCommodityAndTypeQuery = entityManager.createNamedQuery("Commodity.StockType", Stock.class);
		stocksByOneOfTwoStockTypesQuery = entityManager.createNamedQuery("Demand", Stock.class);
	}

	/**
	 * Readable constants to refer to the methods which provide information about the persistent members of the class
	 */
	public enum Selector {
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

		Selector(String text, String imageName, String toolTip) {
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
	 * See for example {@link Stock#productiveNamedSingle(int, String, String) stockProductiveByNameSingle}
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
		CLASS("Social Class"), INDUSTRY("Industry");
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
	public enum ValueExpression {
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
		return Commodity.commodityByPrimaryKey(pk.timeStamp, pk.commodity);
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
		this.pk=new StockPK();
		pk.timeStamp = template.pk.timeStamp;
		pk.project = template.pk.project;
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
		stockUsedUp=template.stockUsedUp;
	}

	/**
	 * generic selector which returns a numerical attribute depending on the {@link ValueExpression}
	 * 
	 * @param a
	 *            (QUANTITY, VALUE OR PRICE) selects whether to return the quantity, the value or the price of this stock
	 * @return the quantity if a=QUANTITY, etc.
	 */
	public double get(ValueExpression a) {
		switch (a) {
		case QUANTITY:
			return quantity;
		case VALUE:
			return ViewManager.valueExpression(value, DisplayControls.valuesExpressionDisplay);
		case PRICE:
			return ViewManager.valueExpression(price, DisplayControls.pricesExpressionDisplay);
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
	 * @param selector
	 *            chooses which member to evaluate
	 * @return a String representation of the members, formatted according to the relevant format string
	 */

	public ReadOnlyStringWrapper wrappedString(Selector selector) {
		chooseComparison();
		switch (selector) {
		case OWNER:
			return new ReadOnlyStringWrapper(pk.owner);
		case OWNERTYPE:
			return new ReadOnlyStringWrapper(ownerType.text());
		case COMMODITY:
			return new ReadOnlyStringWrapper(pk.commodity);
		case STOCKTYPE:
			return new ReadOnlyStringWrapper(pk.stockType);
		case QUANTITY:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, quantity));
		case VALUE:
			return new ReadOnlyStringWrapper(
					String.format(ViewManager.largeNumbersFormatString, ViewManager.valueExpression(value, DisplayControls.valuesExpressionDisplay)));
		case PRICE:
			return new ReadOnlyStringWrapper(
					String.format(ViewManager.largeNumbersFormatString, ViewManager.valueExpression(price, DisplayControls.pricesExpressionDisplay)));
		case REPLENISHMENTDEMAND:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, replenishmentDemand));
		case PRODUCTION_COEFFICIENT:
			return new ReadOnlyStringWrapper(String.format(ViewManager.smallNumbersFormatString, productionCoefficient));
		case CONSUMPTION_COEFFICIENT:
			return new ReadOnlyStringWrapper(String.format(ViewManager.smallNumbersFormatString, consumptionCoefficient));
		default:
			return null;
		}
	}

	/**
	 * generic selector which returns a boolean depending on the {@link ValueExpression}
	 * 
	 * @param a
	 *            (QUANTITY, VALUE OR PRICE) selects whether to inspect the quantity, the value or the price of this stock
	 * @return true if the selected attribute is different from the corresponding attribute of the comparator stock.
	 */
	public boolean changed(ValueExpression a) {
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
	 * @param selector
	 *            chooses which member to evaluate
	 * @return whether this member has changed or not. False if selector is unavailable here
	 */

	public boolean changed(Selector selector) {
		chooseComparison();
		switch (selector) {
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
	public String showDelta(String item, ValueExpression valueExpression) {
		chooseComparison();
		if (!changed(valueExpression))
			return item;
		switch (valueExpression) {
		case QUANTITY:
			return String.format(ViewManager.largeNumbersFormatString, quantity - comparator.quantity);
		case VALUE:
			return String.format(ViewManager.largeNumbersFormatString, value - comparator.value);
		case PRICE:
			return String.format(ViewManager.largeNumbersFormatString, price - comparator.price);
		default:
			return item;
		}
	}

	/**
	 * chooses the comparator depending on the state set in the {@code ViewManager.comparatorToggle} radio buttons
	 */

	private void chooseComparison() {
		switch (TrackingControls.getComparatorState()) {
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
	 * an observable list of stocks of a particular stock type, for display by ViewManager, at the current project and timeStampDisplayCursor.
	 * timeStampDisplayCursor, which
	 * may diverge from timeStamp, identifies the row that the user last clicked on.
	 * 
	 * @param stockType
	 *            the stockType (Productive, Sales, Consumption, Money) of this stock
	 * 
	 * @return an observableList of stocks
	 */
	public static ObservableList<Stock> stocksByStockTypeObservable(String stockType) {
		Stock.stocksByStockTypeQuery.setParameter("project", Simulation.projectCurrent).setParameter("stockType", stockType).setParameter("timeStamp",
				Simulation.timeStampDisplayCursor);
		ObservableList<Stock> result = FXCollections.observableArrayList();
		for (Stock s : Stock.stocksByStockTypeQuery.getResultList()) {
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
		double melt = Global.getGlobal().getMelt();

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
		setValue(oldValue + valueAdded); // overwrite what was done by the simple call to changeBy
	}

	/**
	 * Set the size of the stock to the Quantity and adjust the value and price accordingly. Throw runtime error if the result would be less than zero
	 * 
	 * @param newQuantity
	 *            the quantity to be added to the size of the stock (negative if subtracted)
	 */
	public void modifyTo(double newQuantity) {
		double melt = Global.getGlobal().getMelt();
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

		if (!pk.commodity.equals(to.getCommodityName())) {
			Dialogues.alert(logger,"The simulation tried to transfer stock between commodities of different types.This is a programme error. Please contact the developer");
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
						to.getCommodityName(), toPrice / toQuantity, unitPrice);
			}
			if (!MathStuff.equals(toValue / toQuantity, unitValue)) {
				Dialogues.alert(logger, "The unit price of the stock [%s] is %.2f and the unit price of its use value is  %.2f",
						to.getCommodityName(), toPrice / toQuantity, unitPrice);
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
				quantityTransferred, pk.commodity, pk.owner, to.getCommodityName(), to.getOwner()));
		logger.debug(String.format("   Recipient [%s] size is: %.2f", to.getCommodityName(), to.getQuantity()));
		logger.debug(String.format("   Donor [%s] size is: %.2f ", pk.commodity, quantity));

		to.modifyBy(quantityTransferred);
		modifyBy(-quantityTransferred);

		logger.debug(String.format("   Recipient [%s] size is now: %.2f ", to.getCommodityName(), to.getQuantity()));
		logger.debug(String.format("   Donor [%s] size is now: %.2f ", pk.commodity, quantity));
	}

	/**
	 * set the comparators for all stock entities in the current project, for the given timeStampID
	 * 
	 * @param timeStampID
	 *            the timeStampID of the Stock entities whose comparators will be set
	 */

	public static void setComparators(int timeStampID) {
		for (Stock s : all(timeStampID)) {
			s.setPreviousComparator(stockByPrimaryKey(Simulation.projectCurrent, Simulation.getTimeStampComparatorCursor(), s.getOwner(),
					s.getCommodityName(), s.getStockType()));
			s.setStartComparator(stockByPrimaryKey(Simulation.projectCurrent, 1, s.getOwner(), s.getCommodityName(), s.getStockType()));
			s.setEndComparator(
					stockByPrimaryKey(Simulation.projectCurrent, Simulation.timeStampIDCurrent, s.getOwner(), s.getCommodityName(), s.getStockType()));
			s.setCustomComparator(
					stockByPrimaryKey(Simulation.projectCurrent, Simulation.timeStampIDCurrent, s.getOwner(), s.getCommodityName(), s.getStockType()));
		}
	}

	/**
	 * get the single stock with the primary key given by all the parameters
	 * 
	 * @param project
	 *            the given project
	 * @param timeStamp
	 *            the given timeStamp
	 * @param industry
	 *            the name of the owning industry, as a String
	 * @param commodity
	 *            the name of the use value of this stock, as a String
	 * @param stockType
	 *            the type of this stock (money, productive, sales, consumption) as a String
	 * @return the single stock defined by this primary key, null if it does not exist
	 */
	public static Stock stockByPrimaryKey(int project, int timeStamp, String industry, String commodity, String stockType) {
		stockByPrimaryKeyQuery.setParameter("project", project).setParameter("timeStamp", timeStamp).setParameter("owner", industry)
				.setParameter("commodity", commodity).setParameter("stockType", stockType);
		try {
			return stockByPrimaryKeyQuery.getSingleResult();
		} catch (NoResultException r) {
			return null;
		}
	}

	/**
	 * the money stock of a Industry defined by the name of the industry and the use value it produces, for the current project and a given timeStamp
	 * 
	 * @param timeStamp
	 *            the given timeStammp
	 * @param industry
	 *            the Industry to which the stock belongs
	 * 
	 * @return the single stock of money owned by the industry
	 */
	public static Stock stockMoneyByOwnerSingle(int timeStamp, String industry) {
		stockByPrimaryKeyQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp).setParameter("owner", industry)
				.setParameter("stockType", Stock.STOCKTYPE.MONEY.text()).setParameter("commodity", "Money");
		try {
			return stockByPrimaryKeyQuery.getSingleResult();
		} catch (javax.persistence.NoResultException e) {
			return null;// because this query throws a fit if it doesn't find anything
		}
	}

	/**
	 * a list of all stocks at the current project and a given timeStamp
	 * 
	 * @param timeStamp
	 *            the given timeStamp
	 * 
	 * @return a list of stocks at the current project and the given timeStamp
	 */
	public static List<Stock> all(int timeStamp) {
		stocksAllQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp);
		return stocksAllQuery.getResultList();
	}

	/**
	 * a list of all stocks at the current project and timeStamp
	 * 
	 * @return a list of stocks at the current project and timeStamp
	 */
	public static List<Stock> all() {
		stocksAllQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", Simulation.timeStampIDCurrent);
		return stocksAllQuery.getResultList();
	}

	/**
	 * a list of all stocks for the given commodity at the current project and a given timestamp.
	 * 
	 * @param timeStamp
	 *            the given timeStamp
	 * @param commodityName
	 *            the commodity name of the stocks
	 * @return a list of stocks for the given commodity at the currently selected time and for the currently selected project
	 */
	public static List<Stock> comoditiesCalled(int timeStamp, String commodityName) {
		stocksByCommodityQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp).setParameter("commodity",
				commodityName);
		return stocksByCommodityQuery.getResultList();
	}

	/**
	 * a list of all stocks that constitute sources of demand (productive and consumption but not money or sales), for the current project and a given timeStamp
	 * 
	 * @return a list of all stocks that constitute sources of demand
	 */
	public static List<Stock> sourcesOfDemand() {
		stocksByOneOfTwoStockTypesQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", Simulation.timeStampIDCurrent);
		stocksByOneOfTwoStockTypesQuery.setParameter("stockType1", Stock.STOCKTYPE.PRODUCTIVE.text()).setParameter("stockType2",
				Stock.STOCKTYPE.CONSUMPTION.text());
		return stocksByOneOfTwoStockTypesQuery.getResultList();
	}

	/**
	 * a list of all the productive stocks that are managed by a given industry, at the current project and a given timeStamp
	 *
	 * @param timeStamp
	 *            the given timeStamp
	 * @param industry
	 *            the industry that manages these productive stocks
	 * @return a list of the productive stocks managed by this industry
	 */
	public static List<Stock> productiveByIndustry(int timeStamp, String industry) {
		stocksByOwnerAndTypeQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp);
		stocksByOwnerAndTypeQuery.setParameter("owner", industry).setParameter("stockType", Stock.STOCKTYPE.PRODUCTIVE.text());
		return stocksByOwnerAndTypeQuery.getResultList();
	}

	/**
	 * the single productive stock of a industry defined by the name of the industry, the use value it produces, for the current project and a given timeStamp
	 * 
	 * @param timeStamp
	 *            the given timeStamp
	 * @param industry
	 *            the industry to which the stock belongs
	 * @param commodity
	 *            the commodity of the stock
	 * @return the single productive stock, with the given commodity, of the named industry
	 */
	public static Stock productiveNamedSingle(int timeStamp, String industry, String commodity) {
		stockByPrimaryKeyQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp)
				.setParameter("owner", industry).setParameter("stockType", Stock.STOCKTYPE.PRODUCTIVE.text()).setParameter("commodity", commodity);
		try {
			return stockByPrimaryKeyQuery.getSingleResult();
		} catch (javax.persistence.NoResultException e) {
			return null;// because this query throws a fit if it doesn't find anything
		}
	}

	/**
	 * a list of the various consumer goods owned by a given social class, at the current project and a given timeStamp
	 *
	 * @param timeStamp
	 *            the given timeStamp
	 * @param socialClass
	 *            the socialClass that consumes these stocks
	 * @return a list of the consumption stocks owned by this social class
	 */
	public static List<Stock> consumedByClass(int timeStamp, String socialClass) {
		stocksByOwnerAndTypeQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp);
		stocksByOwnerAndTypeQuery.setParameter("owner", socialClass).setParameter("stockType", Stock.STOCKTYPE.CONSUMPTION.text());
		return stocksByOwnerAndTypeQuery.getResultList();
	}

	/**
	 * the single stock of a consumer good of the given use value owned by the given social class, at the current project and a given timeStamp
	 *
	 * @param timeStamp
	 *            the given timeStamp
	 * @param socialClass
	 *            the socialClass that consumes these stocks
	 * @param commodity
	 *            the required use value
	 * @return the single consumption stocks of the given commodity that is owned by this social class
	 */
	public static Stock consumptionByCommodityAndClassSingle(int timeStamp, String socialClass, String commodity) {
		stockByPrimaryKeyQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp);
		stockByPrimaryKeyQuery.setParameter("owner", socialClass).setParameter("stockType", Stock.STOCKTYPE.CONSUMPTION.text()).setParameter("commodity",
				commodity);
		try {
			return stockByPrimaryKeyQuery.getSingleResult();
		} catch (javax.persistence.NoResultException e) {
			return null;// because this query throws a fit if it doesn't find anything
		}
	}

	/**
	 * a list of sales Stock of a given use value for the current project and a given timeStamp.
	 * NOTE only the industry will vary, and at present only one of these industries will produce this use value. However in general more than one industry may
	 * produce it so we yield a list here.
	 * 
	 * @param timeStamp
	 *            the given timeStamp
	 * 
	 * @param commodity
	 *            the use value that the sales stocks contain
	 * @return a list of the sales stocks that contain the given use value
	 *         Note: there can be more than one seller of the same use value
	 */
	public static List<Stock> salesByCommodity(int timeStamp, String commodity) {
		stocksByCommodityAndTypeQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp).setParameter("commodity", commodity);
		stocksByCommodityAndTypeQuery.setParameter("stockType", Stock.STOCKTYPE.SALES.text());
		return stocksByCommodityAndTypeQuery.getResultList();
	}

	/**
	 * @return the entityManager
	 */
	public static EntityManager getEntityManager() {
		return entityManager;
	}

	// named query getters

	/**
	 * @return the socialClassByPrimaryKeyQuery
	 */
	public static TypedQuery<Stock> getStockByPrimaryKeyQuery() {
		return stockByPrimaryKeyQuery;
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

	public double getProductionCoefficient() {
		return this.productionCoefficient;
	}

	public void setProductionCoefficient(double productionCoefficient) {
		this.productionCoefficient = productionCoefficient;
	}

	public double getConsumptionCoefficient() {
		return consumptionCoefficient;
	}

	public void setConsumptionCoefficient(double consumptionCoefficient) {
		this.consumptionCoefficient = consumptionCoefficient;
	}

	public double getQuantity() {
		return this.quantity;
	}

	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}

	public double getReplenishmentDemand() {
		return replenishmentDemand;
	}

	public void setReplenishmentDemand(double quantityDemanded) {
		this.replenishmentDemand = MathStuff.round(quantityDemanded);
	}

	public String getStockType() {
		return pk.stockType;
	}

	public void setStockType(String stockType) {
		this.pk.stockType = stockType;
	}

	public String getCommodityName() {
		return pk.commodity;
	}

	public void setCommodityName(String commodityName) {
		pk.commodity = commodityName;
	}

	public String getOwner() {
		return pk.owner;
	}

	public void setTimeStamp(int timeStamp) {
		pk.timeStamp = timeStamp;
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

	public double getIntrinsicValue() {
		return value / Global.getGlobal().getMelt();
	}

	/**
	 * @return the price
	 */
	public double getPrice() {
		return price;
	}

	/**
	 * @return the intrinsic expression of the price
	 */

	public double getIntrinsicPrice() {
		return price / Global.getGlobal().getMelt();
	}

	/**
	 * @param price
	 *            the price to set
	 */
	public void setPrice(double price) {
		this.price = price;
	}

	public Integer getProject() {
		return pk.project;
	}

	public int getTimeStamp() {
		return pk.timeStamp;
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
	 * @param stockUsedUp the stockUsedUp to set
	 */
	public void setStockUsedUp(double stockUsedUp) {
		this.stockUsedUp = stockUsedUp;
	}

}