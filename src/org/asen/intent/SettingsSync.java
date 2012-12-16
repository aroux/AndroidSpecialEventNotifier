package org.asen.intent;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.joda.time.Interval;

@Getter
@RequiredArgsConstructor(suppressConstructorProperties=true)
public class SettingsSync implements Serializable {

	private static final long serialVersionUID = 1533410620609415529L;

	public static final String ACTION_ID = "SETTINGS_SYNC";

	private final String pollTime;

	private final List<Interval> pollInterval;
}
