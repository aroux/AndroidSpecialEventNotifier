package org.asen.activity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.asen.R;
import org.asen.service.dto.Event;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MyArrayAdapter extends ArrayAdapter<Event> {

	DateFormat df = new SimpleDateFormat("E-HH:mm");

	public MyArrayAdapter(Context context, int activity) {
		super(context, activity);
	}

	private String firstToUpperCase(String str) {
		if (str.length() == 0) {
			return str;
		}
		return "" + Character.toUpperCase(str.charAt(0)) + str.substring(1);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Event event = getItem(position);

		LayoutInflater inflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.activity_main, parent, false);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.list_icon_view);
		imageView.setImageResource(event.getIcon());
		RelativeLayout textLayout = (RelativeLayout) rowView.findViewById(R.id.list_text_layout);
		TextView categoryView = (TextView) textLayout.findViewById(R.id.list_category_view);
		categoryView.setText(firstToUpperCase(event.getCategory()));
		TextView textView = (TextView) textLayout.findViewById(R.id.list_text_view);
		textView.setText(event.getText());
		RelativeLayout titleLayout = (RelativeLayout) textLayout.findViewById(R.id.list_title_layout);
		TextView titleView = (TextView) titleLayout.findViewById(R.id.list_title_view);
		titleView.setText(event.getTitle());
		TextView dateView = (TextView) titleLayout.findViewById(R.id.list_date_view);
		dateView.setText(df.format(event.getDate()) + " - ");
		return rowView;
		//return super.getView(position, convertView, parent);
	}
}
