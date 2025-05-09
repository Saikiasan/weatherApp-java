package com.example.weatherapp.model;

import java.util.ArrayList;
import java.util.Random;

public class MockWeatherData {

    // Generate mock hourly data for 12 hours
    public static WeatherResponse generateMockData() {
        WeatherResponse weatherResponse = new WeatherResponse();
        weatherResponse.hourly = new ArrayList<>();

        Random random = new Random();

        for (int i = 0; i < 12; i++) {
            WeatherResponse.Hourly hourly = new WeatherResponse.Hourly();
            hourly.dt = System.currentTimeMillis() / 1000L + (i * 3600); // Simulate hourly timestamps
            hourly.temp = random.nextInt(15) + 15; // Random temperature between 15 and 30 degrees Celsius

            // Mock weather icon (could be a random icon code, e.g., "01d" for clear sky)
            MockData.Hourly.Weather weather = new WeatherResponse.Hourly.Weather();
            weather.icon = "01d"; // Clear sky, you can make it random too
            hourly.weather = new ArrayList<MockData.Hourly.Weather>();
            hourly.weather.add(weather);

            weatherResponse.hourly.add(hourly);
        }

        return weatherResponse;
    }
}
