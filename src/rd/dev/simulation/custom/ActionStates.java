package rd.dev.simulation.custom;

import java.util.ArrayList;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import rd.dev.simulation.Capitalism;
import rd.dev.simulation.command.CircuitsProduce;
import rd.dev.simulation.command.ClassesReproduce;
import rd.dev.simulation.command.Command;
import rd.dev.simulation.command.Distribute;
import rd.dev.simulation.command.ImmediateConsequences;
import rd.dev.simulation.command.PreTrade;
import rd.dev.simulation.command.Produce;
import rd.dev.simulation.command.Revenue;
import rd.dev.simulation.command.Accumulate;
import rd.dev.simulation.command.Constrain;
import rd.dev.simulation.command.Demand;
import rd.dev.simulation.command.Supply;
import rd.dev.simulation.command.Trade;

/**
 * This class is central to the operation of the simulation. See al{@link Capitalism#initializeActionStates()}.
 * It defines the possible actions of the simulation, which are of two types: (1) sub-actions such as {@link Supply} which carry out
 * primitive actions;(2) super-actions such as {@link PreTrade} which conducts a series of primitive actions. The super-Actions correspond to normal
 * economic phases or 'aspects' of the reproduction of an economy namely the purchase of commodities, the production of commodities, and the distribution of
 * revenues. The primitives don't have a distinct economic meaning but can help understand the logical components of a complete economic activity. This is
 * given visual representation at several places in the simulation, notably in the logfile and in the actionButtons, which have a tree structure so that the
 * user may decide either to study the detail of a phase or simply the it overall effects.
 */

public enum ActionStates {
	M_C_PreTrade("Exchange", new PreTrade(), 
			"Exchange Money for Commodities", 
			false), 
	M_C_Supply("Supply", new Supply(),
			"Calculate and register the supply of each use value",
			true), 
	M_C_Demand("Demand", new Demand(), 
			"Calculate and register the demand for each use value", 
			true), 
	M_C_Constrain("Constrain",new Constrain(),
			"Calculate the proportion of demand for each use value that can be satisfied by the supply. Then adjust output downward accordingly",
			true), 
	M_C_Trade("Trade", new Trade(), 
			"Carry out the purchases determined by the allocation of supply", 
			true), 
	C_P_Produce("Production",	new Produce(), 
			"Circuits produce goods, classes reproduce themselves",	
			false), 
	C_P_CircuitsProduce("Produce", new CircuitsProduce(), 
			"Circuits produce goods", 
			true), 
	C_P_ClassesReproduce("Reproduce",new ClassesReproduce(), 
			"Circuits produce goods, consuming productive stocks and labour power",	
			true), 
	C_P_ImmediateConsequences("Consequences", new ImmediateConsequences(),
			"The combined consequence of production and reproduction is a change in unit values and prices. This action calculates them",
			true), 
	C_M_Distribute("Distribution", new Distribute(),
			"Distribution: recalculate unit values and prices and the MELT. Transfer surplus (M'-M)to the capitalist class. Accumulate",
			false),
	C_M_Revenue("Revenue", new Revenue(),"Distribute the surplus",true),
	C_M_Accumulate("Accumulate", new Accumulate(),  "Use the surplus", true);
	
	/**
	 * the nextAction will be a primitive action if this is a primitive action (eg after Supply we have to do Demand).
	 * if this is a superAction such as PreTrade, the nextAction is the next superAction
	 */
	protected ActionStates nextAction; // the next logical permissible action

	/**
	 * if this is a superAction such as M_C, the nextAction is the next superAction. But in that case we must also know the next
	 * permissible primitive action, which is a component of this superAction (eg registerSupply is the firstSubAction of M-C)
	 */
	protected ActionStates firstSubAction;// the next logical permissible primitive action

	/**
	 * the text to be used on this actionState's button.
	 * Also used to serialise it in the Project persistent entity
	 */
	public String text;

	/**
	 * the command that will be executed when this button is pressed
	 */
	private Command command;

	/** 
	 *the tooltip associated with the button that carries out this action
	 */
	 protected String tooltip;
	 
	 /**
	  * true if this is a substate, false if it is a superstate
	  */
	protected boolean isSubState;
	
	/**
	 * if this is a subState, superAction says what its superAction is
	 * TODO not needed I think
	 */
	public ActionStates superAction;
	
	/**
	 * an integer representation of superAction
	 * TODO store the superAction directly into the database?
	 */
	protected int superActionAsInteger;
	
	/**
	 * the button that executes this action, via its command member
	 */
	protected Button button;

	protected TreeItem<String> treeItem;

	/**
	 * if this is a superAction, this  list contains its subActions
	 */
	protected ArrayList<ActionStates> children =new ArrayList<ActionStates>();

	/**
	 * at last, the constructor
	 * @param text the text that goes on the button and is used to store this actionState in the Project record
	 * @param command the command that will be executed when the button is pressed
	 * @param tooltip the tooltip that will be displayed when the mouse hovers over the button
	 * @param isSubState true if this is a primitive command, false if it is a superAction
	 */
	ActionStates(String text, Command command, String tooltip, boolean isSubState) {
		this.text = text;
		this.command = command;
		this.tooltip = tooltip;
		this.isSubState = isSubState;
	}

	/**
	 * define the successor of this action. Normal usage would be (1) if this is a subAction which has a successor subAction, this would be the nextAction (2)
	 * if this is the last SubAction in the current superAction, the nextAction of this would be the next SuperAction (3) if this is a superAction,the
	 * nextAction would be the next SuperAction.
	 * 
	 * @param nextAction
	 *            the nextAction
	 */
	public void setSuccessor(ActionStates nextAction) {
		this.nextAction = nextAction;
	}

	/**
	 * if this action has a permissible subAction (the first in the list of its components) make a note, so the corresponding button can be enabled
	 * 
	 * @param subAction
	 *            the next permissible subAction, if there is one (otherwise, don't call this method!)
	 */
	public void setPermissibleSubAction(ActionStates subAction) {
		this.firstSubAction = subAction;
	}

	/**
	 * if this is a subAction, this defines its parent. This is used to place the subAction in the tree under the appropriate superAction
	 * 
	 * @param superAction
	 *            the parent superAction
	 */
	public void setParent(ActionStates superAction) {
		superAction.children.add(this); // tell parent I am a child
		this.superAction=superAction;   // tell me who my parent is
	}

	public String getText() {
		return text;
	}

	public Command getCommand() {
		return command;
	}

	/**
	 * @return the button
	 */
	public Button getButton() {
		return button;
	}

	/**
	 * @param button
	 *            the button to set
	 */
	public void setButton(Button button) {
		this.button = button;
	}

}
