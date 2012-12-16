package org.asen.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import lombok.RequiredArgsConstructor;

import org.asen.intent.EventSearchRequest;
import org.asen.intent.EventSearchResponse;
import org.asen.intent.PollerStatusRequest;
import org.asen.intent.PollerStatusResponse;
import org.asen.intent.SettingsSync;
import org.asen.service.dto.Event;
import org.asen.service.dto.EventsContainer;
import org.asen.service.matcher.EventMatcher;
import org.asen.service.parser.EventParser;
import org.asen.service.twitter.TweetService;
import org.asen.time.LocalTimeUtils;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalTime;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

public abstract class EventService extends Service {

	Logger logger = LogManager.getLogManager().getLogger("");

	private final Map<String, IntervalEventsLoaderThread> loaders = new HashMap<String, IntervalEventsLoaderThread>();

	private NotificationManager notificationMananger = null;

	private final AtomicInteger notifCounter = new AtomicInteger(0);

	private final static List<Interval> pollPeriods = new ArrayList<Interval>();

	private static long DEFAULT_POLL_TIME = 1000 * 60 * 10;

	private long pollTime = DEFAULT_POLL_TIME;

	public class LocalBinder extends Binder {
		EventService getService() {
			return EventService.this;
		}
	}

	private final IBinder mBinder = new LocalBinder();

	static {
		pollPeriods.add(LocalTimeUtils.create(new LocalTime(6,30), new LocalTime(8,30)));
		pollPeriods.add(LocalTimeUtils.create(new LocalTime(16,30), new LocalTime(18,30)));
	}

	protected EventService(String name){
		//super(name);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		if (notificationMananger == null) {
			notificationMananger = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		}
		onHandleIntent(intent);
		return START_STICKY;
	}


	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	protected void onHandleIntent(Intent intent) {
		if (EventSearchRequest.ACTION_ID.equals(intent.getAction())) {
			EventSearchRequest esr = (EventSearchRequest) intent.getExtras().getSerializable(EventSearchRequest.ACTION_ID);
			String url = esr.getAccessData();
			switch (esr.getSearchAction()) {
			case SYNC:
				EventsContainer events = processSyncAction(url, esr.getParser(), true);
				responseBack(Intent.ACTION_SYNC, events, esr);
				break;
			case POLL:
				if (!loaders.containsKey(url)) {
					IntervalEventsLoaderThread loader = new IntervalEventsLoaderThread(
							new AtomicBoolean(true), url, esr.getMatcher(), esr.getParser());
					loader.setPollTime(pollTime);
					loader.start();
					loaders.put(url, loader);
					sendPollerStatus(url, true);
				} else {
					IntervalEventsLoaderThread loader = loaders.get(url);
					loader.stopPoller();
					loaders.remove(url);
					sendPollerStatus(url, false);
				}
				break;
			}
		} else if (PollerStatusRequest.ACTION_ID.equals(intent.getAction())) {
			PollerStatusRequest pollerStatus = (PollerStatusRequest) intent.getExtras().getSerializable(PollerStatusRequest.ACTION_ID);
			sendPollerStatus(pollerStatus.getUrl(), loaders.containsKey(pollerStatus.getUrl()));
		} else if (SettingsSync.ACTION_ID.equals(intent.getAction())) {
			SettingsSync settingsSync = (SettingsSync) intent.getExtras().getSerializable(SettingsSync.ACTION_ID);
			if (settingsSync.getPollInterval() != null ){
				pollPeriods.clear();
				pollPeriods.addAll(settingsSync.getPollInterval());
			}
			if (settingsSync.getPollTime() != null) {
				try {
					pollTime = Long.parseLong(settingsSync.getPollTime());
					for (IntervalEventsLoaderThread loader : loaders.values()) {
						loader.setPollTime(pollTime);
						// Needed to take new pollTime into account
						loader.pollNow();
					}
				}catch (NumberFormatException e) {
					// Ignore setting
				}
			}
		} else {
			logger.log(Level.SEVERE, "Intent action not supported : " + intent.getAction());
		}
	}

	abstract protected EventsContainer processSyncAction(String accessData, EventParser eventParser, boolean refresh);

	private void responseBack(String action, EventsContainer events, EventSearchRequest request) {
		EventSearchResponse response = new EventSearchResponse(events, request);
		Intent bIntent = new Intent(EventSearchResponse.ACTION_ID);
		Bundle bundle = new Bundle();
		bundle.putSerializable(EventSearchResponse.ACTION_ID, response);
		bIntent.putExtras(bundle);
		sendBroadcast(bIntent);
	}

	private void sendPollerStatus(String url, boolean status) {
		PollerStatusResponse pollerStatus = new PollerStatusResponse(url, status);
		Intent bIntent = new Intent(PollerStatusResponse.ACTION_ID);
		Bundle bundle = new Bundle();
		bundle.putSerializable(PollerStatusResponse.ACTION_ID, pollerStatus);
		bIntent.putExtras(bundle);
		sendBroadcast(bIntent);
	}

	@RequiredArgsConstructor(suppressConstructorProperties=true)
	private class IntervalEventsLoaderThread extends Thread {

		private long sleepTime;
		private final AtomicBoolean cont;
		private final String url;
		private final EventMatcher matcher;
		private final EventParser parser;
		private final DateFormat df = new SimpleDateFormat("HH:mm:ss");
		private Thread runningThread;

		public void setPollTime(long pollTime) {
			sleepTime = pollTime;
		}

		public void pollNow() {
			runningThread.interrupt();
		}

		private boolean pollIsEnabled() {
			DateTime now = LocalTimeUtils.create(LocalTime.now());
			return pollIsEnabled(now);
		}

		private boolean pollIsEnabled(DateTime date) {
			for (Interval interval : pollPeriods) {
				if (interval.contains(date)) {
					return true;
				}
			}
			return false;
		}

		public void stopPoller() {
			cont.set(false);
			runningThread.interrupt();
		}

		@Override
		public void run() {

			runningThread = Thread.currentThread();
			while (cont.get()) {
				try {
					if (pollIsEnabled()) {

						EventsContainer events = processSyncAction(url, parser, false);

						if (matcher != null) {
							for (Event event : events.getEvents()) {

								DateMidnight eventDateMidnight = new DateMidnight(event.getDate());
								if (eventDateMidnight.equals(DateMidnight.now())) {
									DateTime eventDateTime = LocalTimeUtils.create(new LocalTime(event.getDate()));
									if (pollIsEnabled(eventDateTime)) {

										boolean matched = matcher.match(event);

										if (matched) {
											sendNotification(event);
										}
									}
								}
							}
						}
					}
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					logger.log(Level.WARNING, "Polling interrupted.");
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
