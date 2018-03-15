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

import capitalism.model.Stock.VALUE_EXPRESSION;
import capitalism.utils.Dialogues;
import capitalism.utils.Reporter;
import capitalism.view.TabbedTableViewer;
import capitalism.view.ViewManager;
import capitalism.view.custom.TrackingControlsBox;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * The persistent class for the socialClasses database table.
 * 
 */
@Entity
@Table(name = "socialclasses")
@Embeddable
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "SocialClass")
public class SocialClass implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(SocialClass.class);

	@XmlElement @EmbeddedId protected SocialClassPK pk;
	@XmlElement @Column(name = "Size") protected double size;

	// The proportion of the population of this class that supplies labour power
	// Not yet used, but intended for 'mixed' classes (eg small producers, pension-holders)

	@XmlElement @Column(name = "ParticipationRatio") protected double participationRatio;

	// the money that this class will spend in the current period
	@XmlElement @Column(name = "Revenue") protected double revenue;

	// Comparators
	@Transient private SocialClass comparator;
	@Transient private SocialClass previousComparator;
	@Transient private SocialClass startComparator;
	@Transient private SocialClass customComparator;
	@Transient private SocialClass endComparator;

	// Data Management
	private static EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("DB_SOCIALCLASSES");
	private static EntityManager entityManager;
	private static TypedQuery<SocialClass> primaryQuery;
	private static TypedQuery<SocialClass> allInProjectAndTimeStampQuery;
	private static TypedQuery<SocialClass> allInProjectQuery;
	private static TypedQuery<SocialClass> allQuery;

	static {
		entityManager = entityManagerFactory.createEntityManager();
		primaryQuery = entityManager.createQuery(
				"SELECT c FROM SocialClass c where c.pk.projectID= :project and c.pk.timeStampID = :timeStamp and c.pk.name=:socialClassName",
				SocialClass.class);
		allQuery = entityManager.createQuery(
				"SELECT c FROM SocialClass c ", SocialClass.class);
		allInProjectAndTimeStampQuery = entityManager.createQuery(
				"SELECT c FROM SocialClass c where c.pk.projectID= :project and c.pk.timeStampID = :timeStamp ", SocialClass.class);
		allInProjectQuery = entityManager.createQuery(
				"SELECT c FROM SocialClass c where c.pk.projectID= :project ", SocialClass.class);
	}

	/**
	 * set the comparators for the socialClass entity at the given project and timeStamp
	 * @param projectID
	 *            the projectID which selects the entity
	 * 
	 * @param timeStampID
	 *            the timeStampID which selects the entity
	 */
	public static void setComparators(int projectID, int timeStampID) {
		logger.debug("Setting comparators for socialClasses in project {} with timeStamp {}", projectID, timeStampID);
		Project project=Project.get(projectID);
		allInProjectAndTimeStampQuery.setParameter("project", projectID).setParameter("timeStamp", timeStampID);
		for (SocialClass sc : allInProjectAndTimeStampQuery.getResultList()) {
			sc.setPreviousComparator(single(projectID, project.getTimeStampComparatorCursor(), sc.name()));
			sc.setStartComparator(single(projectID, 1, sc.name()));
			sc.setEndComparator(single(projectID, project.getTimeStampID(), sc.name()));
			sc.setCustomComparator(single(projectID, project.getTimeStampID(), sc.name()));
		}
	}

	/**
	 * Readable constants to refer to the methods which provide information about the persistent members of the class
	 */
	public enum SOCIALCLASS_ATTRIBUTE {
		// @formatter:off
		SOCIALCLASSNAME("Social Class",null,TabbedTableViewer.HEADER_TOOL_TIPS.SOCIALCLASS.text()), 
		SIZE("Population","population.png","The number of people in this social class"), 
		MONEY("Money","money.png","The stock of money owned by this class"), 
		SALES("Labour Power","labourPower.png","The amount of time that has been contracted to industries for waged work"), 
		CONSUMPTIONSTOCKS("Consumer Goods","necessities.png","Necessities: consumer goods normally consumed by society in order to reproduce its members"), 
		QUANTITYDEMANDED("Demand","demand.png","The demand for necessities: being phased out in favour of a more sophisticated treatment"), 
		PARTICIPATIONRATIO("Participation Ratio",null,"The proportion of this class which provides waged labour"), 
		REVENUE("Revenue","glass-and-bottle-of-wine.png","The money this class has at its disposal after receiving the income due to it"), 
		TOTAL("Assets","TotalCapital.png","The total assets of this social class");
		// @formatter:on

		String text;
		String imageName;
		String toolTip;

		SOCIALCLASS_ATTRIBUTE(String text, String imageName, String toolTip) {
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
	 * @param template
	 *            the socialClass bean to copy - usually from the previous timeStamp
	 */
	public void copy(SocialClass template) {
		this.pk.timeStampID = template.pk.timeStampID;
		this.pk.projectID = template.pk.projectID;
		this.pk.name = template.pk.name;
		this.size = template.size;
		this.participationRatio = template.participationRatio;
		this.revenue = template.revenue;
	}

	/**
	 * an observable list of type SocialClass for display by ViewManager, at the current project and timeStampDisplayCursor. timeStampDisplayCursor, which
	 * may diverge from timeStamp, identifies the row that the user last clicked on.
	 * 
	 * @param projectID
	 *            the given projectID
	 * 
	 * @param timeStampID
	 *            the given timeStampID
	 * @return an ObservableList of SocialClasses
	 */
	public static ObservableList<SocialClass> socialClassesObservable(int projectID, int timeStampID) {
		SocialClass.allInProjectAndTimeStampQuery.setParameter("project", projectID).setParameter("timeStamp", timeStampID);
		ObservableList<SocialClass> result = FXCollections.observableArrayList();
		for (SocialClass s : SocialClass.allInProjectAndTimeStampQuery.getResultList()) {
			result.add(s);
		}
		return result;
	}

	/**
	 * provides a wrapped version of the selected member which the display will recognise, as a ReadOnlyStringWrapper.
	 * 
	 * @param attribute
	 *            chooses which member to evaluate
	 * @param valueExpression
	 *            selects the display attribute where relevant (QUANTITY, VALUE, PRICE)
	 * @return a String representation of the members, formatted according to the relevant format string
	 */
	public ReadOnlyStringWrapper wrappedString(SOCIALCLASS_ATTRIBUTE attribute, VALUE_EXPRESSION valueExpression) {
		switch (attribute) {
		case SOCIALCLASSNAME:
			return new ReadOnlyStringWrapper(pk.name);
		case SIZE:
			return new ReadOnlyStringWrapper(String.format(ViewManager.getLargeFormat(), size));
		case CONSUMPTIONSTOCKS:
			return new ReadOnlyStringWrapper(String.format(ViewManager.getLargeFormat(), consumptionAttribute(valueExpression)));
		case MONEY:
			return new ReadOnlyStringWrapper(String.format(ViewManager.getLargeFormat(), moneyAttribute(valueExpression)));
		case SALES:
			return new ReadOnlyStringWrapper(String.format(ViewManager.getLargeFormat(), salesAttribute(valueExpression)));
		case QUANTITYDEMANDED:
			return new ReadOnlyStringWrapper(String.format(ViewManager.getLargeFormat(), necessitiesQuantityDemanded()));
		case REVENUE:
			return new ReadOnlyStringWrapper(String.format(ViewManager.getLargeFormat(), revenue));
		case TOTAL:
			return new ReadOnlyStringWrapper(String.format(ViewManager.getLargeFormat(), totalAttribute(valueExpression)));
		default:
			return null;
		}
	}

	/**
	 * informs the display whether the selected member of this entity has changed, compared with the 'comparator' Commodity which normally
	 * comes from a different timeStamp.
	 * 
	 * @param attribute
	 *            chooses which member to evaluate
	 * @param valueExpression
	 *            selects the display attribute where relevant (QUANTITY, VALUE, PRICE)
	 * @return whether this member has changed or not. False if selector is unavailable here
	 */
	public boolean changed(SOCIALCLASS_ATTRIBUTE attribute, VALUE_EXPRESSION valueExpression) {
		chooseComparison();
		switch (attribute) {
		case SOCIALCLASSNAME:
			return false;
		case SIZE:
			return size != comparator.size;
		case CONSUMPTIONSTOCKS:
			return consumptionAttribute(valueExpression) != comparator.consumptionAttribute(valueExpression);
		case MONEY:
			return moneyAttribute(valueExpression) != comparator.moneyAttribute(valueExpression);
		case QUANTITYDEMANDED:
			return necessitiesQuantityDemanded() != comparator.necessitiesQuantityDemanded();
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
	 * @param attribute
	 *            chooses which member to evaluate
	 * @param valueExpression
	 *            selects the display attribute where relevant (QUANTITY, VALUE, PRICE)
	 * @param item
	 *            the item which the calling method (normally a TableCell) proposes to display
	 * @return the item if unchanged, otherwise the difference between the item and its former magnitude
	 */
	public String showDelta(String item, SOCIALCLASS_ATTRIBUTE attribute, VALUE_EXPRESSION valueExpression) {
		chooseComparison();
		if (!changed(attribute, valueExpression))
			return item;
		switch (attribute) {
		case SOCIALCLASSNAME:
			return item;
		case SIZE:
			return String.format(ViewManager.getLargeFormat(), size - comparator.size);
		case CONSUMPTIONSTOCKS:
			return String.format(ViewManager.getLargeFormat(),
					consumptionAttribute(valueExpression) - comparator.consumptionAttribute(valueExpression));
		case MONEY:
			return String.format(ViewManager.getLargeFormat(), moneyAttribute(valueExpression) - comparator.moneyAttribute(valueExpression));
		case QUANTITYDEMANDED:
			return String.format(ViewManager.getLargeFormat(), necessitiesQuantityDemanded() - comparator.necessitiesQuantityDemanded());
		case REVENUE:
			return String.format(ViewManager.getLargeFormat(), revenue - comparator.revenue);
		case SALES:
			return String.format(ViewManager.getLargeFormat(), salesAttribute(valueExpression) - comparator.salesAttribute(valueExpression));
		case TOTAL:
			return String.format(ViewManager.getLargeFormat(), totalAttribute(valueExpression) - comparator.totalAttribute(valueExpression));
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
	 * The value-expression of the magnitude of a named consumption Stock owned by this class
	 * 
	 * @param consumptionStockName
	 *            the commodity of the consumption Stock
	 * 
	 * @return the magnitude of the named Stock, expressed as defined by {@code displayAttribute}, null if this does not exist
	 */
	public ReadOnlyStringWrapper wrappedString(String consumptionStockName) {
		try {
			Stock namedStock = Stock.consumptionByCommodityAndClassSingle(pk.projectID, pk.timeStampID, pk.name, consumptionStockName);
			String result = String.format(ViewManager.getLargeFormat(), namedStock.get(TabbedTableViewer.displayAttribute));
			return new ReadOnlyStringWrapper(result);
		} catch (Exception e) {
			return null;
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
		Reporter.report(logger, 1, "Reproducing the sales stock of the class [%s]", pk.name);

		Stock salesStock = salesStock();
		if (salesStock != null) {
			double existingLabourPower = salesStock.getQuantity();
			Commodity commodity = Commodity.single(pk.projectID, pk.timeStampID, salesStock.name());
			double turnoverTime = commodity.getTurnoverTime();
			double newLabourPower = size * participationRatio / turnoverTime;
			double extraLabourPower = newLabourPower - existingLabourPower;
			if (extraLabourPower > 0) {
				Reporter.report(logger, 2, "The sales stock of the class [%s] was %.0f and is now %.0f", pk.name, existingLabourPower,
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
		Reporter.report(logger, 1, "Replenishing the class [%s]", pk.name);
		for (Stock s : consumptionStocks()) {
			double quantityConsumed = s.getQuantity();
			s.modifyBy(-quantityConsumed);
			s.setStockUsedUp(s.getStockUsedUp() + quantityConsumed);
			Commodity commodity = s.getCommodity();
			double stockUsedUp = commodity.getStockUsedUp() + quantityConsumed;
			commodity.setStockUsedUp(stockUsedUp);// TODO eliminate and replace by query based on stocks
			Reporter.report(logger, 2, "Consumption stock of class [%s] reduced to %.0f from %.0f; used up stock now comes to %.0f",
					pk.name, s.getQuantity(), quantityConsumed, stockUsedUp);
		}
	}

	/**
	 * get the Stock of money owned by this class. If this stock does not exist (which is an error) return null.
	 * 
	 * @return the money stock that is owned by this social class.
	 */
	public Stock moneyStock() {
		return Stock.moneyByOwner(pk.projectID, pk.timeStampID, pk.name);
	}

	/**
	 * get the sales stock of this social class. If this stock does not exist (which is an error) return null.
	 * NOTE:The only commodity classes can sell is Labour power.
	 * 
	 * @return the sales stock that is owned by this social class.
	 */
	public Stock salesStock() {
		// TODO we can't assume there is only one type of labour power
		// also we should allow for theories in which social classes sell other things
		return Stock.single(pk.projectID, pk.timeStampID, pk.name, "Labour Power", Stock.STOCKTYPE.SALES.text());
	}

	/**
	 * a list of the various consumer goods owned by a this class, in the current project and the timeStamp of this class
	 *
	 * @return a list of the consumption stocks owned by this social class
	 */
	public List<Stock> consumptionStocks() {
		return Stock.consumedByClass(pk.projectID, pk.timeStampID, pk.name);
	}

	/**
	 * temporary fix to yield the stock of necessities, while we convert to multiple consumption goods
	 * TODO phase this out
	 * 
	 * @return a Single Consumption Stock called either "COnsumption" or "Necessities" if one of these exists, null otherwise
	 */
	public Stock getNecessitiesStock() {
		for (Stock s : Stock.consumedByClass(pk.projectID, pk.timeStampID, pk.name)) {
			if (s.name().equals("Consumption"))
				return s;
			if (s.name().equals("Necessities"))
				return s;
		}
		return null;
	}

	/**
	 * Get the consumption stock of the named commodity that is owned by this class
	 * 
	 * @param commodityName
	 *            the name of the prescribed consumer good
	 * @return the Stock of the consumer good named commodity that this class owns
	 */
	public Stock getNecessitiesStock(String commodityName) {
		return Stock.consumptionByCommodityAndClassSingle(pk.projectID, pk.timeStampID, pk.name, commodityName);
	}

	/**
	 * get the quantity of the Stock of money owned by this class. Return 0 if the stock cannot be found
	 * 
	 * @return the quantity of money owned by this social class.
	 */
	public double moneyQuantity() {
		Stock s = moneyStock();
		return s == null ? 0 : s.getQuantity();
	}

	/**
	 * get the value of the Stock of money owned by this class. Return 0 if the stock cannot be found
	 * 
	 * @return the quantity of money owned by this social class.
	 */
	public double moneyValue() {
		Stock s = moneyStock();
		return s == null ? 0 : s.getValue();
	}

	/**
	 * get the price of the Stock of money owned by this class. Return 0 if the stock cannot be found
	 * 
	 * @return the price of money owned by this social class.
	 */
	public double moneyPrice() {
		Stock s = moneyStock();
		return s == null ? 0 : s.getPrice();
	}

	/**
	 * return the quantity of the Stock of sales owned by this social class. Return 0 if the stock cannot be found
	 * 
	 * @return the quantity of sales stock owned by this social class
	 */
	public double salesQuantity() {
		Stock s = salesStock();
		return s == null ? 0 : s.getQuantity();
	}

	/**
	 * return the value of the sales stock owned by this social class. Return 0 if the stock cannot be found
	 * 
	 * @return the value of the sales stock owned by this social class
	 */
	public double salesValue() {
		Stock s = salesStock();
		return s == null ? 0 : s.getValue();
	}

	/**
	 * return the price of the Stock of sales owned by this social class. Return 0 if the stock cannot be found
	 * 
	 * @return the quantity of sales stock owned by this social class
	 */
	public double salesPrice() {
		Stock s = salesStock();
		return s == null ? 0 : s.getPrice();
	}

	/**
	 * return the quantity demanded of the Stock of necessities owned by this social class.
	 * Return NaN if the stock cannot be found (which is an error)
	 * 
	 * @return the quantity demanded of the consumption goods owned by this social class
	 */
	public double necessitiesQuantityDemanded() {

		Stock s = getNecessitiesStock();
		return s == null ? Float.NaN : s.getReplenishmentDemand();
	}

	/**
	 * set the quantity demanded of the Stock of consumption good owned by this social class. Report if the stock cannot be found (which is an error)
	 * 
	 * @param quantityDemanded
	 *            the quantity of the stock of consumption goods
	 * 
	 */
	public void setNecessitiesQuantityDemanded(double quantityDemanded) {
		Stock s = getNecessitiesStock();
		if (s != null) {
			s.setReplenishmentDemand(quantityDemanded);
		} else {
			logger.error("ERROR: Social class {} attempted to set the quantity demanded of its consumption stock, but it does not have one",
					pk.name);
		}
	}

	/**
	 * Get a single social class defined by its primary key, including a timeStamp that may differ from the current timeStamp
	 * 
	 * @param socialClassName
	 *            the name of the social Class in the primary key
	 * @param projectID
	 *            the project in the primary key
	 * @param timeStampID
	 *            the timeStamp in the primary key
	 * @return the single social class with the name socialClassName, for the given project and timeStamp
	 */

	public static SocialClass single(int projectID, int timeStampID, String socialClassName) {
		primaryQuery.setParameter("project", projectID).setParameter("timeStamp", timeStampID).setParameter("socialClassName", socialClassName);
		try {
			return primaryQuery.getSingleResult();
		} catch (javax.persistence.NoResultException e) {
			return null;// because this query throws a fit if it doesn't find anything
		}
	}

	/**
	 * a list of social classes in the database
	 * Mainly for validation purposes though it could have other uses
	 * 
	 * @return a list of all social classes in the database
	 */
	public static List<SocialClass> all() {
		return allQuery.getResultList();
	}

	
	/**
	 * a list of social classes, for the given projectID.
	 * Mainly for validation purposes though it could have other uses
	 * 
	 * @param projectID
	 *            the given projectID
	 * @return a list of all social classes for the given project 
	 */
	public static List<SocialClass> all(int projectID) {
		allInProjectQuery.setParameter("project", projectID);
		return allInProjectQuery.getResultList();
	}

	
	/**
	 * a list of social classes, for the current project and timeStamp
	 * 
	 * @param projectID
	 *            the given projectID
	 * @param timeStampID
	 *            the given timeStampID *
	 * @return a list of all social classes for the current project and timeStamp
	 */
	public static List<SocialClass> all(int projectID, int timeStampID) {
		allInProjectAndTimeStampQuery.setParameter("project", projectID).setParameter("timeStamp", timeStampID);
		return allInProjectAndTimeStampQuery.getResultList();
	}

	/**
	 * a named social class for the current project and a given timeStamp.
	 * 
	 * @param projectID
	 *            the given projectID
	 * @param timeStampID
	 *            the given timeStampID
	 * @param socialClassName
	 *            the name of the social Class
	 * @return the single social class with the name socialClassName, for the given project and timeStamp
	 */
	public static SocialClass withName(int projectID, int timeStampID, String socialClassName) {
		primaryQuery.setParameter("project", projectID).setParameter("timeStamp", timeStampID)
				.setParameter("socialClassName", socialClassName);
		try {
			return primaryQuery.getSingleResult();
		} catch (NoResultException r) {
			return null;
		}
	}

	/**
	 * @return the entityManager
	 */
	public static EntityManager getEntityManager() {
		return entityManager;
	}

	/**
	 * 
	 * @return the projectID of this SocialClass entity
	 */

	public Integer getProjectID() {
		return pk.projectID;
	}

	/**
	 * 
	 * @return the timeStampID of this SocialClass entity
	 */
	public int getTimeStampID() {
		return pk.timeStampID;
	}

	/**
	 * Set the timeStampID of this SocialClass entity
	 * 
	 * @param timeStampID
	 *            the timeStamp to set
	 */

	public void setTimeStamp(int timeStampID) {
		pk.timeStampID = timeStampID;
	}

	/**
	 * Set the projectID of this social class
	 * 
	 * @param projectID
	 *            the projectID to set
	 */
	public void setProjectID(int projectID) {
		pk.projectID = projectID;
	}

	/**
	 * Get the name of this SocialClass entity
	 * 
	 * @return the name of this SocialClass entity
	 */

	public String name() {
		return pk.name;
	}

	/**
	 * 
	 * @return the size of this SocialClass entity
	 */
	public double getSize() {
		return size;
	}

	/**
	 * Set the size of this SocialClass entity
	 * 
	 * @param size
	 *            the size to set
	 */

	public void setSize(double size) {
		this.size = size;
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
	 * generic selector which returns a numerical attribute of the sales stock depending on the {@link Stock.VALUE_EXPRESSION}
	 * 
	 * @param a
	 *            (QUANTITY, VALUE OR PRICE) selects whether to return the quantity, the value or the price of this stock
	 * @return the quantity of sales stock if a=QUANTITY, etc. If there is no sales Stock return zero.
	 */
	public double salesAttribute(Stock.VALUE_EXPRESSION a) {
		Stock salesStock = salesStock();
		return salesStock == null ? 0 : salesStock.get(a);
	}

	/**
	 * generic selector which returns a numerical attribute of the consumption stock depending on the {@link Stock.VALUE_EXPRESSION}
	 * 
	 * @param a
	 *            (QUANTITY, VALUE OR PRICE) selects whether to return the quantity, the value or the price of this stock
	 * @return the quantity of consumption goods if a=QUANTITY, etc.
	 */
	public double consumptionAttribute(Stock.VALUE_EXPRESSION a) {
		return getNecessitiesStock().get(a);
	}

	/**
	 * generic selector which returns a numerical attribute of the money stock depending on the {@link Stock.VALUE_EXPRESSION}
	 * 
	 * @param a
	 *            (QUANTITY, VALUE OR PRICE) selects whether to return the quantity, the value or the price of this stock
	 * @return the quantity of money if a=QUANTITY, etc.
	 */
	public double moneyAttribute(Stock.VALUE_EXPRESSION a) {
		return moneyStock().get(a);
	}

	/**
	 * get either total value or total price, depending on the attribute
	 * 
	 * @param a
	 *            one of Stock.ValueExpression.QUANTITY,Stock.ValueExpression.VALUE,Stock.ValueExpression.PRICE
	 * @return the total value if a=VALUE, the total price if a=PRICE and NaN if a=QUANTITY
	 */
	public double totalAttribute(Stock.VALUE_EXPRESSION a) {
		if (a == Stock.VALUE_EXPRESSION.QUANTITY) {
			return Double.NaN;
		}
		return moneyStock().get(a) + salesAttribute(a) + getNecessitiesStock().get(a);
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

	public void setName(String name) {
		pk.name = name;
	}
}