package rd.dev.simulation.model;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class GlobalPK implements Serializable {

	private static final long serialVersionUID = 1L;
	@Basic(optional = false) @Column(name = "project") protected int project;
	@Basic(optional = false) @Column(name = "timeStamp") protected int timeStamp;

	protected GlobalPK() {
	}

	public GlobalPK(int project, int timeStamp) {
		this.project = project;
		this.timeStamp = timeStamp;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		hash += (int) project;
		hash += timeStamp;
		return hash;
	}

	@Override
	public boolean equals(Object object) {

		if (!(object instanceof GlobalPK)) {
			return false;
		}
		GlobalPK other = (GlobalPK) object;
		if (this.project != other.project) {
			return false;
		}
		if (this.timeStamp != other.timeStamp) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "demo.GlobalsPK[ project=" + project + ", timeStamp=" + timeStamp + " ]";
	}
}
