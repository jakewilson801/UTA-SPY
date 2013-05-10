package com.jakewilson.BusCatcher;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class FavoritesActivity extends ListActivity {
	protected ArrayList<Favorite> allFavs;
	protected ListActivity myact;
	protected Button mapIt;
	protected TextView routeFilterLbl;

	@Override
	public void onCreate(Bundle i) {
		super.onCreate(i);
		setContentView(R.layout.favorites);

		mapIt = (Button) findViewById(R.id.button2);

		mapIt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (allFavs.size() > 0) {
					Intent i = new Intent(FavoritesActivity.this,
							FavoriteMap.class);
					startActivity(i);
				} else {
					Toast.makeText(getApplicationContext(),
							"Select some favorites from nearby!",
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		updateList();

	}

	public void updateList() {
		JSONArray m;
		myact = this;
		try {
			m = JSONSharedPreferences.loadJSONArray(this, "prefs", "favs");

			allFavs = new ArrayList<Favorite>();

			for (int x = 0; x < m.length(); x++) {
				Favorite f = new Favorite();
				f.setName(m.getJSONObject(x).getString("Name"));
				f.setStopId(m.getJSONObject(x).getString("Id"));
				f.setLat(m.getJSONObject(x).getDouble("lat"));
				f.setLng(m.getJSONObject(x).getDouble("lng"));
				allFavs.add(f);

			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.setListAdapter(new ItemAdapter(this, R.layout.favrow, allFavs));
	}

	private class ItemAdapter extends ArrayAdapter<Favorite> {

		public ArrayList<Favorite> allFavs;
		private Holder holder;

		public ItemAdapter(Context context, int textViewResourceId,
				ArrayList<Favorite> myItems) {
			super(context, textViewResourceId, myItems);
			this.allFavs = myItems;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			View v = convertView;
			if (convertView == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				// LayoutInflater vi = getLayoutInflater();
				v = vi.inflate(R.layout.favrow, null);

				v.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View arg0, MotionEvent arg1) {
						return false;
					}

				});
				holder = new Holder();
				holder.name = (TextView) v.findViewById(R.id.textView1);
				holder.delete = (ImageButton) v
						.findViewById(R.id.deleteFavorite);
				v.setTag(holder);
				holder.delete.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						log("hit the delete");
						allFavs.remove(position);
						try {
							log("inside of try ");
							JSONArray updated = new JSONArray();
							for (Favorite fs : allFavs) {
								JSONObject j = new JSONObject();
								j.put("Id", fs.getStopId());
								j.put("Name", fs.getName());
								j.put("lat", fs.getLat());
								j.put("lng", fs.getLng());
								updated.put(j);

							}
							JSONSharedPreferences.saveJSONArray(myact, "prefs",
									"favs", updated);
							log(JSONSharedPreferences.loadJSONArray(myact,
									"prefs", "favs"));
						} catch (Exception e) {
							e.getMessage();
						}
						updateList();
					}

				});
			} else {
				holder = (Holder) convertView.getTag();
			}
			final Favorite o = allFavs.get(position);
			if (o != null) {
				holder.name.setTag(position);
				holder.delete.setTag(position);

				// first remove any listener already present
				// www holder.delete.setOnClickListener(null);
				holder.name.setText(o.getName());
				// you can now set checked state. we already removed the
				// listener, so it will not trigger the checkd change call

				// set the listener to the views now. ( so that check events
				// will only be triggerde manually by user and not by scrolling
				// the lsit

				return v;
			}
			return v;

		}

		class Holder {
			TextView name;
			ImageButton delete;

		}
	}

	public void onResume() {
		super.onResume();
		updateList();
	}

	public void onPause() {
		super.onPause();
		updateList();

	}

	public void log(Object o) {
		Log.d("JSON:", o.toString());
	}
}
