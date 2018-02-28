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

package capitalism.command;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.Simulation;
import capitalism.model.Commodity;
import capitalism.model.Industry;
import capitalism.model.SocialClass;
import capitalism.model.Stock;
import capitalism.model.Commodity.ORIGIN;
import capitalism.utils.Dialogues;
import capitalism.utils.MathStuff;
import capitalism.utils.Reporter;
import capitalism.view.custom.ActionStates;

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
		advanceOneStep(ActionStates.M_C_Trade.text(), ActionStates.M_C_PreTrade.text());

		productivePurchasesTrade();
		socialClassesTrade();
	}

	/**
	 * each productive industry purchases the stocks that it needs
	 */
	private void productivePurchasesTrade() {
		List<Industry> industries = Industry.industriesAll();
		Reporter.report(logger, 1, "The %d industries will now try to purchase the stocks they need. ", industries.size());

		for (Industry buyer : industries) {
			String buyerName = buyer.getIndustryName();
			Stock buyerMoneyStock = buyer.getMoneyStock();
			List<Stock> stocks = buyer.productiveStocks();

			Reporter.report(logger, 1, "Industry [%s] will purchase %d productive stocks to facilitate output of $%.0f ",
					buyerName, stocks.size(), buyer.getOutput());

			for (Stock s : stocks) {
				Commodity stockCommodity = s.getCommodity();
				double quantityPurchased = s.getReplenishmentDemand();
				double unitPrice = stockCommodity.getUnitPrice();
				if (quantityPurchased > 0) {
					Reporter.report(logger, 2, "Industry [%s] is purchasing %.0f units of [%s] for $%.0f", s.getOwner(), quantityPurchased,
							s.getCommodityName(), quantityPurchased * unitPrice);
					Stock sellerMoneyStock = null;
					Stock sellerSalesStock = null;
					if (s.getCommodity().getOrigin() == ORIGIN.SOCIALlY_PRODUCED){
						// ask each class if it has some labour power to sell
						// TODO at this point we only accept the first offer
						// eventually we need to allow multiple sellers of Labour Power
						// but this should be part of a general reform to allow multiple sellers of every commodity
						for (SocialClass sc : SocialClass.socialClassesAll()) {
							Stock salesStock = sc.getSalesStock();
							if (salesStock != null) {
								sellerMoneyStock = sc.getMoneyStock();
								sellerSalesStock = salesStock;
								Reporter.report(logger, 2, "Social class [%s] is going to sell %.0f units of [%s]", 
										sc.getSocialClassName(), quantityPurchased,s.getCommodityName());
							}
						}
						if (sellerSalesStock == null) {
							Dialogues.alert(logger, "Nobody is selling labour Power");
						}
						try {
							//TODO write industry procedure 'sell(quantity,buyer)' or alternatively buy(quantity,seller)?
							//problem here is duplicating what happens in a social class
							//we could make both of them implement an interface 'owner', or indeed, extend a class 'owner'

							sellerSalesStock.transferStock(s, quantityPurchased);
							buyerMoneyStock.transferStock(sellerMoneyStock, quantityPurchased * unitPrice);
						} catch (RuntimeException r) {
							Dialogues.alert(logger, "Problems transferring money. This is a programme error, so contact the developer " + r.getMessage());
						}
					} else {
						for (Industry seller:stockCommodity.industries()) {
							double marketShare=seller.getSalesQuantity()/stockCommodity.totalSupply();
							double quantitySold=marketShare*quantityPurchased;
							Reporter.report(logger, 2, "The industry [%s] is selling %.0f units of [%s]", 
									seller.getIndustryName(), quantitySold, stockCommodity.commodityName());
							sellerMoneyStock = seller.getMoneyStock();
							sellerSalesStock = seller.getSalesStock();
							try {
								sellerSalesStock.transferStock(s, quantitySold);
								buyerMoneyStock.transferStock(sellerMoneyStock, quantitySold* unitPrice);
							} catch (RuntimeException r) {
								Dialogues.alert(logger, "Problems transferring money. This is a programme error, so contact the developer " + r.getMessage());
							}
						}
					}
				}
			}
		}
	}

	/**
	 * each social class purchases the consumption goods that it needs
	 */
	private void socialClassesTrade() {
		Reporter.report(logger, 1, "Social Classes will now try to purchase the stocks they need");
		for (SocialClass buyer : SocialClass.socialClassesAll()) {
			String buyerName = buyer.getSocialClassName();
			for (Commodity u : Commodity.commoditiesByFunction(Commodity.FUNCTION.CONSUMER_GOOD)) {
				List<Industry> sellers = u.industries();

				Industry seller=sellers.get(0);// TODO very temporary; just get the top one.
	
				if (seller == null) {
					Dialogues.alert(logger, "Nobody seems to be selling the consumption good called [%s]", u.commodityName());
					break;
				}
				Stock consumptionStock = buyer.getConsumptionStock(u.commodityName());
				Stock buyerMoneyStock = buyer.getMoneyStock();
				Stock sellerSalesStock = seller.getSalesStock();
				Stock sellerMoneyStock = seller.getMoneyStock();
				double unitPrice = u.getUnitPrice();
				double allocatedDemand = consumptionStock.getReplenishmentDemand();
				double quantityAdded = allocatedDemand;
				double maximumQuantityAdded = buyerMoneyStock.getQuantity() / u.getUnitPrice();

				// a few little consistency checks

				if ((u == null) || (consumptionStock == null) || (buyerMoneyStock == null) || (sellerMoneyStock == null)
						|| (sellerSalesStock == null)) {
					Dialogues.alert(logger, "A stock required by [%s] to meet its needs is missing", buyerName);
					break;
				}
				if (buyer.getRevenue() > buyer.getMoneyQuantity()+MathStuff.epsilon) {
					logger.debug("Class {} has revenue {} and money {}",
							buyer.getSocialClassName(), buyer.getRevenue(),buyer.getMoneyQuantity());
					Dialogues.alert(logger,
							"Class %s has more revenue than money while purchasing the commodity %s. "
							+ "This is most probably a data error; try giving them more money."
							+ "If the problem persists, contact the developer",
							buyer.getSocialClassName(),u.commodityName());
					break;
				}
				if (maximumQuantityAdded < quantityAdded - MathStuff.epsilon) {
					logger.debug("Class {} cannot buy {} and instead has to buy {} with money {}",
							buyer.getSocialClassName(), quantityAdded,maximumQuantityAdded,buyer.getMoneyQuantity());
					Dialogues.alert(logger, "[%s] do not have enough money. This could be a data error; try giving them more money. If the problem persists, contact the developer", buyer.getSocialClassName());
					quantityAdded = maximumQuantityAdded;
					break;
				}

				// OK, it seems as if we are good to go
				
				Reporter.report(logger, 2, "The social class [%s] is buying %.0f units of [%s] for %.0f",
						buyerName, quantityAdded, u.commodityName(), quantityAdded * unitPrice);
				try {
					sellerSalesStock.transferStock(consumptionStock, quantityAdded);
					buyerMoneyStock.transferStock(sellerMoneyStock, quantityAdded * unitPrice);
					
				} catch (RuntimeException r) {
					logger.error("Transfer mis-specified:" + r.getMessage());
					r.printStackTrace();
				}
				double usedUpRevenue = quantityAdded * unitPrice;
				buyer.setRevenue(buyer.getRevenue() - usedUpRevenue);
				Reporter.report(logger, 2, "Disposable revenue reduced by $%.0f", usedUpRevenue);
			}
		}
	}


}