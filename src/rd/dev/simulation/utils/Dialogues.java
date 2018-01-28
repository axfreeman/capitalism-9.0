package rd.dev.simulation.utils;

import java.io.File;

import org.apache.logging.log4j.Logger;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.DirectoryChooser;
import rd.dev.simulation.Capitalism;

public class Dialogues {

	/**
	 * legacy version of alert(logger, formatString, args) to support calls being phased out.
	 * 
	 * TODO eliminate these calls
	 * 
	 * record the alert in the specified log file and display it in an alert window for the user to see
	 * 
	 * @param logger
	 *            the logger to use
	 * @param formatString
	 *            a format string suitable for the {@code String.format()} method
	 */

	public static void alert(Logger logger, String formatString) {

		alert(logger,formatString,(Object[]) null);
		
	}

	/**
	 * record the alert in the specified log file and display it in an alert window for the user to see
	 * 
	 * @param logger
	 *            the logger to use
	 * @param args
	 *            the arguments for a call to {@code String.format()} using formatString
	 * @param formatString
	 *            a format string suitable for the {@code String.format()} method
	 */

	public static void alert(Logger logger, String formatString, Object... args) {

		RuntimeException r = new RuntimeException(formatString);
		logger.debug(formatString);


		Reporter.report(logger, 0, formatString, args);
		StackTraceElement a[] = r.getStackTrace();
		for (int i = 0; i < a.length; i++) {
			String logMessage = a[i].toString();
			logger.debug("++++++++ at " + logMessage);
		}

		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle("Warning");
		alert.setHeaderText("An error occurred");
		alert.setContentText("Consult debug.log for details");

		alert.showAndWait();
		@SuppressWarnings("unused") double debugHalt = 0;

	}

	public static void info(String header, String message) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Information");
		alert.setHeaderText(header);
		alert.setContentText(message);
		alert.showAndWait();
	}

	public static File directoryChooser(String title) {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle(title);
		File defaultDirectory = new File("c:/Users/afree/Documents");
		chooser.setInitialDirectory(defaultDirectory);
		File selectedDirectory = chooser.showDialog(Capitalism.primaryStage);
		return selectedDirectory;
	}
}
