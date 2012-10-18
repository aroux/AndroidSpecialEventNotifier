package org.asen.service.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Event implements Serializable {

	private static final long serialVersionUID = 1695729732288368379L;

	private String text;

	private Date date;
}
