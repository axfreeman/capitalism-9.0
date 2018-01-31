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
import java.util.Observable;
import javax.persistence.*;

import org.apache.commons.math3.util.Precision;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.beans.property.ReadOnlyStringWrapper;
import rd.dev.simulation.Simulation;
import rd.dev.simulation.datamanagement.DataManager;
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
		@NamedQuery(name = "UseValues.findAll", query = "SELECT u FROM UseValue u"),
		@NamedQuery(name = "UseValues.Primary", query = "SELECT u FROM UseValue u where u.pk.project= :project AND u.pk.timeStamp= :timeStamp and u.pk.useValueName=:useValueName"),
		@NamedQuery(name = "UseValues.project.timeStamp", query = "SELECT u FROM UseValue u where u.pk.project= :project and u.pk.timeStamp = :timeStamp"),
		@NamedQuery(name = "UseValues.productive", query = "SELECT u FROM UseValue u where u.pk.project= :project and u.pk.timeStamp = :timeStamp and u.useValueCircuitType='Capitalist'"),
		@NamedQuery(name = "UseValueType", query = "SELECT u FROM UseValue u where u.pk.project= :project and u.pk.timeStamp = :timeStamp and u.useValueType=:useValueType")
})
@Embeddable
public class UseValue extends Observable implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger("Commodity");

	// The primary key (composite key containing project, timeStamp and productUseValueName)
	@EmbeddedId protected UseValuePK pk;

	@Column(name = "useValueCircuitType") private String useValueCircuitType; // describes the way this is produced (by capitalist production, or socially)
	@Column(name = "description") private String description; // TODO redundant, eliminate at some point
	@Column(name = "turnoverTime") private double turnoverTime;
	@Column(name = "unitValue") private double unitValue;
	@Column(name = "unitPrice") private double unitPrice;
	@Column(name = "totalSupply") private double totalSupply;// registers the total commodities up for sale
	@Column(name = "totalQuantity") private double totalQuantity; // bigger than total supply because it includes commodities not available for purchase
	@Column(name = "totalDemand") private double totalDemand;
	@Column(name = "surplus") private double surplus; // if after production there is an excess of inventory over use, it is recorded here
	@Column(name = "totalValue") private double totalValue;// corresponds to total quantity; is the value of all commodities of this type
	@Column(name = "totalPrice") private double totalPrice;// corresponds to total quantity - price of all commodities of this type
	@Column(name = "allocationShare") private double allocationShare;// proportion of total demand that can actually be supplied
	@Column(name = "useValueType") private USEVALUETYPE useValueType;// see enum USEVALUENAME for list of possible types
	@Transient private UseValue comparator;

	/**
	 * Types of commodities, basis of a rudimentary typology system for use values
	 * 
	 * @author afree
	 *
	 */
	public enum USEVALUETYPE {
		LABOURPOWER("Labour Power"), MONEY("Money"), PRODUCTIVE("Productive"), NECESSITIES("Necessities"), LUXURIES("Luxuries");
		String text;

		USEVALUETYPE(String text) {
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
	 * Readable constants to refer to the methods which provide information about the persistent members of the class
	 */
	public enum Selector {
		USEVALUENAME, USEVALUECIRCUITTYPE, TURNOVERTIME, UNITVALUE, UNITPRICE, TOTALSUPPLY, TOTALQUANTITY, TOTALDEMAND, SURPLUS, TOTALVALUE, TOTALPRICE, ALLOCATIONSHARE, USEVALUETYPE
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
	 *            TODO get BeanUtils to do this, or find some other way. There must be a better way but many people complain about it
	 */

	public void copyUseValue(UseValue useValueTemplate) {
		this.pk.timeStamp = useValueTemplate.pk.timeStamp;
		this.pk.useValueName = useValueTemplate.pk.useValueName;
		this.pk.project = useValueTemplate.pk.project;
		this.useValueCircuitType = useValueTemplate.useValueCircuitType;
		this.description = useValueTemplate.description;
		this.turnoverTime = useValueTemplate.turnoverTime;
		this.unitValue = useValueTemplate.unitValue;
		this.unitPrice = useValueTemplate.unitPrice;
		this.totalSupply = useValueTemplate.totalSupply;
		this.totalQuantity = useValueTemplate.totalQuantity;
		this.totalDemand = useValueTemplate.totalDemand;
		this.surplus = useValueTemplate.surplus;
		this.totalValue = useValueTemplate.totalValue;
		this.totalPrice = useValueTemplate.totalPrice;
		this.allocationShare = useValueTemplate.allocationShare;
		this.useValueType = useValueTemplate.useValueType;
	}

	/**
	 * Calculate the total supply of this use value by consulting all salesStocks (these can belong either to circuits or social classes).
	 * In general many circuits will produce the same use value. For a simple model there will only be one.
	 * TODO upgrade this to many circuits: critical to the dynamics of technical change
	 * 
	 */
	public void registerSupply() {
		totalSupply = 0.0;
		for (Stock s : DataManager.stocksSalesByUseValue(this.pk.timeStamp, pk.useValueName)) {
			totalSupply += s.getQuantity();
		}
		Reporter.report(logger, 1, "  The quantity of commodity [%s] that can be supplied from sales inventories is %.2f. ", pk.useValueName, totalSupply);
	}

	/**
	 * Calculate the total quantity, value and price of this useValue, from the stocks of this useValue
	 * Validate against existing total if requested
	 * 
	 * @param validate
	 *            report if the result differs from what is already there.
	 */

	public void calculateAggregates(boolean validate) {
		double oldTotalQuantity = totalQuantity;
		double oldTotalValue = totalValue;
		double oldTotalPrice = totalPrice;
		double totalQuantity = 0;
		double totalValue = 0;
		double totalPrice = 0;
		for (Stock s : DataManager.stocksByUseValue(this.pk.timeStamp, pk.useValueName)) {
			totalQuantity += s.getQuantity();
			totalValue += s.getValue();
			totalPrice += s.getPrice();
			logger.debug(String.format("  Stock of type [%s] with name [%s] has added quantity %.2f; value %.2f, and price %.2f. ",
					s.getStockType(), s.getCircuit(), s.getQuantity(), s.getPrice(), s.getValue()));
		}
		totalQuantity = Precision.round(totalQuantity, Simulation.getRoundingPrecision());
		totalValue = Precision.round(totalValue, Simulation.getRoundingPrecision());
		totalPrice = Precision.round(totalPrice, Simulation.getRoundingPrecision());
		if (validate) {
			if (totalQuantity != getTotalQuantity()) {
				Reporter.report(logger, 2, "  ALERT: Total quantity of [%s] (%.2f) is different from registered quantity (%.2f)", pk.useValueName,
						totalQuantity, oldTotalQuantity);
			}
			if (totalValue != getTotalValue()) {
				Reporter.report(logger, 2, "  ALERT: Total value of [%s] (%.2f) is different from registered value (%.2f)", pk.useValueName, totalValue,
						oldTotalValue);
			}
			if (totalPrice != getTotalPrice()) {
				Reporter.report(logger, 2, "  ALERT: Total price of [%s] (%.2f) is different from registered price (%.2f)", pk.useValueName, totalPrice,
						oldTotalPrice);
			}
		}
		setTotalQuantity(totalQuantity);
		setTotalValue(totalValue);
		setTotalPrice(totalPrice);
		Reporter.report(logger, 2, "  Total quantity of the commodity [%s] is %.2f (value %.2f, price %.2f). ",
				pk.useValueName, totalQuantity, totalPrice, totalValue);
	}

	/**
	 * informs the display whether the selected member of this entity has changed, compared with the 'comparator' UseValue which normally
	 * comes from a different timeStamp.
	 * 
	 * We don't mind the hardwiring because we don't really intend this code to be re-usable, it's not hard to modify, and it results in compact
	 * and readable usage code (see (@link TabbedTableViewer#populateUseValuesViewTable})
	 * 
	 * @param selector
	 *            chooses which member to evaluate
	 * @return whether this member has changed or not. False if selector is unavailable here
	 */

	public boolean changed(Selector selector) {
		switch (selector) {
		case USEVALUENAME:
			return false;
		case USEVALUECIRCUITTYPE:
			return false;
		case UNITPRICE:
			return unitPrice != comparator.getUnitPrice();
		case UNITVALUE:
			return unitValue != comparator.getUnitValue();
		case TOTALVALUE:
			return totalValue != comparator.totalValue;
		case TOTALPRICE:
			return totalPrice != comparator.totalPrice;
		case TOTALQUANTITY:
			return totalQuantity != comparator.totalQuantity;
		case TOTALSUPPLY:
			return totalSupply != comparator.getTotalSupply();
		case TOTALDEMAND:
			return totalDemand != comparator.totalDemand;
		case SURPLUS:
			return surplus != comparator.surplus;
		case TURNOVERTIME:
			return turnoverTime != comparator.getTurnoverTime();
		case ALLOCATIONSHARE:
			return allocationShare != comparator.allocationShare;
		default:
			return false;
		}
	}

	/**
	 * provides a wrapped version of the selected member which the display will recognise, as a ReadOnlyStringWrapper.
	 * 
	 * We don't mind the hardwiring because we don't really intend this code to be re-usable, it's not hard to modify, and it results in compact
	 * and readable usage code (see (@link TabbedTableViewer#populateUseValuesViewTable})
	 * 
	 * @param selector
	 *            chooses which member to evaluate
	 * @return a String representation of the members, formatted according to the relevant format string
	 */

	public ReadOnlyStringWrapper wrappedString(Selector selector) {
		switch (selector) {
		case USEVALUENAME:
			return new ReadOnlyStringWrapper(pk.useValueName);
		case USEVALUECIRCUITTYPE:
			return new ReadOnlyStringWrapper(useValueCircuitType);
		case UNITPRICE:
			return new ReadOnlyStringWrapper(String.format(ViewManager.smallNumbersFormatString, unitPrice));
		case UNITVALUE:
			return new ReadOnlyStringWrapper(String.format(ViewManager.smallNumbersFormatString, unitValue));
		case TOTALVALUE:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, totalValue));
		case TOTALPRICE:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, totalPrice));
		case TOTALQUANTITY:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, totalQuantity));
		case TOTALSUPPLY:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, totalSupply));
		case TOTALDEMAND:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, totalDemand));
		case SURPLUS:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, surplus));
		case TURNOVERTIME:
			return new ReadOnlyStringWrapper(String.format(ViewManager.smallNumbersFormatString, turnoverTime));
		case ALLOCATIONSHARE:
			return new ReadOnlyStringWrapper(String.format(ViewManager.smallNumbersFormatString, allocationShare));
		case USEVALUETYPE:
			return new ReadOnlyStringWrapper(useValueType.text);
		default:
			return null;
		}
	}

	/**
	 * sets a comparator use value, which comes from a different timestamp. This informs the 'change' method which
	 * communicates to the GUI interface so it knows to colour changed magnitudes differently.
	 */
	public void setComparator() {
		this.comparator = DataManager.useValueByPrimaryKey(pk.project, Simulation.getTimeStampComparatorCursor(), pk.useValueName);
	}

	/**
	 * Rudimentary typology of use values
	 * 
	 * @return the type of this useValue, as given by the {@code USEVALUENAME} enum
	 */

	public USEVALUETYPE getUseValueType() {
		return useValueType;
	}

	// GETTERS AND SETTERS FOR THE PERSISTENT MEMBERS

	public String getUseValueName() {
		return pk.useValueName;
	}

	public int getTimeStamp() {
		return pk.timeStamp;
	}

	public int getProject() {
		return pk.project;
	}

	public double getTotalSupply() {
		return totalSupply;
	}

	public void setTotalSupply(double totalSupply) {
		this.totalSupply = totalSupply;
	}

	public double getTotalQuantity() {
		return totalQuantity;
	}

	public void setTotalQuantity(double totalQuantity) {
		this.totalQuantity = totalQuantity;
	}

	public double getTotalDemand() {
		return totalDemand;
	}

	public void setTotalDemand(double totalDemand) {
		this.totalDemand = totalDemand;
	}

	public double getTotalValue() {
		return totalValue;
	}

	public void setTotalValue(double totalValue) {
		this.totalValue = totalValue;
	}

	public double getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(double totalPrice) {
		this.totalPrice = totalPrice;
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

	public String getUseValueCircuitType() {
		return this.useValueCircuitType;
	}

	public void setUseValueCircuitType(String useValueCircuitType) {
		this.useValueCircuitType = useValueCircuitType;
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
	 * @return the surplus
	 */
	public double getSurplus() {
		return surplus;
	}

	/**
	 * @param surplus
	 *            the surplus to set
	 */
	public void setSurplus(double surplus) {
		this.surplus = surplus;
	}
}