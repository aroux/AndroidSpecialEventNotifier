package org.asen.service.twitter;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.asen.service.EventService;
import org.asen.service.dto.Event;
import org.asen.service.dto.EventsContainer;
import org.asen.service.dto.converter.EventConverter;
import org.asen.service.parser.EventParser;
import org.asen.service.twitter.json.TweetEntry;
import org.asen.service.twitter.json.TwitterResult;
import org.asen.service.twitter.utils.HtmlManipulator;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TweetService extends EventService {

	Logger logger = LogManager.getLogManager().getLogger("");

	Gson gson = new GsonBuilder().setDateFormat("EEE, dd MMM yyyy HH:mm:ss ZZZZZ").create();

	private static EventConverter<TwitterResult> tweetsConverter = new EventConverter<TwitterResult>() {
		@Override
		public EventsContainer convert(TwitterResult t, EventParser parser) {
			List<Event> events = new ArrayList<Event>();
			for (TweetEntry entry : t.getTweets()) {
				Event event = parser.parse(entry.getText());
				event.setDate(entry.getDate());
				events.add(event);
			}
			return new EventsContainer(events);
		}
	};

	public TweetService() {
		super(TweetService.class.getSimpleName());
	}

	@Override
	protected EventsContainer processSyncAction(String accessData, EventParser parser, boolean refresh) {
		try {
			//refresh = true;
			SharedPreferences tweetServiceSettings = getSharedPreferences("TWEET_SERVICE", 0);
			String maxId = tweetServiceSettings.getString("maxId", null);
			TwitterResult result = getLastTweets(accessData + (refresh || maxId == null ? "" : "&since_id=" + maxId));
			if (!refresh) {
				tweetServiceSettings.edit().putString("maxId", result.getMaxIdStr()).commit();
			}
			return tweetsConverter.convert(result, parser);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Impossible to dl data : " + e.getClass().getSimpleName() + " : " + e.getMessage());
		}
		return null;
	}

	private TwitterResult getLastTweets(String urlStr) throws IOException {
		TwitterResult result = gson.fromJson(downloadLastTweetsRaw(urlStr), TwitterResult.class);
		for (TweetEntry entry : result.getTweets()) {
			entry.setText(HtmlManipulator.replaceHtmlEntities(entry.getText()));
		}
		return result;
	}

	private String downloadLastTweetsRaw(String urlStr) throws IOException {
		URL url = new URL(urlStr);
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		StringBuilder strBuilder = new StringBuilder();

		try {
			InputStream in = new BufferedInputStream(urlConnection.getInputStream());
			InputStreamReader isr = new InputStreamReader(in);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				strBuilder.append(line);
			}
		} finally {
			urlConnection.disconnect();
		}

		urlConnection.disconnect();

		return strBuilder.toString();
	}
}
