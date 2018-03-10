package capitalism.view.editor;

import capitalism.model.SocialClass;
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

public class EditableSocialClass {
	private StringProperty name;
	// The proportion of the population of this class that supplies labour power (not yet used)
	private DoubleProperty participationRatio;
	// the money that this class will spend in the current period
	private DoubleProperty revenue;

	enum ESC_ATTRIBUTE {
		NAME,PR,REVENUE;
	}

	public EditableSocialClass() {
		name = new SimpleStringProperty();
		revenue=new SimpleDoubleProperty();
		participationRatio=new SimpleDoubleProperty();
	}

	public static ObservableList<EditableSocialClass> editableSocialClasses(int timeStampID, int projectID) {
		ObservableList<EditableSocialClass> result = FXCollections.observableArrayList();
		for (SocialClass c : SocialClass.allCurrent()) {
			EditableSocialClass oneRecord = new EditableSocialClass();
			oneRecord.setName(c.name());
			oneRecord.setParticipationRatio(c.getparticipationRatio());
			oneRecord.setRevenue(c.getRevenue());
			oneRecord.setName(c.name());
			result.add(oneRecord);
		}
		return result;
	}

	public void set(ESC_ATTRIBUTE attribute, double d) {
		switch (attribute) {
		case PR:
			participationRatio.set(d);
			break;
		case REVENUE:
			revenue.set(d);
			break;
		default:
		}
	}

	public double getDouble(ESC_ATTRIBUTE attribute) {
		switch (attribute) {
		case PR:
			return participationRatio.get();
		case REVENUE:
			return participationRatio.get();
		default:
			return Double.NaN;
		}
	}

	public void set(ESC_ATTRIBUTE attribute, String newValue) {
		switch (attribute) {
		case NAME:
			name.set(newValue);
			break;
		default:
			break;
		}
	}

	public String getString(ESC_ATTRIBUTE attribute) {
		switch (attribute) {
		case NAME:
			return getName();
		default:
			return "";
		}
	}

	
	public static TableColumn<EditableSocialClass, Double> makeDoubleColumn(String header, String fieldName, ESC_ATTRIBUTE attribute) {
		TableColumn<EditableSocialClass, Double> col = new TableColumn<EditableSocialClass, Double>(header);
		Callback<TableColumn<EditableSocialClass, Double>, TableCell<EditableSocialClass, Double>> cellFactory = new Callback<TableColumn<EditableSocialClass, Double>, TableCell<EditableSocialClass, Double>>() {
			public TableCell<EditableSocialClass, Double> call(TableColumn<EditableSocialClass, Double> p) {
				return new EditableSocialClassCell();
			}
		};
		col.setCellValueFactory(
				// TODO need to abstract here
				new PropertyValueFactory<EditableSocialClass, Double>(fieldName));
		col.setCellFactory(cellFactory);
		col.setOnEditCommit(
				new EventHandler<TableColumn.CellEditEvent<EditableSocialClass, Double>>() {
					@Override public void handle(TableColumn.CellEditEvent<EditableSocialClass, Double> t) {
						((EditableSocialClass) t.getTableView().getItems().get(
								t.getTablePosition().getRow())).set(attribute, t.getNewValue());
					}
				});
		return col;
	}
	
	public static TableColumn<EditableSocialClass, String> makeStringColumn(String header, String fieldName, ESC_ATTRIBUTE attribute) {
		TableColumn<EditableSocialClass, String> col = new TableColumn<EditableSocialClass, String>(header);
		Callback<TableColumn<EditableSocialClass, String>, TableCell<EditableSocialClass, String>> cellFactory = new Callback<TableColumn<EditableSocialClass, String>, TableCell<EditableSocialClass, String>>() {
			public TableCell<EditableSocialClass, String> call(TableColumn<EditableSocialClass, String> p) {
				return new EditableSocialClassStringCell();
			}
		};
		col.setCellValueFactory(
				// TODO need to abstract here
				new PropertyValueFactory<EditableSocialClass, String>(fieldName));
		col.setCellFactory(cellFactory);
		col.setOnEditCommit(
				new EventHandler<TableColumn.CellEditEvent<EditableSocialClass, String>>() {
					@Override public void handle(TableColumn.CellEditEvent<EditableSocialClass, String> t) {
						((EditableSocialClass) t.getTableView().getItems().get(
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
	 * @return the participationRatio
	 */
	public double getParticipationRatio() {
		return participationRatio.get();
	}

	/**
	 * @param participationRatio the participationRatio to set
	 */
	public void setParticipationRatio(double participationRatio) {
		this.participationRatio.set(participationRatio);
	}

	/**
	 * @return the revenue
	 */
	public Double getRevenue() {
		return revenue.get();
	}

	/**
	 * @param revenue the revenue to set
	 */
	public void setRevenue(double revenue) {
		this.revenue.set(revenue);
	}


}
