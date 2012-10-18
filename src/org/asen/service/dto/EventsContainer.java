package org.asen.service.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@RequiredArgsConstructor(suppressConstructorProperties=true)
public class EventsContainer implements Serializable {

	private static final long serialVersionUID = -4916005573466447562L;

	private final List<Event> events;

	public List<Event> getEvents() {
		return Collections.unmodifiableList(events);
	}
}
