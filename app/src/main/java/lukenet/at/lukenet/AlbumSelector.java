package lukenet.at.lukenet;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;

public class AlbumSelector extends AppCompatActivity {

    ArrayAdapter<String> forecastAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("AlbumSelector", "Starting application");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_selector);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        final Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                EditText t = (EditText) findViewById(R.id.editText);
                t.setText("Juhu, das liebste!");

            }
        });

        final Button button1 = (Button) findViewById(R.id.button2);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                EditText t = (EditText) findViewById(R.id.editText);
                t.setText("Leider nicht das liebste!");

            }
        });

        String[] weatherlist = {"monday - sunny - 30°", "tuesday - sunny - 26°", "wednesday - rainy - 20°+"};
        ArrayList<String> weatherlist2 = new ArrayList<String>(Arrays.asList(weatherlist));

        forecastAdapter = new ArrayAdapter<String>(this, R.layout.list_view, R.id.list_item, weatherlist2);

        final ListView listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(forecastAdapter);

        new WeatherDownloaderTask().execute("1004", "CH");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_album_selector, menu);
        getMenuInflater().inflate(R.menu.forecast_fragment, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            Log.i("AlbumSelector", "Refresh button has been clicked");
            new WeatherDownloaderTask().execute("1004", "CH");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
