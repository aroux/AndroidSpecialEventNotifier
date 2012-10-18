package org.asen.service.twitter.json.tcs.parser;

import org.asen.service.dto.DetailedEvent;
import org.asen.service.dto.Event;
import org.asen.service.parser.EventParser;

public class TCSParser implements EventParser {

	private static final long serialVersionUID = -2699459514194572423L;

	private static String[] problems = { //
		"trafic en accordéon", //
		"trafic perturbé", //
		"voie de droite fermée", //
		"voie de gauche fermée", //
		"bouchon", //
		"véhicule en panne", //
		"tunnel fermé dans les deux sens", //
		"route fermée", //
		"véhicule en feu", //
		"objet sur la chaussée", //
		"piétons sur la chaussée", //
		"retour à la normale"
	};

	@Override
	public DetailedEvent parse(Event event) {
		String split[] = event.getText().split(" - ");
		if (split.length != 3) {
			return defaultDetailedEvent(event);
		}

		String where = split[1].trim();
		if (where.length() == 0) {
			return defaultDetailedEvent(event);
		}

		String splitDescription[] = split[2].split(",");
		String category = "Unknown";
		if (split.length > 2) {
			category = splitDescription[1].trim();
		}

		String shortDescription = splitDescription[0];
		String longDescription = event.getText();

		DetailedEvent detailedEvent = new DetailedEvent();
		detailedEvent.setCategory(category);
		detailedEvent.setEvent(event);
		detailedEvent.setLongDescription(longDescription);
		detailedEvent.setShortDescription(shortDescription);
		detailedEvent.setWhere(where);
		return detailedEvent;
	}

	private DetailedEvent defaultDetailedEvent(Event event) {
		DetailedEvent detailedEvent = new DetailedEvent();
		detailedEvent.setEvent(event);
		detailedEvent.setCategory("Unknown");
		detailedEvent.setWhere("Somewhere");
		detailedEvent.setLongDescription(event.getText());
		detailedEvent.setShortDescription(event.getText());
		return detailedEvent;
	}

}
