package com.example.raitakrushibandhu;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserMobile;
    private DatabaseReference userRef;
    private String mobileKey;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvUserName = findViewById(R.id.tvUserName);
        tvUserMobile = findViewById(R.id.tvUserMobile);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        mobileKey = prefs.getString("mobile", null);

        if (mobileKey == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference("Users").child(mobileKey);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                String mobile = snapshot.child("mobile").getValue(String.class);

                tvUserName.setText("Name: " + (name != null ? name : "N/A"));
                tvUserMobile.setText("Mobile: " + (mobile != null ? mobile : "N/A"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.profile);  // highlight current tab

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.home) {
                startActivity(new Intent(this, HomeActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.diagnose) {
                startActivity(new Intent(this, DiagnoseHistory.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.profile) {
                return true;
            }

            return false;
        });
    }
}
