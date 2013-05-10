package com.jakewilson.BusCatcher;

import com.google.android.maps.GeoPoint;

public class Stop {

	
	private double lat;
	private double lng;
	private String id; 
	private String name;
	private GeoPoint location; 
	private boolean isFavorite = false; 
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLng() {
		return lng;
	}
	public void setLng(double lng) {
		this.lng = lng;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
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
	public void setLocation(double anyLat, double anyLng) {
		GeoPoint point = new GeoPoint((int) (anyLat * 1E6), (int) (anyLng * 1E6));
		this.location = point; 
	}
	public boolean isFavorite() {
		return isFavorite;
	}
	public void setFavorite(boolean isFavorite) {
		this.isFavorite = isFavorite;
	} 
	
}
