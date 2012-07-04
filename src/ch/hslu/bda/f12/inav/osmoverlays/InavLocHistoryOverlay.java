package ch.hslu.bda.f12.inav.osmoverlays;

import java.util.ArrayList;
import java.util.Date;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.Activity;
import android.location.Location;
import android.widget.Toast;
import ch.hslu.bda.f12.inav.serviceinterface.PositionResponse;

/**
 * Stellt grüne Marker über der Karte dar. Diese können angeklickt werden. Dann
 * wird ein Toast mit dem Inhalt der PositionResponse dargestellt.
 * 
 * @author fabian
 * 
 */
public class InavLocHistoryOverlay {

	private ItemizedIconOverlay<OverlayItem> locHistoryOverlay;
	private ArrayList<OverlayItem> locHistoryList;
	private Activity a;

	public InavLocHistoryOverlay(MapView mapView, Activity a) {
		this.a = a;

		locHistoryList = new ArrayList<OverlayItem>();

		locHistoryOverlay = new ItemizedIconOverlay<OverlayItem>(
				locHistoryList, new ItemTapListener(),
				new DefaultResourceProxyImpl(a.getApplicationContext()));

		mapView.getOverlays().add(locHistoryOverlay);
		
		//addTestPoints();
	}

	private void addTestPoints() {
		ArrayList<PositionResponse> responses = new ArrayList<PositionResponse>();
		responses.add(new PositionResponse(47.01452145197068, 8.306252277728595));
		for(PositionResponse pos : responses){
			addLocation(pos);
		}
	}

	private class ItemTapListener implements OnItemGestureListener<OverlayItem> {

		public boolean onItemLongPress(int index, OverlayItem o) {
			// Nix
			return false;
		}

		public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
			Toast.makeText(a, item.getTitle(), Toast.LENGTH_LONG).show();
			return true;
		}

	}

	public void addLocation(PositionResponse res) {
		Location location = res.toLocation();
		locHistoryOverlay.addItem(new OverlayItem(new Date(location.getTime())
				.toString()
				+ "x:"
				+ res.getX()
				+ " y:"
				+ res.getY()
				+ " z:"
				+ res.getZ(), "", new GeoPoint(location)));
	}

}
