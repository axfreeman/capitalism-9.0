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

package capitalism.view;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.view.custom.DisplayControlsBox;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * Utilities for customising the display of tables
 *
 */
public class TableUtilities {
	static final Logger logger = LogManager.getLogger("TableUtilities");

	// TODO I think there is going to be a memory leak somewhere here but haven't had time to check it out
	private static ImageView makeLittleMinus(){
		ImageView littleMinus=new ImageView("littleminusred.png");
		littleMinus.setFitWidth(10);
		littleMinus.setFitHeight(10);
		return littleMinus;
	}
	private static ImageView makeLittlePlus() {
		ImageView littlePlus=new ImageView("littleplus.png");
		littlePlus= new ImageView("littleplusred.png");
		littlePlus.setFitWidth(10);
		littlePlus.setFitHeight(10);
		return littlePlus;
	}
	
	/**
	 * Skeleton method to add a context menu to a table: not used at present
	 * 
	 * @param table
	 *            the table to add the context menu to
	 */

	public static void createTableContextMenu(TableView<?> table) {
		ContextMenu contextMenu = new ContextMenu();

		EventHandler<ContextMenuEvent> eventHandler = new EventHandler<ContextMenuEvent>() {
			@Override public void handle(ContextMenuEvent e) {
				logger.debug("Context menu selected");
				contextMenu.show(table, e.getScreenX(), e.getSceneY());
			}
		};

		table.setOnContextMenuRequested(eventHandler);

		MenuItem item1 = new MenuItem("Item 1");
		item1.setOnAction(new EventHandler<ActionEvent>() {

			@Override public void handle(ActionEvent event) {
				logger.debug("Context menu item 1 selected");
			}
		});
		MenuItem item2 = new MenuItem("Item 2");
		item2.setOnAction(new EventHandler<ActionEvent>() {

			@Override public void handle(ActionEvent event) {
				logger.debug("Context menu item 2 selected");
			}
		});

		// Add MenuItem to ContextMenu
		contextMenu.getItems().addAll(item1, item2);
	}

	/**
	 * Add a graphic to a table columnn dynamically. If the graphic is null, do nothing.
	 * 
	 * @param imageURL
	 *            the URL of the image to be added
	 * @param column
	 *            the column
	 * @param toolTip
	 * 			an optional toolTip text to be added to the column header           
	 */

	public static void addGraphicToColummnHeader(TableColumn<?, ?> column, String imageURL, String toolTip) {
		Label label = new Label();
		label.setText(column.getText());
		column.setText("");
		if (toolTip!=null)
			label.setTooltip(new Tooltip(toolTip));
		column.setGraphic(label);
		if (imageURL != null) {
			ImageView image = new ImageView(imageURL);
			image.setFitWidth(20);
			image.setFitHeight(20);
			label.setGraphic(image);
			column.setText("");
			column.setUserData("HasGraphic"); 				// tells us that the heading has been doctored 
			label.setContentDisplay(ContentDisplay.TEXT_ONLY);
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
				// doctor the column if it is not a superColumn
				// otherwise doctor its subcolumns
				if (column.getColumns().size() == 0) {
					doctorColumnHeader(column);
				} else {
					for (TableColumn<?, ?> subColumn : column.getColumns()) {
						doctorColumnHeader(subColumn);
					}
				}
			}
		}
	}

	/**
	 * make supercolumn headers clickable so user can contract and expand them
	 * 
	 * @param superColumn
	 *            the column whose header is to be modified
	 * 
	 * @param showColumn
	 *            the one column that will be displayed in the contracted state
	 */

	public static void setSuperColumnHandler(TableColumn<?, ?> superColumn, TableColumn<?, ?> showColumn) {
		EventHandler<MouseEvent> eventHandler = new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent e) {
				
// use this code if we want to insist on a double click				
				if (e.getButton() != MouseButton.PRIMARY)
					return;
				if (e.getClickCount() != 2)
					return;
				
				// toggle display of all or only the minimal subColumns
				
				Label columnLabel=(Label)superColumn.getGraphic();
				switch ((String) superColumn.getUserData()) {
				case "Maximised":
					logger.debug("User minimised the column {}", superColumn.getText());
					superColumn.setUserData("Minimised");
					for (TableColumn<?, ?> subColumn : superColumn.getColumns()) {
						subColumn.setVisible(false);
					}
					showColumn.setVisible(true);
					columnLabel.setGraphic(makeLittlePlus());
					break;
				case "Minimised":
					logger.debug("User maximised the column {}", superColumn.getText());
					for (TableColumn<?, ?> subColumn : superColumn.getColumns()) {
						subColumn.setVisible(true);
					}
					superColumn.setUserData("Maximised");

					columnLabel.setGraphic(makeLittleMinus());
					break;
				default:
					break;
				}
			}
		};
		Label label = new Label();
		label.setText(superColumn.getText());
		label.setGraphic(makeLittleMinus());

