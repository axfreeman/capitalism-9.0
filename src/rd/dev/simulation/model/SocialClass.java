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
import rd.dev.simulation.custom.TabbedTableViewer;
import rd.dev.simulation.datamanagement.DataManager;
import rd.dev.simulation.model.Stock.ValueExpression;
import rd.dev.simulation.utils.Dialogues;
import rd.dev.simulation.utils.Reporter;
import rd.dev.simulation.view.ViewManager;

/**
 * The persistent class for the socialClasses database table.
 * 
 */
@Entity
@Table(name = "socialclasses")
@NamedQueries({
		@NamedQuery(name = "All", query = "SELECT c FROM SocialClass c where c.pk.project= :project and c.pk.timeStamp = :timeStamp "),
		@NamedQuery(name = "Primary", query = "SELECT c FROM SocialClass c where c.pk.project= :project and c.pk.timeStamp = :timeStamp and c.pk.socialClassName =:socialClassName"),
})

@Embeddable
public class SocialClass extends Observable implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final Logger logger = LogManager.getLogger(SocialClass.class);

	@EmbeddedId protected SocialClassPK pk;
	@Column(name = "Size") protected double size;
	@Column(name = "ConsumptionPerPerson") protected double consumptionStocksRequiredPerPerson;

	// The proportion of the population of this class that supplies labour power
	// Not yet used, but intended for 'mixed' classes (eg small producers, pension-holders)
	@Column(name = "ParticipationRatio") protected double participationRatio;
	@Column(name = "Revenue") protected double revenue; // the money that this class will spend in the current period

	@Transient private SocialClass comparator;
	@Transient private SocialClass previousComparator;
	@Transient private SocialClass startComparator;
	@Transient private SocialClass customComparator;
	@Transient private SocialClass endComparator;

	public enum Selector {
		// @formatter:off
		SOCIALCLASSNAME("Social Class",null,TabbedTableViewer.HEADER_TOOL_TIPS.SOCIALCLASS.text()), 
		SIZE("Population","population.png",null), 
		CONSUMPTIONPERPERSON("Consumption per person","",null), 
		MONEY("Money","money.png",null), 
		SALES("Labour Power","labourPower.png",null), 
		CONSUMPTIONSTOCKS("Consumer Goods","necessities.png",null), 
		QUANTITYDEMANDED("Demand","demand.png",null), 
		PARTICIPATIONRATIO("Participation Ratio",null,null), 
		REVENUE("Revenue","glass-and-bottle-of-wine.png",null), 
		TOTAL("Assets","TotalCapital.png",null);
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
	 * A 'bare constructor' is required by JPA and this is it. However, when the new socialClass is constructed, the constructor does not automatically create a
	 * new PK entity. So we create a 'hollow' primary key which must then be populated by the caller before persisting the entity
	 */
	public SocialClass() {
		this.pk = new SocialClassPK();
	}

	/**
	 * make a carbon copy of the socialClassTemplate
	 * 
	 * @param socialClassTemplate
	 *            TODO get BeanUtils to do this, or find some other way. There must be a better way but many people complain about it
	 */
	public void copySocialClass(SocialClass socialClassTemplate) {
		this.pk.timeStamp = socialClassTemplate.pk.timeStamp;
		this.pk.project = socialClassTemplate.pk.project;
		this.pk.socialClassName = socialClassTemplate.pk.socialClassName;
		this.size = socialClassTemplate.size;
		this.consumptionStocksRequiredPerPerson = socialClassTemplate.consumptionStocksRequiredPerPerson;
		this.participationRatio = socialClassTemplate.participationRatio;
		this.revenue = socialClassTemplate.revenue;
	}

	// METHODS THAT RETRIEVE STOCKS

	/**
	 * get the Stock of money owned by this class. If this stock does not exist (which is an error) return null.
	 * 
	 * @return the money stock that is owned by this social class.
	 */
	public Stock getMoneyStock() {
		return DataManager.stockMoneyByIndustrySingle(pk.timeStamp, pk.socialClassName);
	}

	/**
	 * get the sales stock of this social class. If this stock does not exist (which is an error) return null.
	 * NOTE:The only commodity classes can sell is Labour power.
	 * TODO generalise this.
	 * 
	 * @return the sales stock that is owned by this social class.
	 */
	public Stock getSalesStock() {
		// TODO we can't assume there is only one type of labour power
		// also we should allow for theories in which social classes sell other things
		return DataManager.stockByPrimaryKey(Simulation.projectCurrent, pk.timeStamp, pk.socialClassName, "Labour Power", Stock.STOCKTYPE.SALES.text());
	}

	/**
	 * provides a wrapped version of the selected member which the display will recognise, as a ReadOnlyStringWrapper.
	 * 
	 * @param selector
	 *            chooses which member to evaluate
	 * @param valueExpression
	 *            selects the display attribute where relevant (QUANTITY, VALUE, PRICE)
	 * @return a String representation of the members, formatted according to the relevant format string
	 */

	public ReadOnlyStringWrapper wrappedString(Selector selector, Stock.ValueExpression valueExpression) {
		switch (selector) {
		case SOCIALCLASSNAME:
			return new ReadOnlyStringWrapper(pk.socialClassName);
		case CONSUMPTIONPERPERSON:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, consumptionStocksRequiredPerPerson));
		case SIZE:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, size));
		case CONSUMPTIONSTOCKS:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, consumptionAttribute(valueExpression)));
		case MONEY:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, moneyAttribute(valueExpression)));
		case SALES:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, salesAttribute(valueExpression)));
		case QUANTITYDEMANDED:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, consumptionQuantityDemanded()));
		case REVENUE:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, revenue));
		case TOTAL:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, totalAttribute(valueExpression)));
		default:
			return null;
		}
	}

	/**
	 * The value-expression of the magnitude of a named consumption Stock owned by this class
	 * 
	 * @param consumptionStockName
	 *            the useValue of the consumption Stock
	 * 
	 * @return the magnitude of the named Stock, expressed as defined by {@code displayAttribute}, null if this does not exist
	 */

	public ReadOnlyStringWrapper wrappedString(String consumptionStockName) {
		try {
			Stock namedStock = DataManager.stockConsumptionByUseValueAndClassSingle(pk.timeStamp, pk.socialClassName, consumptionStockName);
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
	 * @param selector
	 *            chooses which member to evaluate
	 * @param valueExpression
	 *            selects the display attribute where relevant (QUANTITY, VALUE, PRICE)
	 * @return whether this member has changed or not. False if selector is unavailable here
	 */

	public boolean changed(Selector selector, Stock.ValueExpression valueExpression) {
		chooseComparison();
		switch (selector) {
		case SOCIALCLASSNAME:
			return false;
		case SIZE:
			return size != comparator.size;
		case CONSUMPTIONPERPERSON:
			return consumptionStocksRequiredPerPerson != comparator.consumptionStocksRequiredPerPerson;
		case CONSUMPTIONSTOCKS:
			return consumptionAttribute(valueExpression) != comparator.consumptionAttribute(valueExpression);
		case MONEY:
			return moneyAttribute(valueExpression) != comparator.moneyAttribute(valueExpression);
		case QUANTITYDEMANDED:
			return consumptionQuantityDemanded() != comparator.consumptionQuantityDemanded();
		case REVENUE:
			return revenue != comparator.revenue;
		case SALES:
			return salesAttribute(valueExpression) != comparator.salesAttribute(valueExpression);
		case TOTAL:
			return totalAttribute(valueExpression) != comparator.totalAttribute(valueExpression);
		default:
			return false;
		}
	}

	/**
	 * If the selected field has changed, return the difference between the current value and the former value
	 * 
	 * @param selector
	 *            chooses which member to evaluate
	 * @param displayAttribute
	 *            selects the display attribute where relevant (QUANTITY, VALUE, PRICE)
	 * @param item
	 *            the item which the calling method (normally a TableCell) proposes to display
	 * @return the item if unchanged, otherwise the difference between the item and its former magnitude
	 */

	public String showDelta(String item, Selector selector, ValueExpression displayAttribute) {
		chooseComparison();
		if (!changed(selector, displayAttribute))
			return item;
		switch (selector) {
		case SOCIALCLASSNAME:
			return item;
		case SIZE:
			return String.format(ViewManager.largeNumbersFormatString, size - comparator.size);
		case CONSUMPTIONPERPERSON:
			return String.format(ViewManager.largeNumbersFormatString, consumptionStocksRequiredPerPerson - comparator.consumptionStocksRequiredPerPerson);
		case CONSUMPTIONSTOCKS:
			return String.format(ViewManager.largeNumbersFormatString,
					consumptionAttribute(displayAttribute) - comparator.consumptionAttribute(displayAttribute));
		case MONEY:
			return String.format(ViewManager.largeNumbersFormatString, moneyAttribute(displayAttribute) - comparator.moneyAttribute(displayAttribute));
		case QUANTITYDEMANDED:
			return String.format(ViewManager.largeNumbersFormatString, consumptionQuantityDemanded() - comparator.consumptionQuantityDemanded());
		case REVENUE:
			return String.format(ViewManager.largeNumbersFormatString, revenue - comparator.revenue);
		case SALES:
			return String.format(ViewManager.largeNumbersFormatString, salesAttribute(displayAttribute) - comparator.salesAttribute(displayAttribute));
		case TOTAL:
			return String.format(ViewManager.largeNumbersFormatString, totalAttribute(displayAttribute) - comparator.totalAttribute(displayAttribute));
		default:
			return item;
		}
	}

	/**
	 * regenerate the labour power of this class
	 * NOTE any class might theoretically sell labour power and some classes will be mixed, for example small commodity producers
	 * So the worker/capitalist distinction, if made, is a property of a particular project, in that if there is a full
	 * separation, one class (in the project) will sell labour power and will have no property revenue
	 * whereas another will sell no labour power and will only receive property revenue
	 * (TODO this is as yet underdeveloped because we have to develop the different types of property-based revenue such as land etc)
	 */

	public void regenerate() {
		Reporter.report(logger, 1, "Reproducing the sales stock of the class [%s]", pk.socialClassName);

		Stock salesStock = getSalesStock();
		if (salesStock != null) {
			double existingLabourPower = salesStock.getQuantity();
			UseValue useValue = DataManager.useValueByName(pk.timeStamp, salesStock.getUseValueName());
			double turnoverTime = useValue.getTurnoverTime();
			double newLabourPower = size * participationRatio / turnoverTime;
			double extraLabourPower = newLabourPower - existingLabourPower;
			if (extraLabourPower > 0) {
				Reporter.report(logger, 2, "The sales stock of the class [%s] was %.0f and is now %.0f", pk.socialClassName, existingLabourPower,
						newLabourPower);
				salesStock.modifyBy(extraLabourPower);
			}
		}
	}

	/**
	 * Account for the consumption of this social class.
	 * At present, very simple: eat it all up.
	 */

	public void consume() {
		Reporter.report(logger, 1, "Replenishing the class [%s]", pk.socialClassName);
		for (Stock s : DataManager.stocksConsumptionByClass(Simulation.timeStampIDCurrent, pk.socialClassName)) {
			double quantityConsumed = s.getQuantity();
			s.modifyBy(-quantityConsumed);
			Reporter.report(logger, 2, "Consumption stock of class [%s] reduced to %.0f from %.0f", pk.socialClassName, s.getQuantity(), quantityConsumed);
		}
	}

	/**
	 * temporary fix to yield the stock of necessities, while we convert to multiple consumption goods
	 * TODO phase this out
	 * 
	 * @return a Single Consumption Stock called either "COnsumption" or "Necessities" if one of these exists, null otherwise
	 */

	public Stock getConsumptionStock() {
		for (Stock s : DataManager.stocksConsumptionByClass(Simulation.timeStampIDCurrent, pk.socialClassName)) {
			if (s.getUseValueName().equals("Consumption"))
				return s;
			if (s.getUseValueName().equals("Necessities"))
				return s;
		}
		return null;
	}

	/**
	 * Get the consumption stock of the named useValue that is owned by this class
	 * 
	 * @param useValueName
	 *            the name of the prescribed consumer good
	 * @return the Stock of the consumer good named useValue that this class owns
	 */

	public Stock getConsumptionStock(String useValueName) {
		return DataManager.stockConsumptionByUseValueAndClassSingle(pk.timeStamp, pk.socialClassName, useValueName);
	}

	// METHODS THAT RETRIEVE ATTRIBUTES OF STOCKS

	/**
	 * get the quantity of the Stock of money owned by this class. Return 0 if the stock cannot be found
	 * 
	 * @return the quantity of money owned by this social class.
	 */
	public double getMoneyQuantity() {
		Stock s = getMoneyStock();
		return s == null ? 0 : s.getQuantity();
	}

	/**
	 * get the value of the Stock of money owned by this class. Return 0 if the stock cannot be found
	 * 
	 * @return the quantity of money owned by this social class.
	 */
	public double getMoneyValue() {
		Stock s = getMoneyStock();
		return s == null ? 0 : s.getValue();
	}

	/**
	 * get the price of the Stock of money owned by this class. Return 0 if the stock cannot be found
	 * 
	 * @return the price of money owned by this social class.
	 */
	public double getMoneyPrice() {
		Stock s = getMoneyStock();
		return s == null ? 0 : s.getPrice();
	}

	/**
	 * return the quantity of the Stock of sales owned by this social class. Return 0 if the stock cannot be found
	 * 
	 * @return the quantity of sales stock owned by this social class
	 */
	public double getSalesQuantity() {
		Stock s = getSalesStock();
		return s == null ? 0 : s.getQuantity();
	}

	/**
	 * return the value of the sales stock owned by this social class. Return 0 if the stock cannot be found
	 * 
	 * @return the value of the sales stock owned by this social class
	 */
	public double getSalesValue() {
		Stock s = getSalesStock();
		return s == null ? 0 : s.getValue();
	}

	/**
	 * return the price of the Stock of sales owned by this social class. Return 0 if the stock cannot be found
	 * 
	 * @return the quantity of sales stock owned by this social class
	 */
	public double getSalesPrice() {
		Stock s = getSalesStock();
		return s == null ? 0 : s.getPrice();
	}

	/**
	 * return the quantity demanded of the Stock of consumption good owned by this social class.
	 * Return NaN if the stock cannot be found (which is an error)
	 * 
	 * @return the quantity demanded of the consumption goods owned by this social class
	 */
	public double consumptionQuantityDemanded() {

		Stock s = getConsumptionStock();
		return s == null ? Float.NaN : s.getReplenishmentDemand();
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
	 * set the quantity demanded of the Stock of consumption good owned by this social class. Report if the stock cannot be found (which is an error)
	 * 
	 * @param quantityDemanded
	 *            the quantity of the stock of consumption goods
	 * 
	 */
	public void setConsumptionQuantityDemanded(double quantityDemanded) {
		Stock s = getConsumptionStock();
		if (s != null) {
			s.setReplenishmentDemand(quantityDemanded);
		} else {
			logger.error("ERROR: Social class {} attempted to set the quantity demanded of its consumption stock, but it does not have one",
					pk.socialClassName);
		}
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

	/**
	 * @return the consumptionStocksRequiredPerPerson
	 */
	public double getConsumptionStocksRequiredPerPerson() {
		return consumptionStocksRequiredPerPerson;
	}

	/**
	 * @param consumptionStocksRequiredPerPerson
	 *            the consumptionStocksRequiredPerPerson to set
	 */
	public void setConsumptionStocksRequiredPerPerson(double consumptionStocksRequiredPerPerson) {
		this.consumptionStocksRequiredPerPerson = consumptionStocksRequiredPerPerson;
	}

	public String getSocialClassName() {
		return pk.socialClassName;
	}

	public double getSize() {
		return size;
	}

	public void setSize(double size) {
		this.size = size;
	}

	public boolean changedSize() {
		return size != comparator.getSize();
	}

	/**
	 * @return the participationRatio
	 */
	public double getparticipationRatio() {
		return participationRatio;
	}

	/**
	 * @param participationRatio
	 *            the participationRatio to set
	 */
	public void setparticipationRatio(double participationRatio) {
		this.participationRatio = participationRatio;
	}

	/**
	 * set the comparator for a SocialClass, so we can display differences
	 * 
	 * @param comparator
	 *            the socialClass entity, normally drawn from a previous timeStamp, with which this will be compared
	 */
	public void setComparator(SocialClass comparator) {
		this.comparator = comparator;
	}

	/**
	 * generic selector which returns a numerical attribute of the sales stock depending on the {@link Stock.ValueExpression}
	 * 
	 * @param a
	 *            (QUANTITY, VALUE OR PRICE) selects whether to return the quantity, the value or the price of this stock
	 * @return the quantity of sales stock if a=QUANTITY, etc. If there is no sales Stock return zero.
	 */
	public double salesAttribute(Stock.ValueExpression a) {
		Stock salesStock = getSalesStock();
		return salesStock == null ? 0 : salesStock.get(a);
	}

	/**
	 * generic selector which returns a numerical attribute of the consumption stock depending on the {@link Stock.ValueExpression}
	 * 
	 * @param a
	 *            (QUANTITY, VALUE OR PRICE) selects whether to return the quantity, the value or the price of this stock
	 * @return the quantity of consumption goods if a=QUANTITY, etc.
	 */
	public double consumptionAttribute(Stock.ValueExpression a) {
		return getConsumptionStock().get(a);
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
	 * get either total value or total price, depending on the attribute
	 * 
	 * @param a
	 *            one of Stock.ValueExpression.QUANTITY,Stock.ValueExpression.VALUE,Stock.ValueExpression.PRICE
	 * @return the total value if a=VALUE, the total price if a=PRICE and NaN if a=QUANTITY
	 */

	public double totalAttribute(Stock.ValueExpression a) {
		if (a == Stock.ValueExpression.QUANTITY) {
			return Double.NaN;
		}
		return getMoneyStock().get(a) + salesAttribute(a) + getConsumptionStock().get(a);
	}

	/**
	 * @return the revenue
	 */
	public double getRevenue() {
		return revenue;
	}

	/**
	 * @param revenue
	 *            the revenue to set
	 */
	public void setRevenue(double revenue) {

		// a little consistency check

		if (revenue < 0) {
			Dialogues.alert(logger,
					"Capitalist revenue will fall below zero if $%.0f is deducted from it. This is probably a programme error. Contact the developer", revenue);
			return;
		}
		this.revenue = revenue;
	}

	/**
	 * @return the previousComparator
	 */
	public SocialClass getPreviousComparator() {
		return previousComparator;
	}

	/**
	 * @param previousComparator
	 *            the previousComparator to set
	 */
	public void setPreviousComparator(SocialClass previousComparator) {
		this.previousComparator = previousComparator;
	}

	/**
	 * @return the startComparator
	 */
	public SocialClass getStartComparator() {
		return startComparator;
	}

	/**
	 * @param startComparator
	 *            the startComparator to set
	 */
	public void setStartComparator(SocialClass startComparator) {
		this.startComparator = startComparator;
	}

	/**
	 * @return the customComparator
	 */
	public SocialClass getCustomComparator() {
		return customComparator;
	}

	/**
	 * @param customComparator
	 *            the customComparator to set
	 */
	public void setCustomComparator(SocialClass customComparator) {
		this.customComparator = customComparator;
	}

	/**
	 * @return the endComparator
	 */
	public SocialClass getEndComparator() {
		return endComparator;
	}

	/**
	 * @param endComparator
	 *            the endComparator to set
	 */
	public void setEndComparator(SocialClass endComparator) {
		this.endComparator = endComparator;
	}
}