package rd.dev.simulation.model;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class TimeStampPK implements Serializable {

	private static final long serialVersionUID = 1L;
	@Basic(optional = false) @Column(name = "timeStampID") protected int timeStampID;
	@Basic(optional = false) @Column(name = "projectFK") protected int projectFK;

	protected TimeStampPK() {
	}

	public TimeStampPK(int timeStampID, int projectFK) {
		this.timeStampID = timeStampID;
		this.projectFK = projectFK;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		hash += (int) timeStampID;
		hash += (int) projectFK;
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		// TODO: Warning - this method won't work in the case the id fields are not set
		if (!(object instanceof TimeStampPK)) {
			return false;
		}
		TimeStampPK other = (TimeStampPK) object;
		if (this.timeStampID != other.timeStampID) {
			return false;
		}
		if (this.projectFK != other.projectFK) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "demo.TimeStampPK[ timeStamp=" + timeStampID + ", project=" + projectFK + " ]";
	}

}
