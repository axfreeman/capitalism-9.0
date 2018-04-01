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

package capitalism.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.reporting.Dialogues;

/**
 * this class delivers web content as string to the WebView components of the app.
 */
public class WebStuff {
	private static final Logger logger = LogManager.getLogger("WebStuff");

	  public static String getFile(String fileName) {

			StringBuilder result = new StringBuilder("");

			//Get file from resources folder
			ClassLoader classLoader = WebStuff.class.getClassLoader();
			URL pathURL= classLoader.getResource(fileName);
			if (pathURL==null) {
				Dialogues.alert(logger, "Help File Name missing. This is a programme error. Please contact the developer");
				return"<p>Programme Error; please contact developer</p>";
			}
			String	pathName=pathURL.getFile();
			File file = new File(pathName);

			try (Scanner scanner = new Scanner(file)) {

				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					result.append(line).append("\n");
				}

				scanner.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
				
			return result.toString();

		  }
}
