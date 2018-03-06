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
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author afree
 */
@Embeddable
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="IndustryPK")
public class IndustryPK implements Serializable {
	static final long serialVersionUID = 001L;
	@XmlElement @Basic(optional = false) @Column(name = "project") protected int project;
	@XmlElement @Basic(optional = false) @Column(name = "timeStamp") protected int timeStamp;
	@XmlElement @Basic(optional = false) @Column(name = "industryName") protected String name;

	public IndustryPK() {
	}

	public IndustryPK(int project, int timeStamp, String industryName) {
		this.project = project;
		this.timeStamp = timeStamp;
		this.name = industryName;
	}
	
	@Override
	public int hashCode() {
		int hash = 0;
		hash += (int) project;
		hash += timeStamp;
		hash += (name != null ? name.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {

		if (!(object instanceof IndustryPK)) {
			return false;
		}
		IndustryPK other = (IndustryPK) object;
		if (this.project != other.project) {
			return false;
		}
		if (this.timeStamp != other.timeStamp) {
			return false;
		}
		if ((this.name == null && other.name != null)
				|| (this.name != null && !this.name.equals(other.name))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "demo.IndustriesPK[ project=" + project + ", timeStamp=" + timeStamp + ", industryName=" + name + " ]";
	}

}
