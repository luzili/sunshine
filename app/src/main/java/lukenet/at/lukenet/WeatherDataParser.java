package lukenet.at.lukenet;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by luke on 07/02/16.
 */
public class WeatherDataParser {
    /**
     * Given a string of the form returned by the api call:
     * http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7
     * retrieve the maximum temperature for the day indicated by dayIndex
     * (Note: 0-indexed, so 0 would refer to the first day).
     */
    public static double getMaxTemperatureForDay(String weatherJsonStr, int dayIndex)
            throws JSONException {

        JSONObject obj = new JSONObject(weatherJsonStr);
        JSONObject dayValues = obj.getJSONArray("list").getJSONObject(dayIndex).getJSONObject("temp");
        System.out.println(dayValues.names());
        return dayValues.getDouble("max");
    }

    public static String[] parseWeatherJson(String weatherJsonStr) {

        return null;
    }

}
