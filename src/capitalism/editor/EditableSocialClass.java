/*
 *  Copyright (C) Alan Freeman 2017-2019
 *  
 *  This file is part of the Capitalism Simulation, abbreviated to CapSim
 *  in the remainder of this project
 *
 *  Capsim is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either project 3 of the License, or
 *  (at your option) any later project.
*
*   Capsim is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU General Public License for more details.
*
*   You should have received a copy of the GNU General Public License
*   along with Capsim.  If not, see <http://www.gnu.org/licenses/>.
*/
package capitalism.editor;

import capitalism.model.SocialClass;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;

public class EditableSocialClass {
	private StringProperty name;
	// The proportion of the population of this class that supplies labour power (not yet used)
	private DoubleProperty participationRatio;
	// the money that this class will spend in the current period
	private DoubleProperty revenue;
	private EditableStock money;
	private EditableStock sales;

	enum ESC_ATTRIBUTE {
		NAME("Class Name"),PR("Participation Ratio"),REVENUE("Revenue"),MONEY("Money"), SALES("Sales Stock");
		protected String text;
		private ESC_ATTRIBUTE(String text) {
			this.text=text;
		}
	}

	public EditableSocialClass() {
		name = new SimpleStringProperty();
		revenue=new SimpleDoubleProperty();
		participationRatio=new SimpleDoubleProperty();
		money = new EditableStock();
		sales= new EditableStock();
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
		case MONEY:
			money.getQuantityProperty().set(d);
		case SALES:
			sales.getQuantityProperty().set(d);
		default:
		}
	}

	public double getDouble(ESC_ATTRIBUTE attribute) {
		switch (attribute) {
		case PR:
			return participationRatio.get();
		case REVENUE:
			return participationRatio.get();
		case MONEY:
			return money.getQuantityProperty().get();
		case SALES:
			return sales.getQuantityProperty().get();
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

	
	public static TableColumn<EditableSocialClass, Double> makeDoubleColumn(ESC_ATTRIBUTE attribute) {
		TableColumn<EditableSocialClass, Double> col = new TableColumn<EditableSocialClass, Double>(attribute.text);
		Callback<TableColumn<EditableSocialClass, Double>, TableCell<EditableSocialClass, Double>> cellFactory = new Callback<TableColumn<EditableSocialClass, Double>, TableCell<EditableSocialClass, Double>>() {
			public TableCell<EditableSocialClass, Double> call(TableColumn<EditableSocialClass, Double> p) {
				return new EditableSocialClassCell();
			}
		};
		col.setCellValueFactory(
				// TODO need to abstract here
				cellData -> cellData.getValue().doubleProperty(attribute)
//				new PropertyValueFactory<EditableSocialClass, Double>(fieldName)
				);
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
	
	public static TableColumn<EditableSocialClass, String> makeStringColumn(ESC_ATTRIBUTE attribute) {
		TableColumn<EditableSocialClass, String> col = new TableColumn<EditableSocialClass, String>(attribute.text);
		Callback<TableColumn<EditableSocialClass, String>, TableCell<EditableSocialClass, String>> cellFactory = new Callback<TableColumn<EditableSocialClass, String>, TableCell<EditableSocialClass, String>>() {
			public TableCell<EditableSocialClass, String> call(TableColumn<EditableSocialClass, String> p) {
				return new EditableSocialClassStringCell();
			}
		};
		col.setCellValueFactory(
				// TODO need to abstract here
				cellData -> cellData.getValue().stringProperty(attribute)
//				new PropertyValueFactory<EditableSocialClass, String>(fieldName)
				);
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
	
	private ObservableValue<Double> doubleProperty(ESC_ATTRIBUTE attribute) {
		switch (attribute) {
		case PR:
			return participationRatio.asObject();
		case REVENUE:
			return revenue.asObject();
		default:
			return new SimpleDoubleProperty(Double.NaN).asObject();
		}
	}

	private ObservableValue<String> stringProperty(ESC_ATTRIBUTE attribute){
		switch (attribute) {
		case NAME:
			return name;
		default:
			return new SimpleStringProperty("");
		}
		
	}


	private static class EditableSocialClassCell extends TableCell<EditableSocialClass, Double> {
		private TextField textField;
		public EditableSocialClassCell() {
		}
		@Override public void startEdit() {
			super.startEdit();
			if (textField == null) {
				createTextField();
			}
			setGraphic(textField);
			setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			textField.selectAll();
		}

		@Override public void cancelEdit() {
			super.cancelEdit();
			setText(String.valueOf(getItem()));
			setContentDisplay(ContentDisplay.TEXT_ONLY);
		}

		@Override public void updateItem(Double item, boolean empty) {
			super.updateItem(item, empty);
			if (empty) {
				setText(null);
				setGraphic(null);
			} else {
				if (isEditing()) {
					if (textField != null) {
						textField.setText(getString());
					}
					setGraphic(textField);
					setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				} else {
					setText(getString());
					setContentDisplay(ContentDisplay.TEXT_ONLY);
				}
			}
		}

		private void createTextField() {
			textField = new TextField(getString());
			textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			textField.setOnKeyPressed(new EventHandler<KeyEvent>() {

				@Override public void handle(KeyEvent t) {
					if (t.getCode() == KeyCode.ENTER) {
						commitEdit(Double.parseDouble(textField.getText()));
					} else if (t.getCode() == KeyCode.ESCAPE) {
						cancelEdit();
					}
				}
			});
		}

		private String getString() {
			return getItem() == null ? "" : getItem().toString();
		}
	}

	private static class EditableSocialClassStringCell extends TableCell<EditableSocialClass, String> {
		private TextField textField;
		public EditableSocialClassStringCell() {
		}
		@Override public void startEdit() {
			super.startEdit();
			if (textField == null) {
				createTextField();
			}
			setGraphic(textField);
			setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			textField.selectAll();
		}

		@Override public void cancelEdit() {
			super.cancelEdit();
			setText(String.valueOf(getItem()));
			setContentDisplay(ContentDisplay.TEXT_ONLY);
		}

		@Override public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);
			if (empty) {
				setText(null);
				setGraphic(null);
			} else {
				if (isEditing()) {
					if (textField != null) {
						textField.setText(getString());
					}
					setGraphic(textField);
					setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				} else {
					setText(getString());
					setContentDisplay(ContentDisplay.TEXT_ONLY);
				}
			}
		}

		private void createTextField() {
			textField = new TextField(getString());
			textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			textField.setOnKeyPressed(new EventHandler<KeyEvent>() {

				@Override public void handle(KeyEvent t) {
					if (t.getCode() == KeyCode.ENTER) {
						commitEdit(textField.getText());
					} else if (t.getCode() == KeyCode.ESCAPE) {
						cancelEdit();
					}
				}
			});
		}

		private String getString() {
			return getItem() == null ? "" : getItem().toString();
		}
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
