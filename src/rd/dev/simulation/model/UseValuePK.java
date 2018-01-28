package rd.dev.simulation.model;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the usevalues database table.
 * 
 */
@Embeddable
public class UseValuePK implements Serializable {
	// default serial project id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	@Column (name="project") protected int project;
	@Column (name="timeStamp") protected int timeStamp;
	@Column (name="UseValueType") protected String useValueType;

	protected UseValuePK() {
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof UseValuePK)) {
			return false;
		}
		UseValuePK castOther = (UseValuePK) other;
		return (this.project == castOther.project) && (this.timeStamp == castOther.timeStamp) && this.useValueType.equals(castOther.useValueType);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.project;
		hash = hash * prime + this.timeStamp;
		hash = hash * prime + this.useValueType.hashCode();

		return hash;
	}
}