package capitalism.view.editor;

import capitalism.view.editor.EditableCommodity.EC_ATTRIBUTE;
import capitalism.view.editor.EditableIndustry.EI_ATTRIBUTE;
import capitalism.view.editor.EditableSocialClass.ESC_ATTRIBUTE;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.VBox;

/*
 * with acknowledgements to http://java-buddy.blogspot.ca/2012/04/javafx-2-editable-tableview.html
 */

public class Editor extends VBox {

	private static EditorControlBar ecb = new EditorControlBar();

	private static ObservableList<EditableCommodity> commodityData = FXCollections.observableArrayList();
	private static TableView<EditableCommodity> commodityTable = new TableView<EditableCommodity>();
	private static ObservableList<EditableIndustry> industryData= FXCollections.observableArrayList();
	private static TableView<EditableIndustry> industryTable = new TableView<EditableIndustry>();	
	private static ObservableList<EditableSocialClass> socialClassData = FXCollections.observableArrayList();
	private static TableView<EditableSocialClass> socialClassTable = new TableView<EditableSocialClass>();

	public Editor() {
		
		makeCommodityTable();
		makeIndustryTable();
		makeSocialClassTable();

		// box for the commodity table
		VBox commodityBox = new VBox();
		commodityBox.setPrefHeight(300);
		commodityBox.setPrefWidth(7600);
		commodityBox.getChildren().add(commodityTable);

		// box for the industry table
		VBox industryBox = new VBox();
		industryBox.setPrefHeight(300);
		industryBox.setPrefWidth(7600);
		industryBox.getChildren().add(industryTable);
		
		// box for the social class table
		VBox socialClassBox = new VBox();
		socialClassBox.setPrefHeight(300);
		socialClassBox.setPrefWidth(7600);
		socialClassBox.getChildren().add(socialClassTable);
		
		// the tabbed pane
		TabPane tabPane = new TabPane();
		tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		Tab commodityTab = new Tab("Commodities");
		commodityTab.setContent(commodityBox);
		Tab industryTab = new Tab("Industries");
		industryTab.setContent(industryBox);
		Tab socialClassTab = new Tab("Classes");
		socialClassTab.setContent(socialClassBox);

		tabPane.getTabs().addAll(commodityTab, industryTab, socialClassTab);
		getChildren().addAll(ecb, tabPane);
	}

	// TODO get the header and field names from the attributes
	
	private void makeCommodityTable() {
		commodityTable.setEditable(true);
		commodityTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		commodityTable.getColumns().add(EditableCommodity.makeStringColumn("Commodity", "name", EC_ATTRIBUTE.NAME));
		commodityTable.getColumns().add(EditableCommodity.makeStringColumn("Function", "function", EC_ATTRIBUTE.FUNCTION));
		commodityTable.getColumns().add(EditableCommodity.makeStringColumn("Origin", "origin", EC_ATTRIBUTE.ORIGIN));
		commodityTable.getColumns().add(EditableCommodity.makeDoubleColumn("Unit value", "unitValue", EC_ATTRIBUTE.UNIT_VALUE));
		commodityTable.getColumns().add(EditableCommodity.makeDoubleColumn("Unit price", "unitPrice", EC_ATTRIBUTE.UNIT_PRICE));
		commodityTable.getColumns().add(EditableCommodity.makeDoubleColumn("Turnover Time", "turnoverTime", EC_ATTRIBUTE.TURNOVER));
		commodityTable.setItems(commodityData);
	}
	
	private void makeIndustryTable() {
		industryTable.setEditable(true);
		industryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		industryTable.getColumns().add(EditableIndustry.makeStringColumn("Industry", "name", EI_ATTRIBUTE.NAME));
		industryTable.getColumns().add(EditableIndustry.makeDoubleColumn("Output", "output", EI_ATTRIBUTE.OUTPUT));
		industryTable.getColumns().add(EditableIndustry.makeStringColumn("Commodity Produced", "commodityName", EI_ATTRIBUTE.COMMODITY_NAME));
		industryTable.setItems(industryData);
	}
	
	private void makeSocialClassTable() {
		socialClassTable.setEditable(true);
		socialClassTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		socialClassTable.getColumns().add(EditableSocialClass.makeStringColumn("Social Class", "name", ESC_ATTRIBUTE.NAME));
		socialClassTable.getColumns().add(EditableSocialClass.makeDoubleColumn("Participation Ratio", "participationRatio", ESC_ATTRIBUTE.PR));
		socialClassTable.getColumns().add(EditableSocialClass.makeDoubleColumn("Revenue", "revenue", ESC_ATTRIBUTE.PR));
			socialClassTable.setItems(socialClassData);
		
	}

	/**
	 * @return the data
	 */
	public static ObservableList<EditableCommodity> getCommodityData() {
		return commodityData;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public static void setCommodityData(ObservableList<EditableCommodity> data) {
		Editor.commodityData = data;
		commodityTable.setItems(Editor.commodityData);
	}
	/**
	 * @param data
	 *            the data to set
	 */
	public static void setIndustryData(ObservableList<EditableIndustry> data) {
		Editor.industryData = data;
		industryTable.setItems(Editor.industryData);
	}
	/**
	 * @param data
	 *            the data to set
	 */
	public static void setSocialClassData(ObservableList<EditableSocialClass> data) {
		Editor.socialClassData = data;
		socialClassTable.setItems(Editor.socialClassData);
	}

}
