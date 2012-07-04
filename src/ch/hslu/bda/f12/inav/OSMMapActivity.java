package ch.hslu.bda.f12.inav;

import java.io.File;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MyLocationOverlay;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;
import ch.hslu.bda.f12.inav.LayerChangeListener.Layer;
import ch.hslu.bda.f12.inav.osmoverlays.InavLocHistoryOverlay;
import ch.hslu.bda.f12.inav.osmoverlays.InavPathOverlay;
import ch.hslu.bda.f12.inav.serviceinterface.CaptureImageActivity;
import ch.hslu.bda.f12.inav.serviceinterface.PositionResponse;

/**
 * Nutzt OpenStreetMap um Google-Maps-ähnliche Funktionalität zu erhalten. Dabei
 * werden unterschiedliche Layer übereinander gelegt. - mapView : Tiles.
 * Hintergrund wie Strassen, Bäume etc. - layerOverlay : grösstenteils
 * transparente Tiles. Zeichnet Räume. - locProviderOverlay : Gelbes Männchen
 * mit Accuracy Radius. Position nach Android LocationManager (beste geschätzte
 * Position) - locHistoryOverlay : Grüne Marker (Händchen) letzte Antworten des
 * Servers. - layerManager : Zeichnet für jede Ebene Textboxen am rechten Rand.
 * 
 * @author fabian
 * 
 */
public class OSMMapActivity extends Activity implements OnMenuItemClickListener {

	private boolean firstLocation = true;
	private String service_url;

	private LocationManager locationManager;
	private LocationListener locationListener;
	private MapView mapView;

	private MenuItem menu_preferences;

	private OSMTileLayerManager tileLayerManager;

