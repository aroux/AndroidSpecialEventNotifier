package org.asen.service.dto.converter;

import org.asen.service.dto.EventsContainer;

public interface EventConverter<T> {

	EventsContainer convert(T t);

}
