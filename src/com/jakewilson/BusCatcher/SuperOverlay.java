package com.jakewilson.BusCatcher;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class SuperOverlay extends OverlayItem {
	public Stop s; 
	public Vehicle v; 
	public boolean isStop = false; 

	public SuperOverlay(GeoPoint point, String title, String snippet, Stop s) {
		super(point, title, snippet);
		this.s = s; 
		this.isStop = true; 
	}
	public SuperOverlay(GeoPoint point, String t, String s, Vehicle v){
		super(point, t, s);
		this.v = v; 
		
	}
	public Stop getStop(){
		return this.s; 
	}
}
