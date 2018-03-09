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

import capitalism.model.Industry;
import capitalism.view.TabbedTableViewer;
import capitalism.view.ViewManager;
import capitalism.view.custom.DisplayControlsBox;
import capitalism.view.custom.TrackingControlsBox;
import javafx.scene.control.TableCell;
import javafx.scene.paint.Color;
public class IndustryTableCell extends TableCell<Industry, String> {
	static final Logger logger = LogManager.getLogger("CircuitTableCell");

	Industry.INDUSTRY_ATTRIBUTE iNDUSTRY_ATTRIBUTE;

	public IndustryTableCell(Industry.INDUSTRY_ATTRIBUTE iNDUSTRY_ATTRIBUTE) {
		this.iNDUSTRY_ATTRIBUTE = iNDUSTRY_ATTRIBUTE;
	}

	@Override protected void updateItem(String item, boolean empty) {

		super.updateItem(item, empty);
		if (item == null) {// this happens,it seems, when the tableRow is used for the column header
			return;
		}
		Industry industry = getTableView().getItems().get(getIndex());
		if (industry == null) {
			logger.debug(" Null Industry");
			return;
		}
		
		String deltaModifier="";
		
		if (industry.changed(iNDUSTRY_ATTRIBUTE,TabbedTableViewer.displayAttribute)) {
			setTextFill(Color.RED);
			deltaModifier=(TrackingControlsBox.displayDeltas?ViewManager.deltaSymbol:"");
		}

		String valueAndPriceModifier= deltaModifier+DisplayControlsBox.expressionSymbol;
		String quantityModifier=deltaModifier;
		
		if(TrackingControlsBox.displayDeltas) {
			item=industry.showDelta(item, iNDUSTRY_ATTRIBUTE,TabbedTableViewer.displayAttribute);
		}

		switch (iNDUSTRY_ATTRIBUTE) {
		case OUTPUT:
		case PROPOSEDOUTPUT:
		case PROFITRATE:
			if (DisplayControlsBox.displayHints)
				setStyle("-fx-background-color: rgba(220,220,220,0.3)");
			setText(quantityModifier+item);
			break;
		case PRODUCTIVESTOCKS:
		case SALESSTOCK:
		case MONEYSTOCK:
			switch (TabbedTableViewer.displayAttribute) {
			case PRICE:
				if (DisplayControlsBox.displayHints)
					setStyle("-fx-background-color: rgba(255,240,204,0.3)");
				setText(valueAndPriceModifier+ item);
				break;
			case VALUE:
				if (DisplayControlsBox.displayHints)
					setStyle("-fx-background-color: rgb(255,225,225,0.3)");
				setText(valueAndPriceModifier + item);
				break;
			case QUANTITY:
				if (DisplayControlsBox.displayHints)
					setStyle("-fx-background-color: rgba(220,220,220,0.3)");
				setText(quantityModifier+item);
				break;
			}
			break;
		case INITIALCAPITAL:
		case CURRENTCAPITAL:
		case PROFIT:
			if (DisplayControlsBox.displayHints)
				setStyle("-fx-background-color: rgba(255,240,204,0.3)");
			setText(valueAndPriceModifier + item);
			break;
		default:
			setText(item);
			break;
		}
	}
}