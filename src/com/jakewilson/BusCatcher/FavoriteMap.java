package com.jakewilson.BusCatcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class FavoriteMap extends MapActivity {
	protected InputStream is;
	protected static final int CONTEXTMENU_DELETEITEM = 0;
	protected static final String PREFS = "prefs";
	protected static ProgressBar pBar;
	protected static ProgressBar pBarTwo;
	protected GeoPoint point = null;
	protected LocationManager lm;
	protected LocationListener ll;
	protected boolean hasLocation = false;
	protected double lat;
	protected double lng;
	protected static MapView mapView;
	protected static MapController mc;
	protected static String currentStop;
	protected static String url;
	protected static ArrayList<Vehicle> allBuses;
	protected static ArrayList<Stop> closeStops;
	protected static DataBaseHelper myDbHelper;
	// protected ImageView search;
	protected static ImageView refresh;
	protected static TextView tv;
	protected static MapActivity myact;

	protected static Drawable busIcon;
	protected static Overlays busOverlay;
	protected MapActivity closeApp;
	CustomItemizedOverlay<CustomOverlayItem> busStops;
	CustomItemizedOverlay<CustomOverlayItem> traxStops;
	CustomItemizedOverlay<CustomOverlayItem> bus;
	CustomItemizedOverlay<CustomOverlayItem> location;
	static CustomItemizedOverlay<CustomOverlayItem> currBus;
	static CustomItemizedOverlay<CustomOverlayItem> currTrax;
	protected static String CURRENTSTOP;
	protected static GeoPoint currLoc;
	protected static int numOverlays;
	protected static boolean stopSelected = false;
	protected ArrayList<Stop> favStops;
	protected static int numStops;
	protected static boolean fromRefresh = false;
	protected TextView routeFilterLbl;
	protected static int routeFilter; 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.favoritemap);
		mapView = (MapView) findViewById(R.id.mapView);
		routeFilterLbl = (TextView) findViewById(R.id.textView2);
		routeFilter = SettingsActivity.getRoute(this); 
		if(SettingsActivity.getRoute(this)!= -1){
			routeFilterLbl.setText("Route Filter: " +String.valueOf(SettingsActivity.getRoute(this)));
			routeFilterLbl.setBackgroundResource(R.color.ured); 
		}else{
			routeFilterLbl.setText("Route Filter: No Filter");
			routeFilterLbl.setBackgroundResource(R.color.ured); 
		}
		// search = (ImageView) findViewById(R.id.search);
		refresh = (ImageView) findViewById(R.id.refresh);
		tv = (TextView) findViewById(R.id.textView2);
		myact = this;
		closeApp = this;
		currLoc = null;
		// CONTROL TO THE Obvious refresh button built into the view
		refresh.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				GetVehicle getCloseBuses = new GetVehicle();
				if (stopSelected) {
					getCloseBuses.execute(CURRENTSTOP);
					fromRefresh = true;
				} else
					Toast.makeText(myact, "Select a stop.", Toast.LENGTH_SHORT)
							.show();
				return false;
			}

		});
		mapView.setBuiltInZoomControls(true);

		mc = mapView.getController();

		pBar = (ProgressBar) findViewById(R.id.progressDialog1);
		pBarTwo = (ProgressBar) findViewById(R.id.progressBar1);

		if (isNetworkAvailable(FavoriteMap.this)) {
			try {
				JSONArray m = JSONSharedPreferences.loadJSONArray(this,
						"prefs", "favs");
				favStops = new ArrayList<Stop>();
				log("JSONARRY size: " + m.length());
				for (int i = 0; i < m.length(); i++) {
					Stop f = new Stop();
					f.setName(m.getJSONObject(i).getString("Name"));
					f.setId(m.getJSONObject(i).getString("Id"));
					f.setLat(m.getJSONObject(i).getDouble("lat"));
					f.setLng(m.getJSONObject(i).getDouble("lng"));
					f.setLocation(f.getLat(), f.getLng());
					favStops.add(f);
					JSONArray temp = JSONSharedPreferences.loadJSONArray(this,
							"prefs", "favs");
					for (int x = 0; x < temp.length(); x++) {
						JSONObject test = temp.getJSONObject(x);
						String tempStop = test.getString("Id");
						for (Stop v : favStops) {
							if (v.getId().equals(tempStop))
								v.setFavorite(true);

						}
					}
				}

			} catch (Exception e) {

			}
			numStops = favStops.size();
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					FavoriteMap.this);
			builder.setMessage(
					"No network connection.  Please try again when your within coverage area.")
					.setTitle("Network Connection")
					.setCancelable(false)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									closeApp.finish();
									Toast.makeText(
											closeApp,
											"Sorry Network is required to use this application.",
											Toast.LENGTH_SHORT);
								}
							});
			AlertDialog alert = builder.create();
			alert.show();

		}
		plotPoints(favStops);
	}

	public static void setCurrentStop(Stop s) {
		stopSelected = true;
		String temp = s.getId();

		CURRENTSTOP = temp;
		currLoc = s.getLocation();
		GetVehicle getCloseBuses = new GetVehicle();
		getCloseBuses.execute(temp);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		CreateMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return MenuChoice(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, view, menuInfo);
		CreateMenu(menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		return MenuChoice(item);
	}

	private void CreateMenu(Menu menu) {
		menu.setQwertyMode(true);
		MenuItem mnu1 = menu.add(0, 0, 0, "Last Selected Stop");
		{
			mnu1.setAlphabeticShortcut('l');
			mnu1.setIcon(R.drawable.locationicon);
		}

	}

	private boolean MenuChoice(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			if (currLoc != null)
				mc.animateTo(currLoc);
			else
				Toast.makeText(this, "Select a stop.", Toast.LENGTH_SHORT)
						.show();
			return true;
		case 1:
			Intent i = new Intent(myact, HomeActivity.class);
			startActivity(i);
			return true;
		case 2:
			Intent x = new Intent(this, AboutActivity.class);
			startActivity(x);
			return true;
		}
		return false;
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public void plotPoints(ArrayList<Stop> closeStops) {

		List<Overlay> mapOverlays = mapView.getOverlays();

		Drawable signIcon = this.getResources().getDrawable(R.drawable.bustop);
		Drawable locationIcon = this.getResources().getDrawable(
				R.drawable.location);
		Drawable traxLocation = this.getResources().getDrawable(
				R.drawable.train);
		busStops = new CustomItemizedOverlay<CustomOverlayItem>(signIcon,
				mapView);
		location = new CustomItemizedOverlay<CustomOverlayItem>(locationIcon,
				mapView);
		traxStops = new CustomItemizedOverlay<CustomOverlayItem>(traxLocation,
				mapView);

		if (closeStops.size() > 0) {
			for (Stop s : closeStops) {
				// /FIX!!!!!!!
				if (s.getId().contains("TX")) {
					CustomOverlayItem traxItem = new CustomOverlayItem(
							s.getLocation(), s.getName(),
							"Tap to find nearest transit.", s);
					traxStops.addOverlay(traxItem);
					mapOverlays.add(traxStops);
				} else {
					CustomOverlayItem stopItem = new CustomOverlayItem(
							s.getLocation(), s.getName(),
							"Tap to find nearest transit.", s);
					busStops.addOverlay(stopItem);
					mapOverlays.add(busStops);
				}

			}
		} else {
			Toast.makeText(this, "No stops in favorites add some!.",
					Toast.LENGTH_SHORT).show();
		}
		mc.animateTo(closeStops.get(0).getLocation());
		mc.setZoom(15);

	}

	public void log(Object o) {
		Log.d("BUS", o.toString());

	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	private static class GetVehicle extends AsyncTask<String, Void, String> {
		Vehicle b;
		GeoPoint g;
		String s;
		boolean success;

		@Override
		protected void onPreExecute() {
			refresh.setVisibility(View.INVISIBLE);
			pBar.setVisibility(View.VISIBLE);
			pBarTwo.setVisibility(View.VISIBLE);
		}

		@Override
		protected String doInBackground(String... geo) {
			// TODO Build settings into minutes away
			if (routeFilter != -1) {
				url = "http://api.rideuta.com/SIRI/SIRI.svc/StopMonitor?stopid="
						+ geo[0]
						+ "&minutesout=60&onwardcalls=true&filterroute="
						+ routeFilter
						+ "&usertoken=UMBMO0302LX";
			} else {
				url = "http://api.rideuta.com/SIRI/SIRI.svc/StopMonitor?stopid="
						+ geo[0]
						+ "&minutesout=60&onwardcalls=true&filterroute=&usertoken=UMBMO0302LX";
			}
			DocumentBuilder db;
			logS("URL: " + url);
			try {
				db = DocumentBuilderFactory.newInstance().newDocumentBuilder();

				InputSource is = new InputSource();
				is.setCharacterStream(new StringReader(download(url)));
				Document doc;
				doc = db.parse(is);
				NodeList nodes = doc
						.getElementsByTagName("MonitoredVehicleJourney");
				logS("number of nodes = " + nodes.getLength());
				numOverlays = nodes.getLength();
				allBuses = new ArrayList<Vehicle>();

				if (!(nodes.getLength() == 0)) {
					// LOOKS GOOD WE HAVe ourselfs a bus or trax
					for (int i = 0; i < nodes.getLength(); i++) {
						double lat = 0;
						double lng = 0;

						b = new Vehicle();
						Element element = (Element) nodes.item((i));

						// / THIS CHECK IS DUE TO An API bug because the logic
						// above checking the number a
						// nodes will fail if the node has null data in it
						if (getTextValue(element, "Longitude") != null) {
							lng = Double.parseDouble(getTextValue(element,
									"Longitude"));
							lat = Double.parseDouble(getTextValue(element,
									"Latitude"));
							logS("lat:" + lat + ":");
							logS("lng:" + lng + ":");
							g = new GeoPoint((int) (lat * 1E6),
									(int) (lng * 1E6));
							b.setLocation(g);
							b.setLineRef(getTextValue(element, "LineRef"));
							b.setName(getTextValue(element, "PublishedLineName"));
							allBuses.add(b);
							success = true;
						} else
							success = false;
					}
				}

			} catch (ParserConfigurationException e) {
				logS("ParserConfig exception while downloading vehicle");
				e.printStackTrace();
			} catch (SAXException e) {
				logS("SAXException while downloading vehicle");
				e.printStackTrace();
			} catch (IOException e) {
				logS("IO exception while downloading vehicle");
				e.printStackTrace();
			}
			return s;

		}

		@Override
		protected void onPostExecute(String result) {
			refresh.setVisibility(View.VISIBLE);
			pBar.setVisibility(View.GONE);
			pBarTwo.setVisibility(View.GONE);
			if (success)
				updateMap();
			else
				Toast.makeText(myact, "No vehicles close to stop.",
						Toast.LENGTH_SHORT).show();
		}

		// TODO Download set timeout
		public String download(String URL) {
			String result = null;
			if (URL.startsWith("http://")) {
				try {
					String urlStr = URL;
					URL url = new URL(urlStr);
					URLConnection conn = url.openConnection();
					conn.setConnectTimeout(8000);
					BufferedReader rd = new BufferedReader(
							new InputStreamReader(conn.getInputStream()));
					StringBuilder sb = new StringBuilder();
					String line;
					while ((line = rd.readLine()) != null) {
						sb.append(line);
					}
					rd.close();
					result = sb.toString();
					logS(result);
				} catch (Exception e) {
					Toast.makeText(myact, "Connection timeout..",
							Toast.LENGTH_SHORT).show();
				}
			}
			return result;
		}

		private String getTextValue(Element ele, String tagName) {
			try {
				String textVal = null;
				NodeList nl = ele.getElementsByTagName(tagName);
				if (nl != null && nl.getLength() > 0) {
					Element el = (Element) nl.item(0);
					textVal = el.getFirstChild().getNodeValue();

				}
				return textVal;
			} catch (NullPointerException e) {

				return null;
			}

		}

	}

	public static void updateMap() {
		List<Overlay> mapOverlays = mapView.getOverlays();
		int overlaySize = mapOverlays.size();
		// mapOverlays = mapView.getOverlays(); if (busOverlay != null) {
		logS("#number of overlays" + mapOverlays.size());
		// Iterator<Overlay> myIt = mapOverlays.listIterator(overlaySize);
		if (mapOverlays.size() > numStops) {
			for (int i = overlaySize - 1; i > numStops - 1; i--) {
				mapOverlays.remove(i);
				logS("tried to remove a overlay");

			}
			logS("#number of overlays" + mapOverlays.size());
			mapView.invalidate();
		}

		// MURRAY SLC GeoPoint point = new GeoPoint((int) (40.760146 * 1E6),
		// (int) (-111.907770 * 1E6));

		pBarTwo.setVisibility(View.GONE); //
		refresh.setVisibility(View.VISIBLE); // plotPoints(closeStops);
		busIcon = myact.getResources().getDrawable(R.drawable.bus);
		Drawable trainIcon = myact.getResources()
				.getDrawable(R.drawable.train2);
		currTrax = new CustomItemizedOverlay<CustomOverlayItem>(trainIcon,
				mapView);
		currBus = new CustomItemizedOverlay<CustomOverlayItem>(busIcon, mapView);
		int index = 0;
		if (allBuses.size() > 0) {

			for (Vehicle r : allBuses) { //
				if (CURRENTSTOP.contains("TX")) {
					CustomOverlayItem traxItem = new CustomOverlayItem(
							r.getLocation(), r.getName(),r.getLineRef(), r);
					currTrax.addOverlay(traxItem);
					mapOverlays.add(currTrax);
					index++;
				} else {
					CustomOverlayItem traxItem = new CustomOverlayItem(
							r.getLocation(), r.getName(), r.getLineRef(), r);
					currBus.addOverlay(traxItem);
					mapOverlays.add(currBus);
					index++;
				}

			}
		} else {
			Toast.makeText(myact, "No vehicles close to stop.",
					Toast.LENGTH_SHORT).show();
		}

		// miles

		if (allBuses.size() > 0) {
			Location location = new Location("ClosestBus");
			location.setLatitude(allBuses.get(0).getLocation().getLatitudeE6() / 1E6);
			location.setLongitude(allBuses.get(0).getLocation()
					.getLongitudeE6() / 1E6); // =
			Location currentLoc = new Location("CurrentLocation");
			currentLoc.setLatitude(currLoc.getLatitudeE6() / 1E6);
			currentLoc.setLongitude(currLoc.getLongitudeE6() / 1E6);

			double distanceAway = location.distanceTo(currentLoc);
			logS("Distance away. " + distanceAway);
			// ///////////TODO Settings miles away from stop 16 093.44 meters =
			// 10
			if (distanceAway < 16093) {
				double latitudeSpan = Math.round(Math.abs(currLoc
						.getLatitudeE6()
						- allBuses.get(0).getLocation().getLatitudeE6()));
				double longitudeSpan = Math.round(Math.abs(currLoc
						.getLongitudeE6()
						- allBuses.get(0).getLocation().getLongitudeE6()));

				// double currentLatitudeSpan = (double)
				// mapView.getLatitudeSpan();
				// double currentLongitudeSpan = (double) mapView
				// .getLongitudeSpan();

				// double ratio = currentLongitudeSpan / currentLatitudeSpan;
				// if (longitudeSpan < (double) (latitudeSpan + 1E7) * ratio) {
				// longitudeSpan = ((double) (latitudeSpan + 1E7) * ratio);
				// }
				if (!fromRefresh) {
					mc.zoomToSpan((int) (latitudeSpan * 2),
							(int) (longitudeSpan * 2));
				} else {
					GeoPoint pointz = new GeoPoint(allBuses.get(0)
							.getLocation().getLatitudeE6(), allBuses.get(0)
							.getLocation().getLongitudeE6());
					mc.animateTo(pointz);
					fromRefresh = false;
				}

				// mapView.invalidate();
			} else
				Toast.makeText(myact,
						"No vehicles within 10 miles of this stop",
						Toast.LENGTH_SHORT).show();
		}
	}

	public void onPause() {
		super.onPause();
		log("Favorite onPause called");
	}

	public void onResume() {
		super.onResume();
		log("Favorite onResume called");
	}

	public void onStop() {
		super.onStop();
		log("Favorite onStop called");
		myact = null;
	}

	public void onDestroy() {
		super.onDestroy();
		log("Favorite ondestory called");
	}

	public static void logS(Object o) {
		Log.d("VEHICLE DL: ", o.toString());
	}

}
