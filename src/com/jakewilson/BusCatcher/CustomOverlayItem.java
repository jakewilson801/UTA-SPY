package com.jakewilson.BusCatcher;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class CustomOverlayItem extends OverlayItem {

	protected String mImageURL;
	protected Stop s; 
	protected Vehicle v; 
	public boolean isStop = false; 
	public CustomOverlayItem(GeoPoint point, String title, String snippet, String imageURL) {
		super(point, title, snippet);
		mImageURL = imageURL;
	}
	
	public CustomOverlayItem(GeoPoint point, String title, String snippet, Stop s) {
		super(point, title, snippet);
		this.s = s; 
		this.isStop = true; 
	}

	public CustomOverlayItem(GeoPoint point, String title, String snippet, Vehicle v) {
		super(point, title, snippet);
		this.v = v; 
		this.isStop = false; 
	}

	public Stop getStop(){
		return this.s; 
	}
	public String getImageURL() {
		return mImageURL;
	}

	public void setImageURL(String imageURL) {
		this.mImageURL = imageURL;
	}

}


