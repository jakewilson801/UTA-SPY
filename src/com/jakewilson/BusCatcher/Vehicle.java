package com.jakewilson.BusCatcher;

import com.google.android.maps.GeoPoint;

public class Vehicle {

	private GeoPoint location;
	private GeoPoint stop; 
	private String lineRef; 
	private String name; 
	
	public String getLineRef() {
		return lineRef;
	}

	public void setLineRef(String lineRef) {
		this.lineRef = lineRef;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public GeoPoint getLocation() {
		return location;
	}

	public void setLocation(GeoPoint location) {
		this.location = location;
	}

	public GeoPoint getStop() {
		return stop;
	}

	public void setStop(GeoPoint stop) {
		this.stop = stop;
	} 
	
}
