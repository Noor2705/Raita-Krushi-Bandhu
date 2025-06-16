package com.example.raitakrushibandhu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

public class LoginActivity extends AppCompatActivity {

    private EditText phoneET, passwordET;
    private Button loginBtn, registerBtn;
    private TextView forgotPasswordTV;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user already logged in
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String loggedInUser = prefs.getString("loggedInUser", null);
        if (loggedInUser != null) {
            // User already logged in, go to HomeActivity directly
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish(); // close login activity so user can't come back with back button
            return;
        }

        setContentView(R.layout.activity_login);

        // Bind views
        phoneET          = findViewById(R.id.phoneET);
        passwordET       = findViewById(R.id.etPassword);
        loginBtn         = findViewById(R.id.btnLogin);
        registerBtn      = findViewById(R.id.btn_register);
        forgotPasswordTV = findViewById(R.id.tvForgotPassword);

        // Firebase Realtime Database “Users” node
        userRef = FirebaseDatabase.getInstance().getReference("Users");

        // Login flow
        loginBtn.setOnClickListener(v -> loginUser());

        // Navigate to Register
        registerBtn.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

        // Navigate to Forgot Password
        forgotPasswordTV.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class))
        );
    }

    private void loginUser() {
        String phone = phoneET.getText().toString().trim();
        String pass  = passwordET.getText().toString().trim();

        if (phone.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this,
                    "Please fill in both fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Check phone node in DB
        userRef.child(phone)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {
                        if (snap.exists()) {
                            String stored = snap.child("password").getValue(String.class);
                            if (pass.equals(stored)) {
                                Toast.makeText(LoginActivity.this,
                                        "Login successful",
                                        Toast.LENGTH_SHORT).show();

                                // Save logged in user session in SharedPreferences
                                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                                prefs.edit().putString("loggedInUser", phone).apply();

                                // Start HomeActivity
                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this,
                                        "Incorrect password",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this,
                                    "User not found. Please register.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(LoginActivity.this,
                                "Database error: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
