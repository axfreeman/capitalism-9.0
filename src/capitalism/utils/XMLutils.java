package capitalism.utils;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.Capitalism;
import capitalism.model.Commodity;
import capitalism.model.Industry;

public class XMLutils {
	private static final Logger logger = LogManager.getLogger("XML handler");

	public static void makeXML(int timeStamp) {
		JAXBContext commodityContext;
		JAXBContext industryContext;
		File output;
		try {
			output = new File(Capitalism.getUserBasePath() + "Capitalism.xml");
		} catch (Exception e) {
			Dialogues.alert(logger, "Could not create file to save the database because {}", e.getMessage());
			return;
		}
		try {
			commodityContext = JAXBContext.newInstance(Commodity.class);
			Marshaller commodityMarshaller = commodityContext.createMarshaller();
			commodityMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			for (Commodity commodity : Commodity.commoditiesAll(timeStamp)) {
				commodityMarshaller.marshal(commodity, output);
				commodityMarshaller.marshal(commodity, System.out);
			}
			industryContext = JAXBContext.newInstance(Industry.class);
			Marshaller industryMarshaller = industryContext.createMarshaller();
			industryMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			for (Industry industry : Industry.industriesAll(timeStamp)) {
				industryMarshaller.marshal(industry, output);
				industryMarshaller.marshal(industry, System.out);
			}
		} catch (JAXBException e) {
			Dialogues.alert(logger, "Could not save the database because {}", e.getMessage());
		}

	}
}
