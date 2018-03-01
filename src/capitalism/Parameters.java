package capitalism;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Parameters {

	/**
	 * if FULLPRICING is ON, money and labour power are included in the price dynamics
	 */
	private static boolean fullPricing = false;
	public static enum FULL_PRICING {
		ON("Include money in capital"),OFF("Exclude money from capital");
		String text;

		private FULL_PRICING(String text) {
			this.text = text;
		}

		public String text() {
			return text;
		}
		
		public static ObservableList<String>options(){
		    return FXCollections.observableArrayList(
			        ON.text(),
			        OFF.text()
			    );
		}
		public static FULL_PRICING fromText(String text) {
			switch (text) {
			case "Include money in capital":
				return ON;
			case "Exclude money from capital":
			default:
				return OFF;
			}
		}
	}

	
	/**
	 * Determines how the supply of labour power responds to demand
	 * a primitive response function to be expanded and hopefully user-customized
	 * if FLEXIBLE, labour power will expand to meet demand (reserve army)
	 * if FIXED, labour power cannot expand to meet demand and provides a supply constraint on output
	 */
	public static enum LABOUR_RESPONSE {
		FIXED("Fixed"),FLEXIBLE("Flexible");
		String text;

		private LABOUR_RESPONSE(String text) {
			this.text = text;
		}

		public String text() {
			return text;
		}
		
		public static ObservableList<String>options(){
		    return FXCollections.observableArrayList(
			        FIXED.text(),
			        FLEXIBLE.text()
			    );
		}
		public static LABOUR_RESPONSE fromText(String text) {
			switch (text) {
			case "Flexible":
				return FLEXIBLE;
			case "Fixed":
			default:
				return FIXED;
			}
		}
	}
	

	/**
	 * Determines whether the MELT is constant, or adjusts to the prices when these are set externally 
	 */
	public static enum MELT_RESPONSE {
		VALUE_DRIVEN("Value-Driven"), PRICE_DRIVEN("Price-Driven");
		String text;

		private MELT_RESPONSE(String text) {
			this.text = text;
		}

		public String text() {
			return text;
		}
		public static ObservableList<String>options(){
		    return FXCollections.observableArrayList(
			        VALUE_DRIVEN.text(),
			        PRICE_DRIVEN.text()
			    );
		}
		public static MELT_RESPONSE fromText(String text) {
			switch (text) {
			case "Dynamic":
				return PRICE_DRIVEN;
			case "Fixed":
			default:
				return VALUE_DRIVEN;
			}
		}
	}
	
	public static enum PRICE_RESPONSE{
		VALUES("Track Values"),EQUALIZED("Equal Profit Rate"),DYNAMIC("Dynamic");
		String text;

		private PRICE_RESPONSE(String text) {
			this.text = text;
		}

		public String text() {
			return text;
		}
		public static ObservableList<String>options(){
		    return FXCollections.observableArrayList(
			        VALUES.text(),
			        EQUALIZED.text(),
			        DYNAMIC.text()
			    );
		}
		
		public static PRICE_RESPONSE fromText(String text) {
			switch (text) {
			case "Track Values":
				return VALUES;
			case "Equal Profit Rate":
			default:
				return DYNAMIC;
			}
		}
	}
	/**
	 * @return the fullPricing
	 */
	public static boolean isFullPricing() {
		return fullPricing;
	}

	/**
	 * @param fullPricing
	 *            the fullPricing to set
	 */
	public static void setFullPricing(boolean fullPricing) {
		Parameters.fullPricing = fullPricing;
	}
}
