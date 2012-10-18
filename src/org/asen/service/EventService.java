package org.asen.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import lombok.RequiredArgsConstructor;

import org.asen.intent.EventSearchRequest;
import org.asen.intent.EventSearchResponse;
import org.asen.service.dto.DetailedEvent;
import org.asen.service.dto.Event;
import org.asen.service.dto.EventsContainer;
import org.asen.service.matcher.EventMatcher;
import org.asen.service.parser.EventParser;
import org.asen.service.twitter.TweetService;

import android.R;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public abstract class EventService extends IntentService {

	Logger logger = LogManager.getLogManager().getLogger("");

	private final Map<String, IntervalEventsLoaderThread> loaders = new HashMap<String, IntervalEventsLoaderThread>();

	private final AtomicReference<Boolean> contLoaders = new AtomicReference<Boolean>();

	private NotificationManager notificationMananger = null;

	private final AtomicInteger notifCounter = new AtomicInteger(0);

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
				EventsContainer events = processSynchAction(url);
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

	abstract protected EventsContainer processSynchAction(String accessData);

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

		@Override
		public void run() {
			while (cont.get()) {
				EventsContainer events = processSynchAction(url);

				if (matcher != null) {
					for (Event event : events.getEvents()) {
						boolean matched = matcher.match(event);

						if (matched) {
							sendNotification(event);
						}
						//logger.log(Level.INFO, (matched ? "MATCHED" : "NOT MATCHED") + " : event -> " + event);
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

			DetailedEvent detailedEvent = parser.parse(event);

			CharSequence tickerText = "Traffic Warning";
			CharSequence contentTitle = df.format(event.getDate()) + " : " + detailedEvent.getWhere();
			CharSequence contentText =  detailedEvent.getCategory() + " : " + detailedEvent.getShortDescription();
			Notification notification = new Notification(R.drawable.btn_star, tickerText,System.currentTimeMillis());
			Intent notificationIntent = new Intent(EventService.this, TweetService.class);
			PendingIntent contentIntent = PendingIntent.getActivity(EventService.this, 0, notificationIntent, 0);
			notification.setLatestEventInfo(getApplicationContext(), contentTitle, contentText, contentIntent);
			notificationMananger.notify(notifCounter.incrementAndGet(), notification);
		}
	}
}