package com.example.raitakrushibandhu;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("Users");

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void openRegister(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    public void openLogin(View view) {
        startActivity(new Intent(this, LoginActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.setting) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        if (id == R.id.feedback) {
            startActivity(new Intent(this, FeedbackActivity.class));
            return true;
        }

        if (id == R.id.contact) {
            startActivity(new Intent(this, ContactActivity.class));
            return true;
        }

        if (id == R.id.thx) {
            startActivity(new Intent(this, ThanksActivity.class));
            return true;
        }

        if (id == R.id.legal_notices) {
            startActivity(new Intent(this, LegalNoticesActivity.class));
            return true;
        }

        if (id == R.id.quickstart) {
            startActivity(new Intent(this, QuickStartActivity.class));
            return true;
        }

        if (id == R.id.menu_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish(); // Close current activity
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}

