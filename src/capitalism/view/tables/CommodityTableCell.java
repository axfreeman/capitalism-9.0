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
package capitalism.view.tables;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.model.Commodity;
import capitalism.view.ViewManager;
import capitalism.view.custom.DisplayControlsBox;
import capitalism.view.custom.TrackingControlsBox;
import javafx.scene.control.TableCell;
import javafx.scene.paint.Color;

public class CommodityTableCell extends TableCell<Commodity, String> {
	static final Logger logger = LogManager.getLogger("CommodityTableCell");

	Commodity.SELECTOR selector;

	public CommodityTableCell(Commodity.SELECTOR selector) {
		this.selector = selector;
	}

	@Override protected void updateItem(String item, boolean empty) {
		super.updateItem(item, empty);
		if (item == null) {// this happens,it seems, when the tableRow is used for the column header-or perhaps when cell is empty?
			return;
		}
		Commodity commodity = getTableView().getItems().get(getIndex());
		if (commodity == null) {
			logger.debug(" Null Use Value");
			return;
		}
		
		if (item.equals("NaN")) {
			setText("-");
			return;
		}
		String deltaModifier="";
		
		if (commodity.changed(selector)) {
			setTextFill(Color.RED);
			deltaModifier=(TrackingControlsBox.displayDeltas?ViewManager.deltaSymbol:"");
		}

		String valueAndPriceModifier= deltaModifier+DisplayControlsBox.expressionSymbol;
		String quantityModifier=deltaModifier;
		
		if(TrackingControlsBox.displayDeltas) {
			item=commodity.showDelta(item, selector);
		}
		switch (selector) {
		case ALLOCATIONSHARE:
		case REPLENISHMENT_DEMAND:
		case TOTALSUPPLY:
		case TOTALQUANTITY:
		case PROFITRATE:
			if (DisplayControlsBox.displayHints)
				setStyle("-fx-background-color: rgba(220,220,220,0.3)");
			item=quantityModifier+item;
			break;
		case UNITVALUE:
		case TOTALVALUE:
			if (DisplayControlsBox.displayHints)
				setStyle("-fx-background-color: rgb(255,225,225,0.3)");
			item = valueAndPriceModifier+ item;
			break;
		case UNITPRICE:
		case TOTALPRICE:
		case INITIALCAPITAL:
		case PROFIT:
			if (DisplayControlsBox.displayHints)
				setStyle("-fx-background-color: rgba(255,240,204,0.3)");
			item = valueAndPriceModifier + item;
			break;
		default:
			item=quantityModifier+item;
		}
		setText(item);
	}
}
