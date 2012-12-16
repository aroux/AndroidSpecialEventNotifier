package org.asen.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import lombok.RequiredArgsConstructor;

import org.asen.intent.EventSearchRequest;
import org.asen.intent.EventSearchResponse;
import org.asen.service.dto.Event;
import org.asen.service.dto.EventsContainer;
import org.asen.service.matcher.EventMatcher;
import org.asen.service.parser.EventParser;
import org.asen.service.twitter.TweetService;
import org.asen.time.LocalTimeUtils;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalTime;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;

public abstract class EventService extends IntentService {

	Logger logger = LogManager.getLogManager().getLogger("");

	private final Map<String, IntervalEventsLoaderThread> loaders = new HashMap<String, IntervalEventsLoaderThread>();

	private final AtomicReference<Boolean> contLoaders = new AtomicReference<Boolean>();

	private NotificationManager notificationMananger = null;

	private final AtomicInteger notifCounter = new AtomicInteger(0);


	private final static List<Interval> pollPeriods = new ArrayList<Interval>();

	static {
		pollPeriods.add(LocalTimeUtils.create(new LocalTime(6,30), new LocalTime(8,30)));
		pollPeriods.add(LocalTimeUtils.create(new LocalTime(16,30), new LocalTime(18,30)));
	}

	protected EventService(String name){
		super(name);
		contLoaders.set(true);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		if (notificationMananger == null) {
			notificationMananger = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		}
		return START_STICKY;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (EventSearchRequest.ACTION_ID.equals(intent.getAction())) {
			EventSearchRequest esr = (EventSearchRequest) intent.getExtras().getSerializable(EventSearchRequest.ACTION_ID);
			String url = esr.getAccessData();
			switch (esr.getSearchAction()) {
			case SYNC:
				EventsContainer events = processSyncAction(url, esr.getParser(), true);
				//				for (Event event : events.getEvents()) {
				//					DetailedEvent detailedEvent = esr.getParser().parse(event);
				//					event.setIcon(detailedEvent.getIcon());
				//				}
				responseBack(Intent.ACTION_SYNC, events, esr);
				break;
			case POLL:
				if (!loaders.containsKey(url)) {
					IntervalEventsLoaderThread loader = new IntervalEventsLoaderThread(getSleepTime(), //
							contLoaders, url, esr.getMatcher(), esr.getParser());
					loader.start();
					loaders.put(url, loader);
				}
				break;
			}
		} else {
			logger.log(Level.SEVERE, "Intent action not supported : " + intent.getAction());
		}
	}

	abstract protected EventsContainer processSyncAction(String accessData, EventParser eventParser, boolean refresh);

	abstract protected long getSleepTime();

	private void responseBack(String action, EventsContainer events, EventSearchRequest request) {
		EventSearchResponse response = new EventSearchResponse(events, request);
		Intent bIntent = new Intent(EventSearchResponse.ACTION_ID);
		Bundle bundle = new Bundle();
		bundle.putSerializable(EventSearchResponse.ACTION_ID, response);
		bIntent.putExtras(bundle);
		sendBroadcast(bIntent);
	}

	@RequiredArgsConstructor(suppressConstructorProperties=true)
	private class IntervalEventsLoaderThread extends Thread {

		private final long sleepTime;
		private final AtomicReference<Boolean> cont;
		private final String url;
		private final EventMatcher matcher;
		private final EventParser parser;
		DateFormat df = new SimpleDateFormat("HH:mm:ss");

		private boolean pollIsEnabled() {
			DateTime now = LocalTimeUtils.create(LocalTime.now());

			for (Interval interval : pollPeriods) {
				if (interval.contains(now)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public void run() {

			while (cont.get()) {

				if (pollIsEnabled()) {

					EventsContainer events = processSyncAction(url, parser, false);

					if (matcher != null) {
						for (Event event : events.getEvents()) {
							boolean matched = matcher.match(event);

							if (matched) {
								sendNotification(event);
							}
							//logger.log(Level.INFO, (matched ? "MATCHED" : "NOT MATCHED") + " : event -> " + event);
						}
					}
				}

				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					logger.log(Level.WARNING, "Sleep interrupted");
				}
			}
		}


		private void sendNotification(Event event) {

			//DetailedEvent detailedEvent = parser.parse(event);

			CharSequence tickerText = "Traffic Warning";
			CharSequence contentTitle = df.format(event.getDate()) + " : " + event.getTitle();
			CharSequence contentText =  event.getText();
			Notification notification = new Notification(event.getIcon(), tickerText,System.currentTimeMillis());
			notification.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			Intent notificationIntent = new Intent(EventService.this, TweetService.class);
			PendingIntent contentIntent = PendingIntent.getActivity(EventService.this, 0, notificationIntent, 0);
			notification.setLatestEventInfo(getApplicationContext(), contentTitle, contentText, contentIntent);
			notificationMananger.notify(notifCounter.incrementAndGet(), notification);
		}
	}
}
