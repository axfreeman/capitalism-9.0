package rd.dev.simulation.custom;

import java.io.IOException;
import java.util.HashMap;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import rd.dev.simulation.model.Global;
import rd.dev.simulation.view.ViewManager;

/**
 * rough and ready custom GUI control to display a numeric quantity and a description of it. Used to display the values in a Global entity.
 * The descriptions and the numbers are left and right aligned within each grid element
 * This is done by the {@link Glabel} controls that are added to the grid by the {@link addLabel} method.
 * TODO find a way to bind this to the persistent entity Global
 *
 */
public class SwitchableGraphicsGrid extends AnchorPane {

	@FXML GridPane gridPane;

	HashMap<String, Label> labelsByDescription = new HashMap<String, Label>();

	public SwitchableGraphicsGrid() {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SwitchableGraphicsGrid_layout.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			System.out.println(exception.getMessage());
			exception.printStackTrace();
		}
		setPadding(new Insets(3, 6, 6, 3));

		DropShadow dropShadow = new DropShadow();
		dropShadow.setOffsetX(2.0);
		dropShadow.setOffsetY(2.0);
		dropShadow.setColor(Color.BROWN);
		setEffect(dropShadow);
	}

	public void setGridCell(int col, int row, Global global, Global.GLOBAL_SELECTOR selector) {
		Glabel glabel = new Glabel();
		glabel.setDescription(selector.text());
		gridPane.add(glabel, row, col);
		Label label = glabel.numberLabel;
		String text = global.value(selector);
		
		String deltaModifier="";

		if (global.changed(selector)) {
			label.setTextFill(Color.RED);
			if (ViewManager.displayDeltas) {
				text=global.showDelta(text, selector);
				deltaModifier=(ViewManager.displayDeltas?ViewManager.deltaSymbol:"");
			}
		}
		label.setText(deltaModifier+text);
	}

	/**
	 * fill out the values in the grid from the supplied Global
	 * TODO obtain the global from DataManager
	 * TODO bind the global to the persistent JPA Global record so this happens automatically
	 * 
	 * @param floatFormatString
	 *            a format string that determines how the global is displayed
	 * @param global
	 *            the persistent Global that is used to get the numeric values
	 */
	public void populate(String floatFormatString, Global global) {
		setGridCell(0, 0, global, Global.GLOBAL_SELECTOR.INITIALCAPITAL);
		setGridCell(0, 1, global, Global.GLOBAL_SELECTOR.CURRENTCAPITAL);
		setGridCell(0, 2, global, Global.GLOBAL_SELECTOR.PROFIT);
		setGridCell(0, 3, global, Global.GLOBAL_SELECTOR.PROFITRATE);
		setGridCell(1, 0, global, Global.GLOBAL_SELECTOR.TOTALVALUE);
		setGridCell(1, 1, global, Global.GLOBAL_SELECTOR.TOTALPRICE);
		setGridCell(1, 2, global, Global.GLOBAL_SELECTOR.MELT);
		setGridCell(1, 3, global, Global.GLOBAL_SELECTOR.POPULATION_GROWTH_RATE);
		setGridCell(0, 4, global, Global.GLOBAL_SELECTOR.PRICE_DYNAMICS);
		setGridCell(1, 4, global, Global.GLOBAL_SELECTOR.LABOUR_SUPPLY_RESPONSE);
	}
}
