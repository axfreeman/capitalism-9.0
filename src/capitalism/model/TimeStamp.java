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

import capitalism.controller.Parameters;
import capitalism.controller.Simulation;
import capitalism.utils.MathStuff;
import capitalism.view.ViewManager;
import capitalism.view.custom.TrackingControlsBox;

/**
 * The persistent class for the timestamps database table.
 * 
 */
@Entity
@Table(name = "timeStamps")

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "TimeStamp")
public class TimeStamp implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final Logger logger = LogManager.getLogger(TimeStamp.class);

	@XmlElement @EmbeddedId private TimeStampPK pk;
	@XmlElement @Column(name = "description") private String description;
	@XmlElement @Column(name = "superState") private String superState;
	@XmlElement @Column(name = "period") private int period;
	@XmlElement @Column(name = "COMPARATORTIMESTAMPID") private int comparatorTimeStampID;
	@XmlElement @Column(name = "RateOfExploitation") private double rateOfExploitation;
	@XmlElement @Column(name = "MELT") private double melt;
	@XmlElement @Column(name = "PopulationGrowthRate") private double populationGrowthRate;
	@XmlElement @Column(name = "InvestmentRatio") private double investmentRatio;
	@XmlElement @Column(name = "LabourSupplyResponse") private Parameters.LABOUR_RESPONSE labourSupplyResponse;
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
	private static TypedQuery<TimeStamp> primaryQuery;
	private static TypedQuery<TimeStamp> superStateQuery;
	private static TypedQuery<TimeStamp> allInProjectQuery;

	// create the typed queries statically but not as named queries. This makes them easier to find and modify
	static {
		entityManager = entityManagerFactory.createEntityManager();
		primaryQuery = entityManager.createQuery(
				"SELECT t FROM TimeStamp t where t.pk.projectID = :project and t.pk.timeStampID = :timeStamp", TimeStamp.class);
		allInProjectQuery = entityManager.createQuery("Select t from TimeStamp t where t.pk.projectID =:project", TimeStamp.class);
		superStateQuery = entityManager.createQuery(
				"Select t from TimeStamp t where t.pk.projectID=:project and t.period= :period and t.superState=:superState", TimeStamp.class);
	}

	/**
	 * All list of displayable attributes of a timeStamp entity.
	 * Used to determine what to display and how to display it
	 */

	public static enum TIMESTAMP_ATTRIBUTE {
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

		TIMESTAMP_ATTRIBUTE(String text) {
			this.text = text;
		}

		public String text() {
			return text;
		}
	}

	/**
	 * Specialised constructor used to build a particular timeStamp entity
	 * 
	 * @param timeStampID
	 *            the timeStampID
	 * @param projectID
	 *            the projectID
	 * @param period
	 *            the period
	 * @param superState
	 *            the superState
	 * @param comparatorTimeStampID
	 *            the comparator ID
	 * @param description
	 *            the description
	 * 
	 */

	public TimeStamp(int timeStampID, int projectID, int period, String superState, int comparatorTimeStampID, String description) {
		pk = new TimeStampPK();
		pk.timeStampID = timeStampID;
		pk.projectID = projectID;
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
		pk.projectID = template.pk.projectID;
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

	/**
	 * Get a String representation of the attribute of this timeStamp entity selected by {@code selector}.
	 * The attribute may be a simple element of this entity as with {@code LABOUR_SUPPLY_RESPONSE} or it
	 * may depend on a calculation, as with {@code INITTIAL_CAPITAL}.
	 * 
	 * @param selector
	 *            the {@link TIMESTAMP_ATTRIBUTE} that designates the required attribute
	 * @return a String representation of the designated attribute of this timeStamp entity
	 */

	public String value(TIMESTAMP_ATTRIBUTE selector) {
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

	public String showDelta(String item, TIMESTAMP_ATTRIBUTE selector) {
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

	/**
	 * Set the comparators for the TimeStamp entity identified by the current project and the given timeStampID
	 * This is done via a static query, because we have to search for the timeStamp concerned - it's not necessarily
	 * this instantiation.
	 * @param projectID
	 *            all persistent records at this timeStampID will be given comparators equal to the timeStampComparatorCursor
	 * @param timeStampID
	 *            the timeStampID of the entity whose comparators are to be set
	 */

	public static void setComparators(int projectID, int timeStampID) {
		logger.debug("Setting comparators for the timeStamp in project {} with timeStamp {}", projectID, timeStampID);
		Project project=Project.get(projectID);
		primaryQuery.setParameter("project", projectID);
		primaryQuery.setParameter("timeStamp", timeStampID);
		TimeStamp timeStamp = primaryQuery.getSingleResult();
		timeStamp.setPreviousComparator(single(projectID, project.getTimeStampComparatorCursor()));
		timeStamp.setStartComparator(single(projectID, 1));
		timeStamp.setEndComparator(single(projectID, project.getTimeStampID()));
		timeStamp.setCustomComparator(single(projectID, project.getTimeStampID()));
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

	public boolean changed(TIMESTAMP_ATTRIBUTE selector) {
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
		for (Industry c : Industry.all(pk.projectID, pk.timeStampID)) {
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
		for (Industry c : Industry.all(pk.projectID, pk.timeStampID)) {
			currentCapital += c.currentCapital();
		}
		return currentCapital;
	}

	/**
	 * @return the total profit in the economy for this timeStamp and its project
	 */
	public double profit() {
		double profit = 0.0;
		for (Commodity commodity : Commodity.all(pk.projectID, pk.timeStampID)) {
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
		for (Stock s : Stock.all(pk.projectID, pk.timeStampID)) {
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
		for (Stock s : Stock.all(pk.projectID, pk.timeStampID)) {
			if ((!s.getStockType().equals("Money")) || (Simulation.isFullPricing())) {
				totalPrice += s.getPrice();
			}
		}
		return totalPrice;
	}

	/**
	 * Fetch all timeStamps in the given project. Largely for diagnostic purposes though could have other uses
	 * 
	 * @param projectID
	 *            the project ID that contains the timeStamps we want
	 * @return a list of all timeStamps at the given project
	 */

	public static List<TimeStamp> allInCurrentProject(int projectID) {
		allInProjectQuery.setParameter("project", projectID);
		return allInProjectQuery.getResultList();
	}

	/**
	 * Fetch the single TimeStamp entity of the given project and the given timestamp
	 * 
	 * @param projectID
	 *            the project that contains the timeStamp we want
	 * @param timeStampID
	 *            the ID of the timeStamp we want
	 * @return the TimeStamp that has the given timeStampID and projectID
	 */
	public static TimeStamp single(int projectID, int timeStampID) {
		primaryQuery.setParameter("project", projectID);
		primaryQuery.setParameter("timeStamp", timeStampID);
		return primaryQuery.getSingleResult();
	}

	/**
	 * Fetch the single TimeStamp entity of the given project and the given timeStamp
	 * 
	 * @param timeStampID
	 *            the timeStampID of the desired TimeStamp entity
	 * @param projectID
	 *            the projectID of the desired TimeStamp entity
	 * @return the TimeStamp that has the given timeStampID and project
	 */
	public static TimeStamp singleInProjectAndTimeStamp(int projectID, int timeStampID) {
		primaryQuery.setParameter("project", projectID);
		primaryQuery.setParameter("timeStamp", timeStampID);
		return primaryQuery.getSingleResult();
	}

	/**
	 * A list of timeStamp records that belong to this superstate in the given period, project and timeStamp
	 * 
	 * @param period
	 *            the given period
	 * @param projectID
	 *            the given project
	 * @param superStateName
	 *            the name of the superState of which this timeStamp is a child
	 * @return a list of timeStamps that belong to this superstate in the given period and the current projec
	 */
	public static List<TimeStamp> superStateChildren(int period, int projectID, String superStateName) {
		superStateQuery.setParameter("project", projectID);
		superStateQuery.setParameter("period", period).setParameter("superState", superStateName);
		return superStateQuery.getResultList();
	}

	public static EntityManager getEntityManager() {
		return entityManager;
	}

	/**
	 * Get the timeStampID of this timeStamp
	 * 
	 * @return the timeStampID of this timeStamp
	 */

	public Integer getTimeStampID() {
		return pk.timeStampID;
	}

	/**
	 * set the timeStampID. Since this is part of the primary key, it should only be set in those cases
	 * where this entity is not persisted (for example in the treeView)
	 * 
	 * @param timeStampID
	 *            the timeStampID to set
	 */
	public void setTimeStampID(int timeStampID) {
		pk.timeStampID = timeStampID;
	}

	/**
	 * Get the projectID of this timeStamp
	 * 
	 * @return the projectID of this timeStamp
	 */

	public Integer getProjectID() {
		return pk.projectID;
	}

	/**
	 * Get the description of this timeStamp
	 * 
	 * @return the description of this timeStamp
	 */

	public String getDescription() {
		return description;
	}

	/**
	 * Set the description of this timeStamp
	 * 
	 * @param description
	 *            the description to set
	 */

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

	/**
	 * Set the projectID of this timeStamp
	 * 
	 * @param projectID
	 *            the projectID to set
	 * 
	 */
	public void setProjectID(int projectID) {
		pk.projectID = projectID;
	}
}
