package org.asen.time;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.LocalTime;

public class LocalTimeUtils {
	private static final Instant CONSTANT = new Instant(0);

	public static Interval create(LocalTime from, LocalTime to) throws IllegalArgumentException {
		return new Interval(from.toDateTime(CONSTANT), to.toDateTime(CONSTANT));
	}

	public static DateTime create(LocalTime t) {
		return t.toDateTime(CONSTANT);
	}
}