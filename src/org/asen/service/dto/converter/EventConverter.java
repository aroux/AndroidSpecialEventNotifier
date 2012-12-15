package org.asen.service.dto.converter;

import org.asen.service.dto.EventsContainer;
import org.asen.service.parser.EventParser;

public interface EventConverter<T> {

	EventsContainer convert(T t, EventParser parser);

}
