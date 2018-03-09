package capitalism.model;

import java.io.Serializable;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The primary key class for the commodities database table.
 * 
 */
@Embeddable
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public class CommodityPK implements Serializable {
	// default serial project id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	@XmlElement @Column (name="project") protected int projectID;
	@XmlElement @Column (name="timeStamp") protected int timeStampID;
	@XmlElement @Column (name="Name") protected String name;

	protected CommodityPK() {
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof CommodityPK)) {
			return false;
		}
		CommodityPK castOther = (CommodityPK) other;
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