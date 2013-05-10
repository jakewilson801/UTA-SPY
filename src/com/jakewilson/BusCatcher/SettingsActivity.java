package com.jakewilson.BusCatcher;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class SettingsActivity extends Activity {

	private static final String PREFS = "prefs";
	protected RadioButton networkOn;
	protected RadioButton gpsOn;
	protected RadioGroup mRadioGroup;
	protected Spinner sp;
	protected EditText txtStop;
	protected EditText txtRoute;
	protected CheckBox satShow;
	protected CheckBox traffShow;

	// radio0 network
	// radio1 gps
	// editText1 minutes
	// editText2 miles
	@Override
	public void onCreate(Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.settings);

		networkOn = (RadioButton) findViewById(R.id.radio0);
		gpsOn = (RadioButton) findViewById(R.id.radio1);

		mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup1);
		txtStop = (EditText) findViewById(R.id.editText1);
		txtRoute = (EditText) findViewById(R.id.editText2);
		satShow = (CheckBox) findViewById(R.id.checkBox2);
		traffShow = (CheckBox) findViewById(R.id.checkBox1);
		satShow.setChecked(SettingsActivity.getSat(this));
		traffShow.setChecked(SettingsActivity.getTraffic(this));

		txtStop.setText(String.valueOf(SettingsActivity
				.getNumStops(getApplicationContext())));
		networkOn.setChecked(getGpsNetwork(this));
		traffShow.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				SettingsActivity.setTraffic(buttonView.isChecked(),
						getBaseContext());

			}
		});

		satShow.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				SettingsActivity.setSat(buttonView.isChecked(),
						getBaseContext());

			}
		});

		txtStop.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (txtStop.getText().toString().length() > 0) {
					int temp = Integer.parseInt(txtStop.getText().toString());
					if (temp > 75)
						SettingsActivity.setNumStops(75, getBaseContext());
					else
						SettingsActivity.setNumStops(
								Integer.parseInt(txtStop.getText().toString()),
								getBaseContext());
				} else
					SettingsActivity.setNumStops(20, getBaseContext());
				return false;
			}

		});

		txtRoute.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (txtStop.getText().toString().length() > 0) {
					SettingsActivity.setRoute(
							Integer.parseInt(txtRoute.getText().toString()),
							getBaseContext());
				} else
					SettingsActivity.setRoute(-1, getBaseContext());
				return false;
			}

		});
		if (networkOn.isChecked())
			gpsOn.setChecked(false);
		else
			gpsOn.setChecked(true);
		networkOn.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton network,
					boolean isChecked) {
				if (network.isChecked())
					storeGps(true, getApplicationContext());
				else
					storeGps(false, getApplicationContext());

			}
		});

		if ((SettingsActivity.getRoute(this) != -1)) {
			txtRoute.setText(String.valueOf(SettingsActivity.getRoute(this)));
		}
	}

	public static void storeGps(boolean network, Context c) {
		SharedPreferences location = c.getSharedPreferences(PREFS, 0);
		Editor editor = location.edit();

		editor.putBoolean("network", network);
		editor.commit();

	}

	public static boolean getGpsNetwork(Context c) {

		SharedPreferences location = c.getSharedPreferences(PREFS, 0);
		boolean gps = location.getBoolean("network", true);

		return gps;
	}

	public static void setTraffic(boolean traffic, Context c) {
		SharedPreferences location = c.getSharedPreferences(PREFS, 0);
		Editor editor = location.edit();

		editor.putBoolean("traffic", traffic);
		editor.commit();
	}

	public static boolean getTraffic(Context c) {
		SharedPreferences location = c.getSharedPreferences(PREFS, 0);
		boolean traff = location.getBoolean("traffic", false);

		return traff;
	}

	public static void setNumStops(int num, Context c) {
		SharedPreferences location = c.getSharedPreferences(PREFS, 0);
		Editor editor = location.edit();

		editor.putInt("numStops", num);
		editor.commit();
	}

	public static int getNumStops(Context c) {
		SharedPreferences location = c.getSharedPreferences(PREFS, 0);
		int gps = location.getInt("numStops", 20);

		return gps;
	}

	public static void setSat(boolean sat, Context c) {
		SharedPreferences location = c.getSharedPreferences(PREFS, 0);
		Editor editor = location.edit();

		editor.putBoolean("sat", sat);
		editor.commit();
	}

	public static boolean getSat(Context c) {
		SharedPreferences location = c.getSharedPreferences(PREFS, 0);
		boolean gps = location.getBoolean("sat", false);

		return gps;
	}

	public static void setRoute(int rNumber, Context c) {
		SharedPreferences location = c.getSharedPreferences(PREFS, 0);
		Editor editor = location.edit();

		editor.putInt("route", rNumber);
		editor.commit();
	}

	public static int getRoute(Context c) {
		SharedPreferences location = c.getSharedPreferences(PREFS, 0);
		int gps = location.getInt("route", -1);

		return gps;
	}

	@Override
	public void onResume() {
		super.onResume();
		log("Settings in onResume");

	}

	@Override
	public void onStart() {
		super.onStart();
		log("on Start");

	}

	@Override
	public void onPause() {
		super.onPause();
		log("In On pause");
//		if (txtRoute.getText().toString().length() > 0)
//			SettingsActivity.setRoute(
//					Integer.parseInt(txtRoute.getText().toString()), this);
//		else
//			SettingsActivity.setRoute(
//					Integer.parseInt(txtRoute.getText().toString()), this);
//		if (txtStop.getText().toString().length() > 0)
//			SettingsActivity.setNumStops(
//					Integer.parseInt(txtStop.getText().toString()), this);
//		else
//			SettingsActivity.setNumStops(20, this);
	}

	@Override
	public void onStop() {
		super.onStop();
		log("In on stop");

	}

	public void log(Object o) {
		Log.d("Settings Activity: ", o.toString());
	}
}
