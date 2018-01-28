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
import rd.dev.simulation.view.ViewManager;

/**
 * The persistent class for the socialClasses database table.
 * 
 */
@Entity
@Table(name = "socialclasses")
@NamedQueries({
		@NamedQuery(name = "SocialClass.findAll", query = "SELECT c FROM SocialClass c"),
		@NamedQuery(name = "SocialClass.project.timeStamp", query = "SELECT c FROM SocialClass c where c.pk.project= :project and c.pk.timeStamp = :timeStamp "),
		@NamedQuery(name = "SocialClass.PrimaryKey", query = "SELECT c FROM SocialClass c where c.pk.project= :project and c.pk.timeStamp = :timeStamp and c.pk.socialClassName =:socialClassName")
})

@Embeddable
public class SocialClass extends Observable implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final Logger logger = LogManager.getLogger(SocialClass.class);

	/*
	 * MEMO: THE SQL STATEMENT THAT CREATES THE SOCIAL CLASS TABLE
	 * CREATE TABLE `socialclasses` (
	 * `project` INT DEFAULT 1 NOT NULL,
	 * `timeStamp` VARCHAR (10) DEFAULT '1' NOT NULL,
	 * `SocialClassName` VARCHAR(45) DEFAULT NULL,
	 * `Description` VARCHAR(45) DEFAULT NULL,
	 * `Size` DOUBLE DEFAULT NULL,
	 * `SalesStock` DOUBLE DEFAULT NULL,
	 * `Money` DOUBLE DEFAULT NULL,
	 * `ConsumptionGoods` DOUBLE DEFAULT NULL ,
	 * primary key (project, timeStamp, SocialClassName)) ENGINE=INNODB DEFAULT CHARSET=UTF8;
	 */

	@EmbeddedId protected SocialClassPK pk;
	@Column(name = "Size") protected double size;
	@Column(name = "ConsumptionPerPerson") protected double consumptionStocksRequiredPerPerson;
	@Column(name="ReproductionTime") protected double reproductionTime; // the number of periods over which this class wil consume its stock of consumption goods
	@Column(name="Revenue") protected double revenue; // the money that this class will spend in the current period

	@Transient private SocialClass comparator;
	
	public enum Selector{
		SOCIALCLASSNAME,SIZE,CONSUMPTIONPERPERSON,MONEY,SALES,CONSUMPTIONSTOCKS,QUANTITYDEMANDED,REPRODUCTIONTIME,SPENDING,TOTAL
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
		this.reproductionTime=socialClassTemplate.reproductionTime;
		this.revenue=socialClassTemplate.revenue;
	}

	// METHODS THAT RETRIEVE STOCKS

	/**
	 * get the Stock of money owned by this class. If this stock does not exist (which is an error) return null.
	 * 
	 * @return the money stock that is owned by this social class.
	 */
	public Stock getMoneyStock() {
		return DataManager.stockMoneyByCircuitSingle(pk.timeStamp, pk.socialClassName);
	}

	/**
	 * get the sales stock of this social class. If this stock does not exist (which is an error) return null.
	 * NOTE:The only commodity classes can sell is Labour power.
	 * TODO generalise this.
	 * 
	 * @return the sales stock that is owned by this social class.
	 */
	public Stock getSalesStock() {
		return DataManager.stockSalesByCircuitSingle(pk.timeStamp, pk.socialClassName, "Labour Power");
	}

	/**
	 * get the Stock of consumption goods owned by this social class. If this stock does not exist (which is an error) return null.
	 * 
	 * the dataManager
	 * 
	 * @return the Stock of consumption goods stock that is owned by this social class
	 */
	public Stock getConsumptionStock() {
		return DataManager.stockConsumptionByCircuitSingle(pk.timeStamp, pk.socialClassName);
	}

	/**
	 * get the stock of Labour Power of this class (normally applies only to working class but others are possible eg petty bourgeoisie)
	 * 
	 * @return the stock of LabourPower of this class
	 */

	public Stock getLabourPower() {
		return DataManager.stockByPrimaryKey(pk.project, pk.timeStamp, pk.socialClassName, "Labour Power", "Sales");
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
		case SPENDING:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, revenue));
		case TOTAL:
			return new ReadOnlyStringWrapper(String.format(ViewManager.largeNumbersFormatString, totalAttribute(valueExpression)));			
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
	 * @param selector
	 *            chooses which member to evaluate
	 * @param valueExpression
	 *            selects the display attribute where relevant (QUANTITY, VALUE, PRICE)
	 * @return whether this member has changed or not. False if selector is unavailable here
	 */

	public boolean changed(Selector selector, Stock.ValueExpression valueExpression) {
		switch (selector) {
		case SOCIALCLASSNAME:
			return false;
		case SIZE:
			return size!=comparator.size;
		case CONSUMPTIONPERPERSON:
			return consumptionStocksRequiredPerPerson!=comparator.consumptionStocksRequiredPerPerson;
		case CONSUMPTIONSTOCKS:
			return consumptionAttribute(valueExpression)!=comparator.consumptionAttribute(valueExpression);
		case MONEY:
			return moneyAttribute(valueExpression)!=comparator.moneyAttribute(valueExpression);
		case QUANTITYDEMANDED:
			return consumptionQuantityDemanded()!=comparator.consumptionQuantityDemanded();
		case SPENDING:
			return revenue!=comparator.revenue;
		case SALES:
			return salesAttribute(valueExpression)!=comparator.salesAttribute(valueExpression);
		case TOTAL:
			return totalAttribute(valueExpression)!=comparator.totalAttribute(valueExpression);
		default:
			return false;
		}
	}
	
	// METHODS THAT RETRIEVE ATTRIBUTES OF STOCKS

	/**
	 * get the quantity of the Stock of money owned by this class. Return NaN if the stock cannot be found (which is an error)
	 * 
	 * @return the quantity of money owned by this social class.
	 */
	public double getMoneyQuantity() {
		Stock s = getMoneyStock();
		return s == null ? Float.NaN : s.getQuantity();
	}

	/**
	 * get the value of the Stock of money owned by this class. Return NaN if the stock cannot be found (which is an error)
	 * 
	 * @return the quantity of money owned by this social class.
	 */
	public double getMoneyValue() {
		Stock s = getMoneyStock();
		return s == null ? Float.NaN : s.getValue();
	}

	/**
	 * get the price of the Stock of money owned by this class. Return NaN if the stock cannot be found (which is an error)
	 * 
	 * @return the price of money owned by this social class.
	 */
	public double getMoneyPrice() {
		Stock s = getMoneyStock();
		return s == null ? Float.NaN : s.getPrice();
	}

	/**
	 * return the quantity of the Stock of sales owned by this social class. Return NaN if the stock cannot be found (which is an error)
	 * 
	 * @return the quantity of sales stock owned by this social class
	 */
	public double getSalesQuantity() {
		Stock s = getSalesStock();
		return s == null ? Float.NaN : s.getQuantity();
	}

	/**
	 * return the value of the sales stock owned by this social class. Return NaN if the stock cannot be found (which is an error)
	 * 
	 * @return the value of the sales stock owned by this social class
	 */
	public double getSalesValue() {
		Stock s = getSalesStock();
		return s == null ? Float.NaN : s.getValue();
	}

	/**
	 * return the price of the Stock of sales owned by this social class. Return NaN if the stock cannot be found (which is an error)
	 * 
	 * @return the quantity of sales stock owned by this social class
	 */
	public double getSalesPrice() {
		Stock s = getSalesStock();
		return s == null ? Float.NaN : s.getPrice();
	}

	/**
	 * return the quantity of the Stock of consumption good owned by this social class. Return NaN if the stock cannot be found (which is an error)
	 * 
	 * @return the quantity of the consumption goods owned by this social class
	 */
	public double getConsumptionQuantity() {
		Stock s = getConsumptionStock();
		return s == null ? Float.NaN : s.getQuantity();
	}

	/**
	 * return the value of the Stock of consumption goods owned by this social class. Return NaN if the stock cannot be found (which is an error)
	 * 
	 * @return the value of the consumption goods owned by this social class
	 */
	public double getConsumptionValue() {
		Stock s = getConsumptionStock();
		return s == null ? Float.NaN : s.getValue();
	}

	/**
	 * return the price of the Stock of consumption good owned by this social class. Return NaN if the stock cannot be found (which is an error)
	 * 
	 * @return the price of the consumption goods owned by this social class
	 */
	public double getConsumptionPrice() {
		Stock s = getConsumptionStock();
		return s == null ? Float.NaN : s.getPrice();
	}

	/**
	 * return the quantity demanded of the Stock of consumption good owned by this social class. Return NaN if the stock cannot be found (which is an error)
	 * 
	 * @return the quantity demanded of the consumption goods owned by this social class
	 */
	public double consumptionQuantityDemanded() {
		Stock s = getConsumptionStock();
		return s == null ? Float.NaN : s.getQuantityDemanded();
	}

	// METHODS THAT SET ATTRIBUTES OF STOCKS

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
			s.setQuantityDemanded(quantityDemanded);
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
	 * @return the reproductionTime
	 */
	public double getReproductionTime() {
		return reproductionTime;
	}
	

	/**
	 * @param reproductionTime the reproductionTime to set
	 */
	public void setReproductionTime(double reproductionTime) {
		this.reproductionTime = reproductionTime;
	}

	/**
	 * set the comparator Social Class.
	 */
	public void setComparator() {
		this.comparator = DataManager.socialClassByPrimaryKey(pk.project, Simulation.getTimeStampComparatorCursor(), pk.socialClassName);
	}
	
	/**
	 * generic selector which returns a numerical attribute of the sales stock depending on the {@link Stock.ValueExpression}
	 * 
	 * @param a
	 *            (QUANTITY, VALUE OR PRICE) selects whether to return the quantity, the value or the price of this stock
	 * @return the quantity of sales stock if a=QUANTITY, etc.
	 */
	public double salesAttribute(Stock.ValueExpression a) {
		return getSalesStock().get(a);
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
		return getMoneyStock().get(a) + getSalesStock().get(a) + getConsumptionStock().get(a);
	}
	
	/**
	 * @return the revenue
	 */
	public double getRevenue() {
		return revenue;
	}

	/**
	 * @param revenue the revenue to set
	 */
	public void setRevenue(double revenue) {
		this.revenue = revenue;
	}

	/**
	 * say whether the attribute (total value or total price) selected by a has changed
	 * 
	 * @param a
	 *            one of Stock.ValueExpression.QUANTITY,Stock.ValueExpression.VALUE,Stock.ValueExpression.PRICE
	 * @return whether total value has changed if a=VALUE, whether total price has changed if a=PRICE and false N if a=QUANTITY
	 */

	public boolean changedTotalByAttribute(Stock.ValueExpression a) {
		double now = totalAttribute(a);
		double then = comparator.totalAttribute(a);
		return now != then;
	}
}