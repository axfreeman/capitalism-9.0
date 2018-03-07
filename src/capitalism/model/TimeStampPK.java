package capitalism.model;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Embeddable
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="TimeStampPK")
public class TimeStampPK implements Serializable {

	private static final long serialVersionUID = 1L;
	@XmlElement @Basic(optional = false) @Column(name = "timeStampID") protected int timeStampID;
	@XmlElement @Basic(optional = false) @Column(name = "projectFK") protected int projectFK;

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

	/**
	 * @return the timeStampID
	 */
	public int getTimeStampID() {
		return timeStampID;
	}

	/**
	 * @param timeStampID the timeStampID to set
	 */
	public void setTimeStampID(int timeStampID) {
		this.timeStampID = timeStampID;
	}

}
