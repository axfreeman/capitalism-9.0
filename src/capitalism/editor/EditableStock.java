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

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * The editableStock class is much simpler than the other editable classes.
 * Stocks are never edited directly; their editing is managed by their owners
 * So all we need to store is the quantity
 */
public class EditableStock {
	private String name;// Stock name cannot be edited but needs to be known hence a simple String not a StringProperty
	private DoubleProperty actualQuantity;
	private DoubleProperty desiredQuantity;

	/**
	 * Create a stock with the given commodity name.
	 * 
	 * @param name
	 *            the commodity name of the editable stock to be created
	 */
	EditableStock(String name) {
		this.name = name;
		actualQuantity = new SimpleDoubleProperty(0.0);
		desiredQuantity = new SimpleDoubleProperty(0.0);
	}

	/**
	 * @return the actualQuantity
	 */
	public Double getActualQuantity() {
		return actualQuantity.get();
	}

	/**
	 * @param quantity
	 *            the quantity to set
	 */
	public void setActualQuantity(Double quantity) {
		this.actualQuantity.set(quantity);
	}

	public DoubleProperty getActualQuantityProperty() {
		return actualQuantity;
	}

	public void setActualQuantityProperty(DoubleProperty quantity) {
		this.actualQuantity = quantity;
	}

	/**
	 * @return the desiredQuantity
	 */
	public Double getDesiredQuantity() {
		return desiredQuantity.get();
	}

	/**
	 * set the desiredQuantity
	 * 
	 * @param quantity
	 *            the quantity to set
	 */
	public void setDesiredQuantity(Double quantity) {
		this.desiredQuantity.set(quantity);
	}

	public DoubleProperty getDesiredQuantityProperty() {
		return desiredQuantity;
	}

	public void setdesiredQuantityProperty(DoubleProperty quantity) {
		this.desiredQuantity = quantity;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

}
