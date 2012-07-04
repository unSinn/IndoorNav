package ch.hslu.bda.f12.inav;

/**
 * Enum für die Layers, laut Schnittstellenspezifikation ist A=1, F=8
 * 
 * @author fabian
 * 
 */
public interface LayerChangeListener {
	public enum Layer {
		A, B, C, D, E, F
	}

	/**
	 * Wird aufgerufen, sobald neuer Layer gewählt wurde.
	 * 
	 * @param lastLayer
	 * @param newLayer
	 */
	public void onLayerChanged(Layer lastLayer, Layer newLayer);

}
