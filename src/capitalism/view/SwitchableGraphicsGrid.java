package capitalism.view;

import java.util.HashMap;

import capitalism.model.TimeStamp;
import capitalism.view.custom.TrackingControlsBox;
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
 * rough and ready custom GUI control to display a numeric quantity and a description of it. Used to display the values in a TimeStamp entity.
 * The descriptions and the numbers are left and right aligned within each grid element
 * This is done by the {@code Glabel} controls that are added to the grid by the {@link setGridCell} method.
 * TODO find a way to bind this to the persistent entity TimeStamp
 *
 */
public class SwitchableGraphicsGrid extends AnchorPane {

	private static GridPane gridPane=new GridPane();

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
		dropShadow.setColor(Color.CADETBLUE);
		setEffect(dropShadow);
	}

	public void setGridCell(int col, int row, TimeStamp timeStamp, TimeStamp.GLOBAL_SELECTOR selector) {
		Glabel glabel = new Glabel();
		glabel.setDescription(selector.text());
		gridPane.add(glabel, row, col);
		Label label = glabel.getNumberLabel();
		String text = timeStamp.value(selector);
		
		String deltaModifier="";

		if (timeStamp.changed(selector)) {
			label.setTextFill(Color.RED);
			if (TrackingControlsBox.displayDeltas) {
				text=timeStamp.showDelta(text, selector);
				deltaModifier=(TrackingControlsBox.displayDeltas?ViewManager.deltaSymbol:"");
			}
		}
		label.setText(deltaModifier+text);
	}

	/**
	 * fill out the values in the grid from the supplied TimeStamp
	 * 
	 * @param floatFormatString
	 *            a format string that determines how the timeStamp is displayed
	 * @param timeStamp
	 *            the persistent TimeStamp that is used to get the numeric values	
	 */
	public void populate(String floatFormatString, TimeStamp timeStamp) {
		setGridCell(0, 0, timeStamp, TimeStamp.GLOBAL_SELECTOR.INITIALCAPITAL);
		setGridCell(0, 1, timeStamp, TimeStamp.GLOBAL_SELECTOR.CURRENTCAPITAL);
		setGridCell(0, 2, timeStamp, TimeStamp.GLOBAL_SELECTOR.PROFIT);
		setGridCell(0, 3, timeStamp, TimeStamp.GLOBAL_SELECTOR.PROFITRATE);
		setGridCell(1, 0, timeStamp, TimeStamp.GLOBAL_SELECTOR.TOTALVALUE);
		setGridCell(1, 1, timeStamp, TimeStamp.GLOBAL_SELECTOR.TOTALPRICE);
		setGridCell(1, 2, timeStamp, TimeStamp.GLOBAL_SELECTOR.MELT);
		setGridCell(1, 3, timeStamp, TimeStamp.GLOBAL_SELECTOR.POPULATION_GROWTH_RATE);
	}
}
