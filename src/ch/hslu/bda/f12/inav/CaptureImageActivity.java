package ch.hslu.bda.f12.inav;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

public class CaptureImageActivity extends Activity {

	private String service_url;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		readPreferences();
		capture();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case Constants.REQUESTCODE_CAPTURE_IMAGE:
			onActivityResultCaptureImage(resultCode, data);
			break;
		default:
			return;
		}

	}

	private void readPreferences() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		service_url = prefs.getString("service_url",
				"http://10.0.2.2:8080/upload"); // Default value
	}

	private void capture() {
		// create Intent to take a picture and return control to the calling
		// application
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		try {
			// create a file to save the image
			// set the image
			// file name
			File f = getOutputMediaFile();
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));

			// start the image capture Intent
			startActivityForResult(intent, Constants.REQUESTCODE_CAPTURE_IMAGE);
		} catch (NullPointerException npx) {
			Toast.makeText(this, "SD Storage not Mounted", Toast.LENGTH_LONG)
					.show();
		}
	}

	/**
	 * Wird aufgerufen, sobald ein Intent die Kontrolle wieder an die Activity
	 * zurückgibt. In unserem Fall: Sobald der ACTION_IMAGE_CAPTURE-Intent
	 * abgeschlossen ist. Intent data ist aus einem unerfindlichen Grund null?.
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 *      android.content.Intent)
	 * 
	 */
	private void onActivityResultCaptureImage(int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			// Bild erfolgreich aufgenommen und auf SD-Card gespeichert

			// Anfrage an Server zur Positionsbestimmung vorbereiten
			PositionRequest req = new PositionRequest();

			File img;
			try {
				img = compressImage(getOutputMediaFile(), 1280, 960, 75);
				req.setImage(img);

			} catch (Exception e) {
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
			// Über 10.0.2.2 kann das Interface der Host-Maschine angesprochen
			// werden.
			req.setUrl(service_url);
			RestPosterAsyncTask poster = new RestPosterAsyncTask(this);

			poster.execute(req); // ist non-blocking
		} else if (resultCode == RESULT_CANCELED) {
			// User hat BACK-Button oder so gedrückt und kein Bild aufgenommen.
			Toast.makeText(this, "Take Picture canceled", Toast.LENGTH_LONG)
					.show();
			onUploadCanceled();
		}
	}

	/**
	 * Gibt ein File auf dem external Storage (SD-Karte) zurück. In dieses
	 * schreibt der Camera-Intent sein Bild.
	 * 
	 * @return
	 */
	private File getOutputMediaFile() {
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
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"IndoorNav");

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				String err = "failed to create directory";
				Log.d("IndoorNav", err);
				Toast.makeText(this, err, Toast.LENGTH_LONG).show();
				return null;
			}
		}

		// Create a media file name
		File cameraImageFile = new File(mediaStorageDir.getPath()
				+ File.separator + "IMG_IndoorNav.jpg");
		return cameraImageFile;
	}

	/**
	 * 
	 * Komprimiert ein Bild. Grösse und Qualität können angepasst werden.
	 * sourceImg wird dabei überschrieben.
	 * 
	 * @param sourceImg
	 *            zu komprimierendes File
	 * @param with
	 * @param height
	 * @param quality
	 *            zwischen 0 und 100
	 * @return
	 * @throws FileNotFoundException
	 */
	private File compressImage(final File sourceImg, int with, int height,
			int quality) throws Exception {

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inTempStorage = new byte[32 * 1024];
		options.inPurgeable = true;
		options.inDither = false;
		options.inSampleSize = 2;

		Bitmap bitmapOrg = BitmapFactory.decodeFile(sourceImg.toString(),
				options);
		Bitmap scaledImg = Bitmap.createScaledBitmap(bitmapOrg, with, height,
				true);
		OutputStream os = new FileOutputStream(sourceImg);
		scaledImg.compress(CompressFormat.JPEG, quality, os);

		bitmapOrg.recycle();
		scaledImg.recycle();

		return sourceImg;
	}

	public void onUploadCanceled(){
		Intent i = new Intent();
		setResult(Activity.RESULT_CANCELED, i);
		finish();
	}
	
	public void onUploadComplete(PositionResponse r) {
		Intent i = new Intent();
		Bundle b = new Bundle();
		b.putSerializable("PositionResponse", r);
		i.putExtras(b);
		i.setClass(this, PositionResponse.class);

		if (getParent() == null) {
			setResult(Activity.RESULT_OK, i);
		} else {
			getParent().setResult(Activity.RESULT_OK, i);
		}
		finish();
	}

}
