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
*   
*/package rd.dev.simulation.custom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.css.PseudoClass;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import rd.dev.simulation.Capitalism;
import rd.dev.simulation.model.TimeStamp;

/**
 * with acknowledgement to contributors to https://stackoverflow.com/questions/30684308/javafx-treeview-css
 *
 */

public class TimeStampView extends TreeView<TimeStamp> {
	static final Logger logger = LogManager.getLogger("TimeStampView");
	PseudoClass subElementPseudoClass = PseudoClass.getPseudoClass("sub-tree-item");

	public TimeStampView() {
		super();
		setCellFactory(tv -> {
			TreeCell<TimeStamp> cell = new TreeCell<TimeStamp>() {
				@Override public void updateItem(TimeStamp item, boolean empty) {
					super.updateItem(item, empty);
					setDisclosureNode(null);
					if (empty) {
						setText("");
						setGraphic(null);
					} else {
						setText(item.getDescription()); // appropriate text for item
					}
				}
			};
			cell.treeItemProperty().addListener((obs, oldTreeItem, newTreeItem) -> {
				cell.pseudoClassStateChanged(subElementPseudoClass,
						newTreeItem != null && newTreeItem.getParent() != cell.getTreeView().getRoot());
			});
			return cell;
		});
		getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<TimeStamp>>() {
			@Override public void changed(ObservableValue<? extends TreeItem<TimeStamp>> observable, TreeItem<TimeStamp> oldValue,
					TreeItem<TimeStamp> newValue) {
				TimeStamp selectedTimeStamp=newValue.getValue();
				Capitalism.viewManager.viewTimeStamp(selectedTimeStamp);
				}
		});
	}
}
