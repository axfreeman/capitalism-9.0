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
	}

	/**
	 * each productive circuit purchases the stocks that it needs
	 */
	private void productivePurchasesTrade() {
		List<Circuit> circuits = DataManager.circuitsAll();
		Reporter.report(logger, 0, " The %d industries will now try to purchase the stocks they need. ", circuits.size());

		for (Circuit buyer : circuits) {
			String buyerName = buyer.getProductUseValueName();
			Stock buyerMoneyStock = buyer.getMoneyStock();
			List<Stock> stocks = buyer.productiveStocks();

			Reporter.report(logger, 1, " Industry [%s] will purchase %d productive stocks to facilitate output of $%.0f ",
					buyerName, stocks.size(), buyer.getConstrainedOutput());

			for (Stock s : stocks) {
				String useValueName = s.getUseValueName();
				UseValue stockUseValue = s.getUseValue();
				double quantityTransferred = s.getQuantityDemanded();
				double unitPrice = stockUseValue.getUnitPrice();
				if (quantityTransferred > 0) {
					Reporter.report(logger, 1, " Industry [%s] is purchasing %.0f units of [%s] for $%.0f", s.getOwner(), quantityTransferred,
							s.getUseValueName(), quantityTransferred * unitPrice);
					Stock sellerMoneyStock = null;
					Stock sellerSalesStock = null;
					if (s.useValueType() == USEVALUETYPE.LABOURPOWER) {
						// ask each class if it has some labour power to sell
						// TODO at this point we only accept the first offer
						// eventually we need to allow multiple sellers of Labour Power
						// but this should be part of a general reform to allow multiple sellers of every commodity
						for (SocialClass sc : DataManager.socialClassesAll()) {
							Stock salesStock = sc.getSalesStock();
							if (salesStock != null) {
								sellerMoneyStock = sc.getMoneyStock();
								sellerSalesStock = salesStock;
								Reporter.report(logger, 1, " Social class [%s] is going to sell %.0f units of [%s]", 
										sc.getSocialClassName(), quantityTransferred,s.getUseValueName());
							}
						}
						if (sellerSalesStock == null) {
							Dialogues.alert(logger, "Nobody is selling labour Power");
						}
					} else {
						Circuit seller = DataManager.circuitByProductUseValue(useValueName);
						Reporter.report(logger, 1, " The industry [%s] is selling [%s]", seller.getProductUseValueName(), s.getUseValueName());
						sellerMoneyStock = seller.getMoneyStock();
						sellerSalesStock = seller.getSalesStock();
					}
					try {
						transferStock(sellerSalesStock, s, quantityTransferred);
						transferStock(buyerMoneyStock, sellerMoneyStock, quantityTransferred * unitPrice);
					} catch (RuntimeException r) {
						Dialogues.alert(logger, "Problems transferring money. This is a programme error, so contact the developer " + r.getMessage());
					}
				}
			}
		}
	}

	/**
	 * each social class purchases the consumption goods that it needs
	 */
	private void socialClassesTrade() {
		Reporter.report(logger, 0, " Social Classes will now try to purchase the stocks they need");
		for (SocialClass buyer : DataManager.socialClassesAll()) {
			String buyerName = buyer.getSocialClassName();
			Reporter.report(logger, 1, " Purchasing for the social class [%s]", buyerName);
			for (UseValue u : DataManager.useValuesByType(UseValue.USEVALUETYPE.CONSUMPTION)) {
				Circuit seller = DataManager.circuitByProductUseValue(u.getUseValueName());
				if (seller == null) {
					Dialogues.alert(logger, "Nobody seems to be selling the consumption good called [%s]", u.getUseValueName());
					break;
				}
				Stock consumptionStock = buyer.getConsumptionStock(u.getUseValueName());
				Stock buyerMoneyStock = buyer.getMoneyStock();
				Stock sellerSalesStock = seller.getSalesStock();
				Stock sellerMoneyStock = seller.getMoneyStock();
				double unitPrice = u.getUnitPrice();
				double allocatedDemand = consumptionStock.getQuantityDemanded();
				double quantityAdded = allocatedDemand;
				double maximumQuantityAdded = buyerMoneyStock.getQuantity() / u.getUnitPrice();
				double epsilon = Simulation.getEpsilon();

				// a few little consistency checks

				if ((u == null) || (consumptionStock == null) || (buyerMoneyStock == null) || (sellerMoneyStock == null)
						|| (sellerSalesStock == null)) {
					Dialogues.alert(logger, "A stock required by [%s] to meet its needs is missing", buyerName);
					break;
				}
				if (buyer.getRevenue() > buyer.getMoneyQuantity()+Simulation.epsilon) {
					logger.debug("Class {} has revenue {} and money {}",
							buyer.getSocialClassName(), buyer.getRevenue(),buyer.getMoneyQuantity());
					Dialogues.alert(logger,
							"Class %s has more revenue than money while purchasing the commodity %s. "
							+ "This is most probably a data error; try giving them more money."
							+ "If the problem persists, contact the developer",
							buyer.getSocialClassName(),u.getUseValueName());
					break;
				}
				if (maximumQuantityAdded < quantityAdded - epsilon) {
					logger.debug("Class {} cannot buy {} and instead has to buy {} with money {}",
							buyer.getSocialClassName(), quantityAdded,maximumQuantityAdded,buyer.getMoneyQuantity());
					Dialogues.alert(logger, " [%s] do not have enough money. This could be a data error; try giving them more money. If the problem persists, contact the developer", buyer.getSocialClassName());
					quantityAdded = maximumQuantityAdded;
					break;
				}

				// OK, it seems as if we are good to go
				
				Reporter.report(logger, 2, "  The social class [%s] is buying %.0f units of [%s] for %.0f",
						buyerName, quantityAdded, u.getUseValueName(), quantityAdded * unitPrice);
				try {
					transferStock(sellerSalesStock, consumptionStock, quantityAdded);
					transferStock(buyerMoneyStock, sellerMoneyStock, quantityAdded * unitPrice);
					
				} catch (RuntimeException r) {
					logger.error("Transfer mis-specified:" + r.getMessage());
					r.printStackTrace();
				}
				double usedUpRevenue = quantityAdded * unitPrice;
				buyer.setRevenue(buyer.getRevenue() - usedUpRevenue);
				Reporter.report(logger, 2, "  Disposable revenue reduced by $%.0f", usedUpRevenue);
			}
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
			if (!Precision.equals(toPrice / toQuantity, unitPrice, epsilon)) {
				Dialogues.alert(logger, "The unit price of the [%s] is %.2f and the unit price of its use value is  %.2f",
						to.getUseValueName(), toPrice / toQuantity, unitPrice);
			}
			if (!Precision.equals(toValue / toQuantity, unitValue, epsilon)) {
				Dialogues.alert(logger, "The unit price of the stock [%s] is %.2f and the unit price of its use value is  %.2f",
						to.getUseValueName(), toPrice / toQuantity, unitPrice);
			}
		}
		if (fromQuantity != 0) {
			if (!Precision.equals(fromPrice / fromQuantity, unitPrice, epsilon)) {
				Dialogues.alert(logger, "The unit price of the target stock [%s] is %.2f and the unit price of its use value is  %.2f",
						from.getUseValueName(), fromPrice / fromQuantity, unitPrice);
			}
			if (!Precision.equals(fromValue / fromQuantity, unitValue, epsilon)) {
				Dialogues.alert(logger, "The unit price of the target stock [%s] is %.2f and the unit price of its use value is  %.2f",
						from.getUseValueName(), fromPrice / fromQuantity, unitPrice);
			}
		}
		logger.debug(String.format("   Transfer %.2f from [%s] in [%s] to [%s] in [%s]",
				quantityTransferred, from.getUseValueName(), from.getOwner(), to.getUseValueName(), to.getOwner()));
		logger.debug(String.format("   Recipient [%s] size is: %.2f", to.getUseValueName(), to.getQuantity()));
		logger.debug(String.format("   Donor [%s] size is: %.2f ", from.getUseValueName(), from.getQuantity()));

		to.modifyBy(quantityTransferred);
		from.modifyBy(-quantityTransferred);

		logger.debug(String.format("   Recipient [%s] size is now: %.2f ", to.getUseValueName(), to.getQuantity()));
		logger.debug(String.format("   Donor [%s] size is now: %.2f ", from.getUseValueName(), from.getQuantity()));
	}
}