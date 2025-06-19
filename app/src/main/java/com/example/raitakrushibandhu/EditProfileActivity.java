package com.example.raitakrushibandhu;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText nameET, emailET;
    private ImageView profileImage;
    private Button saveBtn, changeImageBtn;
    private Uri selectedImageUri;
    private String currentUserPhone;

    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        nameET = findViewById(R.id.nameEditText);
        emailET = findViewById(R.id.emailEditText);
        profileImage = findViewById(R.id.profileImageView);
        saveBtn = findViewById(R.id.saveProfileBtn);
        changeImageBtn = findViewById(R.id.changeImageBtn);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUserPhone = prefs.getString("loggedInUser", null);
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserPhone);

        // Load current user info (optional)
        loadUserInfo();

        changeImageBtn.setOnClickListener(v -> openImagePicker());

        saveBtn.setOnClickListener(v -> saveProfile());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            profileImage.setImageURI(selectedImageUri);
        }
    }

    private void saveProfile() {
        String name = nameET.getText().toString().trim();
        String email = emailET.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Updating profile...");
        dialog.setCancelable(false);
        dialog.show();

        userRef.child("name").setValue(name);
        userRef.child("email").setValue(email);

        if (selectedImageUri != null) {
            FirebaseStorage.getInstance().getReference("ProfileImages")
                    .child(currentUserPhone + ".jpg")
                    .putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                userRef.child("profileImage").setValue(uri.toString());
                                dialog.dismiss();
                                Toast.makeText(EditProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                                finish();
                            }))
                    .addOnFailureListener(e -> {
                        dialog.dismiss();
                        Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show();
                    });
        } else {
            dialog.dismiss();
            Toast.makeText(EditProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadUserInfo() {
        // Optional: if you want to fetch current data and show
        userRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String name = snapshot.child("name").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);
                String imageUrl = snapshot.child("profileImage").getValue(String.class);

                nameET.setText(name);
                emailET.setText(email);

                if (imageUrl != null) {
                    Glide.with(this).load(imageUrl).into(profileImage);
                }
            }
        });
    }
}
