package ch.hslu.bda.f12.inav;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

/**
 * Diese Klasse beinhaltet Methoden um vereinfacht auf Files des Android-Systems
 * zuzugreifen.
 * 
 * @author fabian
 * 
 */
public class Filemanager {

	private Activity a;

	/**
	 * 
	 * @param a
	 *            Activity, auf welcher im Fehlerfall Toasts dargestellt werden.
	 */
	public Filemanager(Activity a) {
		this.a = a;
	}

	/**
	 * Gibt ein File auf dem external Storage (SD-Karte) zurück. In dieses
	 * schreibt der Camera-Intent sein Bild.
	 * 
	 * @param dir
	 *            Verzeichnis auf dem external Storage (/mnt/sdcard/[dir])
	 * @param file
	 *            Dateiname
	 * @return
	 */
	public File getExternalStorageFile(String dir, String file) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			Log.d("IndoorNav", "MEDIA_MOUNTED");
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			Log.d("IndoorNav", "MEDIA_MOUNTED_READ_ONLY");
		} else {
			Log.d("IndoorNav", "MEDIA not MOUNTED");
		}

		// Temporärer Speicherort für das Foto
		File mediaStorageDir = new File(
				Environment.getExternalStorageDirectory(), dir);

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				String err = "failed to create directory";
				Log.d("IndoorNav", err);
				Toast.makeText(a, err, Toast.LENGTH_LONG).show();
				return null;
			}
		}

		// Create a media file name
		File cameraImageFile = new File(mediaStorageDir.getPath()
				+ File.separator + file);
		return cameraImageFile;
	}

	/**
	 * Kopiert ein File in ein Verzeichnis. Typischerweise wird dies verwendet,
	 * um Files, welche für den Betrieb der App notwendig sind, aus dem APK auf
	 * die SD-Card zu übertragen.
	 * 
	 * @param id
	 * @param to
	 */
	public void copyRawToFile(int id, File to) {

		to.delete();

		InputStream in = null;
		FileOutputStream out = null;

		try {
			in = a.getResources().openRawResource(id);
			out = new FileOutputStream(to);
			byte[] buff = new byte[1024];
			int read = 0;

			while ((read = in.read(buff)) > 0) {
				out.write(buff, 0, read);
			}

		} catch (Exception e) {
			Toast.makeText(a, "installTilesZip: " + e.getMessage(),
					Toast.LENGTH_LONG).show();
		} finally {
			try {
				in.close();
				out.close();
			} catch (Exception e) {
				Toast.makeText(a, "installTilesZip: " + e.getMessage(),
						Toast.LENGTH_LONG).show();
			}
		}
	}

}
