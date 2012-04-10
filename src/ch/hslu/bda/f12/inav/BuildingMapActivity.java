package ch.hslu.bda.f12.inav;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * Stellt die aktuelle Position auf einer Karte dar.
 * 
 * @author fabian
 * 
 */
public class BuildingMapActivity extends Activity {

	private static Bitmap bm;
	private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private TouchImageView view;
	private LocationManager locationManager;
	private LocationListener locationListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		locationListener = new BuildingMapActivity.InnerLocationListener();

		view = new TouchImageView(this);
		view.setMaxZoom(10f);
		updatePostitionIndicator(0f, 0f);
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
		super.onPause();
		locationManager.removeUpdates(locationListener);
	}

	private void updatePostitionIndicator(float x, float y) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inMutable = true;
		options.inPurgeable = true;
		options.inScaled = true;

		// Bild laden
		bm = BitmapFactory.decodeResource(getResources(), R.drawable.layer_f,
				options);

		// Punkt zeichnen
		paint.setColor(0xFFFF0000);
		Canvas c = new Canvas(bm);
		c.drawCircle(x, y, 25, paint);
		c.drawBitmap(bm, 0, 0, paint);

		// Info zeichnen
		c.drawText("x:" + x + "\ny:" + y, 100, 100, paint);

		view.setImageBitmap(bm);
	}

	private class InnerLocationListener implements LocationListener {

		public void onLocationChanged(Location l) {
			// Log.i("StaticMap onLocationChanged", location.toString());
			updatePostitionIndicator((float) l.getLatitude(),
					(float) l.getLongitude());
		}

		public void onProviderEnabled(String provider) {

		}

		public void onProviderDisabled(String provider) {

		}

		public void onStatusChanged(String provider, int status, Bundle extras) {

		}
	}

}
