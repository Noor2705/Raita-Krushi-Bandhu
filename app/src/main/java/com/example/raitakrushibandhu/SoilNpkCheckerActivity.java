package com.example.raitakrushibandhu;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class SoilNpkCheckerActivity extends AppCompatActivity {

    private Spinner soilTypeSpinner;
    private EditText inputNitrogen, inputPhosphorus, inputPotassium;
    private Button checkCompatibilityBtn;
    private TextView compatibilityResult;
    private FirebaseFirestore firestore;
    private TextView moistureTextView, nitrogenTextView, phosphorusTextView, potassiumTextView,
            temperatureTextView, humidityTextView;
    private DatabaseReference soilDataRef;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soil_npk_checker);

        soilTypeSpinner = findViewById(R.id.soilTypeSpinner);
        moistureTextView = findViewById(R.id.tvMoisture);
        nitrogenTextView = findViewById(R.id.tvNitrogen);
        phosphorusTextView = findViewById(R.id.tvPhosphorus);
        potassiumTextView = findViewById(R.id.tvPotassium);
        temperatureTextView = findViewById(R.id.tvTemperature);
        humidityTextView = findViewById(R.id.tvHumidity);
        checkCompatibilityBtn = findViewById(R.id.checkCompatibilityBtn);
        compatibilityResult = findViewById(R.id.compatibilityResult);

        firestore = FirebaseFirestore.getInstance();

        // Populate soil types
        String[] soilTypes = {"Loamy", "Clayey", "Sandy", "Silty", "Peaty", "Chalky"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, soilTypes);
        soilTypeSpinner.setAdapter(adapter);

        checkCompatibilityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkSoilCompatibility();
            }
        });
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
                    Toast.makeText(SoilNpkCheckerActivity.this, "No data found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SoilNpkCheckerActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkSoilCompatibility() {
        String soilType = soilTypeSpinner.getSelectedItem().toString();
        String nStr = nitrogenTextView.getText().toString().trim();
        String pStr = phosphorusTextView.getText().toString().trim();
        String kStr = potassiumTextView.getText().toString().trim();

        if (nStr.isEmpty() || pStr.isEmpty() || kStr.isEmpty()) {
            Toast.makeText(this, "Please enter all NPK values.", Toast.LENGTH_SHORT).show();
            return;
        }

        float n = Float.parseFloat(nStr);
        float p = Float.parseFloat(pStr);
        float k = Float.parseFloat(kStr);

        String result;

        // Example thresholds for general soil health
        if (n < 80 || p < 40 || k < 40) {
            result = "Soil is low in nutrients. Add organic compost or NPK fertilizer.";
        } else if (n > 150 || p > 100 || k > 100) {
            result = "Nutrient levels are high. Avoid over-fertilization.";
        } else {
            result = "NPK levels are optimal for crops in " + soilType + " soil.";
        }

        compatibilityResult.setText(result);

        // Save to Firebase
        Map<String, Object> data = new HashMap<>();
        data.put("soilType", soilType);
        data.put("nitrogen", n);
        data.put("phosphorus", p);
        data.put("potassium", k);
        data.put("recommendation", result);
        data.put("timestamp", System.currentTimeMillis());

        firestore.collection("soilReports")
                .add(data)
                .addOnSuccessListener(documentReference ->
                        Toast.makeText(this, "Soil report saved.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to save soil report.", Toast.LENGTH_SHORT).show());
    }
}
