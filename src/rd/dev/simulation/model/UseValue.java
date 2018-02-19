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
import java.util.Observable;
import javax.persistence.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.beans.property.ReadOnlyStringWrapper;
import rd.dev.simulation.datamanagement.DataManager;
import rd.dev.simulation.utils.MathStuff;
import rd.dev.simulation.utils.Reporter;
import rd.dev.simulation.view.ViewManager;

/**
 * UseValue is the persistent class for the usevalues database table.
 * <p>
 * the embedded primary key is the associated class UseValuePK. All members of the primary key can be accessed via getters and setters in this, the main
 * UseValue class
 */
@Entity
@Table(name = "usevalues")
@NamedQueries({
		@NamedQuery(name = "Primary", query = "SELECT u FROM UseValue u where u.pk.project= :project AND u.pk.timeStamp= :timeStamp and u.pk.useValueName=:useValueName"),
		@NamedQuery(name = "All", query = "SELECT u FROM UseValue u where u.pk.project= :project and u.pk.timeStamp = :timeStamp"),
		@NamedQuery(name = "CommodityOriginType", query = "SELECT u FROM UseValue u where u.pk.project= :project and u.pk.timeStamp = :timeStamp and u.commodityOriginType=:commodityOriginType"),
		@NamedQuery(name = "CommodityFunctionType", query = "SELECT u FROM UseValue u where u.pk.project= :project and u.pk.timeStamp = :timeStamp and u.commodityFunctionType=:commodityFunctionType order by u.displayOrder")

		// UseValueFunctionType
})
@Embeddable
public class UseValue extends Observable implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger("Commodity");

	// The primary key (composite key containing project, timeStamp and industryName)
	@EmbeddedId protected UseValuePK pk;

	@Column(name = "commodityOriginType") private COMMODITY_ORIGIN_TYPE commodityOriginType; // whether this is produced by an enterprise or a class
	@Column(name = "commodityFunctionType") private COMMODITY_FUNCTION_TYPE commodityFunctionType;// see enum COMMODITY_FUNCTION_TYPE for list of possible types
	@Column(name = "turnoverTime") private double turnoverTime;
	@Column(name = "unitValue") private double unitValue;
	@Column(name = "unitPrice") private double unitPrice;
	@Column(name = "surplusProduct") private double surplusProduct; // if after production there is an excess of inventory over use, it is recorded here
	@Column(name = "allocationShare") private double allocationShare;// proportion of total demand that can actually be supplied
	@Column(name = "stockUsedUp") private double stockUsedUp; // stock used up in production in the current period
	@Column(name = "stockProduced") private double stockProduced; // stock produced in the current period
	@Column(name = "imageName") private String imageName; // a graphical image that can be used in column headers in place of text
	@Column(name = "displayOrder") private int displayOrder; // used to determine which order to display columns

	@Transient double surplusRemaining;// records, temporarily, what remains of the surplus product after an industry has expanded

	@Transient private UseValue comparator;
	@Transient private UseValue previousComparator;
	@Transient private UseValue startComparator;
	@Transient private UseValue customComparator;
	@Transient private UseValue endComparator;

	/**
	 * Basic classification of commodity types: how are they used?
	 * 
	 */
	public enum COMMODITY_FUNCTION_TYPE {
		MONEY("Money"), PRODUCTIVE_INPUT("Productive Inputs"), CONSUMER_GOOD("Consumer Goods");
		String text;

		COMMODITY_FUNCTION_TYPE(String text) {
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
	 * Second Basic classification of commodity types (overlaps COMMODITY_FUNCTION_TYPE): how do they come into being?
	 *
	 */
	public enum COMMODITY_ORIGIN_TYPE {
		SOCIALlY_PRODUCED("Social"), INDUSTRIALLY_PRODUCED("Capitalist"), MONEY("Money");
		String text;

		COMMODITY_ORIGIN_TYPE(String text) {
			this.text = text;
		}

		/**
		 * @return the text associated with this type - normally, so it can be displayed for the user
		 */

		public String getText() {
			return text;
		}
	}

	/**
	 * Readable constants to refer to the methods which provide information about the persistent members of the class
	 */
	public enum USEVALUE_SELECTOR {
		// @formatter:off
		USEVALUENAME("Commodity",null,null), 
		USEVALUEOWNERTYPE("Owner Type",null,null), 
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

		USEVALUE_SELECTOR(String text, String imageName, String toolTip) {
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
	 * Constructor for a UseValue entity.
	 * Returns a 'hollow' UseValue with a hollow primary key; it has not been persisted and can therefore contain an inconsistent primary key,
	 * which must be properly set before the entity is committed to the database
	 */

	public UseValue() {
		this.pk = new UseValuePK();
	}

	/**
	 * make a carbon copy of the useValueTemplate
	 * 
	 * @param useValueTemplate
	 *            the useValue bean to copy - usually the one from the previous timeStamp
	 */

	public void copyUseValue(UseValue useValueTemplate) {
		this.pk.timeStamp = useValueTemplate.pk.timeStamp;
		this.pk.useValueName = useValueTemplate.pk.useValueName;
		this.pk.project = useValueTemplate.pk.project;
		this.commodityOriginType = useValueTemplate.commodityOriginType;
		this.turnoverTime = useValueTemplate.turnoverTime;
		this.unitValue = useValueTemplate.unitValue;
		this.unitPrice = useValueTemplate.unitPrice;
		this.surplusProduct = useValueTemplate.surplusProduct;
		this.allocationShare = useValueTemplate.allocationShare;
		this.commodityFunctionType = useValueTemplate.commodityFunctionType;
		this.stockUsedUp = useValueTemplate.stockUsedUp;
		this.stockProduced = useValueTemplate.stockProduced;
		this.imageName = useValueTemplate.imageName;
		this.displayOrder = useValueTemplate.displayOrder;
	}

	/**
	 * Calculate the total quantity, value and price of this useValue, from the stocks of this useValue
	 * Validate against existing total if requested
	 * 
	 * @param validate
	 *            report if the result differs from what is already there.
	 */

	public void calculateAggregates(boolean validate) {
		double totalQuantity = 0;
		double totalValue = 0;
		double totalPrice = 0;
		for (Stock s : DataManager.stocksByUseValue(this.pk.timeStamp, pk.useValueName)) {
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
				pk.useValueName, totalQuantity, totalPrice, totalValue);
	}

	/**
	 * provides a wrapped version of the selected member which the display will recognise, as a ReadOnlyStringWrapper.
	 * 
	 * We don't mind the hardwiring because we don't really intend this code to be re-usable, it's not hard to modify, and it results in compact
	 * and readable usage code (see (@link TabbedTableViewer#populateUseValuesViewTable})
	 * 
	 * @param USEVALUE_SELECTOR
	 *            chooses which member to evaluate
	 * @return a String representation of the members, formatted according to the relevant format string
	 */

	public ReadOnlyStringWrapper wrappedString(USEVALUE_SELECTOR USEVALUE_SELECTOR) {
		switch (USEVALUE_SELECTOR) {
		case USEVALUENAME:
			return new ReadOnlyStringWrapper(pk.useValueName);
		case USEVALUEOWNERTYPE:
			return new ReadOnlyStringWrapper(commodityOriginType.getText());
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
	 * informs the display whether the selected member of this entity has changed, compared with the 'comparator' UseValue which normally
	 * comes from a different timeStamp.
	 * 
	 * We don't mind the hardwiring because we don't really intend this code to be re-usable, it's not hard to modify, and it results in compact
	 * and readable usage code (see (@link TabbedTableViewer#populateUseValuesViewTable})
	 * 
	 * @param uSEVALUE_SELECTOR
	 *            chooses which member to evaluate
	 * @return whether this member has changed or not. False if selector is unavailable here
	 */

	public boolean changed(USEVALUE_SELECTOR uSEVALUE_SELECTOR) {
		chooseComparison();
		switch (uSEVALUE_SELECTOR) {
		case USEVALUENAME:
			return false;
		case USEVALUEOWNERTYPE:
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
	 * @param useValueSelector
	 *            chooses which field to evaluate
	 * 
	 * @param item
	 *            the original item - returned as the result if there is no change
	 * 
	 * @return the original item if nothing has changed, otherwise the change, as an appropriately formatted string
	 */

	public String showDelta(String item, USEVALUE_SELECTOR useValueSelector) {
		chooseComparison();
		if (!changed(useValueSelector))
			return item;
		switch (useValueSelector) {
		case USEVALUENAME:
		case USEVALUEOWNERTYPE:
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
	 * @return a list of industries that produce this useValue
	 */
	public List<Industry> industries() {
		return DataManager.industriesByCommodityName(pk.timeStamp, pk.useValueName);
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
	public void setComparator(UseValue comparator) {
		this.comparator = comparator;
	}

	/**
	 * 
	 * @return the function of this useValue, as given by the {@code COMMODITY_FUNCTION_TYPE} enum
	 */

	public COMMODITY_FUNCTION_TYPE getCommodityFunctionType() {
		return commodityFunctionType;
	}

	// GETTERS AND SETTERS FOR THE PERSISTENT MEMBERS

	public String commodityName() {
		return pk.useValueName;
	}

	public int getTimeStamp() {
		return pk.timeStamp;
	}

	public int getProject() {
		return pk.project;
	}

	public double totalSupply() {
		double supply = 0.0;
		for (Stock s : DataManager.stocksSalesByUseValue(pk.timeStamp, pk.useValueName)) {
			supply += s.getQuantity();
		}
		return supply;
	}

	/**
	 * The total replenishment demand from all stocks of this useValue
	 * 
	 * @return replenishment demand from all stocks of this useValue
	 */

	public double replenishmentDemand() {
		double demand = 0.0;
		for (Stock s : DataManager.stocksByUseValue(pk.timeStamp, pk.useValueName)) {
			demand += s.getReplenishmentDemand();
		}
		return demand;
	}

	/**
	 * The total expansion demand from all stocks of this useValue
	 * 
	 * @return expansion demand from all stocks of this useValue
	 */

	public double expansionDemand() {
		double demand = 0.0;
		for (Stock s : DataManager.stocksByUseValue(pk.timeStamp, pk.useValueName)) {
			demand += s.getExpansionDemand();
		}
		return demand;
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

	public COMMODITY_ORIGIN_TYPE getCommodityOriginType() {
		return this.commodityOriginType;
	}

	public void setCommodityOriginType(COMMODITY_ORIGIN_TYPE useValueIndustryType) {
		this.commodityOriginType = useValueIndustryType;
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
		for (Stock s : DataManager.stocksByUseValue(pk.timeStamp, pk.useValueName)) {
			totalValue += s.getValue();
		}
		return totalValue;
	}

	/**
	 * @return the total price of this use value in the economy at this time
	 */

	public double totalPrice() {
		double totalPrice = 0;
		for (Stock s : DataManager.stocksByUseValue(pk.timeStamp, pk.useValueName)) {
			totalPrice += s.getPrice();
		}
		return totalPrice;
	}

	/**
	 * @return the total quantity of this use value in the economy at this time
	 */

	public double totalQuantity() {
		double totalQuantity = 0;
		for (Stock s : DataManager.stocksByUseValue(pk.timeStamp, pk.useValueName)) {
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
	public UseValue getPreviousComparator() {
		return previousComparator;
	}

	/**
	 * @param previousComparator
	 *            the previousComparator to set
	 */
	public void setPreviousComparator(UseValue previousComparator) {
		this.previousComparator = previousComparator;
	}

	/**
	 * @return the startComparator
	 */
	public UseValue getStartComparator() {
		return startComparator;
	}

	/**
	 * @param startComparator
	 *            the startComparator to set
	 */
	public void setStartComparator(UseValue startComparator) {
		this.startComparator = startComparator;
	}

	/**
	 * @return the customComparator
	 */
	public UseValue getCustomComparator() {
		return customComparator;
	}

	/**
	 * @param customComparator
	 *            the customComparator to set
	 */
	public void setCustomComparator(UseValue customComparator) {
		this.customComparator = customComparator;
	}

	/**
	 * @return the endComparator
	 */
	public UseValue getEndComparator() {
		return endComparator;
	}

	/**
	 * @param endComparator
	 *            the endComparator to set
	 */
	public void setEndComparator(UseValue endComparator) {
		this.endComparator = endComparator;
	}
}
