package capitalism.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.controller.Simulation;
import capitalism.utils.MathStuff;
import capitalism.view.ViewManager;
import capitalism.view.custom.TrackingControlsBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * The persistent class for the timestamps database table.
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
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "TimeStamp")
public class TimeStamp implements Serializable {
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused") private static final Logger logger = LogManager.getLogger(TimeStamp.class);

	@XmlElement @EmbeddedId private TimeStampPK pk;
	@XmlElement @Column(name = "description") protected String description;
	@XmlElement @Column(name = "superState") protected String superState;
	@XmlElement @Column(name = "period") protected int period;
	@XmlElement @Column(name = "COMPARATORTIMESTAMPID") protected int comparatorTimeStampID;
	@XmlElement @Column(name = "RateOfExploitation") private double rateOfExploitation;
	@XmlElement @Column(name = "MELT") private double melt;
	@XmlElement @Column(name = "PopulationGrowthRate") private double populationGrowthRate;
	@XmlElement @Column(name = "InvestmentRatio") private double investmentRatio;
	@XmlElement @Column(name = "LabourSupplyResponse") private Simulation.LABOUR_RESPONSE labourSupplyResponse;
	@XmlElement @Column(name = "priceResponse") private Simulation.PRICE_RESPONSE priceResponse;
	@XmlElement @Column(name = "meltResponse") private Simulation.MELT_RESPONSE meltResponse;
	@XmlElement @Column(name = "CurrencySymbol") private String currencySymbol;
	@XmlElement @Column(name = "QuantitySymbol") private String quantitySymbol;

	@Transient private TimeStamp comparator = null;
	@Transient private TimeStamp previousComparator;
	@Transient private TimeStamp startComparator;
	@Transient private TimeStamp customComparator;
	@Transient private TimeStamp endComparator;

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

	public static enum GLOBAL_SELECTOR {
		// @formatter:off
		INITIALCAPITAL("Initial Capital"), 
		CURRENTCAPITAL("Current Capital"), 
		PROFIT("Profit"), 
		PROFITRATE("Profit Rate"), 
		TOTALVALUE("Total Value"), 
		TOTALPRICE("Total Price"), MELT("MELT"), 
		POPULATION_GROWTH_RATE("Population Growth Rate"), 
		LABOUR_SUPPLY_RESPONSE("Labour Supply Response"),
		PRICE_RESPONSE("Price Response"),
		MELT_RESPONSE("MELT Response");
		// @formatter:on

		String text;

		GLOBAL_SELECTOR(String text) {
			this.text = text;
		}

		public String text() {
			return text;
		}
	}

	public TimeStamp(int timeStampID, int projectFK, int period, String superState, int comparatorTimeStampID, String description) {
		pk = new TimeStampPK();
		pk.timeStampID = timeStampID;
		pk.projectFK = projectFK;
		this.period = period;
		this.superState = superState;
		this.description = description;
		this.comparatorTimeStampID = comparatorTimeStampID;
	}

	/**
	 * make a carbon copy
	 * 
	 * @param template
	 *            the original
	 */
	public TimeStamp(TimeStamp template) {
		pk = new TimeStampPK();
		pk.timeStampID = template.pk.timeStampID;
		pk.projectFK = template.pk.projectFK;
		this.period = template.period;
		this.superState = template.superState;
		this.description = template.description;
		rateOfExploitation = template.rateOfExploitation;
		melt = template.melt;
		populationGrowthRate = template.populationGrowthRate;
		investmentRatio = template.investmentRatio;
		labourSupplyResponse = template.labourSupplyResponse;
		priceResponse = template.priceResponse;
		meltResponse = template.meltResponse;
		currencySymbol = template.currencySymbol;
		quantitySymbol = template.quantitySymbol;
	}

	TimeStamp() {
		pk = new TimeStampPK();
	}

	public String value(GLOBAL_SELECTOR selector) {
		switch (selector) {
		case CURRENTCAPITAL:
			return String.format(ViewManager.getLargeFormat(), currentCapital());
		case INITIALCAPITAL:
			return String.format(ViewManager.getLargeFormat(), initialCapital());
		case LABOUR_SUPPLY_RESPONSE:
			return labourSupplyResponse.text();
		case PRICE_RESPONSE:
			return priceResponse.text();
		case MELT_RESPONSE:
			return meltResponse.text();
		case MELT:
			return String.format(ViewManager.getSmallFormat(), melt);
		case POPULATION_GROWTH_RATE:
			return String.format(ViewManager.getSmallFormat(), populationGrowthRate);
		case PROFIT:
			return String.format(ViewManager.getLargeFormat(), profit());
		case PROFITRATE:
			return String.format(ViewManager.getSmallFormat(), profitRate());
		case TOTALPRICE:
			return String.format(ViewManager.getLargeFormat(), totalPrice());
		case TOTALVALUE:
			return String.format(ViewManager.getLargeFormat(), totalValue());
		default:
			return "";
		}
	}

