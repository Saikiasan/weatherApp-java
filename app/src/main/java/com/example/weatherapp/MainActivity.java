package com.example.weatherapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;

import com.example.weatherapp.model.WeatherResponse;
import com.example.weatherapp.network.ApiClient;
import com.example.weatherapp.network.WeatherApiService;
import com.google.android.gms.location.*;

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
            // You can request permission again or handle the case
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        double lat = location.getLatitude();
                        double lon = location.getLongitude();
                        fetchWeatherData(lat, lon);  // Fetch weather data
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
        condition.setText(weatherResponse.weather.get(0).description);

        // Set wind speed
        TextView windSpeed = findViewById(R.id.wind_speed);
        windSpeed.setText(weatherResponse.wind.speed + " km/h");

        // Set humidity
        TextView humidity = findViewById(R.id.humidity);
        humidity.setText(weatherResponse.main.humidity + "%");

        // Set rain chances (if available)
        TextView rainChances = findViewById(R.id.rain_chances);
        rainChances.setText("N/A"); // Update this if rain data is available from the API

        // Set weather icon (if available)
        String iconCode = weatherResponse.weather.get(0).icon;
        int iconResId = getWeatherIconResource(iconCode);
        ImageView weatherIcon = findViewById(R.id.current_weather_icon);
        weatherIcon.setImageResource(iconResId);

        // Set the date (you can use the current date or fetch it dynamically)
        TextView date = findViewById(R.id.day_date_month);
        date.setText("Monday, 29 Mayeyufs"); // Change this to dynamic date if needed
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
