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
	private DoubleProperty quantity;

	EditableStock(){
		quantity=new SimpleDoubleProperty(0.0);
	}
	
	/**
	 * @return the quantity
	 */
	public Double getQuantity() {
		return quantity.get();
	}

	/**
	 * @param quantity the quantity to set
	 */
	public void setQuantity(Double quantity) {
		this.quantity.set(quantity);
	}
	
	public DoubleProperty getQuantityProperty() {
		return quantity;
	}
	
	public void setQuantityProperty(DoubleProperty quantity) {
		this.quantity=quantity;
	}
	
}
