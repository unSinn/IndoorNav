package ch.hslu.bda.f12.inav;

import java.io.Serializable;

import android.location.Location;
import android.location.LocationManager;

/**
 * Antwort-Entität
 * 
 * @author fabian
 * 
 */
public class PositionResponse implements Serializable {

	private static final long serialVersionUID = 1L;
	private double x, y;
	private int z;
	private String msg;
	private String exception;

	public PositionResponse(double x, double y, int z, String msg) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.msg = msg;
		this.exception = "";
	}

	public PositionResponse() {
		msg = "";
		exception = "";
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

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
	 * Gibt eine zusätzliche Meldung zurück. Elemente sind undefiniert und
	 * sollten nicht zur Programmsteuerung verwendet werden.
	 * 
	 * @return
	 */
	public String getMessage() {
		return msg;
	}

	public void setMessage(String message) {
		this.msg = message;
	}

	public String toString() {
		String s = "PositionResponse: \n";
		s += "\t Position:" + " x:" + x + " y:" + y + " z:" + z + "\n";
		s += "\t Message:\n" + msg + "\n";
		s += "\t Exception:\n" + exception;
		return s;
	}

	public Location toLocation() {
		Location l = new Location(LocationManager.GPS_PROVIDER);
		l.setLatitude(x);
		l.setLongitude(y);
		l.setAltitude(z);
		l.setAccuracy(0.5f);
		// set the time in the location. If the time on this location
		// matches the time on the one in the previous set call, it will be
		// ignored
		l.setTime(System.currentTimeMillis());
		
		return l;
	}

	public String getException() {
		return exception;
	}

	public void setException(String exception) {
		this.exception = exception;
	}

}
