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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.Serializable;
import javax.persistence.Column;
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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import capitalism.controller.Simulation;
import capitalism.utils.MathStuff;
import capitalism.view.ViewManager;
import capitalism.view.custom.TrackingControlsBox;

/**
 *
 * @author afree
 */
@Entity
@Table(name = "globals")
@NamedQueries({
		@NamedQuery(query = "Select c from Global c", name = "globals"),
		@NamedQuery(query = "SELECT c FROM Global c where c.pk.project = :project", name = "globals.project"),
		@NamedQuery(query = "SELECT c FROM Global c where c.pk.project = :project and c.pk.timeStamp = :timeStamp", name = "globals.project.timeStamp")
})
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="Global")
public class Global implements Serializable {

	private static final long serialVersionUID = 1L;
	@EmbeddedId protected GlobalPK pk;
	@XmlElement @Column(name = "RateOfExploitation") private double rateOfExploitation;
	@XmlElement @Column(name = "MELT") private double melt;
	@XmlElement @Column(name = "PopulationGrowthRate") private double populationGrowthRate;
	@XmlElement @Column(name = "InvestmentRatio") private double investmentRatio;
	@XmlElement @Column(name = "LabourSupplyResponse") private Simulation.LABOUR_RESPONSE labourSupplyResponse;
	@XmlElement @Column(name = "priceResponse") private Simulation.PRICE_RESPONSE priceResponse;
	@XmlElement @Column(name = "meltResponse") private Simulation.MELT_RESPONSE meltResponse;
	@XmlElement @Column(name = "CurrencySymbol") private String currencySymbol;
	@XmlElement @Column(name = "QuantitySymbol") private String quantitySymbol;

	@Transient private Global comparator = null;
	@Transient private Global previousComparator;
	@Transient private Global startComparator;
	@Transient private Global customComparator;
	@Transient private Global endComparator;

	private static EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("DB_GLOBALS");
	private static EntityManager entityManager;
	private static TypedQuery<Global> globalQuery;

	static {
		entityManager = entityManagerFactory.createEntityManager();
		globalQuery = entityManager.createNamedQuery("globals.project.timeStamp", Global.class);
	}

	public static enum GLOBAL_SELECTOR {
		// @formatter:off
		INITIALCAPITAL("Initial Capital"), 
		CURRENTCAPITAL("Current Capital"), 
		PROFIT("Profit"), 
		PROFITRATE("Profit Rate"), 
		TOTALVALUE("Total Value"), 
		TOTALPRICE("Total Price"), MELT("MELT"), 
		POPULATION_GROWTH_RATE("Population Growth Rate"), 
		LABOUR_SUPPLY_RESPONSE("Labour Supply Response"),
		PRICE_RESPONSE("Price Response"),
		MELT_RESPONSE("MELT Response");
		// @formatter:on

		String text;

		GLOBAL_SELECTOR(String text) {
			this.text = text;
		}

		public String text() {
			return text;
		}
	}

	public Global() {
		pk = new GlobalPK();
	}

	public Global(Global template) {
		pk = new GlobalPK();
		pk.timeStamp = template.pk.timeStamp;
		pk.project = template.pk.project;
		rateOfExploitation = template.getRateOfExploitation();
		melt = template.getMelt();
		populationGrowthRate = template.getPopulationGrowthRate();
		investmentRatio = template.investmentRatio;
		labourSupplyResponse = template.labourSupplyResponse;
		priceResponse = template.priceResponse;
		meltResponse = template.meltResponse;
		currencySymbol = template.currencySymbol;
		quantitySymbol = template.quantitySymbol;
	}

