package com.jakewilson.BusCatcher;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class CustomItemizedOverlay<Item extends OverlayItem> extends
		BalloonItemizedOverlay<CustomOverlayItem> {

	private ArrayList<CustomOverlayItem> m_overlays = new ArrayList<CustomOverlayItem>();
	private Context c;

	public CustomItemizedOverlay(Drawable defaultMarker, MapView mapView) {
		super(boundCenter(defaultMarker), mapView);
		c = mapView.getContext();
	}

	public void addOverlay(CustomOverlayItem overlay) {
		m_overlays.add(overlay);
		populate();
	}

	@Override
	protected CustomOverlayItem createItem(int i) {
		return m_overlays.get(i);
	}

	@Override
	public int size() {
		return m_overlays.size();
	}

	@Override
	protected boolean onBalloonTap(int index, CustomOverlayItem item) {

		if ((m_overlays.get(index).isStop)) {
			Stop temp = new Stop();
			temp = m_overlays.get(index).getStop();
			if (!(temp.getName().equals("Current Location"))) {
				// log(m_overlays.get(index).getStop().getId());
				if (BusCatcherActivity.myact != null) {
					BusCatcherActivity.setCurrentStop(m_overlays.get(index)
							.getStop());
				}
				if (FavoriteMap.myact != null) {
					FavoriteMap.setCurrentStop(m_overlays.get(index).getStop());
				}

			}
		}

		return true;
	}

	@Override
	protected BalloonOverlayView<CustomOverlayItem> createBalloonOverlayView() {
		// use our custom balloon view with our custom overlay item type:
		return new CustomBalloonOverlayView<CustomOverlayItem>(getMapView()
				.getContext(), getBalloonBottomOffset());
	}

}
