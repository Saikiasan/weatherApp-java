package com.example.weatherapp.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WeatherResponse {

    @SerializedName("name")
    public String cityName;

    @SerializedName("main")
    public Main main;

    @SerializedName("weather")
    public List<Weather> weather;

    @SerializedName("wind")
    public Wind wind;

    @SerializedName("hourly")
    public List<Hourly> hourly; // Added this for hourly forecast

    // You can add more fields as needed

    public static class Main {
        @SerializedName("temp")
        public double temp;

        @SerializedName("feels_like")
        public double feelsLike;

        @SerializedName("humidity")
        public int humidity;

        @SerializedName("pressure")
        public int pressure;
    }

    public static class Weather {
        @SerializedName("main")
        public String main;

        @SerializedName("description")
        public String description;

        @SerializedName("icon")
        public String icon;
    }

    public static class Wind {
        @SerializedName("speed")
        public double speed;
    }

    // Added the Hourly data model for the hourly forecast
    public static class Hourly {
        @SerializedName("pop")
        public double pop; // Probability of precipitation for the hour

        @SerializedName("dt")
        public long dt; // Timestamp for the hour

        @SerializedName("temp")
        public double temp; // Temperature for the hour

        @SerializedName("weather")
        public List<MockData.Hourly.Weather> weather; // Weather description for the hour

        @SerializedName("wind_speed")
        public double windSpeed; // Wind speed for the hour

        public static class Weather extends MockData.Hourly.Weather {
        }
    }
}
