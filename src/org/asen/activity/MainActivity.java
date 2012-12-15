package org.asen.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.asen.R;
import org.asen.intent.EventAction;
import org.asen.intent.EventSearchRequest;
import org.asen.intent.EventSearchResponse;
import org.asen.service.dto.Event;
import org.asen.service.dto.EventsContainer;
import org.asen.service.matcher.EventMatcher;
import org.asen.service.matcher.EventMatchers;
import org.asen.service.parser.EventParser;
import org.asen.service.twitter.TweetService;
import org.asen.service.twitter.json.tcs.parser.TCSParser;

import android.app.ListActivity;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

public class MainActivity extends ListActivity {

	Logger logger = LogManager.getLogManager().getLogger("");

	private MyReceiver myReceiver;

	private final List<Event> allEvents = new ArrayList<Event>();
	private ArrayAdapter<Event> listAdapter;

	private boolean displayFiltered = false;

	/* TODO : put these variables in config*/
	private final EventMatcher matcher = EventMatchers.or( //
			EventMatchers.or(EventMatchers.pattern(".*Lausanne.*"), EventMatchers.pattern(".*Gland.*")).get(), //
			EventMatchers.pattern(".*Cossoney.*")).get();
	private final EventParser parser = new TCSParser();
	private final String url = "http://search.twitter.com/search.json?q=tcstrafica1&rpp=20";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_main);

		// Create a progress bar to display while the list loads
		//		ProgressBar progressBar = new ProgressBar(this);
		//		progressBar.setIndeterminate(true);
		//		LinearLayout layout = new LinearLayout(getBaseContext());
		//		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		//		layoutParams.weight = 1.0f;
		//		layoutParams.gravity = Gravity.CENTER;
		//		layout.setLayoutParams(layoutParams);
		//		layout.addView(progressBar);
		//
		//		getListView().setEmptyView(layout);
		//
		//		ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
		//		root.addView(layout);

		listAdapter = new MyArrayAdapter(this, android.R.layout.simple_list_item_1);
		setListAdapter(listAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_refresh :
			onRefresh();
			return true;
		case R.id.menu_poll :
			onPoll();
			return true;
		case R.id.menu_filter :
			onFilter();
			return true;
		default :
			return super.onOptionsItemSelected(item);
		}
	}

	private void onRefresh() {
		allEvents.clear();
		listAdapter.clear();
		Intent intent = new Intent(MainActivity.this, TweetService.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable(EventSearchRequest.ACTION_ID, new EventSearchRequest(url, EventAction.SYNC, null, parser));
		intent.putExtras(bundle);
		intent.setAction(EventSearchRequest.ACTION_ID);
		PendingIntent pendingIntent = PendingIntent.getService(MainActivity.this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
		try {
			pendingIntent.send();
		} catch (CanceledException e) {
			logger.log(Level.SEVERE, "Impossible to refresh.", e);
		}
	}

	private void onPoll() {

		Intent intent = new Intent(MainActivity.this, TweetService.class);
		Bundle bundle = new Bundle();
		EventSearchRequest request = new EventSearchRequest(url, EventAction.POLL, matcher, parser);
		bundle.putSerializable(EventSearchRequest.ACTION_ID, request);
		intent.putExtras(bundle);
		intent.setAction(EventSearchRequest.ACTION_ID);
		PendingIntent pendingIntent = PendingIntent.getService(MainActivity.this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
		try {
			pendingIntent.send();
		} catch (CanceledException e) {
			logger.log(Level.SEVERE, "Impossible to refresh.", e);
		}
	}

	private void onFilter() {
		displayFiltered = !displayFiltered;
		listAdapter.clear();
		for (Event event : allEvents) {
			if (displayFiltered) {
				if (matcher.match(event)) {
					listAdapter.add(event);
				}
			} else {
				listAdapter.add(event);
			}
		}
	}

	@Override
	protected void onStart() {

		myReceiver = new MyReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(EventSearchResponse.ACTION_ID);
		registerReceiver(myReceiver, intentFilter);
		super.onStart();
	}

	@Override
	protected void onStop() {
		unregisterReceiver(myReceiver);
		super.onStop();
	}

	private class MyReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			EventSearchResponse response = (EventSearchResponse) intent.getExtras().getSerializable(EventSearchResponse.ACTION_ID);
			EventsContainer result = response.getEvents();

			for (Event event : result.getEvents()) {
				allEvents.add(event);
				if (displayFiltered) {
					if (matcher.match(event)) {
						listAdapter.add(event);
					}
				} else {
					listAdapter.add(event);
				}
			}
		}
	}
}
