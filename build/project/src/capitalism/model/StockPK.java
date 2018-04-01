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
package capitalism.model;

import java.io.Serializable;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The primary key class for the stocks database table.
 * 
 */
@Embeddable
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="StockPK")
public class StockPK implements Serializable {
	// default serial project id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	@XmlElement @Column(name = "project") protected int projectID;
	@XmlElement @Column(name = "timeStamp") protected int timeStampID;
	@XmlElement @Column(name = "owner") protected String owner;
	@XmlElement @Column(name = "commodity") protected String commodity;

	// PRODUCTIVE, SALES, MONEY or CONSUMPTION
	// We cannot use an enum because of a bug in H2, promised to be corrected in the next release,
	// that breaks when an enum is in the primary key
	@XmlElement @Column(name = "stockType") protected String stockType;

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
		return (this.projectID == castOther.projectID) && (this.timeStampID == castOther.timeStampID) && this.owner.equals(castOther.owner)
				&& this.commodity.equals(castOther.commodity);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.projectID;
		hash = hash * prime + this.timeStampID;
		hash = hash * prime + this.owner.hashCode();
		hash = hash * prime + this.commodity.hashCode();

		return hash;
	}
}