package ch.hslu.bda.f12.inav.serviceinterface;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.BreakIterator;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import ch.hslu.bda.f12.inav.Constants;
import ch.hslu.bda.f12.inav.Filemanager;
import ch.hslu.bda.f12.inav.R;

/**
 * Diese Activity kann per Intent gestartet werden. Am Ende befindet sich in den
 * Extras eine seralisiertes Objekt vom Typ PositionResponse "PositionResponse".
 * 
 * @author fabian
 * 
 */
public class CaptureImageActivity extends Activity {

	private String service_url;
	private Filemanager fm;

	/**
	 * Konstruktor.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		fm = new Filemanager(this);

		readPreferences();
		capture();
	}

	/**
	 * Die Kamera-Activity gibt die Kontrolle wieder an uns zurück.
	 */
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
				"http://147.88.219.190:8080/ims"); // Default value
	}

	/**
	 * Startet ein ACTION_IMAGE_CAPTURE-Intent um von Android ein Foto machen zu
	 * lassen.
	 */
	private void capture() {
		// create Intent to take a picture and return control to the calling
		// application
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		try {
			// create a file to save the image
			// set the image
			// file name
			File f = fm.getExternalStorageFile("IndoorNavi",
					"IMG_IndoorNav.jpg");
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

			// LastKnownLocation anhängen
			LocationManager locationManager = (LocationManager) this
					.getSystemService(Context.LOCATION_SERVICE);
			Location lastKnownLocation = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if (lastKnownLocation != null) {
				req.setLastKnownLocation(new Position(lastKnownLocation));
			}

			File img;
			try {
				img = compressImage(fm.getExternalStorageFile("IndoorNavi",
						"IMG_IndoorNav.jpg"), 1280, 960, 75);
				req.setImage(img);

			} catch (Exception e) {
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}

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
		// options.inTempStorage = new byte[32 * 1024];
		options.inPurgeable = true;
		options.inInputShareable = true;
		options.inDither = false;
		/*
		 * inSampleSize verringert die grösse im Speicher, aber damit auch die
		 * Qualität. 2 ist ein akzeptabler Kompromiss.
		 */
		options.inSampleSize = 0;

		/*
		 * Wie in
		 * (http://stackoverflow.com/questions/477572/android-strange-out-
		 * of-memory-issue-while-loading-an-image-to-a-bitmap-object)
		 * beschrieben sollte decodeFileDescriptor verwendet werden um
		 * OutOfMemory-Exceptions zu verhindern.
		 */
		FileInputStream fs = new FileInputStream(sourceImg);
		Bitmap bitmapOrg = BitmapFactory.decodeFileDescriptor(fs.getFD(), null,
				options);
		Bitmap scaledImg = Bitmap.createScaledBitmap(bitmapOrg, with, height,
				true);
		OutputStream os = new FileOutputStream(sourceImg);

		scaledImg.compress(CompressFormat.JPEG, quality, os);

		os.close();
		
		bitmapOrg.recycle();
		scaledImg.recycle();

		//System.gc();

		return sourceImg;
	}

	public void onUploadCanceled() {
		Intent i = new Intent();
		setResult(Activity.RESULT_CANCELED, i);
		finish();
	}

	/**
	 * Wird aus dem AsyncTask aufgerufen. Die Activity hat ihre Arbeit getan und
	 * wir beenden sie ohne den User mit finish().
	 * 
	 * @param r
	 */
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

	/**
	 * AsyncTask, der die Kommunikation mit dem Server erledigt. Sendet ein File
	 * mit HTTP-Multipart-POST und erhält eine JSON Antwort.
	 * 
	 * @author fabian
	 * 
	 */
	private static class RestPosterAsyncTask extends
			AsyncTask<PositionRequest, Integer, PositionResponse> {

		private ProgressDialog pd;
		CaptureImageActivity a;

		public RestPosterAsyncTask(CaptureImageActivity a) {
			this.a = a;
		}

		/**
		 * invoked on the UI thread immediately after the task is executed. This
		 * step is normally used to setup the task, for instance by showing a
		 * progress bar in the user interface.
		 * 
		 * Progress Dialog:
		 * http://developer.android.com/guide/topics/ui/dialogs.html
		 */
		public void onPreExecute() {
			pd = ProgressDialog.show(a, "",
					a.getString(R.string.progress_uploading), true);
		}

		/**
		 * Wird von Android im GUI-Thread aufgerufen, sobald der Task
		 * abgeschlossen wurde. Hier kann also problemlos auf das GUI
		 * zugegriffen werden. Wir geben die Kontrolle wieder an die Activity
		 * zurück.
		 */
		@Override
		protected void onPostExecute(PositionResponse r) {
			pd.dismiss();
			a.onUploadComplete(r);
		}

		/**
		 * Wird vom Android System in einem eigenen Thread gestartet, nachdem
		 * AsyncTask.execute() aufgerufen wurde.
		 */
		@Override
		protected PositionResponse doInBackground(PositionRequest... params) {
			PositionRequest posReq = params[0];
			PositionResponse posRes = new PositionResponse();
			try {
				posRes = post(posReq, posRes);
				publishPosition(posRes);

			} catch (Exception e) {
				posRes.setException(true);
				posRes.setMessage(e.getMessage());
				e.printStackTrace();
			}
			return posRes;
		}

		/**
		 * Führt ein Multipart-POST auf dem Webserver aus.
		 * 
		 * Unter Vorlage von:
		 * http://stackoverflow.com/questions/2935946/sending-
		 * images-using-http-post
		 * 
		 * Nach dieser Methode wird im GUI-Thread onPostExecute() aufgerufen.
		 * 
		 * @param posReq
		 * @return
		 */
		private PositionResponse post(PositionRequest posReq,
				PositionResponse posRes) throws Exception {
			// Verbindung konfigurieren
			HttpParams params = new BasicHttpParams();
			// 10 Sekunden Timeout, nach 10 Sekunden gibt es eine Exception
			params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
			HttpClient httpClient = new DefaultHttpClient(params);
			HttpContext localContext = new BasicHttpContext();
			HttpPost httpPost = new HttpPost(posReq.getUrl());
			HttpResponse httpRes = null;

			MultipartEntity entity = new MultipartEntity(
					HttpMultipartMode.BROWSER_COMPATIBLE);

			// Hinzufügen eines "image/jpeg" part
			ContentBody cbFile = new FileBody(posReq.getImage(), "image/jpeg");
			entity.addPart("data", cbFile);

			if (posReq.getLastKnownLocation() != null) {
				// StringBody für die LastKnownLocation, defaultCharset = UTF8
				ContentBody cbLastLocation = new StringBody(posReq
						.getLastKnownLocation().toJSON());
				entity.addPart("lastKnownLocation", cbLastLocation);
			}

			// Upload ausführen.
			httpPost.setEntity(entity);
			httpRes = httpClient.execute(httpPost, localContext);

			// PositionResponse updaten
			updatePositionResult(posRes, httpRes);

			return posRes;
		}

		/**
		 * Publiziert die letzte Position ins Android System per
		 * LocationManager. Da Android keine Möglichkeit anbietet einen eigenen
		 * LocationProvider zu implementieren, werden die Mock-Funktionen
		 * genutzt. Dazu muss die Einstellung "Allow Mock Locations" vom User
		 * gesetzt sein.
		 * 
		 * @param res
		 * @throws Exception
		 */
		private void publishPosition(PositionResponse res) throws Exception {
			// Location in LocationProvider setzten. Hier befinden wir uns noch
			// in einem eigenen Thread darum macht es Sinn dies gleich hier
			// zumachen.

			if (Settings.Secure.getInt(a.getContentResolver(),
					Settings.Secure.ALLOW_MOCK_LOCATION, 0) == 0) {
				return;
			}

			LocationManager locationManager = (LocationManager) a
					.getSystemService(Context.LOCATION_SERVICE);

			locationManager.addTestProvider(LocationManager.GPS_PROVIDER,
					false, false, false, false, true, true, true, 0, 1);
			locationManager.setTestProviderEnabled(
					LocationManager.GPS_PROVIDER, true);

			Location location = res.toLocation();

			Log.i("Published Position:", location.toString());

			locationManager.setTestProviderLocation(
					LocationManager.GPS_PROVIDER, location);
		}

		/**
		 * Liest JSONObject aus der Antwort aus // JSON-Infos aus der
		 * HTTP-Antwort auslesen
		 * 
		 * Das sieht etwa so aus: { "message": "JSONized", "z": 2, "y": 3.21,
		 * "x": 1.234 }
		 * 
		 * 
		 * @param httpRes
		 * @return JSON-Object
		 * @throws Exception
		 */
		private void updatePositionResult(PositionResponse posRes,
				HttpResponse httpRes) throws Exception {

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					httpRes.getEntity().getContent(), "UTF-8"));
			StringBuilder builder = new StringBuilder();
			JSONObject jso = new JSONObject();
			for (String line = null; (line = reader.readLine()) != null;) {
				builder.append(line).append("\n");
			}

			try {
				JSONTokener tokener = new JSONTokener(builder.toString());
				jso = new JSONObject(tokener);
				posRes.setX(jso.getDouble("x"));
				posRes.setY(jso.getDouble("y"));
				posRes.setZ(jso.getInt("z"));
				posRes.setMessage(jso.getString("msg"));
			} catch (Exception e) {
				posRes.setException(true);
				posRes.setMessage(e.getMessage() + "\n Server- Response was:\n"
						+ builder.toString());
			}
		}
	}

}
