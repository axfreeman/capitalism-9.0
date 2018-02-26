package capitalism.view.tables;

import java.util.HashMap;

import capitalism.model.Global;
import capitalism.view.ViewManager;
import capitalism.view.custom.TrackingControls;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;

/**
 * rough and ready custom GUI control to display a numeric quantity and a description of it. Used to display the values in a Global entity.
 * The descriptions and the numbers are left and right aligned within each grid element
 * This is done by the {@code Glabel} controls that are added to the grid by the {@link setGridCell} method.
 * TODO find a way to bind this to the persistent entity Global
 *
 */
public class SwitchableGraphicsGrid extends AnchorPane {

	@FXML GridPane gridPane=new GridPane();

	HashMap<String, Label> labelsByDescription = new HashMap<String, Label>();

	public SwitchableGraphicsGrid() {
		gridPane.setMaxWidth(Double.MAX_VALUE);
		setOpaqueInsets(new Insets(0, 0, 0, 2));
		for (int i = 0; i < 4; i++) {
			ColumnConstraints constraint= new ColumnConstraints(10);
			constraint.setPrefWidth(100);
			constraint.setMaxWidth(Double.MAX_VALUE);
			constraint.setHgrow(Priority.ALWAYS);
			gridPane.getColumnConstraints().add(constraint);
		}
		for (int i = 0; i < 2; i++) {
			RowConstraints rowConstraint=new RowConstraints(30, 30, 30);
			rowConstraint.setVgrow(Priority.SOMETIMES);
			gridPane.getRowConstraints().add(rowConstraint);
		}
		
		getChildren().add(gridPane);
		
		setBottomAnchor(gridPane, 0.0);
		setLeftAnchor(gridPane, 0.0);
		setTopAnchor(gridPane, 0.0);
		setRightAnchor(gridPane, 0.0);
		
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
			if (TrackingControls.displayDeltas) {
				text=global.showDelta(text, selector);
				deltaModifier=(TrackingControls.displayDeltas?ViewManager.deltaSymbol:"");
			}
		}
		label.setText(deltaModifier+text);
	}

	/**
	 * fill out the values in the grid from the supplied Global
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
