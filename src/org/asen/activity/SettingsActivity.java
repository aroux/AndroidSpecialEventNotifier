package org.asen.activity;

import org.asen.R;
import org.asen.time.settings.SettingsUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SettingsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_activity);
		final Button button = (Button) findViewById(R.id.buttonSave);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferences prefs = getSharedPreferences(SettingsUtils.SHARED_PREFERENCES_NAME, 0);
				Editor editor = prefs.edit();
				try {
					saveUrl(editor);
					savePollerTime(editor);
					savePollerInterval(editor);
					saveFilterPattern(editor);

					Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
					startActivity(intent);

					editor.commit();
				} catch (IllegalArgumentException e) {

					AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this) //
					.setMessage(e.getMessage()) //
					.setTitle(R.string.settings_error) //
					.setPositiveButton(R.string.settings_error_ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							// User clicked OK button
						}
					});

					AlertDialog dialog = builder.create();
					dialog.show();
				}
			}
		});
		initPreferences();
	}

	private void initPreferences() {
		SharedPreferences prefs = getSharedPreferences(SettingsUtils.SHARED_PREFERENCES_NAME, 0);

		String filterPattern = prefs.getString(SettingsUtils.FILTER_PATTERN_SETTINGS_KEY, null);
		if (filterPattern != null) {
			EditText filterPatternEdit = (EditText) findViewById(R.id.editTextFilterPattern);
			filterPatternEdit.getText().clear();
			filterPatternEdit.getText().append(filterPattern);
		}

		String urlStr = prefs.getString(SettingsUtils.URL_SETTINGS_KEY, null);
		if (urlStr != null) {
			EditText urlEdit = (EditText) findViewById(R.id.editTextUrl);
			urlEdit.getText().clear();
			urlEdit.getText().append(urlStr);
		}

		String pollerTime = prefs.getString(SettingsUtils.POLLER_TIME_SETTINGS_KEY, null);
		if (pollerTime != null) {
			EditText pollerTimeEdit = (EditText) findViewById(R.id.editTextPollTime);
			pollerTimeEdit.getText().clear();
			pollerTimeEdit.getText().append(pollerTime);
		}

		String pollerIntervalStr = prefs.getString(SettingsUtils.POLLER_INTERVAL_PATTERN_SETTINGS_KEY, null);
		if (pollerIntervalStr != null) {
			EditText pollerIntervalEdit = (EditText) findViewById(R.id.editTextIntervalPattern);
			pollerIntervalEdit.getText().clear();
			pollerIntervalEdit.getText().append(pollerIntervalStr);
		}
	}

	private void saveUrl(Editor prefsEditor) {
		EditText urlEdit = (EditText) findViewById(R.id.editTextUrl);
		Editable urlEditable = urlEdit.getText();
		prefsEditor.putString(SettingsUtils.URL_SETTINGS_KEY, urlEditable.toString());
	}

	private void savePollerTime(Editor prefsEditor) {
		EditText pollerTime = (EditText) findViewById(R.id.editTextPollTime);
		Editable pollerTimeEditable = pollerTime.getText();
		prefsEditor.putString(SettingsUtils.POLLER_TIME_SETTINGS_KEY, pollerTimeEditable.toString());
	}

	private void savePollerInterval(Editor prefsEditor) {
		EditText pollerIntervalPattern = (EditText) findViewById(R.id.editTextIntervalPattern);
		Editable pollerIntervalPatternEditable = pollerIntervalPattern.getText();
		// Just check given pattern is ok
		SettingsUtils.parseIntervals(pollerIntervalPatternEditable.toString());
		prefsEditor.putString(SettingsUtils.POLLER_INTERVAL_PATTERN_SETTINGS_KEY, pollerIntervalPatternEditable.toString());
	}

	private void saveFilterPattern(Editor prefsEditor) {
		EditText filterPattern = (EditText) findViewById(R.id.editTextFilterPattern);
		Editable filterPatternEditable = filterPattern.getText();
		// Just check given pattern is ok
		SettingsUtils.parseFilterPattern(filterPatternEditable.toString());
		prefsEditor.putString(SettingsUtils.FILTER_PATTERN_SETTINGS_KEY, filterPatternEditable.toString());
	}

}
