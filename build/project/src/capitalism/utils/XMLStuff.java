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
import javax.xml.bind.ValidationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.model.OneProject;
import capitalism.reporting.Dialogues;

public class XMLStuff {
	private static final Logger logger = LogManager.getLogger("XML handler");

	public static void exportToXML(OneProject oneProject, File file) {
		JAXBContext commoditiesContext;
		try {
			commoditiesContext = JAXBContext.newInstance(OneProject.class);
			Marshaller commoditiesMarshaller = commoditiesContext.createMarshaller();
			commoditiesMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			commoditiesMarshaller.marshal(oneProject, file);
		} catch (JAXBException e) {
			Dialogues.alert(logger, "Could not save the database because %s", e.getMessage());
		}
	}

	public static void getDatabaseFromXML() {
		File file = null;
		file = Dialogues.loadFileChooser("Location of the new data");
		if (file == null)
			return;
		Unmarshaller jaxbUnmarshaller;
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(OneProject.class);
			jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			OneProject oneProject = (OneProject) jaxbUnmarshaller.unmarshal(file);
			oneProject.importFromEditorToDatabase();
		} catch (ValidationException r) {
			Dialogues.alert(logger, "The file was invalid because {}", r.getMessage());
			return;
		} catch (JAXBException e) {
			Dialogues.alert(logger, "Could not decode this file because {}", e.getMessage());
			return;
		}
	}
}
