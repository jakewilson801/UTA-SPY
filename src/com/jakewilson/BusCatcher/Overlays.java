package com.jakewilson.BusCatcher;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class Overlays extends  BalloonItemizedOverlay<OverlayItem>{
	protected Context c;
	private ArrayList<SuperOverlay> mOverlays = new ArrayList<SuperOverlay>();

	public Overlays(Drawable defaultMarker, Context context, MapView m) {
		super(boundCenterBottom(defaultMarker), m);
		this.c = m.getContext();
	}

	public void addOverlay(SuperOverlay overlay) {
		mOverlays.add(overlay);
		populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		// TODO Auto-generated method stub
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return mOverlays.size();
	}

	@Override
	protected boolean onBalloonTap(int index, OverlayItem i) {
//
//		if ((mOverlays.get(index).isStop)) {
//			Stop temp = new Stop();
//			temp = mOverlays.get(index).getStop();
//			if (!(temp.getName().equals("Current Location"))) {
//				log(mOverlays.get(index).getStop().getId());
//				BusCatcherActivity.setCurrentStop(mOverlays.get(index)
//						.getStop().getId());
//			}
//		}

		return true;
	}

	public void log(Object o) {
		Log.d("OVERLAYS: ", o.toString());
	}
}