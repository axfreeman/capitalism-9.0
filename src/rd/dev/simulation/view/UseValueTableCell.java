package rd.dev.simulation.view;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.scene.control.TableCell;
import javafx.scene.paint.Color;
import rd.dev.simulation.model.UseValue;

public class UseValueTableCell extends TableCell<UseValue, String> {
	static final Logger logger = LogManager.getLogger("UseValueTableCell");

	UseValue.Selector selector;

	public UseValueTableCell(UseValue.Selector selector) {
		this.selector = selector;
	}

	@Override protected void updateItem(String item, boolean empty) {
		int i = getIndex();
		super.updateItem(item, empty);
		if (item == null) {// this happens,it seems, when the tableRow is used for the column header-or perhaps when cell is empty?
			return;
		}
		UseValue useValue = getTableView().getItems().get(i);
		if (useValue == null) {
			logger.debug(" Null Use Value");
			return;
		}
		setText(item);
		setTextFill(useValue.changed(selector) ? Color.RED : Color.BLACK);
		if (ViewManager.displayHints) {
			switch (selector) {
			case TOTALDEMAND:
			case TOTALSUPPLY:
			case TOTALQUANTITY:
				setStyle("-fx-background-color: rgba(220,220,220,0.3)");
				break;
			case UNITVALUE:
			case TOTALVALUE:
				setStyle("-fx-background-color: rgb(255,225,225,0.3)");
				break;
			case UNITPRICE:
			case TOTALPRICE:
				setStyle("-fx-background-color: rgba(255,240,204,0.3)");
				break;
			default:
			}
		}
	}
}