	/**
	 * If the selected field has changed, return the difference between the current value and the former value
	 * 
	 * @param selector
	 *            chooses which field to evaluate
	 * 
	 * @param item
	 *            the original item - returned as the result if there is no change
	 * 
	 * @return the original item if nothing has changed, otherwise the change, as an appropriately formatted string
	 */

	public String showDelta(String item, GLOBAL_SELECTOR selector) {
		chooseComparison();
		switch (selector) {
		case CURRENTCAPITAL:
			return String.format(ViewManager.getLargeFormat(), currentCapital() - comparator.currentCapital());
		case INITIALCAPITAL:
			return String.format(ViewManager.getLargeFormat(), initialCapital() - comparator.initialCapital());
		case MELT:
			return String.format(ViewManager.getSmallFormat(), melt - comparator.melt);
		case PROFIT:
			return String.format(ViewManager.getLargeFormat(), profit() - comparator.profit());
		case PROFITRATE:
			return String.format(ViewManager.getSmallFormat(), profitRate() - comparator.profitRate());
		case TOTALPRICE:
			return String.format(ViewManager.getLargeFormat(), totalPrice() - comparator.totalPrice());
		case TOTALVALUE:
			return String.format(ViewManager.getLargeFormat(), totalValue() - comparator.totalValue());
		case LABOUR_SUPPLY_RESPONSE:
		case MELT_RESPONSE:
		case PRICE_RESPONSE:
		case POPULATION_GROWTH_RATE:
		default:
			return item;
		}
	}

	public static void setComparators(int timeStampID) {
		timeStampByPrimarykeyQuery.setParameter("project", Simulation.projectCurrent);
		timeStampByPrimarykeyQuery.setParameter("timeStamp", timeStampID);
		TimeStamp timeStamp = timeStampByPrimarykeyQuery.getSingleResult();
		timeStamp.setPreviousComparator(getTimeStamp(Simulation.getTimeStampComparatorCursor()));
		timeStamp.setStartComparator(getTimeStamp(1));
		timeStamp.setEndComparator(getTimeStamp(Simulation.timeStampIDCurrent));
		timeStamp.setCustomComparator(getTimeStamp(Simulation.timeStampIDCurrent));
	}

	/**
	 * Shows whether the selected magnitude has changed.
	 * Returns false if this is expected to be constant
	 * 
	 * @param selector
	 *            the magnitude to be selected
	 * @return
	 * 		true if the selected variable has changed, false if it has not
	 */

	public boolean changed(GLOBAL_SELECTOR selector) {
		chooseComparison();
		switch (selector) {
		case CURRENTCAPITAL:
			return currentCapital() != comparator.currentCapital();
		case INITIALCAPITAL:
			return initialCapital() != comparator.initialCapital();
		case MELT:
			return melt != comparator.melt;
		case LABOUR_SUPPLY_RESPONSE:
		case PRICE_RESPONSE:
		case MELT_RESPONSE:
		case POPULATION_GROWTH_RATE:
			return false;
		case PROFIT:
			return profit() != comparator.profit();
		case PROFITRATE:
			return profitRate() != comparator.profitRate();
		case TOTALPRICE:
			return totalPrice() != comparator.totalPrice();
		case TOTALVALUE:
			return totalValue() != comparator.totalValue();
		default:
			return false;
		}
	}

	/**
	 * chooses the comparator depending on the state set in the {@code ViewManager.comparatorToggle} radio buttons
	 */

	private void chooseComparison() {
		switch (TrackingControlsBox.getComparatorState()) {
		case CUSTOM:
			comparator = customComparator;
			break;
		case END:
			comparator = endComparator;
			break;
		case PREVIOUS:
			comparator = previousComparator;
			break;
		case START:
			comparator = startComparator;
		}
	}

	/**
	 * @return the total initial capital in the economy
	 * 
	 */
	public double initialCapital() {
		double initialCapital = 0;
		for (Industry c : Industry.industriesAll(pk.timeStampID)) {
			initialCapital += c.productiveCapital();
		}
		// TODO get this aggregate query working
		// double checkInitialCapital;
		// checkInitialCapital=DataManager.industriesInitialCapital(pk.timeStamp);
		return initialCapital;
	}

