package rd.dev.simulation.view;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.scene.control.TableCell;
import javafx.scene.paint.Color;
import rd.dev.simulation.custom.TabbedTableViewer;
import rd.dev.simulation.model.SocialClass;

public class SocialClassTableCell extends TableCell<SocialClass, String> {
	static final Logger logger = LogManager.getLogger("SocialClassTableCell");
	
	SocialClass.Selector selector;
	
	public SocialClassTableCell(SocialClass.Selector selector){
		this.selector=selector;
	}
	
	@Override
	protected void updateItem(String item, boolean empty) {
		int i=getIndex();
//		logger.debug("Processing an item with index {}", i);
		super.updateItem(item,empty);
		if (item==null) {// this happens,it seems, when the tableRow is used for the column header
			return;
		}
		SocialClass socialClass=getTableView().getItems().get(i);
		if (socialClass==null) {
			logger.debug(" Null Social Class");
			return;
		}
		setText(item);
		setTextFill(socialClass.changed(selector,TabbedTableViewer.displayAttribute) ? Color.RED : Color.BLACK);
		if (ViewManager.displayHints) {
			switch (selector) {
			case MONEY:
			case CONSUMPTIONSTOCKS:
			case SALES:
			case TOTAL:
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
				break;
			case QUANTITYDEMANDED:
				setStyle("-fx-background-color: rgba(220,220,220,0.3)");
				break;
			default:
				break;
			}
		}
	}
}