	public String value(GLOBAL_SELECTOR selector) {
		switch (selector) {
		case CURRENTCAPITAL:
			return String.format(ViewManager.getLargeFormat(), currentCapital());
		case INITIALCAPITAL:
			return String.format(ViewManager.getLargeFormat(), initialCapital());
		case LABOUR_SUPPLY_RESPONSE:
			return labourSupplyResponse.text();
		case PRICE_RESPONSE:
			return priceResponse.text();
		case MELT_RESPONSE:
			return meltResponse.text();
		case MELT:
			return String.format(ViewManager.getSmallFormat(), melt);
		case POPULATION_GROWTH_RATE:
			return String.format(ViewManager.getSmallFormat(), populationGrowthRate);
		case PROFIT:
			return String.format(ViewManager.getLargeFormat(), profit());
		case PROFITRATE:
			return String.format(ViewManager.getSmallFormat(), profitRate());
		case TOTALPRICE:
			return String.format(ViewManager.getLargeFormat(), totalPrice());
		case TOTALVALUE:
			return String.format(ViewManager.getLargeFormat(), totalValue());
		default:
			return "";
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
	 * @return the original item if nothing has changed, otherwise the change, as an appropriately formatted string
	 */

	public String showDelta(String item, GLOBAL_SELECTOR selector) {
		chooseComparison();
		switch (selector) {
		case CURRENTCAPITAL:
			return String.format(ViewManager.getLargeFormat(), currentCapital() - comparator.currentCapital());
		case INITIALCAPITAL:
			return String.format(ViewManager.getLargeFormat(), initialCapital() - comparator.initialCapital());
		case MELT:
			return String.format(ViewManager.getSmallFormat(), melt - comparator.melt);
		case PROFIT:
			return String.format(ViewManager.getLargeFormat(), profit() - comparator.profit());
		case PROFITRATE:
			return String.format(ViewManager.getSmallFormat(), profitRate() - comparator.profitRate());
		case TOTALPRICE:
			return String.format(ViewManager.getLargeFormat(), totalPrice() - comparator.totalPrice());
		case TOTALVALUE:
			return String.format(ViewManager.getLargeFormat(), totalValue() - comparator.totalValue());
		case LABOUR_SUPPLY_RESPONSE:
		case MELT_RESPONSE:
		case PRICE_RESPONSE:
		case POPULATION_GROWTH_RATE:
		default:
			return item;
		}
	}

	/**
	 * Shows whether the selected magnitude has changed.
	 * Returns false if this is expected to be constant
	 * 
	 * @param selector
	 *            the magnitude to be selected
	 * @return
	 * 		true if the selected variable has changed, false if it has not
	 */

	public boolean changed(GLOBAL_SELECTOR selector) {
		chooseComparison();
		switch (selector) {
		case CURRENTCAPITAL:
			return currentCapital() != comparator.currentCapital();
		case INITIALCAPITAL:
			return initialCapital() != comparator.initialCapital();
		case MELT:
			return melt != comparator.melt;
		case LABOUR_SUPPLY_RESPONSE:
		case PRICE_RESPONSE:
		case MELT_RESPONSE:
		case POPULATION_GROWTH_RATE:
			return false;
		case PROFIT:
			return profit() != comparator.profit();
		case PROFITRATE:
			return profitRate() != comparator.profitRate();
		case TOTALPRICE:
			return totalPrice() != comparator.totalPrice();
		case TOTALVALUE:
			return totalValue() != comparator.totalValue();
		default:
			return false;
		}
	}

	public static void setComparators(int timeStampID) {
		globalQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStampID);
		Global currentGlobal = globalQuery.getSingleResult();
		currentGlobal.setPreviousComparator(getGlobal(Simulation.getTimeStampComparatorCursor()));
		currentGlobal.setStartComparator(getGlobal(1));
		currentGlobal.setEndComparator(getGlobal(Simulation.timeStampIDCurrent));
		currentGlobal.setCustomComparator(getGlobal(Simulation.timeStampIDCurrent));
	}

	/**
	 * retrieve the global record for the specified timeStamp and project
	 * 
	 * @param project
	 *            the specified project
	 * @param timeStamp
	 *            the specified timeStamp
	 * @return the global record for the specified timeStamp and project
	 */
	public static Global getGlobal(int project, int timeStamp) {
		globalQuery.setParameter("project", project).setParameter("timeStamp", timeStamp);
		try {
			return globalQuery.getSingleResult();
		} catch (javax.persistence.NoResultException e) {
			return null;// because this query throws a fit if it doesn't find anything
		}
	}

	/**
	 * retrieve the global record for the specified timeStamp and the current Project
	 * 
	 * @param timeStamp
	 *            the specified timeStamp
	 * @return the global record for the current project and the current timeStamp, null if it does not exist (which is an error)
	 */
	public static Global getGlobal(int timeStamp) {
		globalQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", timeStamp);
		try {
			return globalQuery.getSingleResult();
		} catch (javax.persistence.NoResultException e) {
			return null;// because this query throws a fit if it doesn't find anything
		}
	}

	/**
	 * retrieve the global record at the current timeStamp and project
	 * 
	 * @return the global record for the current project and the current timeStamp, null if it does not exist (which is an error)
	 */
	public static Global getGlobal() {
		globalQuery.setParameter("project", Simulation.projectCurrent).setParameter("timeStamp", Simulation.timeStampIDCurrent);
		try {
			return globalQuery.getSingleResult();
		} catch (javax.persistence.NoResultException e) {
			return null;// because this query throws a fit if it doesn't find anything
		}
	}

	public double getRateOfExploitation() {
		return rateOfExploitation;
	}

	public void setRateOfExploitation(double rateOfExploitation) {
		this.rateOfExploitation = rateOfExploitation;
	}

	public double getMelt() {
		return melt;
	}

	public void setMelt(double melt) {
		this.melt = melt;
	}

	public double getPopulationGrowthRate() {
		return populationGrowthRate;
	}

	public void setPopulationGrowthRate(double populationGrowthRate) {
		this.populationGrowthRate = populationGrowthRate;
	}

	/**
	 * @return the total value in the economy
	 */
	public double totalValue() {
		// TODO replace by a sum query
		double totalValue = 0;
		for (Stock s : Stock.all(pk.timeStamp)) {
			if ((!s.getStockType().equals("Money")) || (Simulation.isFullPricing())) {
				totalValue += s.getValue();
			}
		}
		return totalValue;
	}

	/**
	 * @return the total price in the economy
	 */
	public double totalPrice() {
		// TODO replace by a sum query
		double totalPrice = 0;
		for (Stock s : Stock.all(pk.timeStamp)) {
			if ((!s.getStockType().equals("Money")) || (Simulation.isFullPricing())) {
				totalPrice += s.getPrice();
			}
		}
		return totalPrice;
	}

	/**
	 * @return the total initial capital in the economy
	 * 
	 */
	public double initialCapital() {
		double initialCapital = 0;
		for (Industry c : Industry.industriesAll(pk.timeStamp)) {
			initialCapital += c.productiveCapital();
		}
		// TODO get this aggregate query working
		// double checkInitialCapital;
		// checkInitialCapital=DataManager.industriesInitialCapital(pk.timeStamp);
		return initialCapital;
	}

	/**
	 * @return the total current capital in the economy
	 */

	public double currentCapital() {
		double currentCapital = 0;
		for (Industry c : Industry.industriesAll(pk.timeStamp)) {
			currentCapital += c.currentCapital();
		}
		return currentCapital;
	}

	/**
	 * @return the total profit in the economy for the current project and at the timeStamp of this global record
	 */
	public double profit() {
		double profit = 0.0;
		for (Commodity commodity : Commodity.commoditiesAll(pk.timeStamp)) {
			profit += commodity.profit();
		}
		return profit;
	}

	/**
	 * @return the profit rate for the whole economy
	 */

	public double profitRate() {
		double initialCapital = MathStuff.round(initialCapital());
		if (initialCapital == 0) {
			return Double.NaN;
		}
		return profit() / initialCapital();
	}

	/**
	 * set the timeStamp
	 * 
	 * @param timeStamp
	 *            the timeStamp to set
	 */

	public void setTimeStamp(int timeStamp) {
		this.pk.timeStamp = timeStamp;
	}

	public int getProject() {
		return pk.project;
	}

	@Override public int hashCode() {
		int hash = 0;
		hash += (pk != null ? pk.hashCode() : 0);
		return hash;
	}

	@Override public boolean equals(Object object) {
		if (!(object instanceof Global)) {
			return false;
		}
		Global other = (Global) object;
		if ((this.pk == null && other.pk != null)
				|| (this.pk != null && !this.pk.equals(other.pk))) {
			return false;
		}
		return true;
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
	 * @return the entityManager
	 */
	public static EntityManager getEntityManager() {
		return entityManager;
	}

	@Override public String toString() {
		return "demo.Globals[ persistent globalsPK=" + pk + " ]";
	}

	/**
	 * @return the investmentRatio
	 */
	public double getInvestmentRatio() {
		return investmentRatio;
	}

	/**
	 * @param investmentRatio
	 *            the investmentRatio to set
	 */
	public void setInvestmentRatio(double investmentRatio) {
		this.investmentRatio = investmentRatio;
	}

	/**
	 * @return the labourSupplyResponse
	 */
	public Simulation.LABOUR_RESPONSE getLabourSupplyResponse() {
		return labourSupplyResponse;
	}

	/**
	 * @param labourSupplyResponse
	 *            the labourSupplyResponse to set
	 */
	public void setLabourSupplyResponse(Simulation.LABOUR_RESPONSE labourSupplyResponse) {
		this.labourSupplyResponse = labourSupplyResponse;
	}

	/**
	 * @return the currencySymbol
	 */
	public String getCurrencySymbol() {
		return currencySymbol;
	}

	/**
	 * @param currencySymbol
	 *            the currencySymbol to set
	 */
	public void setCurrencySymbol(String currencySymbol) {
		this.currencySymbol = currencySymbol;
	}

	/**
	 * @return the quantitySymbol
	 */
	public String getQuantitySymbol() {
		return quantitySymbol;
	}

	/**
	 * @param quantitySymbol
	 *            the quantitySymbol to set
	 */
	public void setQuantitySymbol(String quantitySymbol) {
		this.quantitySymbol = quantitySymbol;
	}

	/**
	 * @return the comparator
	 */
	public Global getComparator() {
		return comparator;
	}

	/**
	 * @param comparator
	 *            the comparator to set
	 */
	public void setComparator(Global comparator) {
		this.comparator = comparator;
	}

	/**
	 * @return the previousComparator
	 */
	public Global getPreviousComparator() {
		return previousComparator;
	}

	/**
	 * @param previousComparator
	 *            the previousComparator to set
	 */
	public void setPreviousComparator(Global previousComparator) {
		this.previousComparator = previousComparator;
	}

	/**
	 * @return the startComparator
	 */
	public Global getStartComparator() {
		return startComparator;
	}

	/**
	 * @param startComparator
	 *            the startComparator to set
	 */
	public void setStartComparator(Global startComparator) {
		this.startComparator = startComparator;
	}

	/**
	 * @return the customComparator
	 */
	public Global getCustomComparator() {
		return customComparator;
	}

	/**
	 * @param customComparator
	 *            the customComparator to set
	 */
	public void setCustomComparator(Global customComparator) {
		this.customComparator = customComparator;
	}

	/**
	 * @return the endComparator
	 */
	public Global getEndComparator() {
		return endComparator;
	}

	/**
	 * @param endComparator
	 *            the endComparator to set
	 */
	public void setEndComparator(Global endComparator) {
		this.endComparator = endComparator;
	}

	/**
	 * @return the priceResponse
	 */
	public Simulation.PRICE_RESPONSE getPriceResponse() {
		return priceResponse;
	}

	/**
	 * @param priceResponse
	 *            the priceResponse to set
	 */
	public void setPriceResponse(Simulation.PRICE_RESPONSE priceResponse) {
		this.priceResponse = priceResponse;
	}

	/**
	 * @return the meltResponse
	 */
	public Simulation.MELT_RESPONSE getMeltResponse() {
		return meltResponse;
	}

	/**
	 * @param meltResponse
	 *            the meltResponse to set
	 */
	public void setMeltResponse(Simulation.MELT_RESPONSE meltResponse) {
		this.meltResponse = meltResponse;
	}
}
