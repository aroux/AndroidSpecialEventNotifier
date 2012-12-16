package org.asen.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.asen.R;
import org.asen.intent.EventAction;
import org.asen.intent.EventSearchRequest;
import org.asen.intent.EventSearchResponse;
import org.asen.intent.PollerStatusRequest;
import org.asen.intent.PollerStatusResponse;
import org.asen.service.dto.Event;
import org.asen.service.dto.EventsContainer;
import org.asen.service.matcher.EventMatcher;
import org.asen.service.matcher.EventMatchers;
import org.asen.service.parser.EventParser;
import org.asen.service.twitter.TweetService;
import org.asen.service.twitter.json.tcs.parser.TCSParser;

import android.app.ListActivity;
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

	private Menu mainMenu;

	/* TODO : put these variables in config*/
	private final EventMatcher matcher = EventMatchers.or( //
			EventMatchers.pattern(".*la Sarraz.*"), EventMatchers.pattern(".*Gland.*"), //
			EventMatchers.pattern(".*Aubonne.*"), EventMatchers.pattern(".*Villars-Sainte-Croix.*"), //
			EventMatchers.pattern(".*Lausanne-Crissier.*"), EventMatchers.pattern(".*Morges.*"), //
			EventMatchers.pattern(".*Rolle.*"), EventMatchers.pattern(".*Ecublens.*"), //
			EventMatchers.pattern(".*Cossonay.*")).get();
	private final EventParser parser = new TCSParser();
	private final String url = "http://search.twitter.com/search.json?q=tcstrafica1&rpp=20";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		listAdapter = new MyArrayAdapter(this, android.R.layout.simple_list_item_1);
		setListAdapter(listAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		this.mainMenu = menu;
		refreshPollingStatus();
		return true;
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		return super.onMenuOpened(featureId, menu);
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
		startService(intent);
	}

	private void onPoll() {

		Intent intent = new Intent(MainActivity.this, TweetService.class);
		Bundle bundle = new Bundle();
		EventSearchRequest request = new EventSearchRequest(url, EventAction.POLL, matcher, parser);
		bundle.putSerializable(EventSearchRequest.ACTION_ID, request);
		intent.putExtras(bundle);
		intent.setAction(EventSearchRequest.ACTION_ID);
		startService(intent);
	}

	private void refreshPollingStatus() {
		Intent intent = new Intent(MainActivity.this, TweetService.class);
		Bundle bundle = new Bundle();
		PollerStatusRequest request = new PollerStatusRequest(url);
		bundle.putSerializable(PollerStatusRequest.ACTION_ID, request);
		intent.putExtras(bundle);
		intent.setAction(PollerStatusRequest.ACTION_ID);
		startService(intent);
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
		intentFilter.addAction(PollerStatusResponse.ACTION_ID);
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
			if (intent.getAction().equals(EventSearchResponse.ACTION_ID)) {
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
			} else if (intent.getAction().equals(PollerStatusResponse.ACTION_ID)) {
				PollerStatusResponse pollerStatus = (PollerStatusResponse) intent.getExtras().getSerializable(PollerStatusResponse.ACTION_ID);
				MenuItem pollItem = mainMenu.findItem(R.id.menu_poll);
				if (pollerStatus.isEnabled()) {
					pollItem.setTitle(R.string.menu_poll_enabled);
				} else {
					pollItem.setTitle(R.string.menu_poll_disabled);
				}
			}
		}
	}
}
