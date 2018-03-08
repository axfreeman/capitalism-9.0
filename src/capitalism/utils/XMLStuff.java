/*
 *  Copyright (C) Alan Freeman 2017-2019
 *  
 *  This file is part of the Capitalism Simulation, abbreviated to CapSim
 *  in the remainder of this project
 *
 *  Capsim is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either project of the License, or
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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.Capitalism;
import capitalism.model.OneProject;
import capitalism.model.Commodity;
import capitalism.model.Industry;

public class XMLStuff {
	private static final Logger logger = LogManager.getLogger("XML handler");

	public static void makeXMLList(int projectID, int timeStampID) {
		JAXBContext commoditiesContext;
		File output;
		try {
			output = new File(Capitalism.getUserBasePath() + "Capitalism.xml");
		} catch (Exception e) {
			Dialogues.alert(logger, "Could not create file to save the database because {}", e.getMessage());
			return;
		}
		try {
			commoditiesContext = JAXBContext.newInstance(OneProject.class);
			Marshaller commoditiesMarshaller = commoditiesContext.createMarshaller();
			commoditiesMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			OneProject oneProject = new OneProject();
			oneProject.setLists(projectID, timeStampID);
			commoditiesMarshaller.marshal(oneProject, System.out);
			commoditiesMarshaller.marshal(oneProject, output);
		} catch (JAXBException e) {
			Dialogues.alert(logger, "Could not save the database because %s", e.getMessage());
		}

	}

	public static void makeXML(int timeStamp) {
		JAXBContext commodityContext;
		JAXBContext industryContext;
		File output;
		try {
			File saveDirectory = Dialogues.directoryChooser("Location of the new data");
			String saveDirectoryPath = saveDirectory.getCanonicalPath();
			output = new File(saveDirectoryPath + "Capitalism.xml");
		} catch (Exception e) {
			Dialogues.alert(logger, "Could not create file to save the database because {}", e.getMessage());
			return;
		}
		try {
			commodityContext = JAXBContext.newInstance(Commodity.class);
			Marshaller commodityMarshaller = commodityContext.createMarshaller();
			commodityMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			for (Commodity commodity : Commodity.all(timeStamp)) {
				commodityMarshaller.marshal(commodity, output);
				commodityMarshaller.marshal(commodity, System.out);
			}
			industryContext = JAXBContext.newInstance(Industry.class);
			Marshaller industryMarshaller = industryContext.createMarshaller();
			industryMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			for (Industry industry : Industry.all(timeStamp)) {
				industryMarshaller.marshal(industry, output);
				industryMarshaller.marshal(industry, System.out);
			}
		} catch (JAXBException e) {
			Dialogues.alert(logger, "Could not save the database because {}", e.getMessage());
		}
	}

	public static void getXML() {
		File file = null;
		try {
			File saveDirectory = Dialogues.directoryChooser("Location of the new data");
			String saveDirectoryPath = saveDirectory.getCanonicalPath();
			file = new File(saveDirectoryPath + "/Capitalism.xml");
		} catch (Exception e) {
			Dialogues.alert(logger, "Could not load this file because {}", e.getMessage());
			return;
		}
		OneProject oneProject;
		Unmarshaller jaxbUnmarshaller;
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(OneProject.class);
			jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			oneProject = (OneProject) jaxbUnmarshaller.unmarshal(file);
			oneProject.loadLists();
		} catch (JAXBException e) {
			Dialogues.alert(logger, "Could not decode this file because {}", e.getMessage());
			return;
		}
	}
}
