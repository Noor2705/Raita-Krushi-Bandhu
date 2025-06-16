package com.example.raitakrushibandhu;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.*;
import com.google.firebase.database.*;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class ForgotPasswordActivity extends AppCompatActivity {
    private static final String TAG = "ForgotPassword";

    private EditText etPhone, etOtp, etNewPassword, etConfirmPassword;
    private Button btnSendOtp, btnResetPassword;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    private int otpAttempts = 0;
    private long cooldownUntil = 0;
    private CountDownTimer otpTimer;

    private static final int MAX_OTP_ATTEMPTS       = 3;
    private static final long COOLDOWN_DURATION_MS  = 5 * 60 * 1000L;  // 5 minutes
    private static final long OTP_TIMEOUT_SEC       = 120L;           // 2 minutes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Bind views
        etPhone           = findViewById(R.id.etPhone);
        etOtp             = findViewById(R.id.etOtp);
        etNewPassword     = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSendOtp        = findViewById(R.id.btnSendOtp);
        btnResetPassword  = findViewById(R.id.btnResetPassword);

        // Firebase
        mAuth   = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("Users");

        btnSendOtp.setOnClickListener(v -> handleSendOtp());
        btnResetPassword.setOnClickListener(v -> handleResetPassword());
    }

    private void handleSendOtp() {
        String phone = etPhone.getText().toString().trim();
        long now = System.currentTimeMillis();

        // Cooldown check
        if (now < cooldownUntil) {
            long secsLeft = (cooldownUntil - now) / 1000;
            Toast.makeText(this,
                    "Too many attempts. Try again in " + secsLeft + "s",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Enter phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check number exists
        userRef.child(phone)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            startPhoneVerification("+91" + phone);
                        } else {
                            Toast.makeText(ForgotPasswordActivity.this,
                                    "Number not registered. Please sign up.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ForgotPasswordActivity.this,
                                "DB Error: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startPhoneVerification(String phoneNumber) {
        PhoneAuthOptions.Builder optionsBuilder = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(OTP_TIMEOUT_SEC, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(callbacks);

        if (resendToken != null) {
            optionsBuilder.setForceResendingToken(resendToken);
        }

        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build());
        otpAttempts++;
        startOtpCountdown();
    }

    private void startOtpCountdown() {
        if (otpTimer != null) otpTimer.cancel();
        btnSendOtp.setEnabled(false);

        otpTimer = new CountDownTimer(OTP_TIMEOUT_SEC * 1000, 1000) {
            @Override public void onTick(long ms) {
                btnSendOtp.setText("Wait " + (ms / 1000) + "s");
            }
            @Override public void onFinish() {
                btnSendOtp.setEnabled(true);
                btnSendOtp.setText("Send OTP");
                if (otpAttempts >= MAX_OTP_ATTEMPTS) {
                    cooldownUntil = System.currentTimeMillis() + COOLDOWN_DURATION_MS;
                }
            }
        }.start();
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    // Auto-retrieval
                    String code = credential.getSmsCode();
                    if (code != null) {
                        etOtp.setText(code);
                    }
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Log.e(TAG, "Verification failed", e);
                    Toast.makeText(ForgotPasswordActivity.this,
                            "OTP failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCodeSent(@NonNull String id,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    verificationId = id;
                    resendToken    = token;
                    Toast.makeText(ForgotPasswordActivity.this,
                            "OTP sent successfully",
                            Toast.LENGTH_SHORT).show();
                }
            };

    private void handleResetPassword() {
        String code    = etOtp.getText().toString().trim();
        String phone   = etPhone.getText().toString().trim();
        String pass    = etNewPassword.getText().toString().trim();
        String confirm = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(code)) {
            Toast.makeText(this, "Enter OTP", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isValidPassword(pass)) {
            Toast.makeText(this,
                    "Password must be ≥8 chars with upper, lower, digit & special",
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (!pass.equals(confirm)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        PhoneAuthCredential credential =
                PhoneAuthProvider.getCredential(verificationId, code);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userRef.child(phone).child("password").setValue(pass);
                        Toast.makeText(ForgotPasswordActivity.this,
                                "Password reset successful", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(
                                ForgotPasswordActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this,
                                "Invalid or expired OTP", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isValidPassword(String pw) {
        Pattern p = Pattern.compile(
                "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$"
        );
        return p.matcher(pw).matches();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (otpTimer != null) otpTimer.cancel();
    }
}
