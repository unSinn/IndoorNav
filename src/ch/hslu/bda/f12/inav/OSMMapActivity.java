package ch.hslu.bda.f12.inav;

import java.util.ArrayList;
import java.util.Date;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.widget.Toast;

/**
 * Nutzt OpenStreetMap um Google-Maps-ähnliche Funktionalität zu erhalten.
 * 
 * @author fabian
 * 
 */
public class OSMMapActivity extends Activity implements OnMenuItemClickListener {

	private LocationManager locationManager;
	private LocationListener locationListener;
	private MapView view; // zeigt die Map an (Hintergrund)
	private MyLocationOverlay locProviderOverlay;
	private ItemizedIconOverlay<OverlayItem> locHistoryOverlay;
	private ArrayList<OverlayItem> locHistoryList;
	private boolean firstLocation = true;
	private MenuItem menu_capture;

	private static final int REQUESTCODE_OSMMENU = 300;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// View aufbauen
		view = new MapView(this, 256);
		view.setBuiltInZoomControls(true);
		view.setMultiTouchControls(true);
		view.setKeepScreenOn(true);

		// Overlays aufbauen
		// zeichnet eine Person und den Kreis
		locProviderOverlay = new MyLocationOverlay(getBaseContext(), view);
		locProviderOverlay.enableMyLocation();
		view.getOverlays().add(locProviderOverlay);

		// LocHistory
		locHistoryList = new ArrayList<OverlayItem>();

		locHistoryOverlay = new ItemizedIconOverlay<OverlayItem>(
				locHistoryList,

				new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {

					public boolean onItemSingleTapUp(final int index,
							final OverlayItem item) {
						Toast.makeText(OSMMapActivity.this, item.getTitle(),
								Toast.LENGTH_LONG).show();
						return true;
					}

					public boolean onItemLongPress(final int index,
							final OverlayItem item) {
						// Nix
						return false;
					}
				}, new DefaultResourceProxyImpl(this));
		view.getOverlays().add(locHistoryOverlay);

		// LocationManager
		locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		locationListener = new OSMMapActivity.InnerLocationListener();

		setContentView(view);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Register the listener with the Location Manager to receive location
		// updates
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				3000, 0, locationListener);
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 5000, 0, locationListener);

	}

	@Override
	protected void onPause() {
		locationManager.removeUpdates(locationListener);
		super.onPause();
	}

	@Override
	public boolean onTrackballEvent(final MotionEvent event) {
		return view.onTrackballEvent(event);
	}

	/**
	 * Wird aufgerufen, sobald der Menu-Knopf (Hardware-Button) in Android
	 * gedrückt wird. Baut anschliessend das Menu auf und hängt die Listeners
	 * darauf ein.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.osmmap, menu);
		menu_capture = menu.findItem(R.id.menu_item_settings);
		menu_capture.setOnMenuItemClickListener(this);

		super.onCreateOptionsMenu(menu);
		return true;
	}

	/**
	 * 
	 * @param item
	 * @return
	 */
	public boolean onMenuItemClick(MenuItem item) {
		if (item == menu_capture) {
			Intent i = new Intent(this, CaptureImageActivity.class);
			startActivityForResult(i, Constants.REQUESTCODE_CAPTURE_IMAGE);
			return true;
		}
		return false;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case Constants.REQUESTCODE_CAPTURE_IMAGE:
			onActivityResultPostionResponse(resultCode, data);
			break;
		default:
			return;
		}

	}

	private void onActivityResultPostionResponse(int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			Bundle b = data.getExtras();
			if (b != null) {
				PositionResponse res = (PositionResponse) b
						.getSerializable("PositionResponse");
				Location location = res.toLocation();
				locHistoryOverlay.addItem(new OverlayItem(new Date(location
						.getTime()).toString()
						+ "x:"
						+ location.getLatitude()
						+ " y:" + location.getLongitude(), "", new GeoPoint(
						location)));
			}
		}
	}

	private class InnerLocationListener implements LocationListener {

		public void onLocationChanged(Location location) {
			Log.i("OSMMap onLocationChanged", location.toString());

			// Das erste Mal, wenn eine gültige Position gefunden wird, springen
			// wir gleich dorthin und zoomen soweit möglich.
			if (firstLocation) {
				view.getController().setZoom(20);
				view.getController().setCenter(new GeoPoint(location));
				firstLocation = false;
			}
		}

		public void onProviderEnabled(String provider) {

		}

		public void onProviderDisabled(String provider) {

		}

		public void onStatusChanged(String provider, int status, Bundle extras) {

		}
	}

}
