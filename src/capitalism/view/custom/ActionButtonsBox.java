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

package capitalism.view.custom;

import java.util.ArrayList;
import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.controller.command.OnePeriod;
import capitalism.model.Project;
import capitalism.reporting.Dialogues;
import capitalism.view.ViewManager;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;

/**
 * Custom control handles the action buttons. See relevant documentation at
 * <p>
 * https://stackoverflow.com/questions/30063792/adding-a-custom-component-to-scenebuilder-2-0 and
 * <p>
 * https://docs.oracle.com/javafx/2/fxml_get_started/fxml_deployment.htm
 * 
 */
public class ActionButtonsBox extends VBox {
	static final Logger logger = LogManager.getLogger("ActionButtonsBox");
	static private TreeView<String> treeView = null;
	static OnePeriod onePeriod = new OnePeriod();

	// the last action that was executed

	private static ActionStates lastAction;

	/**
	 * from the text associated with an actionState (normally the label text), make a note of the actionState. Used to store persistent {@link Project} records
	 * on the basis of a text representation of the actionstate associated with the project, the last time it was visited by the user.
	 * 
	 */
	private static HashMap<String, ActionStates> actionStatesFromLabel = new HashMap<String, ActionStates>();

	/**
	 * A convenience list of all the buttons, usable to enable/disable and possibly to switch graphics for text
	 */
	private static ArrayList<Button> allButtons = new ArrayList<Button>();

	/**
	 * a list of the superStates, used in populating the treeList
	 */
	private static ArrayList<ActionStates> superStates = new ArrayList<ActionStates>();

	/**
	 * the ActionButtonsBox constructor.
	 */
	public ActionButtonsBox() {
		setMaxWidth(Double.MAX_VALUE);
		createButtons();
	}

	/**
	 * create a treeList of buttons from the information in the {@link ActionStates} class.
	 */
	private void createButtons() {
		TreeItem<String> rootItem = new TreeItem<String>("");
		Button rootButton = new Button("One Period");
		allButtons.add(rootButton);
		rootButton.setPrefWidth(100);
		rootButton.setMaxWidth(USE_PREF_SIZE);
		rootItem.setGraphic(rootButton);
		rootItem.setExpanded(true);

		rootButton.setOnAction((event) -> {
			onePeriod.execute();
			lastAction = ActionStates.lastSuperState();
			enableButtons();
			ViewManager.refreshTimeStampView();
			ViewManager.refreshDisplay();
		});

		// First populate the superAction nodes
		for (ActionStates actionState : ActionStates.values()) {
			if (!actionState.isSubState) {
				superStates.add(actionState);
				addActionState(actionState, rootItem);
			}
		}

		// Now populate the leaf nodes
		for (ActionStates superState : superStates) {
			for (ActionStates subState : superState.children) {
				addActionState(subState, superState.treeItem);
			}
		}

		// Set up the treeView

		treeView = new TreeView<String>(rootItem);
		treeView.prefHeight(USE_COMPUTED_SIZE);
		this.getChildren().add(treeView);

		lastAction = ActionStates.lastSuperState();
		enableButtons();
	}

	public void addActionState(ActionStates actionState, TreeItem<String> rootItem) {
		TreeItem<String> item = new TreeItem<String>("");
		Button button = new Button(actionState.text());
		button.setPadding(Insets.EMPTY);
		button.setMaxSize(80, 12);
		button.setTooltip(new Tooltip(actionState.tooltip));
		allButtons.add(button);
		actionState.setButton(button);
		actionStatesFromLabel.put(actionState.text(), actionState);
		item.setGraphic(button);
		button.setOnAction((event) -> {
			actionState.getCommand().execute();
			lastAction = actionState;
			enableButtons();
			ViewManager.refreshTimeStampView();
			ViewManager.refreshDisplay();

		});
		rootItem.getChildren().add(item);
		actionState.treeItem = item;
	}

	/**
	 * enable only those buttons that are logically permissible after the action {@code lastAction}
	 */
	private void enableButtons() {
		for (Button button : allButtons) {
			button.setDisable(true);
		}
		ActionStates nextAction = lastAction.nextAction;
		if (nextAction == ActionStates.M_C_Exchange) { // we are at the beginning, enable the One Period Button
			treeView.getRoot().getGraphic().setDisable(false);
		}
		logger.debug("The last action was {} and the action {} will be enabled", lastAction.text(), nextAction.text());
		nextAction.button.setDisable(false);
		if (nextAction.firstSubAction != null) {
			nextAction.firstSubAction.button.setDisable(false);
			logger.debug("Enabling the subAction button with text {} from the super actionState {}", nextAction.firstSubAction.button.getText(),
					nextAction.text());
		}
	}

	/**
	 * set lastAction to the {@link ActionStates} enum that has the given label.
	 * 
	 * @param labelText
	 *            the text associated with an ActionStates entity
	 */
	public void setActionStateFromLabel(String labelText) {
		logger.debug("Last action has been reset to {}",labelText);
		lastAction = actionStatesFromLabel.get(labelText);
		if (lastAction==null) {
			Dialogues.alert(logger, "The App asked to revert to a non-existent action button "+labelText);
		}
		enableButtons();
	}

	public static ActionStates getLastAction() {
		return lastAction;
	}
}