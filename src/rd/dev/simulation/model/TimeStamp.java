package rd.dev.simulation.model;

import java.io.Serializable;

import javax.persistence.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The persistent class for the projects database table.
 * 
 */
@Entity
@Table(name = "timeStamps")
@NamedQueries({
		@NamedQuery(name = "timeStamp.findAll", query = "SELECT t FROM TimeStamp t"),
		@NamedQuery(name = "timeStamp.project", query = "SELECT t FROM TimeStamp t where t.pk.projectFK = :project"),
		@NamedQuery(name = "timeStamp.project.timeStamp", query = "SELECT t FROM TimeStamp t where t.pk.projectFK = :project and t.pk.timeStampID = :timeStamp"),
		@NamedQuery(name = "superStates", query = "Select t from TimeStamp t where t.pk.projectFK=:project and t.period= :period and t.superState=:superState")
})

public class TimeStamp implements Serializable {
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused") private static final Logger logger = LogManager.getLogger(TimeStamp.class);

	@EmbeddedId protected TimeStampPK pk;
	@Column(name = "description") protected String description;
	@Column(name = "superState") protected String superState;
	@Column(name = "period") protected int period;
	@Column(name = "COMPARATORTIMESTAMPID") protected int comparatorTimeStampID;

	public TimeStamp(int timeStampID, int projectFK, int period, String superState, int comparatorTimeStampID, String description) {
		pk = new TimeStampPK();
		pk.timeStampID = timeStampID;
		pk.projectFK = projectFK;
		this.period = period;
		this.superState = superState;
		this.description = description;
		this.comparatorTimeStampID=comparatorTimeStampID;
	}

	/**
	 * make a carbon copy
	 * @param timeStamp the original
	 */
	public TimeStamp(TimeStamp timeStamp) {
		pk = new TimeStampPK();
		pk.timeStampID = timeStamp.pk.timeStampID;
		pk.projectFK = timeStamp.pk.projectFK;
		this.period = timeStamp.period;
		this.superState = timeStamp.superState;
		this.description = timeStamp.description;
	}
	TimeStamp() {
		pk = new TimeStampPK();
	}

	public Integer getTimeStampID() {
		return pk.timeStampID;
	}
	
	/**
	 * set the timeStampID. Since this is part of the primary key, it should only be set in those cases
	 * where this entity is not persisted (for example in the treeView)
	 * @param timeStamp the timeStampID to set
	 */
	
	public void setTimeStampID(int timeStamp) {
		pk.timeStampID=timeStamp;
	}

	public Integer getProjectFK() {
		return pk.projectFK;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the superState
	 */
	public String getSuperState() {
		return superState;
	}

	/**
	 * @param superStateName
	 *            the superState to set
	 */
	public void setSuperState(String superStateName) {
		this.superState = superStateName;
	}

	/**
	 * @return the period
	 */
	public int getPeriod() {
		return period;
	}

	/**
	 * @param period
	 *            the period to set
	 */
	public void setPeriod(int period) {
		this.period = period;
	}

	/**
	 * @return the comparatorTimeStampID
	 */
	public int getComparatorTimeStampID() {
		return comparatorTimeStampID;
	}

	/**
	 * @param comparatorTimeStampID the comparatorTimeStampID to set
	 */
	public void setComparatorTimeStampID(int comparatorTimeStampID) {
		this.comparatorTimeStampID = comparatorTimeStampID;
	}

}
