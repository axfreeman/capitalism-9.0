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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.Serializable;
import java.util.Observable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

import rd.dev.simulation.Simulation;

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
@XmlRootElement
public class Global extends Observable implements Serializable {

	private static final long serialVersionUID = 1L;
	@EmbeddedId protected GlobalPK pk;
	@Column(name = "RateOfExploitation") private double rateOfExploitation;
	@Column(name = "MELT") private double melt;
	@Column(name = "Profit")private double profit;
	@Column(name = "ProfitRate") private double profitRate;
	@Column(name = "PopulationGrowthRate") private double populationGrowthRate;
	@Column(name= "InitialCapital") private double initialCapital;
	@Column(name = "CurrentCapital") private double currentCapital;
	@Column(name = "TotalValue") private double totalValue;
	@Column(name = "TotalPrice") private double totalPrice;
	@Column(name = "InvestmentRatio") private double investmentRatio;
	@Column(name = "LabourSupplyResponse") private Simulation.SupplyResponse labourSupplyResponse;
	@Column(name = "CurrencySymbol") private String currencySymbol;
	@Column(name = "QuantitySymbol") private String quantitySymbol;
	
	
	@Transient private double surplusMeansOfProduction = 0; // how much (in $) is available for investment
	
	public Global() {
		pk=new GlobalPK();
	}
	
	public void copyGlobal(Global globalTemplate) {
		pk.timeStamp=globalTemplate.pk.timeStamp;
		pk.project=globalTemplate.pk.project;
		rateOfExploitation = globalTemplate.getRateOfExploitation();
		melt = globalTemplate.getMelt();
		initialCapital=globalTemplate.initialCapital;
		currentCapital=globalTemplate.currentCapital;
		profit=globalTemplate.getProfit();
		profitRate = globalTemplate.getProfitRate();
		populationGrowthRate = globalTemplate.getPopulationGrowthRate();
		totalValue=globalTemplate.totalValue;
		totalPrice=globalTemplate.totalPrice;
		investmentRatio=globalTemplate.investmentRatio;
		labourSupplyResponse=globalTemplate.labourSupplyResponse;
		currencySymbol=globalTemplate.currencySymbol;
		quantitySymbol=globalTemplate.quantitySymbol;
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

	public double getProfitRate() {
		return profitRate;
	}

	public void setProfitRate(double profitRate) {
		this.profitRate = profitRate;
	}

	public double getPopulationGrowthRate() {
		return populationGrowthRate;
	}

	public void setPopulationGrowthRate(double populationGrowthRate) {
		this.populationGrowthRate = populationGrowthRate;
	}

	/**
	 * @return the totalValue
	 */
	public double getTotalValue() {
		return totalValue;
	}

	/**
	 * @param totalValue the totalValue to set
	 */
	public void setTotalValue(double totalValue) {
		this.totalValue = totalValue;
	}

	/**
	 * @return the totalPrice
	 */
	public double getTotalPrice() {
		return totalPrice;
	}

	/**
	 * @return the profit
	 */
	public double getProfit() {
		return profit;
	}

	/**
	 * @param profit the profit to set
	 */
	public void setProfit(double profit) {
		this.profit = profit;
	}

	public void setTimeStamp(int timeStamp) {
		this.pk.timeStamp=timeStamp;
	}
	
	public int getProject() {
		return pk.project;
	}

	/**
	 * @return the initialCapital
	 */
	public double getInitialCapital() {
		return initialCapital;
	}

	/**
	 * @param initialCapital the initialCapital to set
	 */
	public void setInitialCapital(double initialCapital) {
		this.initialCapital = initialCapital;
	}

	/**
	 * @return the currentCapital
	 */
	public double getCurrentCapital() {
		return currentCapital;
	}

	/**
	 * @param currentCapital the currentCapital to set
	 */
	public void setCurrentCapital(double currentCapital) {
		this.currentCapital = currentCapital;
	}

	/**
	 * @param totalPrice the totalPrice to set
	 */
	public void setTotalPrice(double totalPrice) {
		this.totalPrice = totalPrice;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		hash += (pk != null ? pk.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {
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

	@Override
	public String toString() {
		return "demo.Globals[ persistent globalsPK=" + pk + " ]";
	}

	/**
	 * @return the investmentRatio
	 */
	public double getInvestmentRatio() {
		return investmentRatio;
	}

	/**
	 * @param investmentRatio the investmentRatio to set
	 */
	public void setInvestmentRatio(double investmentRatio) {
		this.investmentRatio = investmentRatio;
	}

	/**
	 * @return the labourSupplyResponse
	 */
	public Simulation.SupplyResponse getLabourSupplyResponse() {
		return labourSupplyResponse;
	}

	/**
	 * @param labourSupplyResponse the labourSupplyResponse to set
	 */
	public void setLabourSupplyResponse(Simulation.SupplyResponse labourSupplyResponse) {
		this.labourSupplyResponse = labourSupplyResponse;
	}

	/**
	 * @return the surplusMeansOfProduction
	 */
	public double getSurplusMeansOfProduction() {
		return surplusMeansOfProduction;
	}

	/**
	 * @param totalSurplusOfMeansOfProduction the totalSurplusOfMeansOfProduction to set
	 */
	public void setSurplusMeansOfProduction(double totalSurplusOfMeansOfProduction) {
		this.surplusMeansOfProduction = totalSurplusOfMeansOfProduction;
	}

	/**
	 * @return the currencySymbol
	 */
	public String getCurrencySymbol() {
		return currencySymbol;
	}

	/**
	 * @param currencySymbol the currencySymbol to set
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
	 * @param quantitySymbol the quantitySymbol to set
	 */
	public void setQuantitySymbol(String quantitySymbol) {
		this.quantitySymbol = quantitySymbol;
	}
	

}
