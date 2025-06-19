package com.example.raitakrushibandhu;

import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class WeatherForecastActivity extends AppCompatActivity {

    private TextView weatherText, diseaseAlert;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_forecast);

        weatherText = findViewById(R.id.weatherText);
        diseaseAlert = findViewById(R.id.diseaseAlert);

        firestore = FirebaseFirestore.getInstance();
        fetchForecastData();
    }

    private void fetchForecastData() {
        firestore.collection("weatherForecast")
                .document("latest")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String forecast = documentSnapshot.getString("forecast");
                        String alert = documentSnapshot.getString("alert");

                        weatherText.setText("Forecast: " + forecast);
                        diseaseAlert.setText("\u26A0\uFE0F Alert: " + alert);
                    } else {
                        weatherText.setText("No forecast data found.");
                        diseaseAlert.setText("No alerts.");
                    }
                })
                .addOnFailureListener(e -> {
                    weatherText.setText("Failed to load forecast.");
                    diseaseAlert.setText("");
                });
    }
}
