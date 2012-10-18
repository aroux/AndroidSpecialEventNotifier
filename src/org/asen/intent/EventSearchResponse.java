package org.asen.intent;

import java.io.Serializable;

import org.asen.service.dto.EventsContainer;

import lombok.Getter;

@Getter
public class EventSearchResponse implements Serializable {

	private static final long serialVersionUID = -4988498003955440941L;

	public static final String ACTION_ID = "ACTION_EVENT_SEARCH_RESPONSE";

	private final EventsContainer events;

	private final EventSearchRequest request;

	public EventSearchResponse(EventsContainer someEvents, EventSearchRequest aRequest) {
		events = someEvents;
		request = aRequest;
	}
}
