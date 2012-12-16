package org.asen.service.twitter.json.tcs.parser;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.asen.R;
import org.asen.service.dto.Event;
import org.asen.service.parser.EventParser;


public class TCSParser implements EventParser {

	private static final long serialVersionUID = -2699459514194572423L;

	//	private static String[] problems = { //
	//		"trafic en accordéon", //
	//		"trafic perturbé", //
	//		"voie de droite fermée", //
	//		"voie de gauche fermée", //
	//		"bouchon", //
	//		"véhicule en panne", //
	//		"tunnel fermé dans les deux sens", //
	//		"route fermée", //
	//		"véhicule en feu", //
	//		"objet sur la chaussée", //
	//		"piétons sur la chaussée", //
	//		"retour à la normale"
	//	};

	private static Map<String, Integer> categoryIconMap = new HashMap<String, Integer>();

	static {
		categoryIconMap.put("chaussée recouverte de boue", R.drawable.icn_traffic_yellow);
		categoryIconMap.put("surcharge de trafic", R.drawable.icn_traffic_yellow);
		categoryIconMap.put("accident", R.drawable.icn_traffic_red);
		categoryIconMap.put("véhicule en panne. Danger", R.drawable.icn_traffic_red);
		categoryIconMap.put("roulez très prudemment", R.drawable.icn_traffic_yellow);
		categoryIconMap.put("accident dégagé", R.drawable.icn_traffic_green);
	}

	@Override
	public Event parse(String eventStr) {
		String split[] = eventStr.split(" - ");
		if (split.length != 3) {
			return defaultDetailedEvent(eventStr);
		}

		String title = split[1].trim().replace("->", "→");
		if (title.length() == 0) {
			return defaultDetailedEvent(eventStr);
		}

		String splitDescription[] = split[2].split(",");
		String category = "Unknown";
		Integer iconId = R.drawable.icn_traffic_black;
		if (splitDescription.length > 1) {
			category = splitDescription[1].trim();

			if (categoryIconMap.containsKey(category)) {
				iconId = categoryIconMap.get(category);
			}
		}
		String text = splitDescription[0];
		if (splitDescription.length > 2) {
			for (int i = 2; i<splitDescription.length; ++i) {
				text += (", " + splitDescription[i]);
			}
		}

		Event event = new Event();
		event.setTitle(title);
		event.setCategory(category);
		event.setText(text);
		event.setIcon(iconId);
		return event;
	}

	private Event defaultDetailedEvent(String eventStr) {
		Event event = new Event();
		event.setTitle("Traffic event");
		event.setCategory("Unknown category");
		event.setDate(new Date());
		event.setText(eventStr);
		return event;
	}

}
