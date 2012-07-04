package ch.hslu.bda.f12.inav.serviceinterface;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;

/**
 * Kapselt eine Position
 * 
 * @author fabian
 * 
 */
public class Position implements Serializable {

	private static final long serialVersionUID = 7020390615238972619L;
	private double x, y;
	private double accuracy;
	private int z;

	public Position(Location l) {
		setX(l.getLatitude());
		setY(l.getLongitude());
		setAccuracy(l.getAccuracy());
	}

	public Position() {
		// Ein leerer Konstruktor wird offenbar von PositionResponse benötigt.
	}

	/**
	 * X-Koordinate der Position. Entspicht Latitude.
	 * 
	 * @return
	 */
	public double getX() {
		return x;
	}

	/**
	 * X-Koordinate der Position. Entspicht Latitude.
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * Y-Koordinate der Position. Entspicht Longitude.
	 * 
	 * @return
	 */
	public double getY() {
		return y;
	}

	/**
	 * X-Koordinate der Position. Entspicht Latitude.
	 */
	public void setY(double y) {
		this.y = y;
	}

	public int getZ() {
		return z;
	}

	public void setZ(int z) {
		this.z = z;
	}

	/**
	 * Genauigkeit der Position in Meter. Die effektive Position sollte sich
	 * innerhalb dieses Radius um den Punkt [x/y] befinden.
	 * 
	 * @return
	 */
	public double getAccuracy() {
		return accuracy;
	}

	/**
	 * Genauigkeit der Position in Meter
	 * 
	 * @param accuracy
	 */
	public void setAccuracy(double accuracy) {
		this.accuracy = accuracy;
	}

	/**
	 * Wandelt diese Position in einen JSON-String um.
	 * 
	 * { "x": 123, "y": 123, "z": 1, "accuracy": 5 }
	 * 
	 * @return JSON-String
	 * @throws JSONException
	 */
	public String toJSON() throws JSONException {
		JSONObject jso = new JSONObject();
		jso.put("x", getX());
		jso.put("y", getY());
		jso.put("z", getZ());
		jso.put("accuracy", getAccuracy());
		return jso.toString();
	}

}
