package org.asen.intent;

import java.io.Serializable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.asen.service.matcher.EventMatcher;
import org.asen.service.parser.EventParser;

@Getter
@RequiredArgsConstructor(suppressConstructorProperties=true)
public class EventSearchRequest implements Serializable {

	private static final long serialVersionUID = 6335820254698027251L;

	public static final String ACTION_ID = "ACTION_EVENT_SEARCH_REQUEST";

	private final String accessData;

	private final EventAction searchAction;

	private final EventMatcher matcher;

	private final EventParser parser;
}
