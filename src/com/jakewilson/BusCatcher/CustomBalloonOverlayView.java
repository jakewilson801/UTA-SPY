package com.jakewilson.BusCatcher;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.maps.OverlayItem;

public class CustomBalloonOverlayView<Item extends OverlayItem> extends
		BalloonOverlayView<CustomOverlayItem> {

	private TextView title;
	private TextView snippet;
	private ImageView image;
	private ToggleButton favs;
	private CustomOverlayItem currItem;
	protected ArrayList<Favorite> myFavs;

	public CustomBalloonOverlayView(Context context, int balloonBottomOffset) {
		super(context, balloonBottomOffset);
	}

	@Override
	protected void setupView(Context context, final ViewGroup parent) {

		// inflate our custom layout into parent
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.custom_balloon_overlay, parent);

		// setup our fields
		title = (TextView) v.findViewById(R.id.balloon_item_title);
		snippet = (TextView) v.findViewById(R.id.balloon_item_snippet);
		favs = (ToggleButton) v.findViewById(R.id.balloon_item_image);

		// implement balloon close
		ImageView close = (ImageView) v.findViewById(R.id.balloon_close);
		close.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				parent.setVisibility(GONE);
			}
		});

	}

	@Override
	protected void setBalloonData(CustomOverlayItem item, ViewGroup parent) {

		// map our custom item data to fields
		title.setText(item.getTitle());
		snippet.setText(item.getSnippet());
		currItem = item;
		if (item.isStop) {
			if (item.getStop().isFavorite())
				favs.setChecked(true);
			else
				favs.setChecked(false);

			favs.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton b, boolean isChecked) {
					currItem.getStop()
							.setFavorite(b.isChecked() ? true : false);
					JSONObject favs = new JSONObject();

					if (b.isChecked()) {

						try {
							favs.put("Id", currItem.getStop().getId());
							favs.put("Name", currItem.getStop().getName());
							favs.put("lat", currItem.getStop().getLat());
							favs.put("lng", currItem.getStop().getLng());

							JSONArray m = JSONSharedPreferences.loadJSONArray(
									BusCatcherActivity.myact, "prefs", "favs");
							m.put(favs);
							JSONSharedPreferences.saveJSONArray(
									BusCatcherActivity.myact, "prefs", "favs",
									m);

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						try {
							if (BusCatcherActivity.myact != null) {
								JSONArray m = JSONSharedPreferences
										.loadJSONArray(
												BusCatcherActivity.myact,
												"prefs", "favs");
								myFavs = new ArrayList<Favorite>();
								log("JSONARRY size: " + m.length());
								for (int i = 0; i < m.length(); i++) {
									Favorite f = new Favorite();
									f.setName(m.getJSONObject(i).getString(
											"Name"));
									f.setStopId(m.getJSONObject(i).getString(
											"Id"));
									f.setLat(m.getJSONObject(i)
											.getDouble("lat"));
									f.setLng(m.getJSONObject(i)
											.getDouble("lng"));
									myFavs.add(f);

								}
							} else {
								JSONArray m = JSONSharedPreferences
										.loadJSONArray(FavoriteMap.myact,
												"prefs", "favs");
								myFavs = new ArrayList<Favorite>();
								log("JSONARRY size: " + m.length());
								for (int i = 0; i < m.length(); i++) {
									Favorite f = new Favorite();
									f.setName(m.getJSONObject(i).getString(
											"Name"));
									f.setStopId(m.getJSONObject(i).getString(
											"Id"));
									f.setLat(m.getJSONObject(i)
											.getDouble("lat"));
									f.setLng(m.getJSONObject(i)
											.getDouble("lng"));
									myFavs.add(f);

								}
							}

							log("Size before: " + myFavs.size());
							Favorite f = new Favorite();
							f.setName(currItem.getStop().getName());
							f.setName(currItem.getStop().getId());
							log("CurrItem Name: "
									+ currItem.getStop().getName());

							for (Favorite r : myFavs) {

								if (r.getStopId().equals(
										currItem.getStop().getId())) {
									myFavs.remove(r);
									break;
								}

							}

							log("Size after: " + myFavs.size());
							JSONArray updated = new JSONArray();
							for (Favorite fs : myFavs) {
								JSONObject j = new JSONObject();
								j.put("Id", fs.getStopId());
								j.put("Name", fs.getName());
								j.put("lat", fs.getLat());
								j.put("lng", fs.getLng());
								updated.put(j);

							}
							if(BusCatcherActivity.myact != null){
							JSONSharedPreferences.saveJSONArray(
									BusCatcherActivity.myact, "prefs", "favs",
									updated);
							}else
								JSONSharedPreferences.saveJSONArray(
										FavoriteMap.myact, "prefs", "favs",
										updated);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
//					try {
//						log(JSONSharedPreferences.loadJSONArray(
//								BusCatcherActivity.myact, "prefs", "favs"));
//					} catch (JSONException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
				}

			});
		} else {
			favs.setClickable(false);
			favs.setVisibility(View.INVISIBLE);
		}
		// new FetchImageTask() {
		// protected void onPostExecute(Bitmap result) {
		// if (result != null) {
		// image.setImageBitmap(result);
		// }
		// }
		// }.execute(item.getImageURL());

	}

	public void log(Object o) {
		Log.d("JSON:", o.toString());
	}

}