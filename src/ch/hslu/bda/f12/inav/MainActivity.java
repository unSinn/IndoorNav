package ch.hslu.bda.f12.inav;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author fabian
 * 
 */
public class MainActivity extends Activity implements OnMenuItemClickListener {

	private MenuItem menu_settings;
	private TextView tv;
	private String service_url;

	private PositionResponse currentPosition;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		currentPosition = new PositionResponse(50d, 50d, 0, "OnCreate");

		// Restore preferences
		readPreferences();

		tv = (TextView) this.findViewById(R.id.textView);
		tv.setText("Uploading to: " + service_url);
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
	 * Wird aufgerufen, sobald der Menu-Knopf (Hardware-Button) in Android
	 * gedrückt wird. Baut anschliessend das Menu auf und hängt die Listeners
	 * darauf ein.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		menu_settings = menu.findItem(R.id.menu_item_settings);
		menu_settings.setOnMenuItemClickListener(this);

		super.onCreateOptionsMenu(menu);
		return true;
	}

	/**
	 * Wird aufgerufen, wenn die Kontrolle von einer andern Acitivity wieder
	 * dieser Activity übergeben wird. Typischerweise nach einem Aufruf von
	 * startActivityForResult() Hier wird dann das Routing anhand des
	 * requestCodes gemacht.
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case Constants.REQUESTCODE_PREFERENCES:
			doPreferencesResult(resultCode, data);
			break;
		case Constants.REQUESTCODE_CAPTURE_IMAGE:
			onActivityResultCaptureImage(resultCode, data);
			break;
		default:
			return;
		}

	}

	private void onActivityResultCaptureImage(int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			Bundle b = data.getExtras();
			if (b != null)
				currentPosition = (PositionResponse) b
						.getSerializable("PositionResponse");
			tv.setText(currentPosition.toString());
		}
	}

	private void doPreferencesResult(int resultCode, Intent data) {
		Toast.makeText(this, "Preferences safed.", Toast.LENGTH_LONG).show();
		readPreferences();
		tv.setText("Uploading to: " + service_url);
	}

	/**
	 * Wird aufgerufen, sobald der User den Button zur Positionsbestimmung
	 * gedrückt hat.
	 * 
	 * @param v
	 */
	public void onCaputureButtonPressed(View v) {
		Intent i = new Intent(this, CaptureImageActivity.class);
		startActivityForResult(i, Constants.REQUESTCODE_CAPTURE_IMAGE);
	}

	/**
	 * Wird aufgerufen, sobald der User den Button "Show Map" gedrückt hat.
	 * 
	 * @param v
	 */
	public void onShowBuildingMapButtonPressed(View v) {
		// PositionResponse anhängen
		Bundle bundle = new Bundle();
		bundle.putSerializable("PositionResponse", currentPosition);

		// Explicit Intent
		Intent i = new Intent(this, BuildingMapActivity.class);
		i.putExtras(bundle);

		startActivity(i);
	}

	/**
	 * Wird aufgerufen, sobald der User den Button "Show OSMMap" gedrückt hat.
	 * 
	 * @param v
	 */
	public void onShowOSMMapButtonPressed(View v) {
		// PositionResponse anhängen
		Bundle bundle = new Bundle();
		bundle.putSerializable("PositionResponse", currentPosition);

		// Explicit Intent
		Intent i = new Intent(this, OSMMapActivity.class);
		i.putExtras(bundle);

		startActivity(i);
	}

	/**
	 * 
	 * @param item
	 * @return
	 */
	public boolean onMenuItemClick(MenuItem item) {
		if (item == menu_settings) {
			startActivityForResult(new Intent(this,
					IndoorNavPreferenceActivity.class),
					Constants.REQUESTCODE_PREFERENCES);
			return true;
		}
		return false;
	}

	private void readPreferences() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		service_url = prefs.getString("service_url",
				"http://10.0.2.2:8080/upload"); // Default value
	}
}