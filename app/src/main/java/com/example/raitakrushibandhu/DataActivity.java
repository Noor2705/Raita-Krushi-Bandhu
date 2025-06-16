package com.example.raitakrushibandhu;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.FileWriter;
import java.io.IOException;

public class DataActivity extends AppCompatActivity {

    private TextView weatherTextView, moistureTextView, nitrogenTextView, phosphorusTextView, potassiumTextView;
    private Button downloadButton;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private String API_KEY = "YOUR_OPENWEATHERMAP_API_KEY"; // Replace with your actual API key
    private DatabaseReference soilDataRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        // Initialize views
        weatherTextView = findViewById(R.id.weatherTextView);
        moistureTextView = findViewById(R.id.moisture);
        nitrogenTextView = findViewById(R.id.nitrogen);
        phosphorusTextView = findViewById(R.id.phosphorus);
        potassiumTextView = findViewById(R.id.potassium);
        downloadButton = findViewById(R.id.downloadButton);

        // Initialize Firebase reference
        soilDataRef = FirebaseDatabase.getInstance().getReference("sensor");

        // Fetch soil data from Firebase
        fetchSoilData();

        // Initialize location client
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Request location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }

        // Set up download button
        downloadButton.setOnClickListener(v -> downloadSoilData());
    }

    // Fetch Soil Data from Firebase
    private void fetchSoilData() {
        soilDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Read values as Double and convert them to String
                    Double moisture = snapshot.child("moisture").getValue(Double.class);
                    Double nitrogen = snapshot.child("nitrogen").getValue(Double.class);
                    Double phosphorus = snapshot.child("phosphorous").getValue(Double.class);
                    Double potassium = snapshot.child("potassium").getValue(Double.class);

                    // Check if values are null before setting text
                    moistureTextView.setText("Moisture: " + (moisture != null ? moisture.toString() : "N/A"));
                    nitrogenTextView.setText("Nitrogen: " + (nitrogen != null ? nitrogen.toString() : "N/A"));
                    phosphorusTextView.setText("Phosphorous: " + (phosphorus != null ? phosphorus.toString() : "N/A"));
                    potassiumTextView.setText("Potassium: " + (potassium != null ? potassium.toString() : "N/A"));
                } else {
                    Toast.makeText(DataActivity.this, "No data found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DataActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Get Current Location
    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    fetchWeather(latitude, longitude);
                } else {
                    weatherTextView.setText("Unable to get location");
                }
            }
        });
    }

    // Fetch Weather Data
    private void fetchWeather(double latitude, double longitude) {
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid=" + API_KEY + "&units=metric";

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            double temp = response.getJSONObject("main").getDouble("temp");
                            String weatherCondition = response.getJSONArray("weather").getJSONObject(0).getString("description");

                            String weatherInfo = temp + "°C, " + weatherCondition;
                            weatherTextView.setText(weatherInfo);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            weatherTextView.setText("Error fetching weather");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                weatherTextView.setText("Unable to fetch weather");
                Log.e("WeatherError", "Error fetching weather: " + error.getMessage());
            }
        });

        requestQueue.add(jsonObjectRequest);
    }

    // Download Soil Data as a Text File
    private void downloadSoilData() {
        String data = "Moisture: " + moistureTextView.getText().toString() + "\n"
                + "Nitrogen: " + nitrogenTextView.getText().toString() + "\n"
                + "Phosphorus: " + phosphorusTextView.getText().toString() + "\n"
                + "Potassium: " + potassiumTextView.getText().toString() + "\n";

        try {
            FileWriter writer = new FileWriter(getExternalFilesDir(null) + "/SoilData.txt");
            writer.write(data);
            writer.close();
            Toast.makeText(this, "Report downloaded successfully!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Failed to download report", Toast.LENGTH_SHORT).show();
        }
    }

    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
