package capitalism.view.editor;

import capitalism.model.Industry;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

public class EditableIndustry {
	private StringProperty name;
	protected StringProperty commodityName;
	protected DoubleProperty output;

	enum EI_ATTRIBUTE {
		NAME,COMMODITY_NAME,OUTPUT;
	}

	public EditableIndustry() {
		name = new SimpleStringProperty();
		output= new SimpleDoubleProperty();
		commodityName=new SimpleStringProperty();
	}

	public static ObservableList<EditableIndustry> editableIndustries(int timeStampID, int projectID) {
		ObservableList<EditableIndustry> result = FXCollections.observableArrayList();
		for (Industry c : Industry.allCurrent()) {
			EditableIndustry oneRecord = new EditableIndustry();
			oneRecord.setName(c.name());
			oneRecord.setCommodityName(c.getCommodityName());
			oneRecord.setOutput(c.getOutput());
			result.add(oneRecord);
		}
		return result;
	}

	public void set(EI_ATTRIBUTE attribute, double d) {
		switch (attribute) {
		case OUTPUT:
			output.set(d);
			break;
		default:
		}
	}

	public double getDouble(EI_ATTRIBUTE attribute) {
		switch (attribute) {
		case OUTPUT:
			return getOutput();
		default:
			return Double.NaN;
		}
	}

	public void set(EI_ATTRIBUTE attribute, String newValue) {
		switch (attribute) {
		case NAME:
			name.set(newValue);
			break;
		default:
			break;
		}
	}

	public String getString(EI_ATTRIBUTE attribute) {
		switch (attribute) {
		case NAME:
			return getName();
		default:
			return "";
		}
	}
	
	
	public static TableColumn<EditableIndustry, Double> makeDoubleColumn(String header, String fieldName, EI_ATTRIBUTE attribute) {
		TableColumn<EditableIndustry, Double> col = new TableColumn<EditableIndustry, Double>(header);
		Callback<TableColumn<EditableIndustry, Double>, TableCell<EditableIndustry, Double>> cellFactory = new Callback<TableColumn<EditableIndustry, Double>, TableCell<EditableIndustry, Double>>() {
			public TableCell<EditableIndustry, Double> call(TableColumn<EditableIndustry, Double> p) {
				return new EditableIndustryCell();
			}
		};
		col.setCellValueFactory(
				// TODO need to abstract here
				new PropertyValueFactory<EditableIndustry, Double>(fieldName));
		col.setCellFactory(cellFactory);
		col.setOnEditCommit(
				new EventHandler<TableColumn.CellEditEvent<EditableIndustry, Double>>() {
					@Override public void handle(TableColumn.CellEditEvent<EditableIndustry, Double> t) {
						((EditableIndustry) t.getTableView().getItems().get(
								t.getTablePosition().getRow())).set(attribute, t.getNewValue());
					}
				});
		return col;
	}
	
	public static TableColumn<EditableIndustry, String> makeStringColumn(String header, String fieldName, EI_ATTRIBUTE attribute) {
		TableColumn<EditableIndustry, String> col = new TableColumn<EditableIndustry, String>(header);
		Callback<TableColumn<EditableIndustry, String>, TableCell<EditableIndustry, String>> cellFactory = new Callback<TableColumn<EditableIndustry, String>, TableCell<EditableIndustry, String>>() {
			public TableCell<EditableIndustry, String> call(TableColumn<EditableIndustry, String> p) {
				return new EditableIndustryStringCell();
			}
		};
		col.setCellValueFactory(
				// TODO need to abstract here
				new PropertyValueFactory<EditableIndustry, String>(fieldName));
		col.setCellFactory(cellFactory);
		col.setOnEditCommit(
				new EventHandler<TableColumn.CellEditEvent<EditableIndustry, String>>() {
					@Override public void handle(TableColumn.CellEditEvent<EditableIndustry, String> t) {
						((EditableIndustry) t.getTableView().getItems().get(
								t.getTablePosition().getRow())).set(attribute, t.getNewValue());
					}
				});
		return col;
	}


	public String getName() {
		return name.get();
	}

	public void setName(String name) {
		this.name.set(name);
	}

	/**
	 * @return the commodityName
	 */
	public String getCommodityName() {
		return commodityName.get();
	}

	/**
	 * @param commodityName the commodityName to set
	 */
	public void setCommodityName(String commodityName) {
		this.commodityName.set(commodityName);
	}

	/**
	 * @return the output
	 */
	public Double getOutput() {
		return output.get();
	}

	/**
	 * @param output the output to set
	 */
	public void setOutput(Double output) {
		this.output.set(output);
	}
}
