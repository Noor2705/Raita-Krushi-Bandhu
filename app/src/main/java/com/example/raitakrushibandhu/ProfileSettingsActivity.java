package com.example.raitakrushibandhu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class ProfileSettingsActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private TextView tvName, tvPhone;
    private String currentUserPhone;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadNightMode();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);

        profileImageView = findViewById(R.id.profileImageView);
        tvName = findViewById(R.id.tvName);
        tvPhone = findViewById(R.id.tvPhone);
        Switch nightModeSwitch = findViewById(R.id.nightModeSwitch);
        Button editProfileBtn = findViewById(R.id.editProfileBtn);
        Button signOutBtn = findViewById(R.id.signOutBtn);

        prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUserPhone = prefs.getString("loggedInUser", null);

        loadUserInfo();

        // Edit Profile
        editProfileBtn.setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));

        // Sign Out
        signOutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            prefs.edit().remove("loggedInUser").apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        // Night Mode
        nightModeSwitch.setChecked(prefs.getBoolean("night_mode", false));
        nightModeSwitch.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean("night_mode", isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(isChecked ?
                    AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            recreate();
        });

        // Fullscreen preview on image click
        profileImageView.setOnClickListener(v -> {
            String imageUrl = (String) profileImageView.getTag(); // tag set during image load
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Intent intent = new Intent(this, ImagePreviewActivity.class);
                intent.putExtra("imageUrl", imageUrl);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserInfo(); // Reload when returning
    }

    private void loadUserInfo() {
        FirebaseDatabase.getInstance().getReference("Users").child(currentUserPhone)
                .get().addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue(String.class);
                        String phone = snapshot.child("phone").getValue(String.class);
                        String imageUrl = snapshot.child("profileImage").getValue(String.class);

                        tvName.setText("Name: " + (name != null ? name : ""));
                        tvPhone.setText("Phone: " + (phone != null ? phone : currentUserPhone));

                        if (imageUrl != null) {
                            profileImageView.setTag(imageUrl); // Save URL for preview
                            Glide.with(this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_profile_placeholder)
                                    .transform(new CircleCrop())
                                    .into(profileImageView);
                        } else {
                            profileImageView.setImageResource(R.drawable.ic_profile_placeholder);
                        }
                    }
                });
    }

    private void loadNightMode() {
        prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean nightMode = prefs.getBoolean("night_mode", false);
        AppCompatDelegate.setDefaultNightMode(nightMode ?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }
}