	/**
	 * @return the total current capital in the economy
	 */

	public double currentCapital() {
		double currentCapital = 0;
		for (Industry c : Industry.industriesAll(pk.timeStampID)) {
			currentCapital += c.currentCapital();
		}
		return currentCapital;
	}

	/**
	 * @return the total profit in the economy for the current project and at the timeStamp of this global record
	 */
	public double profit() {
		double profit = 0.0;
		for (Commodity commodity : Commodity.commoditiesAll(pk.timeStampID)) {
			profit += commodity.profit();
		}
		return profit;
	}

	/**
	 * @return the profit rate for the whole economy
	 */

	public double profitRate() {
		double initialCapital = MathStuff.round(initialCapital());
		if (initialCapital == 0) {
			return Double.NaN;
		}
		return profit() / initialCapital();
	}

	/**
	 * @return the total value in the economy
	 */
	public double totalValue() {
		// TODO replace by a sum query
		double totalValue = 0;
		for (Stock s : Stock.all(pk.timeStampID)) {
			if ((!s.getStockType().equals("Money")) || (Simulation.isFullPricing())) {
				totalValue += s.getValue();
			}
		}
		return totalValue;
	}

	/**
	 * @return the total price in the economy
	 */
	public double totalPrice() {
		// TODO replace by a sum query
		double totalPrice = 0;
		for (Stock s : Stock.all(pk.timeStampID)) {
			if ((!s.getStockType().equals("Money")) || (Simulation.isFullPricing())) {
				totalPrice += s.getPrice();
			}
		}
		return totalPrice;
	}

	/**
	 * Get the single TimeStamp entity of the current project and the current timeStamp
	 * 
	 * @return the TimeStamp that has the given timeStampID and project
	 */
	public static TimeStamp getTimeStamp() {
		timeStampByPrimarykeyQuery.setParameter("project", Simulation.projectCurrent);
		timeStampByPrimarykeyQuery.setParameter("timeStamp", Simulation.timeStampIDCurrent);
		return timeStampByPrimarykeyQuery.getSingleResult();
	}

	
	/**
	 * Get the single TimeStamp entity of the current project and the given timeStamp
	 * 
	 * @param timeStampID
	 *            the timeStampID of the TimeStamp entity
	 * @return the TimeStamp that has this timeStampID and the current project
	 */

	public static TimeStamp getTimeStamp(int timeStampID) {
		timeStampByPrimarykeyQuery.setParameter("project", Simulation.projectCurrent);
		timeStampByPrimarykeyQuery.setParameter("timeStamp", timeStampID);
		return timeStampByPrimarykeyQuery.getSingleResult();
	}

	/**
	 * Get the single TimeStamp entity of the current project and the given timeStamp
	 * 
	 * @param timeStampID
	 *            the timeStampID of the desired TimeStamp entity
	 * @param projectID
	 *            the projectID of the desired TimeStamp entity
	 * @return the TimeStamp that has the given timeStampID and project
	 */
	public static TimeStamp getTimeStamp(int projectID,int timeStampID) {
		timeStampByPrimarykeyQuery.setParameter("project", projectID);
		timeStampByPrimarykeyQuery.setParameter("timeStamp", timeStampID);
		return timeStampByPrimarykeyQuery.getSingleResult();
	}

	
	
	/**
	 * An observable list of timeStamp records that belong to this superstate in the given period and the current projec
	 * 
	 * @param period
	 *            the period
	 * @param superStateName
	 *            the name of the superState of which this timeStamp is a child
	 * @return an observable list of timeStamps that belong to this superstate in the given period and the current projec
	 */

