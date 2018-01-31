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
package rd.dev.simulation.view;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.scene.control.TableCell;
import javafx.scene.paint.Color;
import rd.dev.simulation.model.UseValue;

public class UseValueTableCell extends TableCell<UseValue, String> {
	static final Logger logger = LogManager.getLogger("UseValueTableCell");

	UseValue.Selector selector;

	public UseValueTableCell(UseValue.Selector selector) {
		this.selector = selector;
	}

	@Override protected void updateItem(String item, boolean empty) {
		super.updateItem(item, empty);
		if (item == null) {// this happens,it seems, when the tableRow is used for the column header-or perhaps when cell is empty?
			return;
		}
		UseValue useValue = getTableView().getItems().get(getIndex());
		if (useValue == null) {
			logger.debug(" Null Use Value");
			return;
		}
		setTextFill(useValue.changed(selector) ? Color.RED : Color.BLACK);
		switch(ViewManager.pricesExpressionDisplay){
		case MONEY:
			break;
		case TIME:
			break;
		}
		switch (selector) {
		case TOTALDEMAND:
		case TOTALSUPPLY:
		case TOTALQUANTITY:
			if (ViewManager.displayHints)
				setStyle("-fx-background-color: rgba(220,220,220,0.3)");
			break;
		case UNITVALUE:
		case TOTALVALUE:
			if (ViewManager.displayHints)
				setStyle("-fx-background-color: rgb(255,225,225,0.3)");
			item = ViewManager.valuesExpressionSymbol+ item;
			break;
		case UNITPRICE:
		case TOTALPRICE:
		case CAPITAL:
		case SURPLUSVALUE:
			if (ViewManager.displayHints)
				setStyle("-fx-background-color: rgba(255,240,204,0.3)");
			item = ViewManager.pricesExpressionSymbol+ item;
			break;
		default:
		}
		setText(item);
	}
}
