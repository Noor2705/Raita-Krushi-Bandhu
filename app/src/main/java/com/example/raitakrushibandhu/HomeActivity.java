package com.example.raitakrushibandhu;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final String TAG = "HomeActivity";

    private MaterialCardView cardSmartIrrigation, cardPestDetection;
    private TextView tvWeather, tvLocation;

    private FusedLocationProviderClient fusedLocationClient;
    private OkHttpClient httpClient;

    private static final String OPENWEATHER_API_KEY = "b9aee8162b97ae019f1e5c841c7edc8a";
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Views
        cardSmartIrrigation = findViewById(R.id.cardSmartIrrigation);
        cardPestDetection = findViewById(R.id.cardPestDetection);
        tvWeather = findViewById(R.id.tvWeather);
        tvLocation = findViewById(R.id.tvLocation);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        httpClient = new OkHttpClient();

        // Set Card Click Listeners
        cardSmartIrrigation.setOnClickListener(view ->
                startActivity(new Intent(HomeActivity.this, DataActivity.class)));

        cardPestDetection.setOnClickListener(view ->
                startActivity(new Intent(HomeActivity.this, PlantActivity.class)));

        // Check Location and Fetch Weather
        checkLocationPermissionAndFetchWeather();

        // Setup bottom navigation
        setupBottomNavigation();
    }

    private void checkLocationPermissionAndFetchWeather() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fetchLocationAndWeather();
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private void fetchLocationAndWeather() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!gpsEnabled && !networkEnabled) {
            Toast.makeText(this, "Please enable location services", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        fetchWeather(location.getLatitude(), location.getLongitude());
                    } else {
                        requestNewLocationData();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to get location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Location failure: ", e);
                });
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private void requestNewLocationData() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(5000)
                .setNumUpdates(1);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Toast.makeText(HomeActivity.this, "Unable to get location updates", Toast.LENGTH_SHORT).show();
                    return;
                }
                Location loc = locationResult.getLastLocation();
                if (loc != null) {
                    fetchWeather(loc.getLatitude(), loc.getLongitude());
                }
                fusedLocationClient.removeLocationUpdates(this);
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void fetchWeather(double lat, double lon) {
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat +
                "&lon=" + lon + "&units=metric&appid=" + OPENWEATHER_API_KEY;

        Request request = new Request.Builder().url(url).build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(HomeActivity.this, "Failed to get weather data.", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonData = response.body().string();
                    parseAndDisplayWeather(jsonData);
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(HomeActivity.this, "Failed to get weather data.", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private void parseAndDisplayWeather(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            String cityName = jsonObject.getString("name");
            JSONObject main = jsonObject.getJSONObject("main");
            double temp = main.getDouble("temp");
            JSONArray weatherArray = jsonObject.getJSONArray("weather");
            JSONObject weather = weatherArray.getJSONObject(0);
            String weatherDescription = weather.getString("description");

            String weatherText = String.format("Temperature: %.1f°C, %s", temp, capitalize(weatherDescription));
            String locationText = "Location: " + cityName;

            runOnUiThread(() -> {
                tvWeather.setText(weatherText);
                tvLocation.setText(locationText);
            });

        } catch (JSONException e) {
            e.printStackTrace();
            runOnUiThread(() ->
                    Toast.makeText(this, "Error parsing weather data.", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private String capitalize(String input) {
        if (input == null || input.length() == 0) return "";
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocationAndWeather();
            } else {
                Toast.makeText(this, "Location permission denied. Cannot fetch weather.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.home);  // Highlight home icon

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.profile) {
                if (!getClass().equals(ProfileSettingsActivity.class)) {
                    startActivity(new Intent(this, ProfileSettingsActivity.class));
                    overridePendingTransition(0, 0);
                }
                return true;
            } else if (id == R.id.diagnose) {
                if (!getClass().equals(DiagnosisHistoryActivity.class)) {
                    startActivity(new Intent(this, DiagnosisHistoryActivity.class));
                    overridePendingTransition(0, 0);
                }
                return true;
            } else return id == R.id.home; // already on Home
        });
    }
}
