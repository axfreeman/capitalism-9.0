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

import capitalism.Simulation;
import capitalism.model.Commodity;
import capitalism.model.Industry;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 * An IndustryColumn contains the additional information needed to display a switchable graphic and to select the data item for display
 *
 * The data items delivered to the parent TableView for display in its cells are always strings;
 * the type conversion is handled by the Industry class.

 */
public class IndustryColumn extends TableColumn<Industry, String> {

	/**
	 * Produces a column to be displayed in a Industry table({@code TableView<Industry,String>}), whose value is a fixed field in a {@code Industry} bean
	 * that is chosen by the {@code selector} enum. Use the enum to set the header text and graphic, and prepare the column header so its graphic is switchable.
	 * 
	 * @param selector
	 *            an enum specifying which field to display
	 * @param alignedLeft
	 *            true if the field data is to be displayed aligned left (typically text strings such as the names of commodities or owners)
	 */
	IndustryColumn(Industry.Selector selector, boolean alignedLeft) {
		super(selector.text());
		setCellFactory(new Callback<TableColumn<Industry, String>, TableCell<Industry, String>>() {
			@Override public TableCell<Industry, String> call(TableColumn<Industry, String> col) {
				return new CircuitTableCell(selector);
			}
		});
		setCellValueFactory(cellData -> cellData.getValue().wrappedString(selector, TabbedTableViewer.displayAttribute));

		// tailor the visual appearance of the column header

		setPrefWidth(75.0);
		if (!alignedLeft)
			getStyleClass().add("table-column-right");
		TableUtilities.addGraphicToColummnHeader(this, selector.imageName(), selector.tooltip());
	}

	/**
	 * Produces a column to be displayed in a Industry table({@code TableView<Industry,String>}), whose value is a {@code Stock} field referenced by a foreign key
	 * in a {@code Industry} bean. The magnitude is selected by the {@code  name} of the Stock.
	 * Use Stock itself to set the header text and graphic, and prepare the column header so its graphic is switchable.
	 * 
	 * @param commodity
	 *            an enum specifying which productive Stock to display
	 * 
	 */

	IndustryColumn(Commodity commodity) {
		String productiveStockName=commodity.commodityName();
		setCellFactory(new Callback<TableColumn<Industry, String>, TableCell<Industry, String>>() {
			@Override public TableCell<Industry, String> call(TableColumn<Industry, String> col) {
				return new CircuitTableStockCell(productiveStockName);
			}
		});
		setText(productiveStockName);
		setCellValueFactory(cellData -> cellData.getValue().wrappedString(productiveStockName));

		// tailor the visual appearance of the column header

		setPrefWidth(75.0);
		getStyleClass().add("table-column-right");
		Commodity stockCommodity = Commodity.commodityByPrimaryKey(Simulation.timeStampIDCurrent, productiveStockName);
		TableUtilities.addGraphicToColummnHeader(this, stockCommodity.getImageName(), commodity.getToolTip());
	}
}
