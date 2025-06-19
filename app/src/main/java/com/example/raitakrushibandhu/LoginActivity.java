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

        // Check session
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String loggedInUser = prefs.getString("loggedInUser", null);
        if (loggedInUser != null) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        phoneET          = findViewById(R.id.phoneET);
        passwordET       = findViewById(R.id.etPassword);
        loginBtn         = findViewById(R.id.btnLogin);
        registerBtn      = findViewById(R.id.btn_register);
        forgotPasswordTV = findViewById(R.id.tvForgotPassword);

        userRef = FirebaseDatabase.getInstance().getReference("Users");

        loginBtn.setOnClickListener(v -> loginUser());

        registerBtn.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        forgotPasswordTV.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void loginUser() {
        String phone = phoneET.getText().toString().trim();
        String pass  = passwordET.getText().toString().trim();

        if (phone.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            return;
        }

        userRef.child(phone)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {
                        if (snap.exists()) {
                            String stored = snap.child("password").getValue(String.class);
                            if (pass.equals(stored)) {
                                Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                                // Save session
                                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                                prefs.edit().putString("loggedInUser", phone).apply();

                                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "User not found. Please register.", Toast.LENGTH_SHORT).show();
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
