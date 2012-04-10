package ch.hslu.bda.f12.inav;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Preferences Dialog.
 * @author fabian
 *
 */
public class IndoorNavPreferenceActivity extends PreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

}
