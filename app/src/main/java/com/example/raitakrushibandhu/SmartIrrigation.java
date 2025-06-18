package com.example.raitakrushibandhu;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SmartIrrigation extends AppCompatActivity {

    // UI elements
    private TextView tvPumpStatus, tvSprinklerStatus;
    private TextView tvNitrogenValue, tvPhosphorusValue, tvPotassiumValue;
    private Button btnPumpOn, btnPumpOff, btnSprinklerOn, btnSprinklerOff;

    // Firebase references
    private DatabaseReference rootRef;
    private DatabaseReference npkRef;
    private DatabaseReference pumpRef;
    private DatabaseReference sprinklerRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smartirrigation);

        // Initialize Firebase references
        rootRef = FirebaseDatabase.getInstance().getReference();
        npkRef = rootRef.child("data"); // Adjust if your NPK data is under another node
        pumpRef = rootRef.child("controls/pump");
        sprinklerRef = rootRef.child("controls/sprinkler");

        // Bind UI
        tvPumpStatus = findViewById(R.id.tvPumpStatus);
        tvSprinklerStatus = findViewById(R.id.tvSprinklerStatus);
        tvNitrogenValue = findViewById(R.id.tvNitrogenValue);
        tvPhosphorusValue = findViewById(R.id.tvPhosphorusValue);
        tvPotassiumValue = findViewById(R.id.tvPotassiumValue);

        btnPumpOn = findViewById(R.id.btnPumpOn);
        btnPumpOff = findViewById(R.id.btnPumpOff);
        btnSprinklerOn = findViewById(R.id.btnSprinklerOn);
        btnSprinklerOff = findViewById(R.id.btnSprinklerOff);

        // Load real-time NPK sensor data
        loadNPKData();

        // Monitor pump and sprinkler status
        monitorPumpStatus();
        monitorSprinklerStatus();

        // Pump control buttons
        btnPumpOn.setOnClickListener(v -> pumpRef.setValue("ON"));
        btnPumpOff.setOnClickListener(v -> pumpRef.setValue("OFF"));

        // Sprinkler control buttons
        btnSprinklerOn.setOnClickListener(v -> sprinklerRef.setValue("ON"));
        btnSprinklerOff.setOnClickListener(v -> sprinklerRef.setValue("OFF"));
    }

    private void loadNPKData() {
        npkRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer nitrogen = snapshot.child("nitrogen").getValue(Integer.class);
                Integer phosphorus = snapshot.child("phosphorus").getValue(Integer.class);
                Integer potassium = snapshot.child("potassium").getValue(Integer.class);

                tvNitrogenValue.setText("Nitrogen: " + (nitrogen != null ? nitrogen : 0) + " mg/kg");
                tvPhosphorusValue.setText("Phosphorus: " + (phosphorus != null ? phosphorus : 0) + " mg/kg");
                tvPotassiumValue.setText("Potassium: " + (potassium != null ? potassium : 0) + " mg/kg");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Optional: handle error
            }
        });
    }

    private void monitorPumpStatus() {
        pumpRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String status = snapshot.getValue(String.class);
                tvPumpStatus.setText("Status: " + (status != null ? status : "OFF"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Optional: handle error
            }
        });
    }

    private void monitorSprinklerStatus() {
        sprinklerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String status = snapshot.getValue(String.class);
                tvSprinklerStatus.setText("Status: " + (status != null ? status : "OFF"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Optional: handle error
            }
        });
    }
}
