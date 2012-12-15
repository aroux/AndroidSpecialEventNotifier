package org.asen.service.parser;

import java.io.Serializable;

import org.asen.service.dto.Event;

public interface EventParser extends Serializable {

	Event parse(String eventStr);
}
