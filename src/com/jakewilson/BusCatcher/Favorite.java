package com.jakewilson.BusCatcher;

import com.google.android.maps.GeoPoint;

public class Favorite {

	private String name;
	private String stopId;
	private double lat;
	private double lng;
	private GeoPoint location;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStopId() {
		return stopId;
	}

	public void setStopId(String stopId) {
		this.stopId = stopId;
	}

	public boolean equals(Object o) {

		if (this == o)
			return true;

		if (o == null)
			return false;

		if (getClass() != o.getClass())
			return false;
		Favorite f = (Favorite) o;

		return name.equals(f.name) && stopId.equals(f.stopId);
	}

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

	public GeoPoint getLocation() {
		return location;
	}

	public void setLocation(double anyLat, double anyLng) {
		GeoPoint point = new GeoPoint((int) (anyLat * 1E6),
				(int) (anyLng * 1E6));
		this.location = point;
	}
}
