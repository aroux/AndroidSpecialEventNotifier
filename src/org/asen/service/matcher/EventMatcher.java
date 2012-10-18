package org.asen.service.matcher;

import java.io.Serializable;

import org.asen.service.dto.Event;

public interface EventMatcher extends Serializable {

	boolean match(Event event);

}
