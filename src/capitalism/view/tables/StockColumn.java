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

import capitalism.model.Stock;
import capitalism.view.TableUtilities;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
* A StockColumn contains the additional information needed to display a switchable graphic and to select the data item for display
*
* The data items delivered to the parent TableView for display in its cells are always strings;
* the type conversion is handled by the Industry class.

*/
public class StockColumn extends TableColumn<Stock,String>{
	/**
	 * Produces a column to be displayed in a Stock table({@code TableView<Stock,String>}), whose value is a fixed field in a {@code Stock} bean
	 * that is chosen by the {@code selector} enum. Use the enum to set the header text and graphic, and prepare the column header so its graphic is switchable.
	 * 
	 * @param sTOCK_ATTRIBUTE
	 * an enum specifying which field to display
	 * @param leftAlign true if the text in the field should be left aligned (right align is the default)
	 * 
	 */
	public StockColumn(Stock.STOCK_ATTRIBUTE sTOCK_ATTRIBUTE,boolean leftAlign) {
		super(sTOCK_ATTRIBUTE.text());
		setCellFactory(new Callback<TableColumn<Stock, String>, TableCell<Stock, String>>() {
			@Override public TableCell<Stock, String> call(TableColumn<Stock, String> col) {
				return new StockTableCell(sTOCK_ATTRIBUTE);
			}
		});
		setCellValueFactory(cellData -> cellData.getValue().wrappedString(sTOCK_ATTRIBUTE));

		// tailor the visual appearance of the column header

		setPrefWidth(75.0);
		if(!leftAlign) getStyleClass().add("table-column-right");
		TableUtilities.addGraphicToColummnHeader(this, sTOCK_ATTRIBUTE.imageName(),sTOCK_ATTRIBUTE.tooltip());
	}
}
