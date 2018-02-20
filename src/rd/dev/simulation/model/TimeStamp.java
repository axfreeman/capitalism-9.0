package rd.dev.simulation.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import rd.dev.simulation.Simulation;

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
	
	private static EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("DB_TIMESTAMP");
	private static EntityManager entityManager;
	private static TypedQuery<TimeStamp> timeStampsAllByProjectQuery;
	private static TypedQuery<TimeStamp> timeStampByPrimarykeyQuery;
	private static TypedQuery<TimeStamp> timeStampSuperStatesQuery;
	private static TypedQuery<TimeStamp> timeStampsAllQuery;

	static {
		entityManager = entityManagerFactory.createEntityManager();
		timeStampsAllByProjectQuery = entityManager.createNamedQuery("timeStamp.project", TimeStamp.class);
		timeStampSuperStatesQuery = entityManager.createNamedQuery("superStates", TimeStamp.class);
		timeStampsAllQuery = entityManager.createNamedQuery("timeStamp.project", TimeStamp.class);
		timeStampByPrimarykeyQuery = entityManager.createNamedQuery("timeStamp.project.timeStamp", TimeStamp.class);
	}

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


	/**
	 * Get the single TimeStamp entity of the current project and the current timeStamp
	 * @param timeStampID
	 *            the timeStampID of the TimeStamp entity
	 * @return the TimeStamp that has this timeStampID and the current project
	 */

	public static TimeStamp timeStampSingle(int timeStampID) {
		timeStampByPrimarykeyQuery.setParameter("project", Simulation.projectCurrent);
		timeStampByPrimarykeyQuery.setParameter("timeStamp", timeStampID);
		return timeStampByPrimarykeyQuery.getSingleResult();
	}

	public static ObservableList<TimeStamp> timeStampsBySuperState(int period, String superStateName) {
		ObservableList<TimeStamp> timeStamps = FXCollections.observableArrayList();
		timeStampSuperStatesQuery.setParameter("project",Simulation.projectCurrent).setParameter("superState", superStateName).setParameter("period", period);
		for (TimeStamp timeStamp : timeStampSuperStatesQuery.getResultList())
			timeStamps.add(timeStamp);
		return timeStamps;
	}

	public static ObservableList<TimeStamp> timeStampsAll() {
		ObservableList<TimeStamp> timeStamps = FXCollections.observableArrayList();
		timeStampsAllQuery.setParameter("project", 1);
		for (TimeStamp timeStamp : timeStampsAllQuery.getResultList())
			timeStamps.add(timeStamp);
		return timeStamps;
	}
	
	/**
	 * All timeStamps for the current project.
	 * 
	 * @return a list of timeStamps for the current project
	 */
	public static List<TimeStamp> timeStampsByProject() {
		timeStampsAllByProjectQuery.setParameter("project", Simulation.projectCurrent);
		return timeStampsAllByProjectQuery.getResultList();
	}

	/**
	 * set the timeStamp of a given project.
	 * 
	 * @param projectID
	 *            the projectID of a single project
	 * @param timeStamp
	 *            the timeStamp to be set for this project - normally, the timeStamp when the user switches a simulation
	 * @return 0 if fail, the timeStamp otherwise
	 */
	public static int setTimeStampOfProject(int projectID, int timeStamp) {
		Project project = Project.projectSingle(projectID);
		if (project == null) {
			return 0;
		} else {
			project.setTimeStamp(timeStamp);
			return timeStamp;
		}
	}

	/**
	 * set the timeStampDisplayCursor of a given project.
	 * 
	 * @param projectID
	 *            the projectID of a single project
	 * @param timeStampCursor
	 *            the timeStampDisplayCursor to be set for this project - normally, the timeStampDisplayCursor when the user switches a simulation
	 * @return 0 if fail, the timeStampDisplayCursor otherwise
	 */
	public static int setTimeStampCursorOfProject(int projectID, int timeStampCursor) {
		Project project = Project.projectSingle(projectID);
		if (project == null) {
			return 0;
		} else {
			project.setTimeStampDisplayCursor(timeStampCursor);
			return timeStampCursor;
		}
	}
	
	public static EntityManager getEntityManager() {
		return entityManager;
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