	public static ObservableList<TimeStamp> timeStampsBySuperState(int period, String superStateName) {
		ObservableList<TimeStamp> timeStamps = FXCollections.observableArrayList();
		timeStampSuperStatesQuery.setParameter("project", Simulation.projectCurrent).setParameter("superState", superStateName).setParameter("period", period);
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
	 * 
	 * @param timeStamp
	 *            the timeStampID to set
	 */

	public void setTimeStampID(int timeStamp) {
		pk.timeStampID = timeStamp;
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
	 * @param comparatorTimeStampID
	 *            the comparatorTimeStampID to set
	 */
	public void setComparatorTimeStampID(int comparatorTimeStampID) {
		this.comparatorTimeStampID = comparatorTimeStampID;
	}

	/**
	 * @return the rateOfExploitation
	 */
	public double getRateOfExploitation() {
		return rateOfExploitation;
	}

	/**
	 * @param rateOfExploitation
	 *            the rateOfExploitation to set
	 */
	public void setRateOfExploitation(double rateOfExploitation) {
		this.rateOfExploitation = rateOfExploitation;
	}

	/**
	 * @return the melt
	 */
	public double getMelt() {
		return melt;
	}

	/**
	 * @param melt
	 *            the melt to set
	 */
	public void setMelt(double melt) {
		this.melt = melt;
	}

	/**
	 * @return the populationGrowthRate
	 */
	public double getPopulationGrowthRate() {
		return populationGrowthRate;
	}

	/**
	 * @param populationGrowthRate
	 *            the populationGrowthRate to set
	 */
	public void setPopulationGrowthRate(double populationGrowthRate) {
		this.populationGrowthRate = populationGrowthRate;
	}

	/**
	 * @return the investmentRatio
	 */
	public double getInvestmentRatio() {
		return investmentRatio;
	}

	/**
	 * @param investmentRatio
	 *            the investmentRatio to set
	 */
	public void setInvestmentRatio(double investmentRatio) {
		this.investmentRatio = investmentRatio;
	}

	/**
	 * @return the labourSupplyResponse
	 */
	public Simulation.LABOUR_RESPONSE getLabourSupplyResponse() {
		return labourSupplyResponse;
	}

	/**
	 * @param labourSupplyResponse
	 *            the labourSupplyResponse to set
	 */
	public void setLabourSupplyResponse(Simulation.LABOUR_RESPONSE labourSupplyResponse) {
		this.labourSupplyResponse = labourSupplyResponse;
	}

	/**
	 * @return the priceResponse
	 */
	public Simulation.PRICE_RESPONSE getPriceResponse() {
		return priceResponse;
	}

	/**
	 * @param priceResponse
	 *            the priceResponse to set
	 */
	public void setPriceResponse(Simulation.PRICE_RESPONSE priceResponse) {
		this.priceResponse = priceResponse;
	}

	/**
	 * @return the meltResponse
	 */
	public Simulation.MELT_RESPONSE getMeltResponse() {
		return meltResponse;
	}

	/**
	 * @param meltResponse
	 *            the meltResponse to set
	 */
	public void setMeltResponse(Simulation.MELT_RESPONSE meltResponse) {
		this.meltResponse = meltResponse;
	}

	/**
	 * @return the currencySymbol
	 */
	public String getCurrencySymbol() {
		return currencySymbol;
	}

	/**
	 * @param currencySymbol
	 *            the currencySymbol to set
	 */
	public void setCurrencySymbol(String currencySymbol) {
		this.currencySymbol = currencySymbol;
	}

	/**
	 * @return the quantitySymbol
	 */
	public String getQuantitySymbol() {
		return quantitySymbol;
	}

	/**
	 * @param quantitySymbol
	 *            the quantitySymbol to set
	 */
	public void setQuantitySymbol(String quantitySymbol) {
		this.quantitySymbol = quantitySymbol;
	}

	/**
	 * @return the comparator
	 */
	public TimeStamp getComparator() {
		return comparator;
	}

	/**
	 * @param comparator
	 *            the comparator to set
	 */
	public void setComparator(TimeStamp comparator) {
		this.comparator = comparator;
	}

	/**
	 * @return the previousComparator
	 */
	public TimeStamp getPreviousComparator() {
		return previousComparator;
	}

	/**
	 * @param previousComparator
	 *            the previousComparator to set
	 */
	public void setPreviousComparator(TimeStamp previousComparator) {
		this.previousComparator = previousComparator;
	}

	/**
	 * @return the startComparator
	 */
	public TimeStamp getStartComparator() {
		return startComparator;
	}

	/**
	 * @param startComparator
	 *            the startComparator to set
	 */
	public void setStartComparator(TimeStamp startComparator) {
		this.startComparator = startComparator;
	}

	/**
	 * @return the customComparator
	 */
	public TimeStamp getCustomComparator() {
		return customComparator;
	}

	/**
	 * @param customComparator
	 *            the customComparator to set
	 */
	public void setCustomComparator(TimeStamp customComparator) {
		this.customComparator = customComparator;
	}

	/**
	 * @return the endComparator
	 */
	public TimeStamp getEndComparator() {
		return endComparator;
	}

	/**
	 * @param endComparator
	 *            the endComparator to set
	 */
	public void setEndComparator(TimeStamp endComparator) {
		this.endComparator = endComparator;
	}

}
