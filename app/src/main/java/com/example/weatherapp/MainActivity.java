package com.example.weatherapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;

import com.example.weatherapp.model.MockWeatherData;
import com.example.weatherapp.model.WeatherResponse;
import com.example.weatherapp.network.ApiClient;
import com.example.weatherapp.network.WeatherApiService;
import com.google.android.gms.location.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private SharedPreferences preferences;
    String apiKey = BuildConfig.OPENWEATHER_API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getSharedPreferences("WeatherAppPrefs", MODE_PRIVATE);
        boolean isFirstLaunch = preferences.getBoolean("isFirstLaunch", true);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (isFirstLaunch) {
            requestLocationPermission();
            preferences.edit().putBoolean("isFirstLaunch", false).apply();
        } else {
            if (checkPermissions()) {
                if (isInternetAvailable() && isLocationEnabled()) {
                    fetchLocation();
                } else {
                    showSettingsPrompt();
                }
            } else {
                requestLocationPermission();
            }
        }
        View statLayout = findViewById(R.id.status_indicator);

        statLayout.postDelayed(() -> {
            statLayout.setVisibility(View.GONE); // or View.INVISIBLE
        }, 3000); // 1000 milliseconds = 1 second

    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    private boolean isInternetAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void showSettingsPrompt() {
        Toast.makeText(this, "Please enable Location and Internet", Toast.LENGTH_LONG).show();
        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

private void fetchLocation() {
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
        Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
        return;
    }

    fusedLocationClient.getLastLocation()
            .addOnSuccessListener(location -> {
                if (location != null) {
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();
                    fetchWeatherData(lat, lon);
                } else {
                    // Request a new location if last location is null
                    LocationRequest locationRequest = LocationRequest.create()
                            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                            .setInterval(10000)
                            .setFastestInterval(5000)
                            .setNumUpdates(1);

                    fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            if (locationResult == null) {
                                Toast.makeText(MainActivity.this, "Unable to retrieve location", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            Location location1 = locationResult.getLastLocation();
                            double lat = location1.getLatitude();
                            double lon = location1.getLongitude();
                            fetchWeatherData(lat, lon);
                        }
                    }, Looper.getMainLooper());
                }
            });
}


    private void fetchWeatherData(double lat, double lon) {
        WeatherApiService weatherApiService = ApiClient.getWeatherApiService();
        Call<WeatherResponse> call = weatherApiService.getCurrentWeather(lat, lon, apiKey, "metric");
        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful()) {
                    WeatherResponse weatherResponse = response.body();
                    if (weatherResponse != null) {
                        updateUI(weatherResponse);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Failed to retrieve data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void updateUI(WeatherResponse weatherResponse) {
        Log.d("WeatherApp", "Weather data: " + weatherResponse);
        // Set city name
        TextView cityName = findViewById(R.id.current_location);
        cityName.setText(weatherResponse.cityName);

        // Set temperature
        TextView temp = findViewById(R.id.current_temp);
        temp.setText(weatherResponse.main.temp + "°C");

        // Set weather condition
        TextView condition = findViewById(R.id.weather_condition);
        String weatherDescription = weatherResponse.weather.get(0).description;
        String capitalizedDescription = weatherDescription.substring(0, 1).toUpperCase() + weatherDescription.substring(1);
        condition.setText(capitalizedDescription);


        // Set wind speed
        TextView windSpeed = findViewById(R.id.wind_speed);
        windSpeed.setText(weatherResponse.wind.speed + " km/h");

        // Set humidity
        TextView humidity = findViewById(R.id.humidity);
        humidity.setText(weatherResponse.main.humidity + "%");

        TextView rainChances = findViewById(R.id.rain_chances);
        if (weatherResponse.hourly != null && !weatherResponse.hourly.isEmpty()) {
            double pop = weatherResponse.hourly.get(0).pop; // e.g., 0.6
            int rainPercent = (int) (pop * 100);
            rainChances.setText(rainPercent + "%");
        } else {
            rainChances.setText("-");
        }

        // Set weather icon (if available)
        String iconCode = weatherResponse.weather.get(0).icon;
        int iconResId = getWeatherIconResource(iconCode);
        ImageView weatherIcon = findViewById(R.id.current_weather_icon);
        weatherIcon.setImageResource(iconResId);

        TextView date = findViewById(R.id.day_date_month);
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        date.setText(currentDate);


        LinearLayout hourlyContainer = findViewById(R.id.hourly_container);
        hourlyContainer.removeAllViews(); // Clear previous entries

        LayoutInflater inflater = LayoutInflater.from(this);

// Generate mock weather data
        weatherResponse = MockWeatherData.generateMockData();

        if (weatherResponse.hourly != null && !weatherResponse.hourly.isEmpty()) {
            Log.d("WeatherApp", "Hourly data size: " + weatherResponse.hourly.size());

            for (int i = 0; i < Math.min(12, weatherResponse.hourly.size()); i++) {
                WeatherResponse.Hourly hourly = weatherResponse.hourly.get(i);
                View itemView = inflater.inflate(R.layout.hourly_forecast_item, hourlyContainer, false);

                // Set hour
                TextView hourText = itemView.findViewById(R.id.p_time);
                long unixTime = hourly.dt * 1000L;
                String formattedHour = new SimpleDateFormat("ha", Locale.getDefault()).format(new Date(unixTime));
                Log.d("WeatherApp", "Formatted hour: " + formattedHour);
                hourText.setText(formattedHour); // e.g., "3PM"

                // Set temperature
                TextView tempText = itemView.findViewById(R.id.p_weather_temp);
                tempText.setText(String.format(Locale.getDefault(), "%.0f°C", hourly.temp));

                // Set icon
                ImageView iconView = itemView.findViewById(R.id.p_weather_time_icon);
                if (hourly.weather != null && !hourly.weather.isEmpty()) {
                    iconCode = hourly.weather.get(0).icon;
                    int iconRes = getWeatherIconResource(iconCode);
                    Log.d("WeatherApp", "Icon resource: " + iconRes);
                    iconView.setImageResource(iconRes);
                }

                // Add to parent layout
                hourlyContainer.addView(itemView);
            }
        } else {
            Log.d("WeatherApp", "No hourly data available");
        }


    }

    private int getWeatherIconResource(String iconCode) {
        switch (iconCode) {
            case "01d":
                return R.drawable.sunny; // Replace with actual icons
            case "02d":
                return R.drawable.partly_cloudy;
            case "03d":
                return R.drawable.cloudy;
            // Add more icon cases based on the OpenWeather icons
            default:
                return R.drawable.default_icon;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (isInternetAvailable() && isLocationEnabled()) {
                fetchLocation();
            } else {
                showSettingsPrompt();
            }
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

}
