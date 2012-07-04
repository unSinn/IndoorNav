package ch.hslu.bda.f12.inav;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Preferences Dialog. Liest /res/xml/preferences.xml. Die getätigten Einstellungen
 * können anschliessend als SharedPreferences verwendet werden.
 * 
 * @author fabian
 * 
 */
public class IndoorNaviPreferenceActivity extends PreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

}
