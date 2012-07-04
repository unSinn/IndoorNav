package ch.hslu.bda.f12.inav;

import java.util.HashMap;
import java.util.LinkedList;

import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Stellt die Knöpfe (A-F) als TextViews am rechten Bildschirmrand der
 * OSMMapActivity dar und kann diese einfärben, wenn sie ausgewählt wurden.
 * 
 * @author fabian
 * 
 */
public class OSMTextViewOverlay implements OnClickListener, LayerChangeListener {
	private LinearLayout layout;
	private LinkedList<LayerChangeListener> layerListeners;
	private HashMap<Layer, TextView> textViews;
	private Layer lastLayer;

	public OSMTextViewOverlay(OSMMapActivity a) {
		layout = (LinearLayout) a.findViewById(R.id.osmGridLayout);
		layerListeners = new LinkedList<LayerChangeListener>();
		textViews = new HashMap<LayerChangeListener.Layer, TextView>(6);
		createGridTextViews(a);
		addVersionText(a);

		// Hängt sich der Einfachheit halber selbst auch als Listener ein.
		addLayerListener(this);
	}
	private void addVersionText(OSMMapActivity a) {
		TextView tv = new TextView(a.getApplicationContext());
		tv.setTextSize(10);
		tv.setText("Version "+a.getVersion());
		tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
		layout.addView(tv);
	}
	/**
	 * Aufbauen der Textviews A-F.
	 * @param a
	 */
	private void createGridTextViews(OSMMapActivity a) {
		int i = 1;
		Layer[] layers = Layer.values();
		for (Layer l : layers) {

			// Reihenfolge umkehren, damit A zuunterst steht
			l = layers[layers.length - i];

			final TextView tv = new TextView(a.getApplicationContext());
			tv.setText(l.toString());

			tv.setBackgroundColor(Color.argb(100, 200, 200, 200));
			tv.setTextColor(Color.BLACK);
			tv.setTextSize(30f);
			tv.setWidth(75);
			tv.setHeight(75);
			tv.setGravity(Gravity.CENTER);
			tv.setPadding(5, 5, 5, 5);

			layout.addView(tv);

			tv.setOnClickListener(this);

			// Um später die ausgewählte TextView zu refernzieren (Farbe ändern,
			// wenn ausgewählt)
			textViews.put(l, tv);

			i++;
		}
	}

	public void addLayerListener(LayerChangeListener l) {
		layerListeners.add(l);
	}

	public void removeLayerListener(LayerChangeListener l) {
		layerListeners.remove(l);
	}

	public void onClick(View v) {
		TextView tv = (TextView) v;
		String text = tv.getText().toString();
		Layer newLayer = Layer.valueOf(text);
		changeLayer(newLayer);
	}

	/**
	 * Publiziert den Klick des Users an die Listener
	 * 
	 * @param newLayer
	 */
	public void changeLayer(Layer newLayer) {
		if (lastLayer == null) {
			lastLayer = Layer.A;
		}
		for (LayerChangeListener listener : layerListeners) {
			listener.onLayerChanged(lastLayer, newLayer);
		}
		lastLayer = newLayer;
	}

	/**
	 * LayerListener Impl
	 */
	public void onLayerChanged(Layer lastLayer, Layer newLayer) {
		textViews.get(lastLayer).setBackgroundColor(
				Color.argb(100, 200, 200, 200));
		textViews.get(newLayer).setBackgroundColor(
				Color.argb(150, 230, 100, 100));
	}

}