	private MyLocationOverlay locProviderOverlay;
	private OSMTextViewOverlay layerTextOverlay;
	private InavLocHistoryOverlay locHistoryOverlay;
	private InavPathOverlay pathOverlay;
	private Filemanager fm;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.osmmaplayout);
		RelativeLayout layout = (RelativeLayout) findViewById(R.id.osmMapLayout);

		fm = new Filemanager(this);

		// Restore preferences
		readPreferences();

		copyTilesZip();

		setupMapView();

		// Die beiden Layer über dem Background aufbauen
		tileLayerManager = new OSMTileLayerManager(mapView, this);
		layerTextOverlay = new OSMTextViewOverlay(this);
		layerTextOverlay.addLayerListener(tileLayerManager);

		addMyLocationOverlay();
		addHistoryMarkerOverlay();
		addPathOverlay();

		// LocationManager
		locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		locationListener = new OSMMapActivity.InnerLocationListener();

		checkMockLocations();

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		layout.addView(mapView, params);
	}

	/**
	 * Überprüfen ob Mock Locations aktiviert ist, wenn nicht Dialog
	 * präsentieren und den User auffordern, die Einstellung zu setzten.
	 */
	private void checkMockLocations() {

		if (Settings.Secure.getInt(this.getContentResolver(),
				Settings.Secure.ALLOW_MOCK_LOCATION, 0) == 0) {

			String msg = "Mock locations are currently disabled in Settings - this App requires "
					+ "mock locations. Please activate it in the following options dialog.";

			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setTitle("Mock-Locations not activated");
			alertDialog.setMessage(msg);

			alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
				/**
				 * Settings Dialog öffnen. Android-Bug in 2.3.3, der String wird
				 * ohne "com" nicht gefunden und es gibt eine Exception.
				 * https://
				 * groups.google.com/group/android-developers/browse_thread
				 * /thread/2be3d37eac2d13c8?pli=1
				 */
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent();
					intent.setAction("settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS");
					try {
						startActivity(intent);
					} catch (ActivityNotFoundException e) {
						intent.setAction("com.android.settings.APPLICATION_DEVELOPMENT_SETTINGS");
						startActivity(intent);
					}
				}
			});
			alertDialog.show();
		}
	}

	/**
	 * Wird aufgerufen, wenn die App gestoppt wird. Typischerweise erst, wenn
	 * Android der Arbeitsspeicher ausgeht, oder der User die App bewusst
	 * beendet. Speichert die Einstellungen persistent ab.
	 */
	@Override
	protected void onStop() {
		super.onStop();
		// Preferences persistent speichern
		SharedPreferences settings = getSharedPreferences("PrefFileName", 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("service_url", service_url);
		editor.commit();
	}

	/**
	 * Um Ressourcen zu sparen, wurden in onPause() die Positionsabfrage an das
	 * AndroidSystem gestoppt. Nun muss sie wieder gestartet werden.
	 */
	@Override
	protected void onResume() {
		// Register the listener with the Location Manager to receive location
		// updates
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				3000, 0, locationListener);
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 5000, 0, locationListener);
		super.onResume();
	}

	/**
	 * Sobald die Activity in den Hintergrund kommt, wir diese Methode
	 * aufgerufen. Dies ist der richtige Zeitpunkt um etwas Ressourcen zu
	 * sparen, freizugeben.
	 */
	@Override
	protected void onPause() {
		locationManager.removeUpdates(locationListener);
		super.onPause();
	}

	@Override
	public boolean onTrackballEvent(final MotionEvent event) {
		return mapView.onTrackballEvent(event);
	}

	/**
	 * Wird aufgerufen, sobald der Menu-Knopf (Hardware-Button) in Android
	 * gedrückt wird. Baut anschliessend das Menu auf und hängt sich als
	 * Listener darauf ein.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.osmmapmenu, menu);

		menu_preferences = menu.findItem(R.id.menu_item_preferences);
		menu_preferences.setOnMenuItemClickListener(this);

		super.onCreateOptionsMenu(menu);
		return true;
	}

	/**
	 * Wenn ein Menuitem angeklickt wurde.
	 * 
	 * @param item
	 * @return
	 */
	public boolean onMenuItemClick(MenuItem item) {
		if (item == menu_preferences) {
			startActivityForResult(new Intent(this,
					IndoorNaviPreferenceActivity.class),
					Constants.REQUESTCODE_PREFERENCES);
			return true;
		}
		return false;
	}

	/**
	 * Startet den Intent um ein Bild aufzunehmen und zu übertragen
	 */
	public void startCaptureIntent(View view) {
		Intent i = new Intent(this, CaptureImageActivity.class); // explizit
		startActivityForResult(i, Constants.REQUESTCODE_CAPTURE_IMAGE);
	}

	/**
	 * Programmkontrolle kehrt nach startActivityForResult wieder zu dieser Activity zurück. 
	 * Wird nach startActivityForResult() aufgerufen. Routing für die
	 * verschiedenen RequestCodes.
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case Constants.REQUESTCODE_CAPTURE_IMAGE:
			onActivityResultPostionResponse(resultCode, data);
			break;
		case Constants.REQUESTCODE_PREFERENCES:
			doPreferencesResult(resultCode, data);
			break;
		default:
			return;
		}

	}

	/**
	 * Programmkontrolle kehrt nach CaptureImageActivity wieder zurück.
	 * In den Extras des Intents befindet sich womöglich eine PositionResponse. 
	 * @param resultCode
	 * @param data
	 */
	private void onActivityResultPostionResponse(int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			Bundle b = data.getExtras();
			if (b != null) {
				PositionResponse res = (PositionResponse) b
						.getSerializable("PositionResponse");

				if (!res.hasException()) {
					// Punkt auf der Karte erstellen
					locHistoryOverlay.addLocation(res);

					// Layer wechseln
					// 1 = A ... 6 = F
					// Array beginnt aber bei 0
					Layer[] layers = Layer.values();
					int newlayer = res.getZ() - 1;
					if (newlayer <= layers.length && newlayer > 0) {
						layerTextOverlay.changeLayer(Layer.values()[newlayer]);
					}

					// Zur Antwort Scrollen
					scrollTo(res.toLocation(), 20);
				}

				// Message oder Exception darstellen
				if (res.getMessage().length() > 0) {
					Toast.makeText(this, res.getMessage(), Toast.LENGTH_LONG)
							.show();
				}
			}
		}
	}
	
	/**
	 * User hat Einstellungen in IndoorNaviPreferencesActivity vorgenommen.
	 * @param resultCode
	 * @param data
	 */
	private void doPreferencesResult(int resultCode, Intent data) {
		Toast.makeText(this, "Preferences safed.", Toast.LENGTH_LONG).show();
		readPreferences();
	}

	/**
	 * Tiles.zip aus dem APK auf /mnt/sdcard/osmdroid kopieren.
	 */
	private void copyTilesZip() {
		File tilesZip = fm.getExternalStorageFile("osmdroid", "tiles.zip");
		fm.copyRawToFile(R.raw.tiles, tilesZip);
	}

	private void setupMapView() {
		// Offline Tilesource, damit osmdroid die Tiles aus dem tiles.zip liest.
		final ITileSource tileSource = new XYTileSource("background",
				ResourceProxy.string.offline_mode, 10, 20, 256, ".png",
				"background");
		final MapTileProviderBasic tileProvider = new MapTileProviderBasic(
				getApplicationContext());
		tileProvider.setUseDataConnection(false);
		tileProvider.setTileSource(tileSource);

		mapView = new MapView(getApplicationContext(), 256,
				new DefaultResourceProxyImpl(this), tileProvider);
		mapView.setBuiltInZoomControls(true);
		mapView.setMultiTouchControls(true);
		mapView.setKeepScreenOn(true);
		mapView.setUseDataConnection(false);
	}

	private void addMyLocationOverlay() {
		// Overlay MyLocation
		// zeichnet eine Person und einen Radius für die Accuracy
		locProviderOverlay = new MyLocationOverlay(getBaseContext(), mapView);
		locProviderOverlay.enableMyLocation();
		mapView.getOverlays().add(locProviderOverlay);
	}

	/**
	 * LocHistory zeichnet grüne Marker über der Karte
	 */
	private void addHistoryMarkerOverlay() {
		locHistoryOverlay = new InavLocHistoryOverlay(mapView, (Activity) this);
	}

	/**
	 * Zeichnet Pfade als Overlay über der Karte.
	 */
	private void addPathOverlay() {
		pathOverlay = new InavPathOverlay(mapView, (Activity) this);
	}

	private void readPreferences() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		service_url = prefs.getString("service_url",
				"http://147.88.219.190:8080/ims"); // Default value
	}

	/**
	 * Package Version auslesen.
	 * 
	 * @return
	 */
	public int getVersion() {
		int version = -1;
		try {
			PackageInfo pInfo = getPackageManager().getPackageInfo(
					getPackageName(), PackageManager.GET_META_DATA);
			version = pInfo.versionCode;
		} catch (Exception e) {
			Log.e(this.getClass().getSimpleName(), "Name not found", e);
		}
		return version;
	}

	public void scrollTo(Location location, int zoom) {
		mapView.getController().setZoom(zoom);
		mapView.getController().setCenter(new GeoPoint(location));
	}

	private class InnerLocationListener implements LocationListener {

		public void onLocationChanged(Location location) {
			Log.i("OSMMap onLocationChanged", location.toString());

			// Das erste Mal, wenn eine gültige Position gefunden wird,
			// springen
			// wir gleich dorthin und zoomen rein.
			if (firstLocation) {
				scrollTo(location, 18);
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
