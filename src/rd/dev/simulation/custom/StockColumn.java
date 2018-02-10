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

package rd.dev.simulation.custom;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import rd.dev.simulation.model.Stock;
import rd.dev.simulation.view.StockTableCell;

/**
* A StockColumn contains the additional information needed to display a switchable graphic and to select the data item for display
*
* The data items delivered to the parent TableView for display in its cells are always strings;
* the type conversion is handled by the Circuit class.
* TODO parameterise SocialClass so we can re-use for other data models (eg Circuits, for which the code is almost identical
*/
public class StockColumn extends TableColumn<Stock,String>{
	/**
	 * Produces a column to be displayed in a Stock table({@code TableView<Stock,String>}), whose value is a fixed field in a {@code Stock} bean
	 * that is chosen by the {@code selector} enum. Use the enum to set the header text and graphic, and prepare the column header so its graphic is switchable.
	 * 
	 * @param selector
	 * an enum specifying which field to display
	 * 
	 */
	StockColumn(Stock.Selector selector,boolean leftAlign) {
		super(selector.text());
		setCellFactory(new Callback<TableColumn<Stock, String>, TableCell<Stock, String>>() {
			@Override public TableCell<Stock, String> call(TableColumn<Stock, String> col) {
				return new StockTableCell(selector);
			}
		});
		setCellValueFactory(cellData -> cellData.getValue().wrappedString(selector));

		// tailor the visual appearance of the column header

		setPrefWidth(75.0);
		if(!leftAlign) getStyleClass().add("table-column-right");
		TableUtilities.addGraphicToColummnHeader(this, selector.imageName(),selector.tooltip());
	}
}
