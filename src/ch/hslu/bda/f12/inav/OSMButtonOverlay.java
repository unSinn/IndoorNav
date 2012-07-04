package ch.hslu.bda.f12.inav;

import ch.hslu.bda.f12.inav.serviceinterface.CaptureImageActivity;
import android.content.Intent;
import android.widget.LinearLayout;

public class OSMButtonOverlay {
	private LinearLayout layout;
	private OSMMapActivity a;
	
	public OSMButtonOverlay(OSMMapActivity a) {
		this.a = a;
		layout = (LinearLayout) a.findViewById(R.id.osmButtonsLayout);
	}
	
	private void onCaptureButtonClick(){
		Intent i = new Intent(a, CaptureImageActivity.class); // explizit
		a.startActivityForResult(i, Constants.REQUESTCODE_CAPTURE_IMAGE);
	}

}
