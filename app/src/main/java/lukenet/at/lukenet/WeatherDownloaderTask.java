package lukenet.at.lukenet;

import android.net.Uri;
import android.os.AsyncTask;
import android.text.format.Time;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by luke on 04/02/16.
 */
public class WeatherDownloaderTask extends AsyncTask<String, Void, ArrayList<String>> {

    private static final String LOG_TAG = WeatherDownloaderTask.class.getSimpleName();
    private static final String OPEN_WEATHER_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
    private final ArrayAdapter<String> adapter;

    public WeatherDownloaderTask(ArrayAdapter<String> adapter) {
        this.adapter = adapter;
    }

    @Override
    protected ArrayList<String> doInBackground(String... params) {
        String postCode = params[0];
        String country = params[1];

        Log.v(LOG_TAG, "Searching weather for city '" + postCode + "' in country + '" + country + "'");

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
            Log.i(LOG_TAG, "Received JSON-String: '" + forecastJsonStr + "'");
            return getWeatherDataFromJson(forecastJsonStr, 3);
        } catch (IOException | JSONException e) {
            Log.e(LOG_TAG, "Error receiving the weather info from openweathermap.com", e);
            Log.e(LOG_TAG, e.getMessage());
            return new ArrayList<String>();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error closing the BufferedReader", e);
                }
            }
        }
    }

    /**
     * The system calls this to perform work in the UI thread and delivers
     * the result from doInBackground()
     */
    protected void onPostExecute(ArrayList<String> result) {
        Log.i(LOG_TAG, "Updating Weather to " + result);
/*
        adapter.clear();
        for (String day : result) {
            adapter.add(day);
        }
        */
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     * <p/>
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private ArrayList<String> getWeatherDataFromJson(String forecastJsonStr, int numDays)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DESCRIPTION = "main";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        // OWM returns daily forecasts based upon the local time of the city that is being
        // asked for, which means that we need to know the GMT offset to translate this data
        // properly.

        // Since this data is also sent in-order and the first day is always the
        // current day, we're going to take advantage of that to get a nice
        // normalized UTC date for all of our weather.

        Time dayTime = new Time();
        dayTime.setToNow();

        // we start at the day returned by local time. Otherwise this is a mess.
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        // now we work exclusively in UTC
        dayTime = new Time();

        ArrayList<String> resultStrs = new ArrayList<String>();
        for (int i = 0; i < weatherArray.length(); i++) {
            // For now, using the format "Day, description, hi/low"
            String day;
            String description;
            String highAndLow;

            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            // The date/time is returned as a long.  We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".
            long dateTime;
            // Cheating to convert this to UTC time, which is what we want anyhow
            dateTime = dayTime.setJulianDay(julianStartDay + i);
            day = getReadableDateString(dateTime);

            // description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);

            highAndLow = formatHighLows(high, low);
            resultStrs.add(day + " - " + description + " - " + highAndLow);
        }

        for (String s : resultStrs) {
            Log.v(LOG_TAG, "Forecast entry: " + s);
        }
        return resultStrs;

    }

    /* The date/time conversion code is going to be moved outside the asynctask later,
     * so for convenience we're breaking it out into its own method now.
     */
    private String getReadableDateString(long time) {
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }
}
