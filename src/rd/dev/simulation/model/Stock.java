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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.beans.property.ReadOnlyStringWrapper;
import rd.dev.simulation.Simulation;
import rd.dev.simulation.datamanagement.DataManager;
import rd.dev.simulation.utils.Dialogues;
import rd.dev.simulation.utils.Reporter;
import rd.dev.simulation.view.ViewManager;
import org.apache.commons.math3.util.Precision;

/**
 * The persistent class for the stocks database table. It extends the Observable Class so it can provide base data for TableViews.
 * TODO The additional functionality provided by the Observable Class is not used, but it probably should be.
 */
// TODO implement a proper inheritance strategy for stocks of different types,
// in particular money, consumer goods and productive stocks
// TODO is it better to have a single named query for all fields and parameterise it in DataManager? read up on optimization

@Entity
@Table(name = "stocks")
@NamedQueries({
		// select a single stock
		@NamedQuery(name = "Stocks.Primary", query = "SELECT s FROM Stock s WHERE s.pk.project=:project and s.pk.timeStamp =:timeStamp and s.pk.circuit =:circuit and s.pk.useValue= :useValue and s.pk.stockType=:stockType"),

		// select all stocks with the given project and timeStamp
		@NamedQuery(name = "Stocks.basic", query = "SELECT s FROM Stock s where s.pk.project= :project and s.pk.timeStamp = :timeStamp"),

		// select all productive stocks managed by a named circuit
		@NamedQuery(name = "Stocks.project.timeStamp.circuit.productive", query = "SELECT s FROM Stock s "
				+ "where s.pk.project= :project and s.pk.timeStamp = :timeStamp and s.pk.circuit= :circuit and s.pk.stockType='Productive'"),

		// select all stocks of the named useValue
		@NamedQuery(name = "Stocks.project.timeStamp.useValue", query = "SELECT s FROM Stock s where s.pk.project = :project and s.pk.timeStamp = :timeStamp and s.pk.useValue= :useValue"),

		// select the productive stock of the named useValue that is managed by the named circuit
		@NamedQuery(name = "Stocks.project.timeStamp.useValue.circuit", query = "SELECT s FROM Stock s where s.pk.project= :project and s.pk.timeStamp = :timeStamp and s.pk.stockType='Productive' and s.pk.useValue= :useValue and s.pk.circuit= :circuit"),

		// select sales stocks
		@NamedQuery(name = "Stocks.sales", query = "SELECT s FROM Stock s where s.pk.project = :project and s.pk.timeStamp = :timeStamp and s.pk.stockType='Sales' and s.pk.useValue= :useValue"),

		// select all stocks that are sources of demand (stocktypes Productive and Consumption)
		@NamedQuery(name = "Stocks.project.timeStamp.sourcesOfDemand", query = "SELECT s FROM Stock s where s.pk.project = :project and s.pk.timeStamp=:timeStamp "
				+ "and (s.pk.stockType = 'Productive' or s.pk.stockType='Consumption')"),

		// select all stocks of the given stockType
		@NamedQuery(name = "Stocks.project.timeStamp.stockType", query = "SELECT s FROM Stock s where s.pk.project = :project and s.pk.timeStamp=:timeStamp and s.pk.stockType=:stockType")
})

