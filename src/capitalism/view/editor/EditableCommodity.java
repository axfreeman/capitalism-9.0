package capitalism.view.editor;

import capitalism.model.Commodity;
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

public class EditableCommodity {
	private StringProperty name;
	private DoubleProperty turnoverTime;
	private DoubleProperty unitValue;
	private DoubleProperty unitPrice;
	private StringProperty origin; // whether this is produced by an enterprise or a class
	private StringProperty function;// see enum FUNCTION for list of possible types

	enum EC_ATTRIBUTE {
		UNIT_VALUE, UNIT_PRICE, TURNOVER, NAME,FUNCTION,ORIGIN;
	}

	public EditableCommodity() {
		name = new SimpleStringProperty();
		turnoverTime = new SimpleDoubleProperty();
		unitValue = new SimpleDoubleProperty();
		unitPrice = new SimpleDoubleProperty();
		origin=new SimpleStringProperty();
		function  = new SimpleStringProperty();
	}

	public static ObservableList<EditableCommodity> editableCommodities(int timeStampID, int projectID) {
		ObservableList<EditableCommodity> result = FXCollections.observableArrayList();
		for (Commodity c : Commodity.allCurrent()) {
			EditableCommodity oneRecord = new EditableCommodity();
			oneRecord.setName(c.name());
			oneRecord.setUnitValue(c.getUnitValue());
			oneRecord.setUnitPrice(c.getUnitPrice());
			oneRecord.setTurnoverTime(c.getTurnoverTime());
			oneRecord.setFunction(c.getFunction().text());
			oneRecord.setOrigin(c.getOrigin().text());
			result.add(oneRecord);
		}
		return result;
	}

	public void set(EC_ATTRIBUTE attribute, double d) {
		switch (attribute) {
		case UNIT_VALUE:
			unitValue.set(d);
			break;
		case UNIT_PRICE:
			unitPrice.set(d);
			break;
		case TURNOVER:
			turnoverTime.set(d);
			break;
		default:
		}
	}

	public double getDouble(EC_ATTRIBUTE attribute) {
		switch (attribute) {
		case UNIT_VALUE:
			return getUnitValue();
		case UNIT_PRICE:
			return getUnitPrice();
		case TURNOVER:
			return getTurnoverTime();
		default:
			return Double.NaN;
		}
	}

	public void set(EC_ATTRIBUTE attribute, String newValue) {
		switch (attribute) {
		case NAME:
			name.set(newValue);
			break;
		case FUNCTION:
			function.set(newValue);
		case ORIGIN:
			origin.set(newValue);
		default:
			break;
		}
	}

	public String getString(EC_ATTRIBUTE attribute) {
		switch (attribute) {
		case NAME:
			return getName();
		case FUNCTION:
			return getFunction();
		case ORIGIN:
			return getOrigin();
		default:
			return "";
		}
	}
	
	public static TableColumn<EditableCommodity, Double> makeDoubleColumn(String header, String fieldName, EC_ATTRIBUTE attribute) {
		TableColumn<EditableCommodity, Double> col = new TableColumn<EditableCommodity, Double>(header);
		Callback<TableColumn<EditableCommodity, Double>, TableCell<EditableCommodity, Double>> cellFactory = new Callback<TableColumn<EditableCommodity, Double>, TableCell<EditableCommodity, Double>>() {
			public TableCell<EditableCommodity, Double> call(TableColumn<EditableCommodity, Double> p) {
				return new EditableCommodityCell();
			}
		};
		col.setCellValueFactory(
				// TODO need to abstract here
				new PropertyValueFactory<EditableCommodity, Double>(fieldName));
		col.setCellFactory(cellFactory);
		col.setOnEditCommit(
				new EventHandler<TableColumn.CellEditEvent<EditableCommodity, Double>>() {
					@Override public void handle(TableColumn.CellEditEvent<EditableCommodity, Double> t) {
						((EditableCommodity) t.getTableView().getItems().get(
								t.getTablePosition().getRow())).set(attribute, t.getNewValue());
					}
				});
		return col;
	}
	
	public static TableColumn<EditableCommodity, String> makeStringColumn(String header, String fieldName, EC_ATTRIBUTE attribute) {
		TableColumn<EditableCommodity, String> col = new TableColumn<EditableCommodity, String>(header);
		Callback<TableColumn<EditableCommodity, String>, TableCell<EditableCommodity, String>> cellFactory = new Callback<TableColumn<EditableCommodity, String>, TableCell<EditableCommodity, String>>() {
			public TableCell<EditableCommodity, String> call(TableColumn<EditableCommodity, String> p) {
				return new EditableCommodityStringCell();
			}
		};
		col.setCellValueFactory(
				// TODO need to abstract here
				new PropertyValueFactory<EditableCommodity, String>(fieldName));
		col.setCellFactory(cellFactory);
		col.setOnEditCommit(
				new EventHandler<TableColumn.CellEditEvent<EditableCommodity, String>>() {
					@Override public void handle(TableColumn.CellEditEvent<EditableCommodity, String> t) {
						((EditableCommodity) t.getTableView().getItems().get(
								t.getTablePosition().getRow())).set(attribute, t.getNewValue());
					}
				});
		return col;
	}


	public double getUnitValue() {
		return unitValue.get();
	}

	public void setUnitValue(double v) {
		unitValue.set(v);
	}

	public double getUnitPrice() {
		return unitPrice.get();
	}

	public void setUnitPrice(double v) {
		unitPrice.set(v);
	}

	public void setUnitPrice(int v) {
		unitValue.set(v);
	}

	/**
	 * @return the turnoverTime property
	 */
	public double getTurnoverTime() {
		return turnoverTime.get();
	}

	/**
	 * @param turnoverTime
	 *            the turnoverTime to set
	 */
	public void setTurnoverTime(double turnoverTime) {
		this.turnoverTime.set(turnoverTime);
	}

	public String getName() {
		return name.get();
	}

	public void setName(String name) {
		this.name.set(name);
	}

	/**
	 * @return the origin
	 */
	public String getOrigin() {
		return origin.get();
	}

	/**
	 * @param origin the origin to set
	 */
	public void setOrigin(String origin) {
		this.origin.set(origin);
	}

	/**
	 * @return the function
	 */
	public String getFunction() {
		return function.get();
	}

	/**
	 * @param function the function to set
	 */
	public void setFunction(String function) {
		this.function.set(function);
	}

}
