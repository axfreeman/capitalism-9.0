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
 * The primary key class for the socialClass database table.
 * 
 */
@Embeddable
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="SocialClassPK")
public class SocialClassPK implements Serializable {
	// default serial project id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	@XmlElement @Column(name="project")  protected int projectID;
	@XmlElement @Column(name="timeStamp")  protected int timeStampID;
	@XmlElement @Column(name="socialClassName")  protected String name;

	
	public SocialClassPK() {
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof SocialClassPK)) {
			return false;
		}
		SocialClassPK castOther = (SocialClassPK) other;
		return (this.projectID == castOther.projectID) && (this.timeStampID == castOther.timeStampID) && this.name.equals(castOther.name);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.projectID;
		hash = hash * prime + this.timeStampID;
		hash = hash * prime + this.name.hashCode();

		return hash;
	}
}