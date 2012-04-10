package ch.hslu.bda.f12.inav;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;

/**
 * AsyncTask, der die Kommunikation mit dem Server erledigt. Sendet ein File mit
 * HTTP-Multipart-POST und erhält eine JSON Antwort.
 * 
 * @author fabian
 * 
 */
public class RestPosterAsyncTask extends
		AsyncTask<PositionRequest, Integer, PositionResponse> {

	private ProgressDialog pd;
	private CaptureImageActivity a;

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
	 * Wird von Android aufgerufen, sobald der Task abgeschlossen wurde.
	 */
	@Override
	protected void onPostExecute(PositionResponse r) {
		pd.dismiss();
		a.onUploadComplete(r);
	}

	/**
	 * Wird vom Android System aufgerufen, nachdem AsyncTask.execute()
	 * aufgerufen wurde.
	 */
	@Override
	protected PositionResponse doInBackground(PositionRequest... params) {
		PositionRequest posReq = params[0];
		PositionResponse posRes = new PositionResponse();
		try {
			posRes = post(posReq, posRes);
			publishPosition(posRes);

		} catch (Exception e) {
			posRes.setException(e.getMessage());
			e.printStackTrace();
		}
		return posRes;
	}

	/**
	 * Führt ein Multipart-POST auf dem Webserver aus.
	 * 
	 * Unter Vorlage von:
	 * http://stackoverflow.com/questions/2935946/sending-images-using-http-post
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

		// Upload ausführen.
		httpPost.setEntity(entity);
		httpRes = httpClient.execute(httpPost, localContext);

		// JSON-Infos aus der HTTP-Antwort auslesen
		/*
		 * Das sieht etwa so aus: { "message": "JSONized", "z": 2, "y": 3.21,
		 * "x": 1.234 }
		 */
		JSONObject jso = toJSON(httpRes);

		// PositionResponse updaten
		posRes.setX(jso.getDouble("x"));
		posRes.setY(jso.getDouble("y"));
		posRes.setZ(jso.getInt("z"));
		posRes.setMessage(jso.getString("msg"));
		return posRes;
	}

	/**
	 * Publiziert die letzte Position. Da Android keine Möglichkeit anbietet
	 * einen eigenen LocationProvider zu implementieren, werden die
	 * Mock-Funktionen genutzt. Dazu muss die Einstellung "Allow Mock Locations"
	 * vom User gesetzt sein.
	 * 
	 * @param res
	 * @throws Exception
	 */
	private void publishPosition(PositionResponse res) throws Exception {
		// Location in LocationProvider setzten. Hier befinden wir uns noch in
		// einem eigenen Thread darum macht es Sinn dies gleich hier zumachen.

		if (Settings.Secure.getInt(a.getContentResolver(),
				Settings.Secure.ALLOW_MOCK_LOCATION, 0) == 0) {
			Exception ex = new Exception(
					"Mock locations are currently disabled in Settings - this App requires "
							+ "mock locations");
			throw ex;
		}

		LocationManager locationManager = (LocationManager) a
				.getSystemService(Context.LOCATION_SERVICE);

		locationManager.addTestProvider(LocationManager.GPS_PROVIDER, false,
				false, false, false, true, true, true, 0, 1);
		locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER,
				true);

		Location location = res.toLocation();

		Log.i("Published Position:", location.toString());

		locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER,
				location);
	}

	/**
	 * Liest JSONObject aus der Antwort aus
	 * 
	 * @param httpRes
	 * @return JSON-Object
	 * @throws Exception
	 */
	private JSONObject toJSON(HttpResponse httpRes) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				httpRes.getEntity().getContent(), "UTF-8"));
		StringBuilder builder = new StringBuilder();
		for (String line = null; (line = reader.readLine()) != null;) {
			builder.append(line).append("\n");
		}
		JSONTokener tokener = new JSONTokener(builder.toString());
		JSONObject jso = new JSONObject(tokener);
		return jso;
	}
}
