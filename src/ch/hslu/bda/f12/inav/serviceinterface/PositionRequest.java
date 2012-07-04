package ch.hslu.bda.f12.inav.serviceinterface;

import java.io.File;
import java.io.Serializable;

/**
 * Anfrage-Entität
 * 
 * @author fabian
 * 
 */
public class PositionRequest implements Serializable {
	private static final long serialVersionUID = -6407321488158843887L;
	private File image;
	private String url;
	private Position lastKnownLocation;

	public File getImage() {
		return image;
	}

	public void setImage(File image) {
		this.image = image;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Position getLastKnownLocation() {
		return lastKnownLocation;
	}

	public void setLastKnownLocation(Position lastKnownLocation) {
		this.lastKnownLocation = lastKnownLocation;
	}
}
