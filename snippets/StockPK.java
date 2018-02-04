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
package rd.dev.simulation.model;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the stocks database table.
 * 
 */
@Embeddable
public class StockPK implements Serializable {
	// default serial project id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	@Column(name = "project") protected int project;
	@Column(name = "timeStamp") protected int timeStamp;
	@Column(name = "owner") protected String circuit;
	@Column(name = "usevalue") protected String useValue;
	@Column(name = "stockType") protected Stock.STOCKTYPE stockType;

	public StockPK() {
	}

	// no getters and setters
	// instead use protected types
	// simplifies the code in the Stock class

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof StockPK)) {
			return false;
		}
		StockPK castOther = (StockPK) other;
		return (this.project == castOther.project) && (this.timeStamp == castOther.timeStamp) && this.circuit.equals(castOther.circuit)
				&& this.useValue.equals(castOther.useValue);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.project;
		hash = hash * prime + this.timeStamp;
		hash = hash * prime + this.circuit.hashCode();
		hash = hash * prime + this.useValue.hashCode();

		return hash;
	}
}