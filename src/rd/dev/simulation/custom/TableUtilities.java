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

import java.util.ArrayList;

import com.sun.org.apache.xml.internal.resolver.readers.DOMCatalogReader;

import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import rd.dev.simulation.view.ViewManager;

/**
 * Utilities for customising the display of tables
 *
 */
public class TableUtilities {

	/**
	 * add the specified tooltip to the specified table
	 * 
	 * @param tableView
	 *            the table that the tip is to be applied to
	 * @param text
	 *            the tip itself
	 */

	public static void setTip(TableView<?> tableView, String text) {
		Tooltip tip = new Tooltip();
		tip.setText(text);
		tip.setFont(new Font(15));
		tableView.setTooltip(tip);
	}

	/**
	 * Add a graphic to a table columnn dynamically. If the graphic is null, do nothing.
	 * 
	 * @param imageURL
	 *            the URL of the image to be added
	 * @param column
	 *            the column
	 */

	public static void addGraphicToColummnHeader(TableColumn<?, ?> column, String imageURL) {
		if (imageURL != null) {
			ImageView image = new ImageView(imageURL);
			image.setFitWidth(20);
			image.setFitHeight(20);
			column.setGraphic(image);
		}
	}

	/**
	 * replace all column headers in a table with custom headers whose display can be modified.
	 * Only do this if the column already has an embedded graphic.
	 * 
	 * @param tableViews
	 *            An ArrayList of the tables whose columns are to be doctored
	 */
	static void doctorColumnHeaders(ArrayList<TableView<?>> tableViews) {
		for (TableView<?> tableView : tableViews) {
			for (TableColumn<?, ?> column : tableView.getColumns()) {
				doctorOneColumnHeader(column);
				for (TableColumn<?, ?> subColumn : column.getColumns()) {
					doctorOneColumnHeader(subColumn);
				}
			}
		}
	}

	/**
	 * Replace one column header with a custom header whose display can be modified to show graphics
	 * 
	 * @param column
	 *            the column whose header is to be modified
	 */

	public static void doctorOneColumnHeader(TableColumn<?, ?> column) {
		Node columnGraphic = column.getGraphic();
		if (columnGraphic != null) {

			// Use jewelsea's method. Replace the graphic with a label, containing the text and the graphic
			// this gives more flexibility, because a Label has more functionality (such as switching between text and graphic display)

			Label label = new Label();
			label.setText(column.getText());
			label.setGraphic(columnGraphic);
			column.setGraphic(label);
			column.setText("");
			label.setTooltip(new Tooltip(label.getText())); // TODO this doesn't seem to work if there is a table tooltip active
			label.toFront();								// this doesn't have any effect on the tooltip either - needs to be investigated
			column.setUserData("HasGraphic"); 				// tells us that the heading has been doctored }
			label.setContentDisplay(ContentDisplay.TEXT_ONLY);
		}
	}

	/**
	 * Set all columns in the viewer to display a graphic only, if the graphic has been set. This will only work for those columns which have been 'doctored'
	 * using {@code doctorColumnHeaders()}. It applies to all doctored tables - individual tables are not(at present) singled out.
	 * <p>
	 * NOTE; the option to display both text and graphics is not available in the enum {@link ViewManager#graphicsState}, but we could possibly achieve the
	 * effect by 'undoctoring' the column headers.
	 * 
	 * @param tables
	 *            an ArrayList of the tables to be customised
	 * 
	 */
	public static void switchHeaderDisplays(ArrayList<TableView<?>> tables) {
		switch (ViewManager.graphicsState) {
		case TEXT_ONLY:
			ViewManager.graphicsState = ContentDisplay.GRAPHIC_ONLY;
			break;
		case GRAPHIC_ONLY:
			ViewManager.graphicsState = ContentDisplay.TEXT_ONLY;
			break;
		default:
			ViewManager.graphicsState = ContentDisplay.TEXT_ONLY;
		}
		for (TableView<?> table : tables) {
			for (TableColumn<?, ?> column : table.getColumns()) {
				if ("HasGraphic".equals(column.getUserData())) {
					Label columnlabel = (Label) column.getGraphic();
					columnlabel.setContentDisplay(ViewManager.graphicsState);
				}
				for (TableColumn<?, ?> subColumn : column.getColumns()) {
					if ("HasGraphic".equals(subColumn.getUserData())) {
						Label columnlabel = (Label) subColumn.getGraphic();
						columnlabel.setContentDisplay(ViewManager.graphicsState);
					}
				}
			}
		}
	}
}
