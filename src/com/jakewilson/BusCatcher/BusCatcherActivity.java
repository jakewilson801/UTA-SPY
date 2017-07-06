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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
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
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class BusCatcherActivity extends MapActivity {
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
	protected static boolean fromRefresh = false;
	protected boolean gpsOn = false;
	protected Bundle activityState;
	protected static boolean fromGpsScreen;
	protected static boolean gpsNetworkOn = false;
	protected static boolean firstLoad = false;
	protected ImageView search;
	protected ProgressBar searchBar;
	protected TextView routeFilterLbl;
	public static int routeFilter = -1; 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activityState = savedInstanceState;
		setContentView(R.layout.action);
		mapView = (MapView) findViewById(R.id.mapView);

		// search = (ImageView) findViewById(R.id.search);
		refresh = (ImageView) findViewById(R.id.refresh);
		tv = (TextView) findViewById(R.id.textView2);
		search = (ImageView) findViewById(R.id.imageView2);
		searchBar = (ProgressBar) findViewById(R.id.progressBar2);
		pBar = (ProgressBar) findViewById(R.id.progressDialog1);
		pBarTwo = (ProgressBar) findViewById(R.id.progressBar1);
		routeFilterLbl = (TextView) findViewById(R.id.textView2);
		routeFilter = SettingsActivity.getRoute(this); 
		if(SettingsActivity.getRoute(this)!= -1){
			routeFilterLbl.setText("Route Filter: " +String.valueOf(SettingsActivity.getRoute(this)));
			routeFilterLbl.setBackgroundResource(R.color.ured); 
		}else{
			routeFilterLbl.setText("Route Filter: No Filter");
			routeFilterLbl.setBackgroundResource(R.color.ured); 
		}
		search.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mapView.getOverlays().clear();
				buildOptions();
			}
		});
		buildOptions();
	}

	public void buildOptions() {
		AlertDialog.Builder pick = new AlertDialog.Builder(this);

		pick.setTitle("UTA Spy");
		pick.setMessage("Enter Address or Use GPS Location");
		mapView.setBuiltInZoomControls(true);
		mapView.setTraffic(SettingsActivity.getTraffic(this));
		mapView.setSatellite((SettingsActivity.getSat(this)));
		mc = mapView.getController();
		myact = this;
		closeApp = this;
		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		input.setHint("800 North 1000 West, Clinton");
		pick.setView(input);
		myDbHelper = new DataBaseHelper(this);
		try {
			myDbHelper.createDataBase();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log("error Creating dbase");
		}

		// CONTROL TO THE Obvious refresh button built into the view
		refresh.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				GetVehicle getCloseBuses = new GetVehicle();
				if (stopSelected) {
					getCloseBuses.execute(CURRENTSTOP);
					log("TV:" + tv.getText().toString() + ":");
					fromRefresh = true;
				} else
					Toast.makeText(myact, "Select a stop.", Toast.LENGTH_SHORT)
							.show();
				return false;
			}

		});
		pick.setPositiveButton("Search", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();
				GetAddress letsDoThis = new GetAddress();
				letsDoThis.execute(value);
			}
		});

		pick.setNegativeButton("My Location",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
						ll = new MyLocationListener();
						// Reads the static method from settings shared pref

						if (SettingsActivity.getGpsNetwork(myact)) {
							lm.requestLocationUpdates(
									LocationManager.NETWORK_PROVIDER, 0, 0, ll);
							log("network gps is on");
							gpsOn = lm
									.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
							gpsNetworkOn = true;
						} else {
							lm.requestLocationUpdates(
									LocationManager.GPS_PROVIDER, 0, 0, ll);
							log("gps is on");
							gpsOn = lm
									.isProviderEnabled(LocationManager.GPS_PROVIDER);
							gpsNetworkOn = false;
						}

						ll.onProviderDisabled(LocationManager.NETWORK_PROVIDER);

						// TODO as of right now the phone only knows if the GPS
						// provider is
						// enabled not NETWORK

						if (!gpsOn) {
							firstLoad = true;
							buildAlertMessageNoGps();
						} else {

							if (isNetworkAvailable(BusCatcherActivity.this)) {
								GetTheBus log = new GetTheBus();
								GeoPoint temp = getLocation();
								point = temp;
								GeoPoint currLoc = new GeoPoint(temp
										.getLatitudeE6(), temp.getLongitudeE6());
								log.execute(currLoc);

							} else {
								AlertDialog.Builder builder = new AlertDialog.Builder(
										BusCatcherActivity.this);
								builder.setMessage(
										"No network connection.  Please try again when your within coverage area.")
										.setTitle("Network Connection")
										.setCancelable(false)
										.setPositiveButton(
												"OK",
												new DialogInterface.OnClickListener() {
													public void onClick(
															DialogInterface dialog,
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
						}
					}
				});

		pick.show();

	}

	public void onResume() {
		super.onResume();

	}

	public static void setCurrentStop(Stop s) {
		stopSelected = true;
		String temp = s.getId();
		//tv.setText(temp);
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
		MenuItem mnu1 = menu.add(0, 0, 0, "My Location");
		{
			mnu1.setAlphabeticShortcut('l');
			mnu1.setIcon(R.drawable.locationicon);
		}
		MenuItem mnu2 = menu.add(0, 1, 1, "Settings");
		{
			mnu2.setAlphabeticShortcut('s');
			mnu2.setIcon(R.drawable.gear);
		}
	}

	private boolean MenuChoice(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			// TODO Demo
			mc.animateTo(getLocation());
			// mc.animateTo(point);
			return true;
		case 1:
			Intent i = new Intent(BusCatcherActivity.this,
					SettingsActivity.class);
			startActivity(i);
		}
		return false;
	}

	private void buildAlertMessageNoGps() {

		String message = null;
		if (gpsNetworkOn)
			message = "Your GPS is disabled and required for nearby. Turn on?";
		else
			message = "Your Network GPS is disabled and required for nearby. Turn on?";
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message)

		.setCancelable(false)

		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

			public void onClick(
					@SuppressWarnings("unused") final DialogInterface dialog,
					@SuppressWarnings("unused") final int id) {

				Intent intent = new Intent(
						android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);

				startActivity(intent);

			}

		})

		.setNegativeButton("No", new DialogInterface.OnClickListener() {

			public void onClick(final DialogInterface dialog,
					@SuppressWarnings("unused") final int id) {
				// IF they say no prompt to use the shared prefs location
				// check for null in the shared pref
				// if shared != null
				// use prefs for location
				// else

				dialog.cancel();
				closeApp.finish();
				Toast.makeText(closeApp,
						"Sorry GPS is required to use nearby.",
						Toast.LENGTH_SHORT);
			}
		});
		final AlertDialog alert = builder.create();

		alert.show();

	}

	public class MyLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location loc) {
			storeLocation(loc.getLatitude(), loc.getLongitude());
			hasLocation = true;
			lm.removeUpdates(ll);
		}

		@Override
		public void onProviderDisabled(String provider) {
			// buildAlertMessageNoGps();
		}

		@Override
		public void onProviderEnabled(String provider) {
			Toast.makeText(getApplicationContext(), "Gps Enabled",
					Toast.LENGTH_SHORT).show();
			if (provider.equals(LocationManager.GPS_PROVIDER)) {
				// onCreate(activityState);
				SettingsActivity.storeGps(false, myact);
			}
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}

	}

	public void storeLocation(double lat2, double lng2) {
		SharedPreferences location = getSharedPreferences(PREFS, 0);
		Editor editor = location.edit();

		editor.putFloat("lat", (float) lat2);
		editor.putFloat("lng", (float) lng2);
		editor.commit();

	}

	public GeoPoint getLocation() {

		SharedPreferences location = getSharedPreferences(PREFS, 0);
		double lat = (double) location.getFloat("lat", 0);
		double lng = (double) location.getFloat("lng", 0);

		GeoPoint point = new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));
		return point;
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

	private class GetTheBus extends AsyncTask<GeoPoint, Void, GeoPoint> {
		GeoPoint g;

		@Override
		protected void onPreExecute() {
			refresh.setVisibility(View.INVISIBLE);
			pBar.setVisibility(View.VISIBLE);
			pBarTwo.setVisibility(View.VISIBLE);
		}

		@Override
		protected GeoPoint doInBackground(GeoPoint... geo) {

			long start = System.currentTimeMillis();
			// /// TODO Settings for GPS TIMEOUT
			// TODO fix for demo
			while (!hasLocation) {
				long end = System.currentTimeMillis();

				if (end - start > 15000) {

					break;
				}
			}

			long end = System.currentTimeMillis();

			log("found location in" + (end - start));
			try {

				myDbHelper.openDataBase();
				// slc lat 40.760146 lng -111.907770
				// point = new GeoPoint((int) (40.760146 * 1E6),
				// (int) (-111.907770 * 1E6));
				Cursor c = myDbHelper.getStops(getLocation(), getBaseContext());
				currLoc = getLocation();
				// currLoc = point;
				// Cursor c = myDbHelper.getStops(point);
				closeStops = new ArrayList<Stop>();

				for (c.moveToFirst(); c.moveToNext(); c.isAfterLast()) {
					Stop s = new Stop();
					s.setId(c.getString(c.getColumnIndex("stop_code")));
					s.setLat(c.getDouble(c.getColumnIndex("stop_lat")));
					s.setLng(c.getDouble(c.getColumnIndex("stop_lon")));
					s.setName(c.getString(c.getColumnIndex("stop_name")));

					// Below uses lat and lng to create geopoint.
					s.setLocation(s.getLat(), s.getLng());
					closeStops.add(s);
					log("Name: " + s.getName());
				}
				JSONArray temp = JSONSharedPreferences.loadJSONArray(
						BusCatcherActivity.myact, "prefs", "favs");
				for (int i = 0; i < temp.length(); i++) {
					JSONObject test = temp.getJSONObject(i);
					String tempStop = test.getString("Id");
					for (Stop v : closeStops) {
						if (v.getId().equals(tempStop))
							v.setFavorite(true);

					}
				}

				log("close stops: " + closeStops.size());
			} catch (Exception sqle) {

				log("Error in db: " + sqle.getMessage());

			}
			myDbHelper.close();

			return g;

		}

		@Override
		protected void onPostExecute(GeoPoint result) {
			pBar.setVisibility(View.GONE);

			pBarTwo.setVisibility(View.GONE);
			refresh.setVisibility(View.VISIBLE);
			plotPoints(closeStops);
		}

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
			Toast.makeText(this, "No stops near location.", Toast.LENGTH_SHORT)
					.show();
		}
		Stop s = new Stop();
		s.setName("Current Location");
		CustomOverlayItem loc = new CustomOverlayItem(getLocation(),
				s.getName(), "", s);
		location.addOverlay(loc);
		mapOverlays.add(location);
		mc.animateTo(getLocation());
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
					conn.setConnectTimeout(10000);
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
//					Toast.makeText(myact, "Connection timeout..",
//							Toast.LENGTH_SHORT).show();
					logS("Connection timeout: " + e.getMessage()); 
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
		if (mapOverlays.size() > (SettingsActivity.getNumStops(myact))) {
			for (int i = overlaySize - 1; i > ((SettingsActivity
					.getNumStops(myact)) - 1); i--) {
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
							r.getLocation(), r.getName(), "Line: "
									+ r.getLineRef(), r);
					currTrax.addOverlay(traxItem);
					mapOverlays.add(currTrax);
					index++;
				} else {
					CustomOverlayItem traxItem = new CustomOverlayItem(
							r.getLocation(), r.getName(), "Line: "
									+ r.getLineRef(), r);
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
			logS("Distance greater than 10 miles" + (distanceAway > 16093.44));
		//	if (distanceAway < 16093.44) {
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

				// mapView.invalidate();
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
//			} else
//				Toast.makeText(myact,
//						"No vehicles within 10 miles of this stop",
//						Toast.LENGTH_SHORT).show();
		}
	}

	private class GetAddress extends AsyncTask<String, Void, GeoPoint> {
		GeoPoint g;
		String result;
		InputStream is;

		@Override
		protected void onPreExecute() {
			refresh.setVisibility(View.INVISIBLE);
			pBar.setVisibility(View.VISIBLE);
			pBarTwo.setVisibility(View.VISIBLE);
		}

		@Override
		protected GeoPoint doInBackground(String... url) {
			try {
				long start = System.currentTimeMillis();
				// /// TODO Settings for GPS TIMEOUT
				// TODO Could branch the api right here to not read db for now
				// we
				// read the DB

				String catURL = "http://maps.googleapis.com/maps/api/geocode/json?address="
						+ url[0] + ",UT&sensor=false";
				StringBuilder b = new StringBuilder();
				for (int i = 0; i < catURL.length(); i++) {
					if (catURL.charAt(i) == ' ')
						b.append("%20");
					else
						b.append(catURL.charAt(i));
				}
				String tempz = b.toString();
				DefaultHttpClient httpclient = new DefaultHttpClient();
				HttpGet httppost = new HttpGet(tempz);

				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
				result = convertStreamToString();
				log("Result: " + result);
				JSONObject myAwway = new JSONObject(result);

				JSONObject location = myAwway.getJSONArray("results")
						.getJSONObject(0).getJSONObject("geometry")
						.getJSONObject("location");
				double lat = location.getDouble("lat");
				double lng = location.getDouble("lng");
				log("Lat: " + lat + " lng: " + lng);
				if (url[0].length() > 4)
					storeLocation(lat, lng);
				GeoPoint point = new GeoPoint((int) (lat * 1E6),
						(int) (lng * 1E6));

				long end = System.currentTimeMillis();

				log("found address in" + (end - start));

				myDbHelper.openDataBase();
				// slc lat 40.760146 lng -111.907770
				// point = new GeoPoint((int) (40.760146 * 1E6),
				// (int) (-111.907770 * 1E6));
				Cursor c = myDbHelper.getStops(point, getBaseContext());
				currLoc = point;

				// currLoc = point;
				// Cursor c = myDbHelper.getStops(point);
				closeStops = new ArrayList<Stop>();

				for (c.moveToFirst(); c.moveToNext(); c.isAfterLast()) {
					Stop s = new Stop();
					s.setId(c.getString(c.getColumnIndex("stop_code")));
					s.setLat(c.getDouble(c.getColumnIndex("stop_lat")));
					s.setLng(c.getDouble(c.getColumnIndex("stop_lon")));
					s.setName(c.getString(c.getColumnIndex("stop_name")));

					// Below uses lat and lng to create geopoint.
					s.setLocation(s.getLat(), s.getLng());
					closeStops.add(s);
					log("Name: " + s.getName());
				}
				JSONArray temp = JSONSharedPreferences.loadJSONArray(
						BusCatcherActivity.myact, "prefs", "favs");
				for (int i = 0; i < temp.length(); i++) {
					JSONObject test = temp.getJSONObject(i);
					String tempStop = test.getString("Id");
					for (Stop v : closeStops) {
						if (v.getId().equals(tempStop))
							v.setFavorite(true);

					}
				}

				log("close stops: " + closeStops.size());
			} catch (Exception sqle) {

				log("Error in db or getting location: " + sqle.getMessage());
				// TODO error handling close app if exception was caught
			}
			myDbHelper.close();

			return g;

		}

		@Override
		protected void onPostExecute(GeoPoint result) {
			pBar.setVisibility(View.GONE);

			pBarTwo.setVisibility(View.GONE);
			refresh.setVisibility(View.VISIBLE);
			plotPoints(closeStops);
		}

		private String convertStreamToString() {
			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is, "iso-8859-1"), 8);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				is.close();

				result = sb.toString();
				result.trim();
			} catch (Exception e) {
				Log.e("log_tag", "Error converting result " + e.toString());
			}

			return result;
		}

	}

	public void onPause() {
		super.onPause();
		log("Buscatcha onPause called");
	}

	public void onStop() {
		super.onStop();
		myact = null;
		log("buscatcha onStop called");
	}

	public void onDestroy() {
		super.onDestroy();
		// lm.removeUpdates(ll);
		log("buscatcha ondestory called");
	}

	public static void logS(Object o) {
		Log.d("VEHICLE DL: ", o.toString());
	}

}
