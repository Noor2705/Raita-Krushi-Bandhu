package com.example.raitakrushibandhu;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

    // Replace with your OpenWeatherMap API key
    private static final String OPENWEATHER_API_KEY = "b9aee8162b97ae019f1e5c841c7edc8a";

    private LocationCallback locationCallback; // store callback to remove updates later

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        cardSmartIrrigation = findViewById(R.id.cardSmartIrrigation);
        cardPestDetection = findViewById(R.id.cardPestDetection);
        tvWeather = findViewById(R.id.tvWeather);
        tvLocation = findViewById(R.id.tvLocation);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        httpClient = new OkHttpClient();

        cardSmartIrrigation.setOnClickListener(view -> {
            startActivity(new Intent(HomeActivity.this, DataActivity.class));
        });

        cardPestDetection.setOnClickListener(view -> {
            startActivity(new Intent(HomeActivity.this, PlantActivity.class));
        });

        checkLocationPermissionAndFetchWeather();
    }

    private void checkLocationPermissionAndFetchWeather() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fetchLocationAndWeather();
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private void fetchLocationAndWeather() {
        // Check if location services are enabled
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = false;
        boolean networkEnabled = false;

        if (locationManager != null) {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }

        if (!gpsEnabled && !networkEnabled) {
            Toast.makeText(this, "Please enable location services", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        Log.d(TAG, "Last known location found");
                        fetchWeather(location.getLatitude(), location.getLongitude());
                    } else {
                        Log.d(TAG, "Last known location is null, requesting updates");
                        requestNewLocationData();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(HomeActivity.this, "Failed to get location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Location failure: ", e);
                });
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private void requestNewLocationData() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);        // 10 seconds
        locationRequest.setFastestInterval(5000);   // 5 seconds
        locationRequest.setNumUpdates(1);            // only one update needed

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Toast.makeText(HomeActivity.this, "Unable to get location updates", Toast.LENGTH_SHORT).show();
                    return;
                }
                Location loc = locationResult.getLastLocation();
                if (loc != null) {
                    Log.d(TAG, "Location update received");
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
                Log.e(TAG, "Weather fetch failed", e);
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
                    Log.e(TAG, "Weather response unsuccessful");
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
                    Toast.makeText(HomeActivity.this, "Error parsing weather data.", Toast.LENGTH_SHORT).show()
            );
            Log.e(TAG, "Weather JSON parsing error", e);
        }
    }

    private String capitalize(String input) {
        if (input == null || input.length() == 0) return "";
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocationAndWeather();
            } else {
                Toast.makeText(this, "Location permission denied. Cannot fetch weather.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent = null;

        if (id == R.id.setting) {
            intent = new Intent(this, SettingsActivity.class);
        } else if (id == R.id.feedback) {
            intent = new Intent(this, FeedbackActivity.class);
        } else if (id == R.id.contact) {
            intent = new Intent(this, ContactActivity.class);
        } else if (id == R.id.thx) {
            intent = new Intent(this, ThanksActivity.class);
        } else if (id == R.id.legal_notices) {
            intent = new Intent(this, LegalNoticesActivity.class);
        } else if (id == R.id.quickstart) {
            intent = new Intent(this, QuickstartActivity.class);
        } else if (id == R.id.menu_logout) {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            prefs.edit().remove("loggedInUser").apply();

            Intent logoutIntent = new Intent(HomeActivity.this, LoginActivity.class);
            logoutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(logoutIntent);
            finish();

            return true;
        }

        if (intent != null) {
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
