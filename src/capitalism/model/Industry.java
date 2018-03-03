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
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Persistence;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.controller.Simulation;
import capitalism.model.Stock.ValueExpression;
import capitalism.utils.Dialogues;
import capitalism.utils.MathStuff;
import capitalism.utils.Reporter;
import capitalism.view.TabbedTableViewer;
import capitalism.view.ViewManager;
import capitalism.view.custom.DisplayControlsBox;
import capitalism.view.custom.TrackingControlsBox;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

@Entity
@Table(name = "industries")
@NamedQueries({
		@NamedQuery(name = "All", query = "Select c from Industry c where c.pk.project = :project and c.pk.timeStamp = :timeStamp"),
		@NamedQuery(name = "Primary", query = "Select c from Industry c where c.pk.project= :project and c.pk.timeStamp = :timeStamp and c.pk.name= :industryName"),
		@NamedQuery(name = "InitialCapital", query = "Select sum(c.initialCapital) from Industry c where c.pk.project=:project and c.pk.timeStamp=:timeStamp"),
		@NamedQuery(name = "CommodityName", query = "Select c from Industry c where c.pk.project=:project and c.pk.timeStamp=:timeStamp and c.commodityName=:commodityName")
})

@Embeddable
public class Industry implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger("Industry");

	@EmbeddedId protected IndustryPK pk;
	@Column(name = "commodityName") protected String commodityName;
	@Column(name = "output") protected double output;
	@Column(name = "ProposedOutput") protected double proposedOutput;
	@Column(name = "InitialCapital") protected double initialCapital;
	@Column(name = "PersistedProfit") protected double persistedProfit;
	@Column(name = "Growthrate") protected double growthRate;
	@Column(name = "productiveCapital") protected double productiveCapital;

	// Comparators
	@Transient private Industry comparator;
	@Transient private Industry previousComparator;
	@Transient private Industry startComparator;
	@Transient private Industry customComparator;
	@Transient private Industry endComparator;

	// Data Management
	private static EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("DB_INDUSTRIES");
	private static EntityManager entityManager;
	private static TypedQuery<Industry> industriesPrimaryQuery;
	private static TypedQuery<Industry> industriesAllQuery;
	private static TypedQuery<Industry> industriesByCommodityQuery;
	private static TypedQuery<Industry> industryInitialCapitalQuery;

	static {
		entityManager = entityManagerFactory.createEntityManager();
		industriesPrimaryQuery = entityManager.createNamedQuery("Primary", Industry.class);
		industriesAllQuery = entityManager.createNamedQuery("All", Industry.class);
		industryInitialCapitalQuery = entityManager.createNamedQuery("InitialCapital", Industry.class);
		industriesByCommodityQuery = entityManager.createNamedQuery("CommodityName", Industry.class);
	}

	/**
	 * Readable constants to refer to the methods which provide information about the persistent members of the class
	 */
	public enum Selector {
		// @formatter:off
		INDUSTRYNAME("Industry",null,TabbedTableViewer.HEADER_TOOL_TIPS.INDUSTRY.text()), 
		COMMODITYNAME("Product",null,TabbedTableViewer.HEADER_TOOL_TIPS.COMMODITY.text()),
		OUTPUT("Output","constrained output.png",TabbedTableViewer.HEADER_TOOL_TIPS.OUTPUT.text()), 
		PROPOSEDOUTPUT("Proposed Output","maximum output.png",null), 
		INITIALCAPITAL("Initial Capital","capital  2.png",TabbedTableViewer.HEADER_TOOL_TIPS.INITIALCAPITAL.text()), 
		INITIALPRODUCTIVECAPITAL("Productive Capital","capital  2.png",TabbedTableViewer.HEADER_TOOL_TIPS.PRODUCTIVECAPITAL.text()),
		CURRENTCAPITAL("Current Capital","capital 1.png",TabbedTableViewer.HEADER_TOOL_TIPS.CAPITAL.text()), 
		PROFIT("Profit","profit.png",TabbedTableViewer.HEADER_TOOL_TIPS.PROFIT.text()), 
		PROFITRATE("Profit Rate","profitRate.png",TabbedTableViewer.HEADER_TOOL_TIPS.PROFITRATE.text()), 
		PRODUCTIVESTOCKS("Total Inputs","inputs.png","The value or price of all stocks, including labour power, that are used in production and owned by this industry"), 
		MONEYSTOCK("Money","money.png","The stock of money owned by this industry"), 
		SALESSTOCK("Sales","inventory.png","The sales stock owned by this industry"), 
		GROWTHRATE("Growth Rate","growthrate.png","The rate of growth that this industry claims it can achieve, given the resources");
		// @formatter:on

		String text;
		String imageName;
		String toolTip;

		Selector(String text, String imageName, String tooltip) {
			this.text = text;
			this.imageName = imageName;
			this.toolTip = tooltip;
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
	 * Says whether the industry produces means of production or means of consumption.
	 * NOTE we work this out by looking at the use value.
	 * A separate field of this entity could result in duplication, unless it is carefully initialised.
	 * The value 'ERROR' is a precaution - there should be no situation in which this would be returned,
	 * that is to say, the commodity should always be either productive or consumption
	 */
	public enum OUTPUTTYPE {
		PRODUCTIONGOODS, CONSUMPTIONGOODS, ERROR
	}

	/**
	 * Report this industry's OUTPUTTYPE (production goods or consumer goods)
	 * NOTE we work this out by looking at the use value.
	 * A separate field of this entity could result in duplication, unless it is carefully initialised
	 * 
	 * @return this industry's Output Type (production or consumer goods)
	 */
	public OUTPUTTYPE outputType() {
		Commodity u = getCommodity();
		switch (u.getFunction()) {
		case PRODUCTIVE_INPUT:
			return OUTPUTTYPE.PRODUCTIONGOODS;
		case CONSUMER_GOOD:
			return OUTPUTTYPE.CONSUMPTIONGOODS;
		default:
			return OUTPUTTYPE.ERROR;
		}
	}

	/**
	 * Controls whether value properties are reported as intrinsic or extrinsic
	 * This is managed distinctly from whether the value magnitude is returned to the caller as intrinsic or extrinsic
	 * It's really only for display purposes and not any other
	 */

	private enum ATTRIBUTE {
		INITIALCAPITAL, INITIALPRODUCTIVECAPITAL, CURRENTCAPITAL, PROFIT;
	}

	/**
	 * A 'bare constructor' is required by JPA and this is it. However, when the new socialClass is constructed,
	 * the constructor does not automatically create a new PK entity. So we create a 'hollow' primary key which
	 * must then be populated by the caller before persisting the entity
	 */
	public Industry() {
		this.pk = new IndustryPK();
	}

	/**
	 * make a carbon copy of an industry template. This is the normal way a persistent entity is constructed,
	 * since each new record is a modified version of it immediate predecessor
	 * 
	 * @param template
	 *            the industry to be copied into this one.
	 */
	public Industry(Industry template) {
		this.pk = new IndustryPK();
		pk.name = template.getName();
		pk.timeStamp = template.getTimeStamp();
		pk.project = template.getProject();
		commodityName = template.commodityName;
		output = template.output;
		proposedOutput = template.proposedOutput;
		growthRate = template.growthRate;
		initialCapital = template.initialCapital;
		productiveCapital = template.productiveCapital;
		persistedProfit = template.persistedProfit;
	}

	/**
	 * an observable list of type Industry for display by ViewManager, at the current project and timeStampDisplayCursor. timeStampDisplayCursor, which
	 * may diverge from timeStamp, identifies the row that the user last clicked on.
	 * 
	 * @return an ObservableList of industries
	 */
	public static ObservableList<Industry> industriesObservable() {
		Industry.industriesAllQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", Simulation.timeStampDisplayCursor);
		ObservableList<Industry> result = FXCollections.observableArrayList();
		for (Industry c : Industry.industriesAllQuery.getResultList()) {
			result.add(c);
		}
		return result;
	}

	/**
	 * provides a wrapped version of the selected member which the display will recognise, as a ReadOnlyStringWrapper.
	 * 
	 * We don't mind the hardwiring because we don't really intend this code to be re-usable, it's not hard to modify, and it results in compact
	 * and readable usage code (see (@link {@link TabbedTableViewer#makeIndustriesCapitalAccountsTable()})
	 * 
	 * @param selector
	 *            chooses which member to evaluate
	 * @param valueExpression
	 *            selects the value DisplayAsExpression where relevant (QUANTITY, VALUE, PRICE)
	 * @return a String representation of the members, formatted according to the relevant format string
	 */

	public ReadOnlyStringWrapper wrappedString(Selector selector, Stock.ValueExpression valueExpression) {
		switch (selector) {
		case INDUSTRYNAME:
			return new ReadOnlyStringWrapper(pk.name);
		case COMMODITYNAME:
			return new ReadOnlyStringWrapper(commodityName);
		case INITIALCAPITAL:
			return new ReadOnlyStringWrapper(String.format(ViewManager.getLargeFormat(), expressionOf(ATTRIBUTE.INITIALCAPITAL)));
		case INITIALPRODUCTIVECAPITAL:
			return new ReadOnlyStringWrapper(String.format(ViewManager.getLargeFormat(), expressionOf(ATTRIBUTE.INITIALPRODUCTIVECAPITAL)));
		case OUTPUT:
			return new ReadOnlyStringWrapper(String.format(ViewManager.getLargeFormat(), output));
		case PROPOSEDOUTPUT:
			return new ReadOnlyStringWrapper(String.format(ViewManager.getLargeFormat(), proposedOutput));
		case GROWTHRATE:
			return new ReadOnlyStringWrapper(String.format(ViewManager.getLargeFormat(), growthRate));
		case MONEYSTOCK:
			return new ReadOnlyStringWrapper(String.format(ViewManager.getLargeFormat(), moneyAttribute(valueExpression)));
		case SALESSTOCK:
			return new ReadOnlyStringWrapper(String.format(ViewManager.getLargeFormat(), salesAttribute(valueExpression)));
		case PRODUCTIVESTOCKS:
			return new ReadOnlyStringWrapper(String.format(ViewManager.getLargeFormat(), productiveStocksAttribute(valueExpression)));
		case PROFIT:
			return new ReadOnlyStringWrapper(String.format(ViewManager.getLargeFormat(), expressionOf(ATTRIBUTE.PROFIT)));
		case PROFITRATE:
			return new ReadOnlyStringWrapper(String.format(ViewManager.getSmallFormat(), profitRate()));
		case CURRENTCAPITAL:
			return new ReadOnlyStringWrapper(String.format(ViewManager.getLargeFormat(), expressionOf(ATTRIBUTE.CURRENTCAPITAL)));
		default:
			return null;
		}
	}

	/**
	 * informs the display whether the selected member of this entity has changed, compared with the 'comparator' Commodity which normally
	 * comes from a different timeStamp.
	 * 
	 * We don't mind the hardwiring because we don't really intend this code to be re-usable, it's not hard to modify, and it results in compact
	 * and readable usage code (see (@link {@link TabbedTableViewer#makeIndustriesCapitalAccountsTable()})
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
		case INDUSTRYNAME:
			return false;
		case PROPOSEDOUTPUT:
			return proposedOutput != comparator.proposedOutput;
		case GROWTHRATE:
			return growthRate != comparator.growthRate;
		case OUTPUT:
			return output != comparator.output;
		case INITIALCAPITAL:
			return initialCapital != comparator.initialCapital;// no need to access expressionOf() because the result will be the same
		case INITIALPRODUCTIVECAPITAL:
			return productiveCapital != comparator.productiveCapital;// no need to access expressionOf() because the result will be the same
		case PROFITRATE:
			return profitRate() != comparator.profitRate();
		case PROFIT:
			return profit() != comparator.profit();// no need to access expressionOf() because the result will be the same
		case MONEYSTOCK:
			return moneyAttribute(valueExpression) != comparator.moneyAttribute(valueExpression);//TODO using the attribute() method is superfluous I think throughout.
		case SALESSTOCK:
			return salesAttribute(valueExpression) != comparator.salesAttribute(valueExpression);
		case PRODUCTIVESTOCKS:
			double p1 = productiveStocksAttribute(valueExpression);
			double p2 = comparator.productiveStocksAttribute(valueExpression);
			return !MathStuff.equals(p1, p2);
		case CURRENTCAPITAL:
			return currentCapital() != comparator.currentCapital();// no need to access expressionOf() because the result will be the same
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
		chooseComparison();
		if (!changed(selector, valueExpression))
			return item;
		switch (selector) {
		case INDUSTRYNAME:
			return item;
		case PROPOSEDOUTPUT:
			return String.format(ViewManager.getLargeFormat(), proposedOutput - comparator.proposedOutput);
		case GROWTHRATE:
			return String.format(ViewManager.getSmallFormat(), growthRate - comparator.growthRate);
		case OUTPUT:
			return String.format(ViewManager.getLargeFormat(), output - comparator.output);
		case INITIALCAPITAL:
			return String.format(ViewManager.getLargeFormat(), expressionOf(ATTRIBUTE.INITIALCAPITAL)- comparator.expressionOf(ATTRIBUTE.INITIALCAPITAL));
		case INITIALPRODUCTIVECAPITAL:
			return String.format(ViewManager.getLargeFormat(), expressionOf(ATTRIBUTE.INITIALPRODUCTIVECAPITAL)- comparator.expressionOf(ATTRIBUTE.INITIALPRODUCTIVECAPITAL));
		case PROFITRATE:
			return String.format(ViewManager.getSmallFormat(), profitRate() - comparator.profitRate());
		case PROFIT:
			return String.format(ViewManager.getLargeFormat(), expressionOf(ATTRIBUTE.PROFIT)- comparator.expressionOf(ATTRIBUTE.PROFIT));
		case MONEYSTOCK:
			return String.format(ViewManager.getLargeFormat(), moneyAttribute(valueExpression) - comparator.moneyAttribute(valueExpression));
		case SALESSTOCK:
			return String.format(ViewManager.getLargeFormat(), salesAttribute(valueExpression) - comparator.salesAttribute(valueExpression));
		case PRODUCTIVESTOCKS:
			double p1 = productiveStocksAttribute(valueExpression);
			double p2 = comparator.productiveStocksAttribute(valueExpression);
			return String.format(ViewManager.getLargeFormat(), (p1 - p2));
		case CURRENTCAPITAL:
			return String.format(ViewManager.getLargeFormat(), expressionOf(ATTRIBUTE.CURRENTCAPITAL)- comparator.expressionOf(ATTRIBUTE.CURRENTCAPITAL));
		default:
			return item;
		}
	}

	/**
	 * The value-expression of the magnitude of a named productive Stock managed by this industry
	 * 
	 * @param productiveStockName
	 *            the commodity of the productive Stock
	 * 
	 * @return the magnitude of the named Stock, expressed as defined by {@code displayAttribute}, null if this does not exist
	 */

	public ReadOnlyStringWrapper wrappedString(String productiveStockName) {
		try {
			Stock namedStock = Stock.productiveNamedSingle(pk.timeStamp, pk.name, productiveStockName);
			String result = String.format(ViewManager.getLargeFormat(), namedStock.get(TabbedTableViewer.displayAttribute));
			return new ReadOnlyStringWrapper(result);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Retrieve the total quantity, value or price of the productive stocks owned by this industry, depending on the attribute
	 * 
	 * @return the total quantity, value or price of the productive stocks owned by this industry, depending on the attribute
	 * @param a
	 *            an attribute from the enum class Stock.ValueExpression: selects one of QUANTITY, VALUE or PRICE. If QUANTITY, NaN is returned
	 * @return the total of the selected attribute owned by this industry
	 */
	public Double productiveStocksAttribute(Stock.ValueExpression a) {
		if (a == Stock.ValueExpression.QUANTITY) {
			return Double.NaN;
		}
		double total = 0;
		for (Stock s : Stock.productiveByIndustry(pk.timeStamp, pk.name)) {
			total += s.get(a);
		}
		return total;
	}

	/**
	 * generic selector which returns a numerical attribute of the sales stock depending on the {@link Stock.ValueExpression}
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
		
		case CURRENTCAPITAL:
			expression = currentCapital();
			break;
		case INITIALCAPITAL:
			expression = initialCapital();
			break;
		case INITIALPRODUCTIVECAPITAL:
			expression = productiveCapital();
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
	 * Estimate the replenishment and expansion requirements associated with two possible levels of output,
	 * of which the first corresponds to replenishment (continuing at the existing level of output) and the
	 * second to expansion (raising output to a proposed higher level). The replenishment output level is
	 * simply that which was last used (output) while the second is the parameter expandedOutput.
	 * 
	 * The replenishmentDemand of all productive stocks of this industry, including labour power, is set at
	 * a level which will deliver output.
	 * 
	 * The expansionDemand of these stocks records the additional quantity of these stocks required, over and
	 * above replenishmentDemand, in order to deliver expandedOutput.
	 * 
	 * Since the totals of both replenishment and expansion demand can be calculated by summing them, as can
	 * their costs, there is no return result.
	 * 
	 * @param extraOutput
	 *            the additional output proposed
	 */

	public void computeDemand(double extraOutput) {
		for (Stock s : productiveStocks()) {
			s.setReplenishmentDemand(output * s.getProductionCoefficient());
			s.setExpansionDemand(extraOutput * s.getProductionCoefficient());
		}
	}

	/**
	 * calculate the cost of replenishing the inputs, from the productive stocks
	 * 
	 * @return the cost of replenishing the inputs
	 */
	public double replenishmentCosts() {
		double result = 0;
		for (Stock s : productiveStocks()) {
			Commodity u = s.getCommodity();
			double additionalCost = s.getReplenishmentDemand() * u.getUnitPrice();
			result += additionalCost;
		}
		return result;
	}

	/**
	 * calculate the cost of expanding the inputs, from the productive stocks
	 * 
	 * @return the cost of replenishing the inputs
	 */
	public double expansionCosts() {
		double result = 0;
		for (Stock s : productiveStocks()) {
			Commodity u = s.getCommodity();
			double additionalCost = s.getExpansionDemand() * u.getUnitPrice();
			result += additionalCost;
		}
		return result;
	}

	/**
	 * Increase the output of this industry in proportion to {@code growthRate}
	 * TODO at present no reality checks or methods of reducing over-ambitious expenditure
	 * 
	 * @param growthRate
	 *            the proportionate increase in the current output
	 */

	public void expand(double growthRate) {
		this.growthRate = growthRate;
		double extraOutput = output * growthRate;
		computeDemand(extraOutput);
		double costOfExpansion = expansionCosts();
		Reporter.report(logger, 2, "Industry [%s] expands from %.0f to %.0f costing $%.0f ",
				pk.name, output, output + extraOutput, costOfExpansion);

		// transfer funds from the donor class, and reduce its revenue accordingly
		// TODO this should be a method of the SocialClass class
		allocateInvestmentFunds(costOfExpansion);

		// grant the additional output level. No need to recompute demand as this will be done
		// by the Demand phase of the next period.
		setOutput(output + extraOutput);

		// reduce the available surplus of every stock that this industry consumes
		for (Stock s : productiveStocks()) {
			Commodity u = s.getCommodity();
			u.setSurplusProduct(u.getSurplusProduct() - s.getExpansionDemand());
		}
	}

	/**
	 * 
	 * @return the best possible growth rate
	 */

	public double computeGrowthRate() {
		double minimumGrowthRate = Double.MAX_VALUE;
		for (Stock s : productiveStocks()) {
			Commodity u = s.getCommodity();

			// Exclude socially-produced commodities
			if (u.getOrigin() == Commodity.ORIGIN.SOCIALlY_PRODUCED)
				continue;

			double replenishmentDemand = s.getProductionCoefficient() * output;
			double remainingSurplus = u.getSurplusProduct();
			double possibleGrowthRate = remainingSurplus / replenishmentDemand;
			if (possibleGrowthRate < minimumGrowthRate)
				minimumGrowthRate = possibleGrowthRate;
		}
		if (minimumGrowthRate == Double.MAX_VALUE) {
			Dialogues.alert(logger, "Industry {} seems to have no inputs. Please look at your data. If the problem persists, contact the developer",
					pk.name);
			minimumGrowthRate = 0;
		}
		growthRate = minimumGrowthRate;
		return growthRate;
	}

	// METHODS THAT REPORT THINGS WE NEED TO KNOW ABOUT THIS INDUSTRY BY INTERROGATING ITS STOCKS

	/**
	 * Retrieve the Commodity entity that this industry produces
	 * 
	 * @return the Commodity that this industry produces
	 */
	public Commodity getCommodity() {
		return Commodity.commodityByPrimaryKey(pk.project, pk.timeStamp, commodityName);
	}

	/**
	 * get the Stock of money owned by this industry. If this stock does not exist (which is an error) return null.
	 * 
	 * @return the money stock that is owned by this social class.
	 */
	public Stock getMoneyStock() {
		return Stock.stockMoneyByOwnerSingle(pk.timeStamp, pk.name);
	}

	/**
	 * retrieve the sales Stock owned by this industry. If this stock does not exist (which is an error) return null.
	 * 
	 * @return the sales stock owned by this industry
	 */
	public Stock getSalesStock() {

		return Stock.stockByPrimaryKey(Simulation.projectCurrent, pk.timeStamp, pk.name, commodityName,
				Stock.STOCKTYPE.SALES.text());
	}

	/**
	 * the industry that produces a given commodity, for the current project and a given timeStamp. This is also the primary key of the Industry entity
	 * 
	 * @param project
	 *            the project
	 * @param timeStamp
	 *            the timeStamp
	 * @param commodityName
	 *            the name of the commodity that is produced by this industry
	 * @return the industrythat produces {@code name}, or null if this does not exist
	 */
	public static Industry industryByPrimaryKey(int project, int timeStamp, String commodityName) {
		industriesPrimaryQuery.setParameter("project", project).setParameter("timeStamp", timeStamp).setParameter("industryName", commodityName);
		try {
			return industriesPrimaryQuery.getSingleResult();
		} catch (javax.persistence.NoResultException n) {
			return null;// getSingleResult does not return null if it fails; instead, it throws a fit
		}
	}

	/**
	 * a list of industries, for the current project and timeStamp
	 * 
	 * @return a list of industriesfor the current project at the latest timeStamp that has been persisted.
	 */
	public static List<Industry> industriesAll() {
		industriesAllQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", Simulation.timeStampIDCurrent);
		return industriesAllQuery.getResultList();
	}

	/**
	 * a list of industries, for the current project and the given timeStamp
	 * 
	 * @param timeStamp
	 *            the given timeStamp
	 *            *
	 * @return a list of industries for the current project at the specified timeStamp (which should, in general, be different from the currentTimeStamp)
	 */

	public static List<Industry> industriesAll(int timeStamp) {
		industriesAllQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp);
		return industriesAllQuery.getResultList();
	}

	/**
	 * a list of industries, for the current project and the given timeStamp, that produce a given use value
	 * 
	 * 
	 * @param commodityName
	 *            the name of the use value that these industries produce
	 * @param timeStamp
	 *            the given timeStamp
	 * @return a list of industries which produce the given use value at the given timeStamp.
	 */

	public static List<Industry> industriesByCommodityName(int timeStamp, String commodityName) {
		industriesByCommodityQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp)
				.setParameter("commodityName", commodityName);
		return industriesByCommodityQuery.getResultList();
	}

	/**
	 * TODO get this working
	 * 
	 * @param timeStamp
	 *            the timeStamp
	 * 
	 * @return the sum of the initial capital in all industries
	 */

	public static double industriesInitialCapital(int timeStamp) {
		industryInitialCapitalQuery.setParameter("timeStamp", timeStamp).setParameter("project", Simulation.projectCurrent);
		Object o = industryInitialCapitalQuery.getSingleResult();
		double result = (double) o;
		return result;
	}

	/**
	 * set the comparators for the industries records in the current project for the named timeStampID
	 * 
	 * @param timeStampID
	 *            the timeStampID of the industries whose comparators are to be reset
	 */

	public static void setComparators(int timeStampID) {
		for (Industry c : industriesAll(timeStampID)) {
			c.setPreviousComparator(industryByPrimaryKey(Simulation.projectCurrent, Simulation.getTimeStampComparatorCursor(), c.getName()));
			c.setStartComparator(industryByPrimaryKey(Simulation.projectCurrent, 1, c.getName()));
			c.setEndComparator(industryByPrimaryKey(Simulation.projectCurrent, Simulation.timeStampIDCurrent, c.getName()));
			c.setCustomComparator(industryByPrimaryKey(Simulation.projectCurrent, Simulation.timeStampIDCurrent, c.getName()));
		}
	}

	/**
	 * get the quantity of the Stock of money owned by this industry. Return NaN if the stock cannot be found (which is an error)
	 * 
	 * @return the quantity of money owned by this industry.
	 */
	public double getMoneyQuantity() {
		Stock s = getMoneyStock();
		return s == null ? Float.NaN : s.getQuantity();
	}

	/**
	 * get the value of the Stock of money owned by this industry. Return NaN if the stock cannot be found (which is an error)
	 * 
	 * @return the quantity of money owned by this industry.
	 */
	public double getMoneyValue() {
		Stock s = getMoneyStock();
		return s == null ? Float.NaN : s.getValue();
	}

	/**
	 * get the price of the Stock of money owned by this industry. Return NaN if the stock cannot be found (which is an error)
	 * 
	 * @return the price of money owned by this industry.
	 */
	public double getMoneyPrice() {
		Stock s = getMoneyStock();
		return s == null ? Float.NaN : s.getPrice();
	}

	/**
	 * return the quantity of the Stock of sales owned by this industry. Return NaN if the stock cannot be found (which is an error)
	 * 
	 * @return the quantity of sales stock owned by this industry
	 */
	public double getSalesQuantity() {
		Stock s = getSalesStock();
		return s == null ? Float.NaN : s.getQuantity();
	}

	/**
	 * return the value of the sales stock owned by this industry. Return NaN if the stock cannot be found (which is an error)
	 * 
	 * @return the value of the sales stock owned by this industry
	 */
	public double getSalesValue() {
		Stock s = getSalesStock();
		return s == null ? Float.NaN : s.getValue();
	}

	/**
	 * return the price of the Stock of sales owned by this industry. Return NaN if the stock cannot be found (which is an error)
	 * 
	 * @return the quantity of sales stock owned by this industry
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
			logger.error("Industry {} attempted to set the quantity demanded of its consumption stock, but it does not have one", pk.name);
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
			logger.error("Industry {} attempted to set the quantity demanded of its consumption stock, but it does not have one", pk.name);
		}
	}

	/**
	 * provide a list of the productive stocks owned (managed) by this industry
	 * 
	 * @return a list of the productive stocks owned (managed) by this industry
	 */
	public List<Stock> productiveStocks() {
		return Stock.productiveByIndustry(pk.timeStamp, pk.name);
	}

	/**
	 * provide the productive stock with the given name
	 * 
	 * @return the productive stock with the given name
	 * @param name
	 *            the name of the stock
	 */
	public Stock productiveStock(String name) {
		return Stock.productiveNamedSingle(pk.timeStamp, pk.name, name);
	}

	/**
	 * @return the entityManager
	 */
	public static EntityManager getEntityManager() {
		return entityManager;
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

	public String getName() {
		return pk.name;
	}

	public double getOutput() {
		return output;
	}

	public void setOutput(double output) {
		this.output = MathStuff.round(output);
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
		if (!(object instanceof Industry)) {
			return false;
		}
		Industry other = (Industry) object;
		if ((this.pk == null && other.pk != null)
				|| (this.pk != null && !this.pk.equals(other.pk))) {
			return false;
		}
		return true;
	}

	/**
	 * add up the values of all the productive stocks and return them as a result
	 * 
	 * @return the total value of the productive stocks owned by this industry
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
	 * @return the total price of the productive stocks owned by this industry
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
	public double initialCapital() {
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
	 * Data for one industry as an arraylist, for flexibility. At the current project and the given timeStamp
	 * 
	 * @return One record, giving a dump of this industry, including its productive stocks, as an arrayList
	 */

	public ArrayList<String> industryContentsAsArrayList() {
		ArrayList<String> contents = new ArrayList<>();
		contents.add(Integer.toString(pk.timeStamp));
		contents.add(Integer.toString(pk.project));
		contents.add((pk.name));
		for (Stock s : productiveStocks()) {
			contents.add(String.format("%.2f", s.getQuantity()));
			contents.add(String.format("%.2f", s.getPrice()));
			contents.add(String.format("%.2f", s.getValue()));
		}
		contents.add(String.format("%.2f", getMoneyQuantity()));
		contents.add(String.format("%.2f", getSalesQuantity()));
		contents.add(String.format("%.2f", getSalesPrice()));
		contents.add(String.format("%.2f", getSalesValue()));
		contents.add(String.format("%.2f", output));
		contents.add(String.format("%.2f", proposedOutput));
		return contents;
	}

	/**
	 * The current capital of this circult.
	 * this is the sum of all outlays (including mone), that is to say, it is everything that has to be engaged in the business to keep it going
	 * it is always calculated using the price expression of these outlays
	 * 
	 * @return the current capital of this industry
	 * 
	 * 
	 */
	public double currentCapital() {
		return getMoneyPrice() + getSalesPrice() + productiveStocksAttribute(ValueExpression.PRICE);
	}

	/**
	 * Persist the profit. Needed because the Prices phase needs to 'remember' what profits were,
	 * after revenue has been transferred to the capitalist class
	 */
	public void persistProfit() {
		persistedProfit = currentCapital() - initialCapital;
	}

	/**
	 * The profit of this industry.
	 * This is the current capital less the initial capital. It is thus a simple difference independent of sales,
	 * and hence makes no assumption that profit is realised.
	 * Like all capital, it is calculated using the price expression of the magnitudes involved.
	 * 
	 * We used the persisted magnitude of the currentCapital member because this must be 'remembered'
	 * in order that the Prices phase can have access to it. Otherwise it would vanish when profit
	 * is transferred to the owner
	 * 
	 * TODO introduce a loaned capital variable to sort this out - further down the road, however.
	 * 
	 * @return the profit (so far) of this industry.
	 */
	public double profit() {
		return persistedProfit;
	}

	/**
	 * @return the profitRate
	 */
	public double profitRate() {
		if (MathStuff.round(initialCapital) == 0) {
			return Double.NaN;
		}
		return profit() / initialCapital;
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
	 * given the means of production available for investment, determine the possible level of output
	 * and then calculate the additional cost of labour power needed to achieve this.
	 * 
	 * TOODO this is a rudimentary procedure written on the assumption there is only a single stock of type Means of Production.
	 * It needs to be generalised.
	 * 
	 * @param extraMeansOfProduction
	 *            the additional Means of Production which it is proposed this industry should acquire.
	 * @return the output level that can be attained by acquiring extraMeansOfProduction
	 */
	public double computePossibleOutput(double extraMeansOfProduction) {
		Stock mP = productiveStock("Means of Production");
		Commodity u = mP.getCommodity();
		double price = u.getUnitPrice();
		double extraStock = extraMeansOfProduction / price;
		return output + extraStock / mP.getProductionCoefficient();
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
	 * Allocate funds for expansion
	 * 
	 * @param costOfExpansion
	 *            how much needs to be allocated
	 */

	public void allocateInvestmentFunds(double costOfExpansion) {
		Stock recipientMoneyStock = getMoneyStock();
		SocialClass donor = SocialClass.socialClassByName("Capitalists");
		Stock donorMoneyStock = donor.getMoneyStock();
		donorMoneyStock.transferStock(recipientMoneyStock, costOfExpansion);
		donor.setRevenue(donor.getRevenue() - costOfExpansion);
	}

	/**
	 * set the comparator Industry of this Industry. When a TableCell displays a value from this Industry, it asks the Industry
	 * to tell it whether the value has changed in comparison with another timeStamp, and by how much.
	 * The comparator allows the Industry to determine what information is provided
	 * 
	 * @param comparator
	 *            the comparator
	 */
	public void setComparator(Industry comparator) {
		this.comparator = comparator;
	}

	/**
	 * @return the previousComparator
	 */
	public Industry getPreviousComparator() {
		return previousComparator;
	}

	/**
	 * @param previousComparator
	 *            the previousComparator to set
	 */
	public void setPreviousComparator(Industry previousComparator) {
		this.previousComparator = previousComparator;
	}

	/**
	 * @return the startComparator
	 */
	public Industry getStartComparator() {
		return startComparator;
	}

	/**
	 * @param startComparator
	 *            the startComparator to set
	 */
	public void setStartComparator(Industry startComparator) {
		this.startComparator = startComparator;
	}

	/**
	 * @return the customComparator
	 */
	public Industry getCustomComparator() {
		return customComparator;
	}

	/**
	 * @param customComparator
	 *            the customComparator to set
	 */
	public void setCustomComparator(Industry customComparator) {
		this.customComparator = customComparator;
	}

	/**
	 * @return the endComparator
	 */
	public Industry getEndComparator() {
		return endComparator;
	}

	/**
	 * @param endComparator
	 *            the endComparator to set
	 */
	public void setEndComparator(Industry endComparator) {
		this.endComparator = endComparator;
	}

	/**
	 * @return the productiveCapital
	 */
	public double productiveCapital() {
		return productiveCapital;
	}

	/**
	 * @param productiveCapital
	 *            the productiveCapital to set
	 */
	public void setProductiveCapital(double productiveCapital) {
		this.productiveCapital = productiveCapital;
	}

}
