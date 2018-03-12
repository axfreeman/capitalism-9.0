/*
 *  Copyright 2017-2019
 *  
 *  This file is part of the Capitalism Simulation, abbreviated to CapSim
 *  in thEditorManagerf this project
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
package capitalism.editor;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * This class contains the editable information copied from a persistent timeStamp record.
 * After editing, it is saved back to a new persistent timeStamp record.
 * 
 * TODO write an editor page for the timeStamp. At present, the existing persistent data is merely copied in and out.
 * 
 * @author afree
 *
 */

public class EditableTimeStamp {
	private DoubleProperty period;
	private DoubleProperty melt;
	private DoubleProperty populationGrowthRate;
	private DoubleProperty investmentRatio;
	private StringProperty labourSupplyResponse;
	private StringProperty priceResponse;
	private StringProperty meltResponse;
	private StringProperty currencySymbol;
	private StringProperty quantitySymbol;

	EditableTimeStamp() {
		period = new SimpleDoubleProperty(1);
		populationGrowthRate = new SimpleDoubleProperty(1);
		investmentRatio = new SimpleDoubleProperty(0);
		labourSupplyResponse = new SimpleStringProperty("FIXED");
		priceResponse = new SimpleStringProperty("VALUES");
		meltResponse = new SimpleStringProperty("");
		currencySymbol = new SimpleStringProperty("$");
		quantitySymbol = new SimpleStringProperty("#");
	}

	/**
	 * @return the period
	 */
	public Double getPeriod() {
		return period.get();
	}

	/**
	 * @param period
	 *            the period to set
	 */
	public void setPeriod(Double period) {
		this.period.set(period);
	}

	/**
	 * @return the melt
	 */
	public Double getMelt() {
		return melt.get();
	}

	/**
	 * @param melt
	 *            the melt to set
	 */
	public void setMelt(Double melt) {
		this.melt.set(melt);
	}

	/**
	 * @return the populationGrowthRate
	 */
	public Double getPopulationGrowthRate() {
		return populationGrowthRate.get();
	}

	/**
	 * @param populationGrowthRate
	 *            the populationGrowthRate to set
	 */
	public void setPopulationGrowthRate(Double populationGrowthRate) {
		this.populationGrowthRate.set(populationGrowthRate);
	}

	/**
	 * @return the investmentRatio
	 */
	public Double getInvestmentRatio() {
		return investmentRatio.get();
	}

	/**
	 * @param investmentRatio
	 *            the investmentRatio to set
	 */
	public void setInvestmentRatio(Double investmentRatio) {
		this.investmentRatio.set(investmentRatio);
	}

	/**
	 * @return the labourSupplyResponse
	 */
	public String getLabourSupplyResponse() {
		return labourSupplyResponse.get();
	}

	/**
	 * @param labourSupplyResponse
	 *            the labourSupplyResponse to set
	 */
	public void setLabourSupplyResponse(String labourSupplyResponse) {
		this.labourSupplyResponse.set(labourSupplyResponse);
	}

	/**
	 * @return the priceResponse
	 */
	public String getPriceResponse() {
		return priceResponse.get();
	}

	/**
	 * @param priceResponse
	 *            the priceResponse to set
	 */
	public void setPriceResponse(String priceResponse) {
		this.priceResponse.set(priceResponse);
	}

	/**
	 * @return the meltResponse
	 */
	public String getMeltResponse() {
		return meltResponse.get();
	}

	/**
	 * @param meltResponse
	 *            the meltResponse to set
	 */
	public void setMeltResponse(String meltResponse) {
		this.meltResponse.set(meltResponse);
	}

	/**
	 * @return the currencySymbol
	 */
	public String getCurrencySymbol() {
		return currencySymbol.get();
	}

	/**
	 * @param currencySymbol
	 *            the currencySymbol to set
	 */
	public void setCurrencySymbol(String currencySymbol) {
		this.currencySymbol.set(currencySymbol);
	}

	/**
	 * @return the quantitySymbol
	 */
	public String getQuantitySymbol() {
		return quantitySymbol.get();
	}

	/**
	 * @param quantitySymbol
	 *            the quantitySymbol to set
	 */
	public void setQuantitySymbol(String quantitySymbol) {
		this.quantitySymbol.set(quantitySymbol);
	}

}