public class Stock extends Observable implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(Stock.class);

	@EmbeddedId protected StockPK pk;
	@Column(name = "ownertype") private String ownerType;
	@Column(name = "quantity") private double quantity;
	@Column(name = "coefficient") private double coefficient;
	@Column(name = "quantityDemanded") private double quantityDemanded;
	@Column(name = "value") private double value;
	@Column(name = "price") private double price;

	// These transient variables are used to construct the dynamic circuit table. See the documentation there for more explanation
	@Transient protected Stock comparator;

	/**
	 * Readable constants to refer to the methods which provide information about the persistent members of the class
	 */
	public enum Selector {
		CIRCUIT, OWNERTYPE, USEVALUE, STOCKTYPE, QUANTITY, COEFFICIENT, QUANTITYDEMANDED, VALUE, PRICE
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
	 * the UseValue entity of this Stock
	 * 
	 * @return the UseValue entity of this Stock
	 */
	public UseValue getUseValue() {
		return DataManager.useValueByName(pk.timeStamp, pk.useValue);
	}

	/**
	 * Make a carbon copy of the stock.
	 * At present, this is used to construct a comparator stock, so that changes can be highlighted ('differencing') in the display tables.
	 * It may have other uses, but I am not aware of them.
	 * 
	 * @param stockTemplate
	 *            the stock from which to copy
	 */
	public void copyStock(Stock stockTemplate) {
		pk.timeStamp = stockTemplate.pk.timeStamp;
		pk.project = stockTemplate.pk.project;
		pk.circuit = stockTemplate.pk.circuit;
		pk.useValue = stockTemplate.pk.useValue;
		pk.stockType = stockTemplate.pk.stockType;
		ownerType=stockTemplate.ownerType;
		coefficient = stockTemplate.coefficient;
		quantity = stockTemplate.quantity;
		quantityDemanded = stockTemplate.quantityDemanded;
		price = stockTemplate.price;
		value = stockTemplate.value;
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

		// TODO check that if both quantities are rounded properly, this test will work

		double unitValue = unitValue();
		double unitPrice = unitPrice();
		double extraValue=Precision.round(extraQuantity * unitValue,Simulation.getRoundingPrecision());
		double extraPrice=Precision.round(extraQuantity * unitPrice,Simulation.getRoundingPrecision());
		double newValue = value + extraValue;
		double newPrice = price + extraPrice;
		double newQuantity = quantity + extraQuantity;
		
		// a little consistency check
		
		if (newQuantity< -Simulation.getEpsilon()) {
			Dialogues.alert(logger, "Stock of " + pk.useValue + " owned by " + pk.circuit+ " has fallen below zero. ");
		}else if (newValue< -Simulation.getEpsilon()) {
			Dialogues.alert(logger, "Value of " + pk.useValue + " owned by " + pk.circuit+ " has fallen below zero. ");
		}else if (newPrice< -Simulation.getEpsilon()) {
			Dialogues.alert(logger, "Price of " + pk.useValue + " owned by " + pk.circuit+ " has fallen below zero. ");
		}
		quantity = newQuantity;
		value = newValue;
		price = newPrice;
		
		// now adjust the use value totals and the global totals to keep track
		
		UseValue useValue=getUseValue();
		double newUseValueQuantity=useValue.getTotalQuantity()+extraQuantity;
		double newUseValueValue=useValue.getTotalValue()+extraValue;
		double newUseValuePrice=useValue.getTotalPrice()+extraPrice;
		useValue.setTotalQuantity(newUseValueQuantity);
		useValue.setTotalValue(newUseValueValue);
		useValue.setTotalPrice(newUseValuePrice);
		Global global=DataManager.getGlobal(Simulation.timeStampIDCurrent);
		global.setTotalValue(global.getTotalValue()+extraValue);
		global.setTotalPrice(global.getTotalPrice());
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
		double oldValue=value;
		double oldTotalValue=getUseValue().getTotalValue();
		modifyBy(quantity);
		setValue(oldValue+valueAdded); // overwrite what was done by the simple call to changeBy
		getUseValue().setTotalValue(oldTotalValue+valueAdded);
		Global global =DataManager.getGlobal(Simulation.timeStampIDCurrent);
		global.setTotalValue(global.getTotalValue()+valueAdded);
	}

	/**
	 * Set the size of the stock to the Quantity and adjust the value and price accordingly. Throw runtime error if the result would be less than zero
	 * 
	 * @param newQuantity
	 *            the quantity to be added to the size of the stock (negative if subtracted)
	 */
	public void modifyTo(double newQuantity) {
		Global global = DataManager.getGlobal(Simulation.timeStampIDCurrent);
		double melt = global.getMelt();
		double unitValue = unitValue();
		double unitPrice = unitPrice();
		double newValue = Precision.round(newQuantity * unitValue,Simulation.getRoundingPrecision());
		double newPrice = Precision.round(newQuantity * unitPrice,Simulation.getRoundingPrecision());
		double changeInValue=newValue-value;
		double changeInPrice=newPrice-price;
		double changeInQuantity=newQuantity-quantity;
		quantity = newQuantity;
		value = newValue;
		price = newPrice;
		Reporter.report(logger, 2, "  Size of commodity [%s], of type [%s], owned by [%s]: is %.2f. Value set to $%.2f (intrinsic %.2f), and price to %.2f (intrinsic %.2f)", 
				pk.useValue, pk.stockType, pk.circuit, quantity,value, value/melt, price, price/melt);
		UseValue useValue=getUseValue();
		useValue.setTotalQuantity(useValue.getTotalQuantity()+changeInQuantity);
		useValue.setTotalValue(useValue.getTotalValue()+changeInValue);
		useValue.setTotalPrice(useValue.getTotalPrice()+changeInPrice);
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
			return ViewManager.valueExpression(value, ViewManager.valuesExpressionDisplay); 
		case PRICE:
			return ViewManager.valueExpression(price, ViewManager.pricesExpressionDisplay); 
		default:
			throw new RuntimeException("ERROR: unknown attribute selector");
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
		case CIRCUIT:
			return new ReadOnlyStringWrapper(pk.circuit);
		case OWNERTYPE:
			return new ReadOnlyStringWrapper(ownerType);
		case USEVALUE:
			return new ReadOnlyStringWrapper(pk.useValue);
		case STOCKTYPE:
			return new ReadOnlyStringWrapper(pk.stockType);
		case QUANTITY:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, quantity));
		case VALUE:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, ViewManager.valueExpression(value,ViewManager.valuesExpressionDisplay)));
		case PRICE:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, ViewManager.valueExpression(price,ViewManager.pricesExpressionDisplay)));
		case QUANTITYDEMANDED:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, quantityDemanded));
		case COEFFICIENT:
			return new ReadOnlyStringWrapper(String.format(ViewManager.smallNumbersFormatString, coefficient));
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
	 * and readable usage code (see (@link TabbedTableViewer#populateUseValuesViewTable})
	 * 
	 * @param selector
	 *            chooses which member to evaluate
	 * @return whether this member has changed or not. False if selector is unavailable here
	 */

	public boolean changed(Selector selector) {
		switch (selector) {
		case CIRCUIT:
		case USEVALUE:
		case STOCKTYPE:
			return false;
		case QUANTITY:
			return quantity != comparator.quantity;
		case VALUE:
			return value != comparator.value;
		case PRICE:
			return price != comparator.price;
		case QUANTITYDEMANDED:
			return quantityDemanded != comparator.quantityDemanded;
		case COEFFICIENT:
			return coefficient != comparator.quantityDemanded;
		default:
			return false;
		}
	}
	
	/**
	 * Part of primitive typology of use values
	 * @return  the use value type of this stock
	 */
	
	public UseValue.USEVALUETYPE useValueType(){
		UseValue useValue=DataManager.useValueByName(Simulation.timeStampIDCurrent, pk.useValue);
		return useValue.useValueType();
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
		return String.format("[ %12.12s.%12.12s.%12.12s]", pk.stockType, pk.circuit, pk.useValue);
	}

	/**
	 * get the unit price of the use value that this stock contains
	 * 
	 * @return the unit price of the use value that this stock contains
	 */
	private double unitPrice() {
		return getUseValue().getUnitPrice();
	}

	/**
	 * get the unit value of the use value that this stock contains
	 * 
	 * @return the unit value of the use value that this stock contains
	 */
	private double unitValue() {
		return getUseValue().getUnitValue();
	}

	public double getCoefficient() {
		return this.coefficient;
	}

	public void setCoefficient(double coefficient) {
		this.coefficient = coefficient;
	}

	public double getQuantity() {
		return this.quantity;
	}

	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}

	public double getQuantityDemanded() {
		return quantityDemanded;
	}

	public void setQuantityDemanded(double quantityDemanded) {
		this.quantityDemanded = quantityDemanded;
	}

	public String getStockType() {
		return pk.stockType;
	}

	public void setStockType(String stockType) {
		this.pk.stockType = stockType;
	}

	public String getUseValueName() {
		return pk.useValue;
	}

	public void setUseValueName(String useValueName) {
		pk.useValue = useValueName;
	}

	public String getCircuit() {
		return pk.circuit;
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

	public Integer getProject() {
		return pk.project;
	}

	public int getTimeStamp() {
		return pk.timeStamp;
	}

	/**
	 * set the comparator stock.
	 */
	public void setComparator() {
		comparator = DataManager.stockByPrimaryKey(pk.project, Simulation.getTimeStampComparatorCursor(), pk.circuit, pk.useValue, pk.stockType);
	}
}