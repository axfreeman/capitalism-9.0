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
import rd.dev.simulation.Capitalism;
import rd.dev.simulation.Simulation;
import rd.dev.simulation.model.Global;
import rd.dev.simulation.model.Project;
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

		addLabel(0, 0, "Initial Capital");
		addLabel(0, 1, "Current Capital");
		addLabel(0, 2, "Profit");
		addLabel(0, 3, "Profit Rate");
		addLabel(1, 0, "Total Value");
		addLabel(1, 1, "Total Price");
		addLabel(1, 2, "MELT");
		addLabel(1, 3, "Population Growth Rate");
		addLabel(0, 4, "Price Dynamics");
		addLabel(1, 4, "Labour Supply Response");
	}

	/**
	 * add a new Glabel and store a reference to it, defined by the description string.
	 * 
	 * @param col
	 *            the column in the grid
	 * @param row
	 *            the row in the grid
	 * @param description
	 *            the text description
	 */
	private void addLabel(int col, int row, String description) {
		Glabel glabel = new Glabel();
		glabel.setDescription(description);
		gridPane.add(glabel, row, col);
		labelsByDescription.put(description, glabel.numberLabel);
	}

	private void setNumericLabel(String formatString, String description, double number) {
		Label numberLabel = labelsByDescription.get(description);
		numberLabel.setText(String.format(formatString, number));
	}
	
	
	private void setTextLabel(String text,String description) {
		Label numberLabel = labelsByDescription.get(description);
		numberLabel.setText(text);
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
		Project currentProject=Capitalism.selectionsProvider.projectSingle(Simulation.projectCurrent);
		setNumericLabel(ViewManager.largeNumbersFormatString, "Initial Capital",
				ViewManager.valueExpression(global.initialCapital(), ViewManager.valuesExpressionDisplay));
		setNumericLabel(ViewManager.largeNumbersFormatString, "Current Capital",
				ViewManager.valueExpression(global.currentCapital(), ViewManager.valuesExpressionDisplay));
		setNumericLabel(ViewManager.largeNumbersFormatString, "Total Value",
				ViewManager.valueExpression(global.totalValue(), ViewManager.valuesExpressionDisplay));
		setNumericLabel(ViewManager.largeNumbersFormatString, "Total Price",
				ViewManager.valueExpression(global.totalPrice(), ViewManager.pricesExpressionDisplay));
		setNumericLabel(ViewManager.largeNumbersFormatString, "Profit", global.profit());
		setNumericLabel(ViewManager.smallNumbersFormatString, "Profit Rate", global.profitRate());
		setNumericLabel(ViewManager.smallNumbersFormatString, "MELT", global.getMelt());
		setNumericLabel(ViewManager.smallNumbersFormatString, "Population Growth Rate", global.getPopulationGrowthRate());
		setTextLabel(currentProject.getPriceDynamics().getText(),"Price Dynamics");
		setTextLabel(global.getLabourSupplyResponse().toString(),"Labour Supply Response");
	}

	public void changeSmallNumberFormatString() {

	}
}
