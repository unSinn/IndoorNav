package ch.hslu.bda.f12.inav;
import java.io.File;

/**
 * Anfrage-Entität
 * @author fabian
 *
 */
public class PositionRequest{
	
	private File image;
	private String url;
	
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

}
