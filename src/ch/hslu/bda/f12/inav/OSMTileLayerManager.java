package ch.hslu.bda.f12.inav;

import java.util.HashMap;

import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.TilesOverlay;

import android.content.Context;

/**
 * Handelt das Austauschen der verschiedenen Layer (Stockwerke A-F)
 */
public class OSMTileLayerManager implements LayerChangeListener {

	private MapView view;
	private Context context;
	private HashMap<Layer, TilesOverlay> layers;
	private HashMap<Layer, MapTileProviderBasic> providers;

	public OSMTileLayerManager(MapView view, Context context) {
		this.view = view;
		this.context = context;
		layers = new HashMap<Layer, TilesOverlay>(6);
		providers = new HashMap<Layer, MapTileProviderBasic>(6);
		setupLayers();
	}

	private void setupLayers() {
		MapTileProviderBasic tileProvider;
		String layerString;
		XYTileSource layertileSource;
		for (Layer l : Layer.values()) {
			// Tileprovider zusammensetzten, dieser holt die Tiles aus dem
			// Archiv
			tileProvider = new MapTileProviderBasic(
					context.getApplicationContext());
			tileProvider.setUseDataConnection(false);
			layerString = "layer_" + l.toString().toLowerCase();
			layertileSource = new XYTileSource(layerString,
					ResourceProxy.string.offline_mode, 10, 20, 256, ".png",
					layerString);
			tileProvider.setTileSource(layertileSource);
			providers.put(l, tileProvider);

			// Overlay zusammensetzen
			TilesOverlay o = new TilesOverlay(tileProvider, context.getApplicationContext());
			o.setEnabled(false);
			o.setOvershootTileCache(0); // Damit werden nicht mehr Tiles als
										// konfiguriert gecached
			layers.put(l, o);
			view.getOverlays().add(o);
		}
	}

	/**
	 * Hier wird der vorhergehende Layer disabled und der neue enabled. Es muss
	 * unbedingt mit clearTileCache() Heap (Speicher) freigegeben werden, da
	 * sonst OutOfMemory-Exceptions auftreten.
	 * 
	 * view.invalidate() sollte nicht aufgerufen werden, da osmdroid das
	 * eigentlich im Griff haben sollte.
	 * 
	 * Unter Hilfe der folgenden Artikel:
	 * http://code.google.com/p/osmdroid/issues/detail?id=251
	 * http://code.google.com/p/osmdroid/issues/detail?id=267
	 * 
	 */
	public void onLayerChanged(Layer lastLayer, Layer newLayer) {

		layers.get(lastLayer).setEnabled(false);
		providers.get(lastLayer).clearTileCache();

		// fix für osmdroid-Bug:
		// http://code.google.com/p/osmdroid/issues/detail?id=251
		providers.get(newLayer).setTileRequestCompleteHandler(
				view.getTileRequestCompleteHandler());
		
		layers.get(newLayer).setEnabled(true);
		lastLayer = newLayer;

	}
}
