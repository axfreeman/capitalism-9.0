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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * this class delivers web content as string to the WebView components of the app.
 */
public class WebStuff {
	private static final Logger logger = LogManager.getLogger("WebStuff");

	public static String getFile(String fileName) {
		// Get file from local folder
		File file = new File(fileName);
		InputStream fis;
		String result=null;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
//		ClassLoader classLoader = WebStuff.class.getClassLoader();
//		InputStream inStream = classLoader.getResource(fileName);
		try {
			result=inputStreamToString(fis);
		} catch (Exception e1) {
			logger.debug("inStream call failed because {}", e1.getMessage());
			result = null;
		}
		try {
			fis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
// code below fails when app is run from jar
//		StringBuilder result = new StringBuilder("");
//		URL pathURL = null;
//		pathURL = classLoader.getResource(fileName);
//		if (pathURL == null) {
//			Dialogues.alert(logger, "Help File Name missing. This is a programme error. Please contact the developer");
//			logger.debug("Help File Missing");
//			return "<p>Programme Error; please contact developer</p>";
//		}
//		String pathName = pathURL.getFile();
//		logger.debug("pathURL is {}", pathURL.getFile());
//		;
//		File file = new File(pathName);
//
//		try (Scanner scanner = new Scanner(file)) {
//
//			while (scanner.hasNextLine()) {
//				String line = scanner.nextLine();
//				result.append(line).append("\n");
//			}
//
//			scanner.close();
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		return result.toString();

	}

	/**
	 * see https://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string
	 * 
	 */

	public static String inputStreamToString(InputStream inputStream) throws IOException {
		try (ByteArrayOutputStream result = new ByteArrayOutputStream()) {
			byte[] buffer = new byte[1024];
			int length;
			while ((length = inputStream.read(buffer)) != -1) {
				result.write(buffer, 0, length);
			}

			return result.toString(Charset.defaultCharset());
		}
	}
}
