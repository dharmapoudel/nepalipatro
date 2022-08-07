package com.bibta.miti;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Grabber that grabs tithi from online database and stores the data in local database.
 */
public class TithiGrabber {

    public static final String URL = "https://raw.githubusercontent.com/bibekdahal/tithi/master/data.json";

    public interface Listener {
        void onNewDataFetched();
    }

    private Context mContext;

    /**
     * Create new Tithi Grabber object.
     * @param context Context that handles this grabber.
     */
    public TithiGrabber(Context context) {
        mContext = context;
    }

    /**
     * Store tithi data provided in a json object in local database.
     * @param jsonData JSON object containing "data" which is JSON Array of date-tithi pairs.
     * @throws JSONException
     */
    public void storeData(JSONObject jsonData) throws JSONException {
        TithiDb db = new TithiDb(mContext);
        JSONArray data = jsonData.getJSONArray("data");

        // Store each element in data array in the database.
        for (int i=0; i<data.length(); ++i) {
            JSONObject element = data.getJSONObject(i);

            String date = element.getString("date");
            String tithi = element.getString("tithi");
            String extra = element.getString("extra");

            // Check if date exists and update if it does.
            // Insert new if it doesn't.

            Pair<String, String> pair = db.get(date);
            if (pair == null)
                db.insert(date, tithi, extra);
            else
                db.update(date, tithi, extra);
        }
    }

    /**
     * Fetch new tithi data from online database.
     * @param listener Callback listener for listening to fetch complete event.
     */
    @SuppressLint("StaticFieldLeak")
    public void fetchData(final Listener listener) {
        new AsyncTask<Void, Void, Void>() {
            private boolean mSuccess = false;

            @Override
            protected Void doInBackground(Void... params) {
                try {

                    // Make connection to server.

                    /*java.net.URL url = new URL(URL);
                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    connection.setDoOutput(false);
                    connection.setRequestMethod("GET");

                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestProperty("Content-Type", "application/json");

                    // Get the result and store data locally.

                    InputStream in = new BufferedInputStream(connection.getInputStream());
                    String result = new Scanner(in).useDelimiter("\\A").next();
                    storeData(new JSONObject(result));*/


                    String json = null;
                    try {
                        InputStream inputStream = mContext.getAssets().open("data.json");
                        int size = inputStream.available();
                        byte[] buffer = new byte[size];
                        inputStream.read(buffer);
                        inputStream.close();
                        json = new String(buffer, Charset.forName("UTF-8"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    storeData(new JSONObject(json));

                    Log.d("Fetched new data", "Tithi updated");

                    mSuccess = true;

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (mSuccess)
                    listener.onNewDataFetched();
            }
        }.execute();
    }
}
