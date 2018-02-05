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

import org.apache.commons.math3.util.Precision;

import rd.dev.simulation.Simulation;
import rd.dev.simulation.datamanagement.DataManager;

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
	@Column(name = "PopulationGrowthRate") private double populationGrowthRate;
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
		populationGrowthRate = globalTemplate.getPopulationGrowthRate();
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
		//TODO replace by a sum query
		double totalValue=0;
		for (Stock s:DataManager.stocksAll(pk.timeStamp)) {
			totalValue+=s.getValue();
		}
		return totalValue;
	}
	
	/**
	 * @return the total price in the economy
	 */
	public double totalPrice() {
		//TODO replace by a sum query
		double totalPrice=0;
		for (Stock s:DataManager.stocksAll(pk.timeStamp)) {
			totalPrice+=s.getPrice();
		}
		return totalPrice;
	}

	/**
	 * @return the total initial capital in the economy
	 * 
	 */
	public double initialCapital() {
		double initialCapital=0;
		for (Circuit c:DataManager.circuitsAll(pk.timeStamp)) {
			initialCapital+=c.getInitialCapital();
		}
		return initialCapital;
	}
	
	/**
	 * @return the total current capital in the economy
	 */
	
	public double currentCapital() {
		double currentCapital=0;
		for (Circuit c:DataManager.circuitsAll(pk.timeStamp)) {
			currentCapital+=c.currentCapital();
		}
		return currentCapital;
	}
	
	/**
	 * @return the total profit in the economy
	 */
	public double profit() {
		return currentCapital()-initialCapital();
	}
	
	/**
	 * @return the profit rate for the whole economy
	 */
	
	public double profitRate() {
		double initialCapital=Precision.round(initialCapital(),Simulation.getRoundingPrecision());
		if (initialCapital==0) {
			return Double.NaN;
		}
		return profit()/initialCapital();
	}
	
	/**
	 * set the timeStamp
	 * @param timeStamp the timeStamp to set
	 */

	public void setTimeStamp(int timeStamp) {
		this.pk.timeStamp=timeStamp;
	}
	
	public int getProject() {
		return pk.project;
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
