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
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.math3.util.Precision;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.beans.property.ReadOnlyStringWrapper;
import rd.dev.simulation.Simulation;
import rd.dev.simulation.custom.TabbedTableViewer;
import rd.dev.simulation.datamanagement.DataManager;
import rd.dev.simulation.model.Stock.ValueExpression;
import rd.dev.simulation.model.UseValue.USEVALUETYPE;
import rd.dev.simulation.utils.Dialogues;
import rd.dev.simulation.utils.MathStuff;
import rd.dev.simulation.utils.Reporter;
import rd.dev.simulation.view.ViewManager;

@Entity
@Table(name = "circuits")
@NamedQueries({
		@NamedQuery(name = "All", query = "Select c from Circuit c where c.pk.project = :project and c.pk.timeStamp = :timeStamp"),
		@NamedQuery(name = "Primary", query = "Select c from Circuit c where c.pk.project= :project and c.pk.timeStamp = :timeStamp and c.pk.productUseValueName= :productUseValueName"),
		@NamedQuery(name= "InitialCapital", query= "Select sum(c.initialCapital) from Circuit c where c.pk.project=:project and c.pk.timeStamp=:timeStamp")
})

@XmlRootElement
public class Circuit extends Observable implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger("Industry"); // TODO change name throughout to 'industry'

	// TODO at present it's not possible to have multiple circuits producing the same use value. It should be.
	// TODO circuits should have a name that is distinct from what they produce

	@EmbeddedId protected CircuitPK pk;
	@Column(name = "ConstrainedOutput") private double constrainedOutput;
	@Column(name = "ProposedOutput") private double proposedOutput;
	@Column(name = "InitialCapital") private double initialCapital;
	@Column(name = "Growthrate") private double growthRate;
	@Column(name = "costOfMPForExpansion") private double costOfMPForExpansion; // investment in Means of Production needed to achieve proposed expansion
	@Column(name = "costOfLPForExpansion") private double costOfLPForExpansion; // investment in Labour Power needed to achieve proposed expansion

	@Transient private Circuit comparator;

	/**
	 * Readable constants to refer to the methods which provide information about the persistent members of the class
	 */
	public enum Selector {
		PRODUCTUSEVALUETYPE, CONSTRAINEDOUTPUT, PROPOSEDOUTPUT, INITIALCAPITAL, CURRENTCAPITAL, PROFIT, PROFITRATE, PRODUCTIVESTOCKS, MONEYSTOCK, SALESSTOCK, TOTAL, GROWTHRATE
	}

	/**
	 * Says whether the industry produces means of production, necessities or luxuries.
	 * Not (yet) a persistent variable hence we have a method that reports on this.
	 * TODO fully abstract this from the text description of the industry.
	 * 
	 * @author afree
	 *
	 */
	public enum INDUSTRYTYPE {
		MEANSOFPRODUCTION, NECESSITIES, LUXURIES
	}

	/**
	 * A 'bare constructor' is required by JPA and this is it. However, when the new socialClass is constructed, the constructor does not automatically create a
	 * new PK entity. So we create a 'hollow' primary key which must then be populated by the caller before persisting the entity
	 */
	public Circuit() {
		this.pk = new CircuitPK();
	}

	/**
	 * make a carbon copy of a circuit template
	 * 
	 * @param circuitTemplate
	 *            the circuit to be copied into this one.
	 *            TODO get BeanUtils to do this, or find some other way. There must be a better way but many people complain about it
	 */
	public void copyCircuit(Circuit circuitTemplate) {
		pk.productUseValueName = circuitTemplate.getProductUseValueName();
		pk.timeStamp = circuitTemplate.getTimeStamp();
		pk.project = circuitTemplate.getProject();
		constrainedOutput = circuitTemplate.constrainedOutput;
		proposedOutput = circuitTemplate.proposedOutput;
		growthRate = circuitTemplate.growthRate;
		initialCapital = circuitTemplate.initialCapital;
	}

	/**
	 * @return what type of circuit this is
	 *         TODO improve on this
	 */

	public INDUSTRYTYPE industryType() {
		if (pk.productUseValueName.equals("Consumption"))
			return INDUSTRYTYPE.NECESSITIES;
		if (pk.productUseValueName.equals("Luxuries"))
			return INDUSTRYTYPE.LUXURIES;
		return INDUSTRYTYPE.MEANSOFPRODUCTION;
	}

	/**
	 * generic selector which returns a numerical attribute of the money stock depending on the {@link Stock.ValueExpression}
	 * 
	 * @param a
	 *            (QUANTITY, VALUE OR PRICE) selects whether to return the quantity, the value or the price of this stock
	 * @return the quantity of money if a=QUANTITY, etc.
	 */
	public double salesAttribute(Stock.ValueExpression a) {
		return getSalesStock().get(a);
	}

	/**
	 * generic selector which returns a numerical attribute of the money stock depending on the {@link Stock.ValueExpression}
	 * 
	 * @param a
	 *            (QUANTITY, VALUE OR PRICE) selects whether to return the quantity, the value or the price of this stock
	 * @return the quantity of money if a=QUANTITY, etc.
	 */
	public double moneyAttribute(Stock.ValueExpression a) {
		return getMoneyStock().get(a);
	}

	/**
	 * Calculate an aggregate, which may be price or value, of the stocks owned by this circuit excluding money
	 * NOTE it is the responsibility of the caller to commit any changes to persistent memory
	 * 
	 * @param a
	 *            an attribute from the enum class Stock.ValueExpression: selects one of QUANTITY, VALUE or PRICE. If QUANTITY, NaN is returned
	 * @return the total of the selected attribute owned by this circuit excluding money
	 */
	public double totalAttribute(Stock.ValueExpression a) {
		if (a == Stock.ValueExpression.QUANTITY) {
			return Double.NaN;
		}
		return salesAttribute(a) + productiveStocksAttribute(a);
	}

	// METHODS THAT REPORT THINGS WE NEED TO KNOW ABOUT THIS CIRCUIT BY INTERROGATING ITS STOCKS

	/**
	 * Retrieve the UseValue entity that this circuit produces
	 * 
	 * @return the UseValue that this circuit produces
	 */
	public UseValue getUseValue() {
		return DataManager.useValueByPrimaryKey(pk.project, pk.timeStamp, pk.productUseValueName);
	}

	/**
	 * Retrieve the total quantity, value or price of the productive stocks owned by this circuit, depending on the attribute
	 * 
	 * @return the total quantity, value or price of the productive stocks owned by this circuit, depending on the attribute
	 * @param a
	 *            an attribute from the enum class Stock.ValueExpression: selects one of QUANTITY, VALUE or PRICE. If QUANTITY, NaN is returned
	 * @return the total of the selected attribute owned by this circuit
	 */
	public Double productiveStocksAttribute(Stock.ValueExpression a) {
		if (a == Stock.ValueExpression.QUANTITY) {
			return Double.NaN;
		}
		double total = 0;
		for (Stock s : DataManager.stocksProductiveByCircuit(pk.timeStamp, pk.productUseValueName)) {
			total += s.get(a);
		}
		return total;
	}

	/**
	 * get the Stock of money owned by this circuit. If this stock does not exist (which is an error) return null.
	 * 
	 * @return the money stock that is owned by this social class.
	 */
	public Stock getMoneyStock() {
		return DataManager.stockMoneyByCircuitSingle(pk.timeStamp, pk.productUseValueName);
	}

	/**
	 * retrieve the sales Stock owned by this circuit. If this stock does not exist (which is an error) return null.
	 * 
	 * @return the sales stock owned by this circuit
	 */
	public Stock getSalesStock() {
		// TODO the product and the circuit have the same name, because the circuit is selling its own product
		// but if there are multiple producers of the same thing, the circuit should have an independent name of its own
		return DataManager.stockByPrimaryKey(Simulation.projectCurrent, pk.timeStamp, pk.productUseValueName, pk.productUseValueName, Stock.STOCKTYPE.SALES.text());
	}

	/**
	 * provides a wrapped version of the selected member which the display will recognise, as a ReadOnlyStringWrapper.
	 * 
	 * We don't mind the hardwiring because we don't really intend this code to be re-usable, it's not hard to modify, and it results in compact
	 * and readable usage code (see (@link TabbedTableViewer#populateUseValuesViewTable})
	 * 
	 * @param selector
	 *            chooses which member to evaluate
	 * @param valueExpression
	 *            selects the value DisplayAsExpression where relevant (QUANTITY, VALUE, PRICE)
	 * @return a String representation of the members, formatted according to the relevant format string
	 */

	public ReadOnlyStringWrapper wrappedString(Selector selector, Stock.ValueExpression valueExpression) {
		switch (selector) {
		case PRODUCTUSEVALUETYPE:
			return new ReadOnlyStringWrapper(pk.productUseValueName);
		case INITIALCAPITAL:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, initialCapital));
		case CONSTRAINEDOUTPUT:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, constrainedOutput));
		case PROPOSEDOUTPUT:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, proposedOutput));
		case GROWTHRATE:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, growthRate));
		case MONEYSTOCK:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, moneyAttribute(valueExpression)));
		case SALESSTOCK:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, salesAttribute(valueExpression)));
		case PRODUCTIVESTOCKS:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, productiveStocksAttribute(valueExpression)));
		case PROFIT:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, profit()));
		case PROFITRATE:
			return new ReadOnlyStringWrapper(String.format(ViewManager.smallNumbersFormatString, profitRate()));
		case TOTAL:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, totalAttribute(valueExpression)));
		case CURRENTCAPITAL:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, currentCapital()));
		default:
			return null;
		}
	}

	/**
	 * The value-expression of the magnitude of a named productive Stock managed by this circuit
	 * 
	 * @param productiveStockName
	 *            the useValue of the productive Stock
	 * 
	 * @return the magnitude of the named Stock, expressed as defined by {@code valueExpression}, null if this does not exist
	 */

	public ReadOnlyStringWrapper wrappedString(String productiveStockName) {
		try {
			Stock namedStock = DataManager.stockProductiveByNameSingle(pk.timeStamp, pk.productUseValueName, productiveStockName);
			String result = String.format(ViewManager.largeNumbersFormatString, namedStock.get(TabbedTableViewer.displayAttribute));
			return new ReadOnlyStringWrapper(result);
		} catch (Exception e) {
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
	 * @param selector
	 *            chooses which member to evaluate
	 * @param valueExpression
	 *            selects the display attribute where relevant (QUANTITY, VALUE, PRICE)
	 * @return whether this member has changed or not. False if selector is unavailable here
	 */

	public boolean changed(Selector selector, Stock.ValueExpression valueExpression) {
		switch (selector) {
		case PRODUCTUSEVALUETYPE:
			return false;
		case PROPOSEDOUTPUT:
			return proposedOutput != comparator.proposedOutput;
		case GROWTHRATE:
			return growthRate != comparator.growthRate;
		case CONSTRAINEDOUTPUT:
			return constrainedOutput != comparator.constrainedOutput;
		case INITIALCAPITAL:
			return initialCapital != comparator.initialCapital;
		case PROFITRATE:
			return profitRate() != comparator.profitRate();
		case PROFIT:
			return profit() != comparator.profit();
		case MONEYSTOCK:
			return moneyAttribute(valueExpression) != comparator.moneyAttribute(valueExpression);
		case SALESSTOCK:
			return salesAttribute(valueExpression) != comparator.salesAttribute(valueExpression);
		case PRODUCTIVESTOCKS:
			double p1 = productiveStocksAttribute(valueExpression);
			double p2 = comparator.productiveStocksAttribute(valueExpression);
			return !MathStuff.equals(p1, p2);
		case TOTAL:
			return totalAttribute(valueExpression) != comparator.totalAttribute(valueExpression);
		case CURRENTCAPITAL:
			return currentCapital() != comparator.currentCapital();
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
	 * @param valueExpression
	 *            selects the display attribute where relevant (QUANTITY, VALUE, PRICE)
	 * 
	 * @return the original item if nothing has changed, otherwise the change, as an appropriately formatted string
	 */

	public String showDelta(String item, Selector selector, Stock.ValueExpression valueExpression) {
		if (!changed(selector, valueExpression))
			return item;
		switch (selector) {
		case PRODUCTUSEVALUETYPE:
			return item;
		case PROPOSEDOUTPUT:
			return String.format(ViewManager.largeNumbersFormatString, (proposedOutput - comparator.proposedOutput));
		case GROWTHRATE:
			return String.format(ViewManager.smallNumbersFormatString, (growthRate - comparator.growthRate));
		case CONSTRAINEDOUTPUT:
			return String.format(ViewManager.largeNumbersFormatString, (constrainedOutput - comparator.constrainedOutput));
		case INITIALCAPITAL:
			return String.format(ViewManager.largeNumbersFormatString, (initialCapital - comparator.initialCapital));
		case PROFITRATE:
			return String.format(ViewManager.smallNumbersFormatString, (profitRate() - comparator.profitRate()));
		case PROFIT:
			return String.format(ViewManager.largeNumbersFormatString, (profit() - comparator.profit()));
		case MONEYSTOCK:
			return String.format(ViewManager.largeNumbersFormatString, (moneyAttribute(valueExpression) - comparator.moneyAttribute(valueExpression)));
		case SALESSTOCK:
			return String.format(ViewManager.largeNumbersFormatString, (salesAttribute(valueExpression) - comparator.salesAttribute(valueExpression)));
		case PRODUCTIVESTOCKS:
			double p1 = productiveStocksAttribute(valueExpression);
			double p2 = comparator.productiveStocksAttribute(valueExpression);
			return String.format(ViewManager.largeNumbersFormatString, (p1 - p2));
		case TOTAL:
			return String.format(ViewManager.largeNumbersFormatString, (totalAttribute(valueExpression) - comparator.totalAttribute(valueExpression)));
		case CURRENTCAPITAL:
			return String.format(ViewManager.largeNumbersFormatString, (currentCapital() - comparator.currentCapital()));
		default:
			return item;
		}
	}

	/**
	 * calculates the cost of producing at level proposedOutput, given constrainedOutput and the current level of productive inputs
	 * NOTE this cannot be reduced to a simple multiple of existing stocks, because some stocks may already exist. It is thus a non-linear function of Output
	 * NOTE the 'marginal' nature of this calculation doe`s not arise from the non-linearity of the production function itself (though in future extensions it
	 * could).
	 * It arises because some stocks already exist, so that the cost rises as a step function once the output level exceeds the required stock of each input
	 */

	public void calculateOutputCosts() {

		costOfMPForExpansion = 0.0;
		costOfLPForExpansion = 0.0;

		// ask each productive stock to tell us how much it would cost to increase that stock's size sufficient to produce the required output
		double proposedExpansion = proposedOutput - constrainedOutput;
		Reporter.report(logger, 1, " Calculating the cost to industry [%s] of expanding output by %.0f to %.0f from an output of %.0f",
				pk.productUseValueName, proposedExpansion, proposedOutput, constrainedOutput);
		for (Stock s : DataManager.stocksProductiveByCircuit(Simulation.timeStampIDCurrent, pk.productUseValueName)) {
			UseValue u = s.getUseValue();
			if (u == null) {
				Dialogues.alert(logger, "The use value [%s] does not exist", s.getUseValueName());
			} else {
				double coefficient = s.getCoefficient();
				double stockNewPrice = 0;
				double stockLevelRequired = coefficient * u.getTurnoverTime() * proposedExpansion;
				double stockLevelExisting = s.getQuantity();
				double stockNewRequired = stockLevelRequired - stockLevelExisting;
				if (stockNewRequired < 0) {
					Reporter.report(logger, 2,
							"  Circuit [%s] already has %.2f of productive input [%s] which is sufficient to produce at level %.2f, so incurs no extra cost",
							pk.productUseValueName, stockLevelExisting, s.getUseValueName(), proposedOutput);
					stockNewRequired = 0;
				} else {
					Reporter.report(logger, 2, "  Circuit [%s] has %.2f of productive input [%s] which requires an addition of %.2f to produce at level %.0f",
							pk.productUseValueName, stockLevelExisting, s.getUseValueName(), stockNewRequired, proposedOutput);
				}
				stockNewPrice = stockNewRequired * u.getUnitPrice();
				if (s.useValueType() == USEVALUETYPE.LABOURPOWER) {
					costOfLPForExpansion += stockNewPrice;
				} else {
					costOfMPForExpansion += stockNewPrice;
				}
				Reporter.report(logger, 2,
						"  Cost of sufficient stock of [%s] at unit price $%.2f is $%.2f of which Means of Production $%.2f and Labour Power $%.2f",
						s.getUseValueName(), u.getUnitPrice(), stockNewPrice, costOfMPForExpansion, costOfLPForExpansion);
			}
		}
		Reporter.report(logger, 1,
				" Circuit [%s] would spend $%.2f on means of production and $%.2f on labour power (totalling $%.2f), \n  to expand production by %.0f and attain an output of %.0f ",
				pk.productUseValueName, costOfMPForExpansion, costOfLPForExpansion, getCostOfExpansion(), proposedExpansion, proposedOutput);
	}

	/**
	 * get the quantity of the Stock of money owned by this circuit. Return NaN if the stock cannot be found (which is an error)
	 * 
	 * @return the quantity of money owned by this circuit.
	 */
	public double getMoneyQuantity() {
		Stock s = getMoneyStock();
		return s == null ? Float.NaN : s.getQuantity();
	}

	/**
	 * get the value of the Stock of money owned by this circuit. Return NaN if the stock cannot be found (which is an error)
	 * 
	 * @return the quantity of money owned by this circuit.
	 */
	public double getMoneyValue() {
		Stock s = getMoneyStock();
		return s == null ? Float.NaN : s.getValue();
	}

	/**
	 * get the price of the Stock of money owned by this circuit. Return NaN if the stock cannot be found (which is an error)
	 * 
	 * @return the price of money owned by this circuit.
	 */
	public double getMoneyPrice() {
		Stock s = getMoneyStock();
		return s == null ? Float.NaN : s.getPrice();
	}

	/**
	 * return the quantity of the Stock of sales owned by this circuit. Return NaN if the stock cannot be found (which is an error)
	 * 
	 * @return the quantity of sales stock owned by this circuit
	 */
	public double getSalesQuantity() {
		Stock s = getSalesStock();
		return s == null ? Float.NaN : s.getQuantity();
	}

	/**
	 * return the value of the sales stock owned by this circuit. Return NaN if the stock cannot be found (which is an error)
	 * 
	 * @return the value of the sales stock owned by this circuit
	 */
	public double getSalesValue() {
		Stock s = getSalesStock();
		return s == null ? Float.NaN : s.getValue();
	}

	/**
	 * return the price of the Stock of sales owned by this circuit. Return NaN if the stock cannot be found (which is an error)
	 * 
	 * @return the quantity of sales stock owned by this circuit
	 */
	public double getSalesPrice() {
		Stock s = getSalesStock();
		return s == null ? Float.NaN : s.getPrice();
	}

	/**
	 * set the quantity demanded of the Stock of consumption good owned by this social class. Report if the stock cannot be found (which is an error)
	 * 
	 * @param quantity
	 *            the quantity of the stock of consumption goods
	 */
	public void setSalesQuantity(double quantity) {
		Stock s = getSalesStock();
		if (s != null) {
			s.setQuantity(quantity);
		} else {
			logger.error("ERROR: Circuit {} attempted to set the quantity demanded of its consumption stock, but it does not have one", pk.productUseValueName);
		}
	}

	/**
	 * set the quantity demanded of the Stock of consumption good owned by this social class. Report if the stock cannot be found (which is an error)
	 * 
	 * @param quantity
	 *            the quantity of the stock of consumption goods
	 */
	public void setMoneyQuantity(double quantity) {
		Stock s = getMoneyStock();
		if (s != null) {
			s.setQuantity(quantity);
		} else {
			logger.error("ERROR: Circuit {} attempted to set the quantity demanded of its consumption stock, but it does not have one", pk.productUseValueName);
		}
	}

	/**
	 * provide a list of the productive stocks owned (managed) by this circuit
	 * 
	 * @return a list of the productive stocks owned (managed) by this circuit
	 */
	public List<Stock> productiveStocks() {
		return DataManager.stocksProductiveByCircuit(pk.timeStamp, pk.productUseValueName);
	}

	/**
	 * provide the productive stock with the given name
	 * 
	 * @return the productive stock with the given name
	 * @param name
	 *            the name of the stock
	 */
	public Stock productiveStock(String name) {
		return DataManager.stockProductiveByNameSingle(pk.timeStamp, pk.productUseValueName, name);
	}

	public Integer getProject() {
		return pk.project;
	}

	public int getTimeStamp() {
		return pk.timeStamp;
	}

	public void setTimeStamp(int timeStamp) {
		pk.timeStamp = timeStamp;
	}

	public String getProductUseValueName() {
		return pk.productUseValueName;
	}

	public double getConstrainedOutput() {
		return constrainedOutput;
	}

	public void setConstrainedOutput(double output) {
		this.constrainedOutput = output;
	}

	public double getProposedOutput() {
		return proposedOutput;
	}

	public void setProposedOutput(double maximumOutput) {
		this.proposedOutput = maximumOutput;
	}

	@Override public int hashCode() {
		int hash = 0;
		hash += (pk != null ? pk.hashCode() : 0);
		return hash;
	}

	@Override public boolean equals(Object object) {
		if (!(object instanceof Circuit)) {
			return false;
		}
		Circuit other = (Circuit) object;
		if ((this.pk == null && other.pk != null)
				|| (this.pk != null && !this.pk.equals(other.pk))) {
			return false;
		}
		return true;
	}

	/**
	 * @return String representation of the circuit and its stocks
	 *         TODO decide how this should be formatted
	 */
	@Override public String toString() {
		return "Capital circuit called" + pk.productUseValueName;
	}

	/**
	 * add up the values of all the productive stocks and return them as a result
	 * 
	 * @return the total value of the productive stocks owned by this circuit
	 */
	public double productiveStockValue() {
		double result = 0.0;
		for (Stock s : productiveStocks()) {
			result += s.getValue();
		}
		return result;
	}

	/**
	 * add up the prices of all the productive stocks and return them as a result
	 * 
	 * @return the total price of the productive stocks owned by this circuit
	 */
	public double productiveStockPrice() {
		double result = 0.0;
		for (Stock s : productiveStocks()) {
			result += s.getPrice();
		}
		return result;
	}

	/**
	 * @return the initialCapital
	 */
	public double getInitialCapital() {
		return initialCapital;
	}

	/**
	 * @param initialCapital
	 *            the initialCapital to set
	 */
	public void setInitialCapital(double initialCapital) {
		this.initialCapital = initialCapital;
	}

	/**
	 * Data for one circuit as an arraylist, for flexibility. At the current project and the given timeStamp
	 * 
	 * @return One record, giving a dump of this circuit, including its productive stocks, as an arrayList
	 */

	public ArrayList<String> circuitContentsAsArrayList() {
		ArrayList<String> contents = new ArrayList<>();
		contents.add(Integer.toString(pk.timeStamp));
		contents.add(Integer.toString(pk.project));
		contents.add((pk.productUseValueName));
		for (Stock s : productiveStocks()) {
			contents.add(String.format("%.2f", s.getQuantity()));
			contents.add(String.format("%.2f", s.getPrice()));
			contents.add(String.format("%.2f", s.getValue()));
		}
		contents.add(String.format("%.2f", getMoneyQuantity()));
		contents.add(String.format("%.2f", getSalesQuantity()));
		contents.add(String.format("%.2f", getSalesPrice()));
		contents.add(String.format("%.2f", getSalesValue()));
		contents.add(String.format("%.2f", constrainedOutput));
		contents.add(String.format("%.2f", proposedOutput));
		return contents;
	}

	public void setComparator(Circuit comparator) {
		this.comparator = comparator;
	}

	/**
	 * The current capital of this circult.
	 * this is the sum of all outlays (including mone), that is to say, it is everything that has to be engaged in the business to keep it going
	 * it is always calculated using the price expression of these outlays
	 * @return the current capital of this circuit
	 * 
	 * 
	 */
	public double currentCapital() {
		return getMoneyPrice() + getSalesPrice() + productiveStocksAttribute(ValueExpression.PRICE);
	}

	/**
	 * The profit of this circuit.
	 * This is the current capital less the initial capital. It is thus a simple difference independent of sales,
	 * and hence makes no assumption that profit is realised.
	 * Like all capital, it is calculated using the price expression of the magnitudes involved.
	 * @return the profit (so far) of this circuit. 
	 */
	public double profit() {
		return currentCapital()-initialCapital;
	}
	
	/**
	 * @return the profitRate
	 */
	public double profitRate() {
		if (Precision.round(initialCapital,Simulation.getRoundingPrecision())==0){
			return Double.NaN;
		}
		return profit()/initialCapital;
	}

	/**
	 * @return the growthRate
	 */
	public double getGrowthRate() {
		return growthRate;
	}

	/**
	 * @param growthRate
	 *            the growthRate to set
	 */
	public void setGrowthRate(double growthRate) {
		this.growthRate = growthRate;
	}

	/**
	 * @return the costOfExpansion
	 */
	public double getCostOfExpansion() {
		return costOfMPForExpansion + costOfLPForExpansion;
	}

	/**
	 * @return the costOfMPForExpansion
	 */
	public double getCostOfMPForExpansion() {
		return costOfMPForExpansion;
	}

	/**
	 * @return the costOfLPForExpansion
	 */
	public double getCostOfLPForExpansion() {
		return costOfLPForExpansion;
	}

	/**
	 * given the means of production available for investment, determine the possible level of output
	 * and then calculate the additional cost of labour power needed to achieve this.
	 * 
	 * TOODO this is a rudimentary procedure written on the assumption there is only a single stock of type Means of Production.
	 * It needs to be generalised.
	 * 
	 * @param surplusMeansOfProduction
	 *            the additional Means of Production available for investment once the requirements of simple reproduction have been satisfied.
	 */
	public void computePossibleOutput(double surplusMeansOfProduction) {
		Stock mP = productiveStock("Means of Production");
		UseValue u = mP.getUseValue();
		double price = u.getUnitPrice();
		double extraStock = surplusMeansOfProduction / price;
		proposedOutput = constrainedOutput + extraStock / mP.getCoefficient();
		calculateOutputCosts();
	}
}
