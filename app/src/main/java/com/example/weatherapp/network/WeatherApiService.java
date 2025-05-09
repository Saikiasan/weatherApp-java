package com.example.weatherapp.network;

import com.example.weatherapp.model.WeatherResponse;
import com.example.weatherapp.model.ForecastResponse; // Add this for forecast data
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiService {

    // Fetch current weather data
    @GET("data/2.5/weather")
    Call<WeatherResponse> getCurrentWeather(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("appid") String apiKey,
            @Query("units") String units // "metric" or "imperial"
    );

    // Fetch hourly forecast data (24-hour forecast)
    @GET("data/2.5/onecall")
    Call<ForecastResponse> getHourlyForecast(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("appid") String apiKey,
            @Query("units") String units, // "metric" or "imperial"
            @Query("exclude") String exclude // Optionally exclude parts like "current", "minutely" if not needed
    );


}
