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
import rd.dev.simulation.Simulation;
import rd.dev.simulation.datamanagement.DataManager;
import rd.dev.simulation.model.Circuit;
import rd.dev.simulation.model.SocialClass;
import rd.dev.simulation.model.UseValue;
import rd.dev.simulation.view.CircuitTableCell;
import rd.dev.simulation.view.CircuitTableStockCell;
import rd.dev.simulation.view.SocialClassTableCell;

/**
* A CircuitGraphicsColumn contains the additional information needed to display a switchable graphic and to select the data item for display
*
* The data items delivered to the parent TableView for display in its cells are always strings;
* the type conversion is handled by the Circuit class.
* TODO parameterise SocialClass so we can re-use for other data models (eg Circuits, for which the code is almost identical
*/
public class SocialClassColumn extends TableColumn<SocialClass,String>{
	/**
	 * Produces a column to be displayed in a socialClass table({@code TableView<SocialClass,String>}), whose value is a fixed field in a {@code SocialClass} bean
	 * that is chosen by the {@code selector} enum. Use the enum to set the header text and graphic, and prepare the column header so its graphic is switchable.
	 * 
	 * @param selector
	 * an enum specifying which field to display
	 * 
	 * @return a SocialClassColumn ready to be added to a {@code TableView<SocialClass,String>}
	 */
	SocialClassColumn(SocialClass.Selector selector) {
		super(selector.text());
		setCellFactory(new Callback<TableColumn<SocialClass, String>, TableCell<SocialClass, String>>() {
			@Override public TableCell<SocialClass, String> call(TableColumn<SocialClass, String> col) {
				return new SocialClassTableCell(selector);
			}
		});
		setCellValueFactory(cellData -> cellData.getValue().wrappedString(selector, TabbedTableViewer.displayAttribute));
		TableUtilities.addGraphicToColummnHeader(this, selector.imageName());
		setPrefWidth(75.0);
		getStyleClass().add("table-column-right");
	}
	
	/**
	 * Produces a column to be displayed in a Circuit table({@code TableView<Circuit,String>}), whose value is a {@code Stock} field referenced by a foreign key 
	 * in a {@code Circuit} bean. The magnitude is selected by the {@useValueName} of the Stock.
	 * Use Stock itself to set the header text and graphic, and prepare the column header so its graphic is switchable.
	 * 
	 * @param productiveStockName
	 * an enum specifying which productive Stock to display
	 * 
	 * @return a CircuitColumn ready to be added to a {@code TableView<Circuit,String>}
	 */
	
	SocialClassColumn(String consumptionStockName){
		setCellFactory(new Callback<TableColumn<SocialClass, String>, TableCell<SocialClass, String>>() {
			@Override public TableCell<SocialClass, String> call(TableColumn<SocialClass, String> col) {
				return new SocialClassTableStockCell(consumptionStockName);
			}
		});
		setText(consumptionStockName);
		setCellValueFactory(cellData -> cellData.getValue().wrappedString(consumptionStockName));
		getStyleClass().add("table-column-right");
		UseValue stockUseValue=DataManager.useValueByName(Simulation.timeStampIDCurrent, consumptionStockName);
		String imageName=stockUseValue.getImageName();
		TableUtilities.addGraphicToColummnHeader(this, imageName);		
	}
}
