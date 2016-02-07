package lukenet.at.lukenet;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by luke on 04/02/16.
 */
public class WeatherDownloaderTask extends AsyncTask<String, Void, String[]> {

    public static final String LOG_TAG = WeatherDownloaderTask.class.getSimpleName();
    private static final String OPEN_WEATHER_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
    ArrayAdapter<String> v = null;

    @Override
    protected String[] doInBackground(String... params) {
        //v = params[0];

        String postCode = params[0];
        String country = params[1];

        String forecastJsonStr = null;
        BufferedReader reader = null;
        try {
            //UriBuilder u = new UriBuilder();
            Uri u = Uri.parse(OPEN_WEATHER_BASE_URL).buildUpon()
                    .appendQueryParameter("zip", postCode + "," + country)
                    .appendQueryParameter("units", "metric")
                    .appendQueryParameter("cht", "7")
                    .appendQueryParameter("appid", "44db6a862fba0b067b1930da0d769e98")
                    .build();

            //URL weatherUrl = new URL("http://api.openweathermap.org/data/2.5/forecast?zip=9500,at&units=metric&format=json&cht=7&appid=44db6a862fba0b067b1930da0d769e98");
            URL weatherUrl = new URL(u.toString());

            Log.v(LOG_TAG, "Url to download weather info: '" + weatherUrl + "'");
            HttpURLConnection conn = (HttpURLConnection) weatherUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            InputStream inputStream = conn.getInputStream();
            StringBuffer sb = new StringBuffer();
            if (inputStream == null) {
                forecastJsonStr = null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            if (sb.length() == 0) {
                forecastJsonStr = null;
            }
            forecastJsonStr = sb.toString();

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error receiving the weather info from openweathermap.com", e);
            forecastJsonStr = null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error closing the BufferedReader", e);
                }
            }
        }
        return WeatherDataParser.parseWeatherJson(forecastJsonStr);
    }

    /**
     * The system calls this to perform work in the UI thread and delivers
     * the result from doInBackground()
     */
    protected void onPostExecute(String result) {
        Log.i(LOG_TAG, "Updating Weather to " + result);
        /*
        v.clear();
        v.add("huhu");
        v.add("haha");
*/

    }
}
