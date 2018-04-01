package capitalism.view.tables;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.model.Stock;
import javafx.scene.control.TableCell;
import javafx.scene.paint.Color;

public class StockTableCell extends TableCell<Stock, String> {
	static final Logger logger = LogManager.getLogger("StockTableCell");
	
	Stock.STOCK_ATTRIBUTE sTOCK_ATTRIBUTE;
	public StockTableCell(Stock.STOCK_ATTRIBUTE sTOCK_ATTRIBUTE){
		this.sTOCK_ATTRIBUTE=sTOCK_ATTRIBUTE;
	}
	
	@Override
	protected void updateItem(String item, boolean empty) {
		int i=getIndex();
		super.updateItem(item,empty);
		if (item==null) {// this happens,it seems, when the tableRow is used for the column header
			return;
		}
		Stock stock=getTableView().getItems().get(i);
		if (stock==null) {
			logger.debug(" Null Stock");
			return;
		}
		setText(item);
		setTextFill(stock.changed(sTOCK_ATTRIBUTE) ? Color.RED : Color.BLACK);
	}
}
