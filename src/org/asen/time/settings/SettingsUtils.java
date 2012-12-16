package org.asen.time.settings;

import java.util.ArrayList;
import java.util.List;

import org.asen.service.matcher.EventMatcher;
import org.asen.service.matcher.EventMatchers;
import org.asen.time.LocalTimeUtils;
import org.joda.time.Interval;
import org.joda.time.LocalTime;


public class SettingsUtils {

	public final static String SHARED_PREFERENCES_NAME = "ASEN_SETTINGS";

	public final static String URL_SETTINGS_KEY = "URL";
	public final static String POLLER_TIME_SETTINGS_KEY = "POLLER_TIME";
	public final static String POLLER_INTERVAL_PATTERN_SETTINGS_KEY = "POLLER_INTERVAL_PATTERN";
	public final static String FILTER_PATTERN_SETTINGS_KEY = "FILTER_PATTERN";


	public static List<Interval> parseIntervals(String intervalPattern) {
		List<Interval> returnIntervals = new ArrayList<Interval>();
		String intervals[] = intervalPattern.split("\\|");
		for (String interval : intervals ){
			String beginEnd[] = interval.split("-");
			if (beginEnd.length != 2) {
				throw new IllegalArgumentException("Invalid interval definition : " + interval);
			}

			LocalTime begin = buildLocalTime(beginEnd[0]);
			LocalTime end = buildLocalTime(beginEnd[1]);
			returnIntervals.add(LocalTimeUtils.create(begin, end));
		}
		return returnIntervals;
	}

	private static LocalTime buildLocalTime(String localTimeStr) {
		String hoursMinutes[] = localTimeStr.split(":");
		if (hoursMinutes.length != 2) {
			throw new IllegalArgumentException("Invalid time definition : " + localTimeStr);
		}

		int hours;
		try {
			hours = Integer.parseInt(hoursMinutes[0]);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid hours : " + hoursMinutes[0]);
		}

		int minutes;
		try {
			minutes = Integer.parseInt(hoursMinutes[1]);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid minutes : " + hoursMinutes[1]);
		}

		return new LocalTime(hours,minutes);
	}

	public static EventMatcher parseFilterPattern(String filterPattern) {
		String patterns[] = filterPattern.split("\\|");
		List<EventMatcher> localMatchers = new ArrayList<EventMatcher>();
		for (String pattern : patterns) {
			localMatchers.add(EventMatchers.pattern(pattern));
		}
		EventMatcher matchers[] = new EventMatcher[localMatchers.size()];
		return EventMatchers.or(localMatchers.toArray(matchers)).get();
	}
}
