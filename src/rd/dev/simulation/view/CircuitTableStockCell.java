package rd.dev.simulation.view;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.scene.control.TableCell;
import javafx.scene.paint.Color;
import rd.dev.simulation.Simulation;
import rd.dev.simulation.datamanagement.DataManager;
import rd.dev.simulation.model.Circuit;
import rd.dev.simulation.model.Stock;
import rd.dev.simulation.custom.TabbedTableViewer;

public class CircuitTableStockCell extends TableCell<Circuit, String> {
	static final Logger logger = LogManager.getLogger("CircuitTableCell");

	String stockValueUseName;

	public CircuitTableStockCell(String stockUseValueName) {
		this.stockValueUseName = stockUseValueName;
	}

	@Override protected void updateItem(String item, boolean empty) {
		int i = getIndex();
		super.updateItem(item, empty);
		if (item == null)
			return;
		Circuit circuit = getTableView().getItems().get(i);
		if (circuit == null) {
			logger.debug(" Null Circuit");
			return;
		}
		setText(item);
		Stock theStock = DataManager.stockProductiveByNameSingle(Simulation.timeStampDisplayCursor, circuit.getProductUseValueType(), stockValueUseName);
		boolean hasChanged = theStock.changed(TabbedTableViewer.displayAttribute);
		setTextFill(hasChanged ? Color.RED : Color.BLACK);// TODO not yet complete
		if (ViewManager.displayHints) {
			switch (TabbedTableViewer.displayAttribute) {
			case PRICE:
				setStyle("-fx-background-color: rgba(255,240,204,0.3)");
				break;
			case VALUE:
				setStyle("-fx-background-color: rgb(255,225,225,0.3)");
				break;
			case QUANTITY:
				setStyle("-fx-background-color: rgba(220,220,220,0.3)");
				break;
			}
		}
	}
}