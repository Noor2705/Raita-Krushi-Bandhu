package com.example.raitakrushibandhu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DataActivity extends AppCompatActivity {

    private TextView moistureTextView, nitrogenTextView, phosphorusTextView, potassiumTextView,
            temperatureTextView, humidityTextView;

    private Button btnNPKHistory, btnSmartIrrigation;
    private DatabaseReference soilDataRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        // Initialize views
        moistureTextView = findViewById(R.id.tvMoisture);
        nitrogenTextView = findViewById(R.id.tvNitrogen);
        phosphorusTextView = findViewById(R.id.tvPhosphorus);
        potassiumTextView = findViewById(R.id.tvPotassium);
        temperatureTextView = findViewById(R.id.tvTemperature);
        humidityTextView = findViewById(R.id.tvHumidity);

        btnNPKHistory = findViewById(R.id.btnNPKHistory);
        btnSmartIrrigation = findViewById(R.id.btnSmartIrrigation);

        // Button click listeners
        btnNPKHistory.setOnClickListener(v -> startActivity(new Intent(DataActivity.this, NPKHistory.class)));

        btnSmartIrrigation.setOnClickListener(v -> startActivity(new Intent(DataActivity.this, SmartIrrigation.class)));

        // Firebase reference
        soilDataRef = FirebaseDatabase.getInstance().getReference("sensor");

        // Fetch data
        fetchSoilData();
    }

    private void fetchSoilData() {
        soilDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Double moisture = snapshot.child("moisture").getValue(Double.class);
                    Double nitrogen = snapshot.child("nitrogen").getValue(Double.class);
                    Double phosphorus = snapshot.child("phosphorous").getValue(Double.class);
                    Double potassium = snapshot.child("potassium").getValue(Double.class);
                    Double temperature = snapshot.child("temperature").getValue(Double.class);
                    Double humidity = snapshot.child("humidity").getValue(Double.class);

                    moistureTextView.setText(moisture != null ? moisture + "%" : "N/A");
                    nitrogenTextView.setText(nitrogen != null ? nitrogen.toString() : "N/A");
                    phosphorusTextView.setText(phosphorus != null ? phosphorus.toString() : "N/A");
                    potassiumTextView.setText(potassium != null ? potassium.toString() : "N/A");
                    temperatureTextView.setText(temperature != null ? temperature + "°C" : "N/A");
                    humidityTextView.setText(humidity != null ? humidity + "%" : "N/A");
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
}