// somewhat unsuccessful code to style the header, kept as comments for reference		
//		label.setMaxWidth(1000);
//		label.setPrefWidth(superColumn.getPrefWidth()-5);
//		label.setPadding(new Insets(2));
// css style for column headers
//		String headerCSS=".table-view .column-header{\n" + 
//				"    -fx-text-fill: -fx-selection-bar-text;\r\n" + 
//				"    -fx-font-size: 10;\n" + 
//				"    -fx-size: 11 ;\n" + 
//				"    -fx-font-family: \"Arial\";\n" + 
//				"    -fx-background-color: silver;\n" +
//				"}";
//		superColumn.getStyleClass().add(headerCSS);
//		final String cssNumberLabel= "-fx-background-color: silver;\n"
//				+ "-fx-border-color: silver;\n"
//				+ "-fx-border-width: 2;\n";
//		superColumn.setStyle(cssNumberLabel);
		
		superColumn.setGraphic(label);
		superColumn.setText("");
		label.addEventHandler(MouseEvent.MOUSE_CLICKED, eventHandler);
		superColumn.setUserData("Maximised");
	}

	/**
	 * Replace one column header with a custom header whose display can be modified to show graphics.
	 * Use jewelsea's method. Replace the graphic with a label, containing the text and the graphic.
	 * This gives more flexibility, because a Label has more functionality (such as switching between text and graphic display).
	 * 
	 * @param column
	 *            the column whose header is to be modified
	 */

	public static void doctorColumnHeader(TableColumn<?, ?> column) {
		Node columnGraphic = column.getGraphic();
		if (columnGraphic == null)
			return;
		if ("HasGraphic".equals(column.getUserData()))
			return;
		Label label = new Label();
		label.setText(column.getText());
		label.setGraphic(columnGraphic);
		column.setGraphic(label);
		column.setText("");
		column.setUserData("HasGraphic"); 				// tells us that the heading has been doctored }
		label.setContentDisplay(ContentDisplay.TEXT_ONLY);
	}

	/**
	 * Set all columns in the viewer to display a graphic only, if the graphic has been set. This will only work for those columns which have been 'doctored'
	 * using {@code doctorColumnHeaders()}. It applies to all doctored tables - individual tables are not(at present) singled out.
	 * <p>
	 * NOTE; the option to display both text and graphics is not available in the enum {@link DisplayControlsBox#graphicsState}, but we could possibly achieve the
	 * effect by 'undoctoring' the column headers.
	 * 
	 * @param tables
	 *            an ArrayList of the tables to be customised
	 * 
	 */
	public static void switchHeaderDisplays(ArrayList<TableView<?>> tables) {
		switch (DisplayControlsBox.getGraphicsState()) {
		case TEXT_ONLY:
			DisplayControlsBox.setGraphicsState(ContentDisplay.GRAPHIC_ONLY);
			break;
		case GRAPHIC_ONLY:
			DisplayControlsBox.setGraphicsState(ContentDisplay.TEXT_ONLY);
			break;
		default:
			DisplayControlsBox.setGraphicsState(ContentDisplay.TEXT_ONLY);
		}
		for (TableView<?> table : tables) {
			for (TableColumn<?, ?> column : table.getColumns()) {
				if ("HasGraphic".equals(column.getUserData())) {
					Label columnlabel = (Label) column.getGraphic();
					columnlabel.setContentDisplay(DisplayControlsBox.getGraphicsState());
				}
				for (TableColumn<?, ?> subColumn : column.getColumns()) {
					if ("HasGraphic".equals(subColumn.getUserData())) {
						Label columnlabel = (Label) subColumn.getGraphic();
						columnlabel.setContentDisplay(DisplayControlsBox.getGraphicsState());
					}
				}
			}
		}
	}
}
