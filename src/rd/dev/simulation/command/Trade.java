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

package rd.dev.simulation.command;

import java.util.List;

import org.apache.commons.math3.util.Precision;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rd.dev.simulation.Simulation;
import rd.dev.simulation.custom.ActionStates;
import rd.dev.simulation.datamanagement.DataManager;
import rd.dev.simulation.model.Circuit;
import rd.dev.simulation.model.SocialClass;
import rd.dev.simulation.model.Stock;
import rd.dev.simulation.model.UseValue;
import rd.dev.simulation.model.UseValue.USEVALUETYPE;
import rd.dev.simulation.utils.Dialogues;
import rd.dev.simulation.utils.Reporter;

/**
 * This class is responsible for all actions taken when the 'Trade' Button is pressed. It carries out the purchases, transferring output from sales to the
 * purchasers in the amounts decided by the constrained allocation, and pays for them
 * 
 * @author afree
 *
 */
public class Trade extends Simulation implements Command {
	private static final Logger logger = LogManager.getLogger(Trade.class);

	public void execute() {
		Reporter.report(logger, 0, "TRADE");
		advanceOneStep(ActionStates.M_C_Trade.getText(), ActionStates.M_C_PreTrade.getText());

		productivePurchasesTrade();
		socialClassesTrade();
		
		// validate the use value aggregates from the stocks

		calculateUseValueAggregates(true);
	}

	/**
	 * each productive circuit purchases the stocks that it needs
	 */
	private void productivePurchasesTrade() {
		List<Circuit> circuits = DataManager.circuitsAll();
		Reporter.report(logger, 1, " Industries will now purchase the stocks they need. There are %d of them", circuits.size());

		for (Circuit buyer : circuits) {
			String buyerName = buyer.getProductUseValueType();
			Stock buyerMoneyStock = buyer.getMoneyStock();
			List<Stock> stocks = buyer.productiveStocks();

			Reporter.report(logger, 2, "  Industry [%s] will purchase %d productive stocks to facilitate output of $%.2f ", buyerName,	stocks.size(), buyer.getConstrainedOutput());

			for (Stock s : stocks) {
				String useValueName = s.getUseValueName();
				UseValue stockUseValue = s.getUseValue();
				double quantityTransferred = s.getQuantityDemanded();
				double unitPrice = stockUseValue.getUnitPrice();
				Reporter.report(logger,2,"   industry [%s] is purchasing %.2f units of [%s] for $%.2f",s.getCircuit(), quantityTransferred, s.getUseValueName(), quantityTransferred*unitPrice);
				Stock sellerMoneyStock;
				Stock sellerSalesStock; 
				if (s.useValueType()==USEVALUETYPE.LABOURPOWER) {

					// TODO in general, we do not assume that a single class supplies the commodity labour power.
					// For example, small proprietors who also work for wages
					// therefore, we should scan all social classes to see if they have any labour power to offer
					// This is part of a larger deficiency in that we suppose only one supplier of each commodity
					// to be corrected in a later projectCurrent

					SocialClass workers = DataManager.socialClassByName(timeStampIDCurrent, "Workers");
					sellerMoneyStock = workers.getMoneyStock();
					sellerSalesStock = workers.getSalesStock();
				} else {
					Circuit seller = DataManager.circuitByProductUseValue(useValueName);
					sellerMoneyStock = seller.getMoneyStock();
					sellerSalesStock = seller.getSalesStock();
				}
				try {
					transferStock(sellerSalesStock, s, quantityTransferred);
					transferStock(buyerMoneyStock, sellerMoneyStock, quantityTransferred * unitPrice);
				} catch (RuntimeException r) {
					logger.error("ERROR: TRANSFER MIS-SPECIFIED:" + r.getMessage());
					r.printStackTrace();
				}
			}
		}
	}

