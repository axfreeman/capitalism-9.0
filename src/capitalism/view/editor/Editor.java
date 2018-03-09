package capitalism.view.editor;

import capitalism.model.Commodity;
import capitalism.view.tables.CommodityColumn;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.VBox;

public class Editor extends VBox {

	private static EditorControlBar ecb = new EditorControlBar();

	protected static TableView<Commodity> commoditiesTable = new TableView<Commodity>();

	public Editor() {

		// box for the commodity table
		VBox commodityBox = new VBox();
		commodityBox.setPrefHeight(700);
		commodityBox.setPrefWidth(7600);

		// box for the industry table
		VBox industryBox = new VBox();
		industryBox.setPrefHeight(700);
		industryBox.setPrefWidth(7600);

		// box for the social class table
		VBox socialClassBox = new VBox();
		socialClassBox.setPrefHeight(700);
		socialClassBox.setPrefWidth(7600);

		// the tabbed pane
		TabPane tabPane = new TabPane();
		tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		Tab commodityTab = new Tab("Commodities");
		commodityTab.setContent(commodityBox);
		Tab industryTab = new Tab("Industries");
		industryTab.setContent(industryBox);
		Tab socialClassTab = new Tab("Classes");

		// the tables
		commoditiesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		commodityBox.getChildren().add(commoditiesTable);
		commoditiesTable.getColumns().add(new CommodityColumn(Commodity.COMMODITY_ATTRIBUTE.NAME, true));
		commoditiesTable.getColumns().add(new CommodityColumn(Commodity.COMMODITY_ATTRIBUTE.FUNCTION_TYPE, true));
		commoditiesTable.getColumns().add(new CommodityColumn(Commodity.COMMODITY_ATTRIBUTE.UNITVALUE, false));
		commoditiesTable.getColumns().add(new CommodityColumn(Commodity.COMMODITY_ATTRIBUTE.UNITPRICE, false));
		commoditiesTable.getColumns().add(new CommodityColumn(Commodity.COMMODITY_ATTRIBUTE.TURNOVERTIME, false));
		
		tabPane.getTabs().addAll(commodityTab, industryTab, socialClassTab);
		getChildren().addAll(ecb, tabPane);
		commoditiesTable.setItems(Commodity.commoditiesObservable());
	}
}
