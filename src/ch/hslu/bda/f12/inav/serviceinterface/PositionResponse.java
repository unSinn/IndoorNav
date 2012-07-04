package ch.hslu.bda.f12.inav.serviceinterface;

import java.io.Serializable;

import android.location.Location;
import android.location.LocationManager;

/**
 * Antwort-Entität. Wird einerseits verwendet um die Antwort des Servers in Java
 * abzubilden.
 * 
 * @author fabian
 */
public class PositionResponse extends Position implements Serializable {

	private static final long serialVersionUID = 5476318860915915814L;
	
	private String msg;
	private boolean exception;

	public PositionResponse() {
		super();
		msg = "";
		exception = false;
	}
	
	public PositionResponse(double x, double y) {
		super();
		this.setX(x);
		this.setY(y);
		msg = "";
		exception = false;
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
		s += "\t Position:" + " x:" + getX() + " y:" + getY() + " z:" + getZ() + "\n";
		s += "\t Message:\n" + msg + "\n";
		s += "\t Exception:\n" + exception;
		return s;
	}

	public Location toLocation() {
		Location l = new Location(LocationManager.GPS_PROVIDER);
		l.setLatitude(getX());
		l.setLongitude(getY());
		l.setAltitude(getZ());
		l.setAccuracy(0.5f);
		// set the time in the location. If the time on this location
		// matches the time on the one in the previous set call, it will be
		// ignored
		l.setTime(System.currentTimeMillis());

		return l;
	}

	public boolean hasException() {
		return exception;
	}

	public void setException(boolean exception) {
		this.exception = exception;
	}

}