	/**
	 * each social class purchases the consumption goods that it needs
	 */
	private void socialClassesTrade() {
		Reporter.report(logger, 1, " Purchasing for social classes");
		List<SocialClass> socialClasses = DataManager.socialClassesAll(timeStampIDCurrent);
		for (SocialClass buyer : socialClasses) {
			String buyerName = buyer.getSocialClassName();
			Reporter.report(logger, 2, "  Purchasing for the social class [%s]", buyerName);
			UseValue consumptionUseValue = DataManager.useValueByName(timeStampIDCurrent, "Consumption");
			Circuit seller = DataManager.circuitByProductUseValue("Consumption");
			if (seller == null) {
				Dialogues.alert(logger, "NOBODY IS SELLING CONSUMPTION GOODS ");
				break;
			}
			Stock consumptionStock = buyer.getConsumptionStock();
			Stock buyerMoneyStock = buyer.getMoneyStock();
			Stock sellerSalesStock = seller.getSalesStock();
			Stock sellerMoneyStock = seller.getMoneyStock();

			if ((consumptionUseValue == null) || (consumptionStock == null) || (buyerMoneyStock == null) || (sellerMoneyStock == null)
					|| (sellerSalesStock == null)) {
				Dialogues.alert(logger, "A STOCK NEEDED FOR THIS TRANSACTION IS NOT DEFINED");
				break;
			}

			double unitPrice = consumptionUseValue.getUnitPrice();
			double allocatedDemand = consumptionStock.getQuantityDemanded();
			double quantityAdded = allocatedDemand;
			double maximumQuantityAdded = buyerMoneyStock.getQuantity() / consumptionUseValue.getUnitPrice();
			double epsilon=Simulation.getEpsilon();
			if (maximumQuantityAdded < quantityAdded-epsilon) {
				Dialogues.alert(logger, buyer.getSocialClassName()+" does not have enough money. This is a progamme error. See log for details");
				quantityAdded = maximumQuantityAdded;
			}
			Reporter.report(logger, 2, "   The social class [%s] is buying %.2f units of Consumption goods for %.2f", 
					buyerName, quantityAdded, quantityAdded * unitPrice);
			try {
				transferStock(sellerSalesStock, consumptionStock, quantityAdded);
				transferStock(buyerMoneyStock, sellerMoneyStock, quantityAdded * unitPrice);
			} catch (RuntimeException r) {
				logger.error("ERROR: TRANSFER MIS-SPECIFIED:" + r.getMessage());
				r.printStackTrace();
			}
			double usedUpRevenue=quantityAdded*unitPrice;
			buyer.setRevenue(buyer.getRevenue()-usedUpRevenue);
			Reporter.report(logger, 2, "  Disposable revenue reduced by $%.2f", usedUpRevenue);
		}
	}

	/**
	 * Helper function transfers quantityTransferred from fromStock to toStock. Also transfers the value of the stock and the price. Carries out checks and
	 * throws an exception if conditions are violated
	 * 
	 * @param from
	 *            the stock that is losing the value
	 * @param to
	 *            the stock that is gaining the value
	 * @param quantityTransferred
	 *            the amount to transfer
	 */
	public void transferStock(Stock from, Stock to, double quantityTransferred) throws RuntimeException {
		UseValue useValue = from.getUseValue();
		if (quantityTransferred == 0) {
			return;			// Nothing to transfer
		}

		// a little consistency check

		if (!from.getUseValueName().equals(to.getUseValueName())) {
			throw new RuntimeException("ERROR: Attempt to transfer stock between useValues of different types");
		}

		double unitValue = useValue.getUnitValue();
		double unitPrice = useValue.getUnitPrice();
		double toValue = to.getValue();
		double fromValue = from.getValue();
		double toPrice = to.getPrice();
		double fromPrice = from.getPrice();
		double fromQuantity = from.getQuantity();
		double toQuantity = to.getQuantity();

		// another little consistency check

		if (toQuantity != 0) {
			if (!Precision.equals(toPrice / toQuantity,unitPrice,epsilon)) {
				throw new RuntimeException(String.format("ERROR: The unit price of the source stock [%s] is %.2f and the unit price of its use value is  %.2f",
						to.getUseValueName(), toPrice / toQuantity, unitPrice));
			}
			if (!Precision.equals(toValue/ toQuantity,unitValue,epsilon)) {
				throw new RuntimeException(String.format("ERROR: The unit price of the source stock [%s] is %.2f and the unit price of its use value is  %.2f",
						to.getUseValueName(), toPrice / toQuantity, unitPrice));
			}
		}
		if (fromQuantity != 0) {
			if (!Precision.equals(fromPrice / fromQuantity,unitPrice,epsilon)) {
				throw new RuntimeException(String.format("ERROR: The unit price of the target stock [%s] is %.2f and the unit price of its use value is  %.2f",
						from.getUseValueName(), fromPrice / fromQuantity, unitPrice));
			}
			if (!Precision.equals(fromValue/ fromQuantity,unitValue,epsilon)) {
				throw new RuntimeException(String.format("ERROR: The unit price of the target stock [%s] is %.2f and the unit price of its use value is  %.2f",
						from.getUseValueName(), fromPrice / fromQuantity, unitPrice));
			}
		}
		logger.debug(String.format("   Transfer %.2f from [%s] in [%s] to [%s] in [%s]",
				quantityTransferred, from.getUseValueName(), from.getCircuit(), to.getUseValueName(), to.getCircuit()));
		logger.debug(String.format("   Recipient [%s] size is: %.2f", to.getUseValueName(), to.getQuantity()));
		logger.debug(String.format("   Donor [%s] size is: %.2f ", from.getUseValueName(), from.getQuantity()));

		to.modifyBy(quantityTransferred);
		from.modifyBy(-quantityTransferred);

		logger.debug(String.format("   Recipient [%s] size is now: %.2f ", to.getUseValueName(), to.getQuantity()));
		logger.debug(String.format("   Donor [%s] size is now: %.2f ", from.getUseValueName(), from.getQuantity()));
	}
}