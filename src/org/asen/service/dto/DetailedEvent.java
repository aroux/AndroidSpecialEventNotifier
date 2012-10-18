package org.asen.service.dto;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DetailedEvent implements Serializable {

	private static final long serialVersionUID = -3418246622382789196L;

	private Event event;

	private String where;

	private String category;

	private String shortDescription;

	private String longDescription;

}
