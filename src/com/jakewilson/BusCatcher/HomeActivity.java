package com.jakewilson.BusCatcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class HomeActivity extends Activity {
	protected ImageButton about;
	protected ImageButton nearby;
	protected ImageButton favorites;
	protected ImageButton settings;
	protected Context currentAct;

	@Override
	public void onCreate(Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.homes);
		// About imageButton3
		// Nearby imageButton1
		// Favorites imagButtom2
		// Settings imageButton4
		currentAct = this;
		about = (ImageButton) findViewById(R.id.imageButton3);
		nearby = (ImageButton) findViewById(R.id.imageButton1);
		favorites = (ImageButton) findViewById(R.id.imageButton2);
		settings = (ImageButton) findViewById(R.id.imageButton4);

		about.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(HomeActivity.this, AboutActivity.class);
				startActivity(i);

			}
		});
		nearby.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(HomeActivity.this,
						BusCatcherActivity.class);
				startActivity(i);

			}
		});
		favorites.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(HomeActivity.this,
						FavoritesActivity.class);
				startActivity(i);

			}
		});
		settings.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent i = new Intent(HomeActivity.this, SettingsActivity.class);
				startActivity(i);
			}
		});

	}
}
