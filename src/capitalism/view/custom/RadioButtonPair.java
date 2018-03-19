package capitalism.view.custom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.editor.Editor;
import capitalism.view.ViewManager;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

/**
 * Tiny helper class to give a binary choice pair of radio buttons
 * @author afree
 *
 */

public class RadioButtonPair extends HBox{
	private final static Logger logger = LogManager.getLogger("RadioButtonPair");
	private RadioButton firstButton;
	private RadioButton secondButton;
	private ToggleGroup stocksToggle = new ToggleGroup();
	private static boolean result;

	/**
	 * 
	 * @param firstText the text accompanying the first button
	 * @param secondText the text accompanying the second button
	 * @param firstTip the Tooltip accompaning the first button
	 * @param secondTip the Tooltip accompaning the second button
	 */
	
	public RadioButtonPair(String firstText, String secondText, String firstTip, String secondTip) {
		firstButton = new RadioButton(firstText);
		secondButton = new RadioButton(secondText);
		firstButton.setUserData("ONE");
		secondButton.setUserData("TWO");
		
		ViewManager.setTip(firstButton,firstTip);
		ViewManager.setTip(secondButton,secondTip);

		getChildren().addAll(firstButton,secondButton);
		result = true;
		firstButton.setToggleGroup(stocksToggle);
		secondButton.setToggleGroup(stocksToggle);
		firstButton.setUserData(firstText);
		secondButton.setUserData(secondText);
		firstButton.setSelected(true);
		stocksToggle.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			@Override public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
				if (stocksToggle.getSelectedToggle() != null) {
					String selected = stocksToggle.getSelectedToggle().getUserData().toString();
					switch (selected) {
					case "ONE":
						result = true;
						break;
					case "TWO":
						result = false;
						break;
					default:
						logger.error("Unknown radio button {} selected ", selected);
						result =true;
						break;
					}
					Editor.refresh();
				}
			}
		});
		firstButton.setPrefHeight(30);
		secondButton.setPrefHeight(30);
		firstButton.setPadding(new Insets(2, 0, 0, 0));
		secondButton.setPadding(new Insets(2, 0, 0, 0));
	}
	
	/**
	 * Tell us what the user did
	 * @return true if the user selected the first button(default), false otherwise
	 */
	public boolean result() {
		return result;
	}
}
