package org.asen.service.parser;

import java.io.Serializable;

import org.asen.service.dto.DetailedEvent;
import org.asen.service.dto.Event;

public interface EventParser extends Serializable {

	DetailedEvent parse(Event event);
}
