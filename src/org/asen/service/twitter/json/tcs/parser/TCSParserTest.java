package org.asen.service.twitter.json.tcs.parser;

import java.util.Date;

import org.asen.service.dto.DetailedEvent;
import org.asen.service.dto.Event;
import org.junit.Test;

public class TCSParserTest {


	@Test
	public void test() {

		TCSParser parser = new TCSParser();
		Event event = new Event();
		event.setText("#A1 - Genève -> Lausanne - Entre la jonction d'Aubonne et la jonction de Morges-Ouest trafic en accordéon, surcharge de trafic");
		event.setDate(new Date());


		DetailedEvent detailedEvent = parser.parse(event);
		System.out.println(detailedEvent);
	}

}
