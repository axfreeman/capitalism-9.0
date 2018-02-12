/*
 *  Copyright (C) Alan Freeman 2017-2019
 *  
 *  This file is part of the Capitalism Simulation, abbreviated to CapSim
 *  in the remainder of this project
 *
 *  Capsim is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either project 3 of the License, or
 *  (at your option) any later project.
*
*   Capsim is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU General Public License for more details.
*
*   You should have received a copy of the GNU General Public License
*   along with Capsim.  If not, see <http://www.gnu.org/licenses/>.
*/

package rd.dev.simulation.utils;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import rd.dev.simulation.Capitalism;
import rd.dev.simulation.view.LogWindow;

public class Reporter {
	private static final Logger logger = LogManager.getLogger(Reporter.class);
	public static LogWindow logWindow = new LogWindow();				// used by ViewManager and Reporter to tell the user what's going on.

	public Reporter() {
		
	}
	
	/**
	 * Report a message at the INFO level. This is both a helper function to simplify the logging code, and a wrapper to allow us to display what is going
	 * on to the user in a structured way without the tortuous business of writing funky logging appenders. The full functionality of the logging API is not
	 * really needed in this project. Thus, the log4j files and the reporting window are completely disconnected and their only relation to each other is
	 * that this method sends the same message to both of them.
	 * 
	 * @param logger
	 *            the logger of the calling class
	 * @param level
	 *            sets the level in the hierarchy for displaying in the treeView of the logWindow.
	 *            Curently, only three levels exist
	 * @param formatString
	 *            a format string containing text and format characters, to be used to display the arguments using {@code String.format} markup
	 * @param args
	 *            the arguments, if any
	 */
	public static void report(Logger logger, int level, String formatString, Object... args) {
		String message = String.format(formatString, args);
		if (level==0) {
			logger.log(Level.INFO, "");
		}
		logger.log(Level.INFO, message);
		logWindow.addItem(message,level);
	}
	
	/**
	 * empty the log files, except the archive, and reinitialise them with a date stamp at the start of a new session
	 * 
	 */
	public static void initialiseLoggerFiles() {
		// the log files are written into the user directory specified by userBasePath/logfiles - currently fixed, but could be configurable

		String logFilesBase = Capitalism.userBasePath + "\\logfiles\\";

		String logFile1 = logFilesBase + "userview.log";
		String logFile2 = logFilesBase + "debug.log";
		try {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			logger.info("Started new log file at " + dtf.format(now)); // This only goes to archive.log
			String str = "Start of new log at" + dtf.format(now) + "\n";
			BufferedWriter writer1 = new BufferedWriter(new FileWriter(logFile1));
			BufferedWriter writer2 = new BufferedWriter(new FileWriter(logFile2));
			writer1.write(str);
			writer2.write(str);
			writer1.close();
			writer2.close();
		} catch (FileNotFoundException f) {
			logger.error("The log file was not found because of " + f.getMessage());
		} catch (IOException i) {
			logger.error("The log file could not be initialised beause of" + i.getMessage());
		}
	}
}
