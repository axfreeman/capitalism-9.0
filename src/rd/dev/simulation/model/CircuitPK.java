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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 *
 * @author afree
 */
@Embeddable
public class CircuitPK implements Serializable {
	static final long serialVersionUID = 001L;
	@Basic(optional = false) @Column(name = "project") protected int project;
	@Basic(optional = false) @Column(name = "timeStamp") protected int timeStamp;
	@Basic(optional = false) @Column(name = "productUseValueType") protected String productUseValueType;

	public CircuitPK() {
	}

	public CircuitPK(int project, int timeStamp, String productUseValueType) {
		this.project = project;
		this.timeStamp = timeStamp;
		this.productUseValueType = productUseValueType;
	}
	
	@Override
	public int hashCode() {
		int hash = 0;
		hash += (int) project;
		hash += timeStamp;
		hash += (productUseValueType != null ? productUseValueType.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		// TODO: Warning - this method won't work in the case the id fields are not set
		if (!(object instanceof CircuitPK)) {
			return false;
		}
		CircuitPK other = (CircuitPK) object;
		if (this.project != other.project) {
			return false;
		}
		if (this.timeStamp != other.timeStamp) {
			return false;
		}
		if ((this.productUseValueType == null && other.productUseValueType != null)
				|| (this.productUseValueType != null && !this.productUseValueType.equals(other.productUseValueType))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "demo.CapitalcircuitsPK[ project=" + project + ", timeStamp=" + timeStamp + ", productUseValueType=" + productUseValueType + " ]";
	}

}
