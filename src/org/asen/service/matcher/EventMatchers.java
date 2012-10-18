package org.asen.service.matcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.asen.service.dto.Event;


public class EventMatchers  {

	public static FluentEventMatcher and(final EventMatcher...eventMatchers) {
		return new FluentEventMatcher(new EventMatcher() {

			private static final long serialVersionUID = -8304057678427031130L;

			@Override
			public boolean match(Event event) {
				for (EventMatcher matcher : eventMatchers) {
					if (!matcher.match(event)) {
						return false;
					}
				}

				return true;
			}
		});
	}

	public static FluentEventMatcher or(final EventMatcher...eventMatchers) {
		return new FluentEventMatcher(new EventMatcher() {

			private static final long serialVersionUID = -6360533786532719839L;

			@Override
			public boolean match(Event event) {
				for (EventMatcher matcher : eventMatchers) {
					if (matcher.match(event)) {
						return true;
					}
				}

				return false;
			}
		});
	}

	public static EventMatcher pattern(final String pattern) {
		final Pattern p = Pattern.compile(pattern);
		return new EventMatcher() {

			private static final long serialVersionUID = -6360533786532719839L;

			@Override
			public boolean match(Event event) {
				return p.matcher(event.getText()).matches();
			}
		};
	}


	public static class FluentEventMatcher {
		private final EventMatcher matcher;

		private FluentEventMatcher(EventMatcher aMatcher) {
			matcher = aMatcher;
		}

		public FluentEventMatcher and(final EventMatcher...eventMatchers) {
			List<EventMatcher> matchers = new ArrayList<EventMatcher>();
			matchers.add(matcher);
			matchers.addAll(Arrays.asList(eventMatchers));
			return EventMatchers.and((EventMatcher[]) matchers.toArray());
		}

		public FluentEventMatcher or(final EventMatcher...eventMatchers) {
			List<EventMatcher> matchers = new ArrayList<EventMatcher>();
			matchers.add(matcher);
			matchers.addAll(Arrays.asList(eventMatchers));
			return EventMatchers.or((EventMatcher[]) matchers.toArray());
		}

		public EventMatcher get() {
			return matcher;
		}
	}
}
