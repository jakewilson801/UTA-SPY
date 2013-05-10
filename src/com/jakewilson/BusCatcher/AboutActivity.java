package com.jakewilson.BusCatcher;




import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class AboutActivity extends Activity{

	protected Button bo; 
	@Override
	public void onCreate(Bundle b)
	{
		super.onCreate(b); 
		setContentView(R.layout.about); 
		bo = (Button) findViewById(R.id.button1);
		bo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				  Intent intent = new Intent(Intent.ACTION_VIEW);
				  intent.setData(Uri.parse("market://details?id=com.jakewilson.BusCatcher"));
				  startActivity(intent);
				
			}
		});
		
	}
}
