package com.example.weatherapp.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ForecastResponse {

    private List<Hourly> hourly;

    public List<Hourly> getHourly() {
        return hourly;
    }

    public void setHourly(List<Hourly> hourly) {
        this.hourly = hourly;
    }

    public static class Hourly {
        private long dt; // Timestamp
        private Main main;
        private List<Weather> weather;

        public long getDt() {
            return dt;
        }

        public void setDt(long dt) {
            this.dt = dt;
        }

        public Main getMain() {
            return main;
        }

        public void setMain(Main main) {
            this.main = main;
        }

        public List<Weather> getWeather() {
            return weather;
        }

        public void setWeather(List<Weather> weather) {
            this.weather = weather;
        }

        public static class Main {
            private float temp;

            public float getTemp() {
                return temp;
            }

            public void setTemp(float temp) {
                this.temp = temp;
            }
        }

        public static class Weather {
            private String description;
            private String icon;

            public String getDescription() {
                return description;
            }

            public void setDescription(String description) {
                this.description = description;
            }

            public String getIcon() {
                return icon;
            }

            public void setIcon(String icon) {
                this.icon = icon;
            }
        }
    }
}
