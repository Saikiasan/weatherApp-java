package com.example.weatherapp.model;

import java.util.List;

public class MockData {
    public List<Hourly> hourly;

    public static class Hourly {
        public long dt;
        public double temp;
        public List<Weather> weather;

        public static class Weather {
            public String icon;

        }
    }

}
