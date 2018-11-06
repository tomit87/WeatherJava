package co.teltech.weatherjava.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import co.teltech.weatherjava.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = "FETCHING_WEATHER";

    private final String OPEN_WEATHER_API = "http://api.openweathermap.org/data/2.5/weather?q=[QUERY]&appid=ffee28b9ec9430dc8d18e4c8a3d69854&units=metric";

    private EditText searchField;

    private ScrollView weatherData;

    private TextView labelCityName;
    private TextView labelWeatherDescription;
    private TextView labelCurrentTemp;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchField = findViewById(R.id.searchField);

        weatherData = findViewById(R.id.weatherData);

        labelCityName = findViewById(R.id.labelCityName);
        labelWeatherDescription = findViewById(R.id.labelWeatherDescription);
        labelCurrentTemp = findViewById(R.id.labelCurrentTemp);

        progressBar = findViewById(R.id.progressBar);
    }

    public void fetchWeather(View v) {
        weatherData.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        if (searchField.getText().toString().equals("")) {
            Toast.makeText(this, getString(R.string.toast_enter_city_name), Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        } else {
            String url = OPEN_WEATHER_API.replace("[QUERY]", searchField.getText().toString());
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(LOG_TAG, e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        if (jsonObject.has("message")) {
                            final String message = jsonObject.getString("message");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                        } else {
                            JSONArray weatherDescriptionArray = jsonObject.getJSONArray("weather");
                            JSONObject weatherDescription = weatherDescriptionArray.getJSONObject(0);
                            JSONObject weatherDataObject = jsonObject.getJSONObject("main");

                            final String cityName = jsonObject.getString("name");
                            final String weatherDescriptionText = weatherDescription.getString("description");
                            final String currentTemp = Double.toString(weatherDataObject.getDouble("temp"));

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    labelCityName.setText(cityName);
                                    labelWeatherDescription.setText(weatherDescriptionText);
                                    labelCurrentTemp.setText(currentTemp);

                                    progressBar.setVisibility(View.GONE);
                                    weatherData.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
