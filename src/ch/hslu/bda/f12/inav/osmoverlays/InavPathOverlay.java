package ch.hslu.bda.f12.inav.osmoverlays;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.PathOverlay;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Stellt über der Map Linien dar. Damit könnte eine Route für die Navigatin
 * dargestellt werden. Allerdings hat Osmdroid aktuell noch ein Problem mit der
 * Darstellung der Lini.
 * 
 * Siehe http://code.google.com/p/osmdroid/issues/detail?id=221
 * 
 * @author fabian
 * 
 */
public class InavPathOverlay {

	private MapView mapView;
	private Activity a;

	public InavPathOverlay(MapView mapView, Activity a) {
		this.mapView = mapView;
		this.a = a;
		//addTestPath();
	}

	private void addTestPath() {
		PathOverlay p = new PathOverlay(Color.RED, a);
		
		/* WG Path
		p.addPoint(new GeoPoint(47.034646667928676d, 8.27481607209061d));
		p.addPoint(new GeoPoint(47.03474670839013d, 8.274672196083356d));
		p.addPoint(new GeoPoint(47.034701145430944d, 8.274333578813762d));
		p.addPoint(new GeoPoint(47.034697183432655d, 8.274535586541116d));
		p.addPoint(new GeoPoint(47.03473680340231d, 8.274516693732084d));
		*/
		
		p.addPoint(new GeoPoint(47.0144562685053,8.3060782313324));
		p.addPoint(new GeoPoint( 47.014488127723396,8.30608257804565));
		p.addPoint(new GeoPoint(47.01451850509968,8.30566855361515));
		p.addPoint(new GeoPoint(47.01454777109222,8.305673443667478));
		p.addPoint(new GeoPoint(47.0145466597257,8.305688657163612));
		p.addPoint(new GeoPoint(47.01458740981601,8.305695177233384));
		
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStrokeWidth(5.0f);
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(android.graphics.Color.RED);

		p.setPaint(paint);
		mapView.getOverlays().add(p);
	}

}
