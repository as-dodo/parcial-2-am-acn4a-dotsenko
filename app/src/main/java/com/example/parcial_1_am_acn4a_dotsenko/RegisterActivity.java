package com.example.parcial_1_am_acn4a_dotsenko;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final String AVATAR_API_URL = "https://ui-avatars.com/api/";

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        EditText inputFullName = findViewById(R.id.inputFullName);
        EditText inputPhoneNumber = findViewById(R.id.inputPhoneNumber);
        EditText inputEmail = findViewById(R.id.inputEmail);
        EditText inputPassword = findViewById(R.id.inputPassword);
        Button btnCreateAccount = findViewById(R.id.btnCreateAccount);

        btnCreateAccount.setOnClickListener(v -> {
            String fullName = inputFullName.getText().toString().trim();
            String phoneNumber = inputPhoneNumber.getText().toString().trim();
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            if (TextUtils.isEmpty(fullName)
                    || TextUtils.isEmpty(phoneNumber)
                    || TextUtils.isEmpty(email)
                    || TextUtils.isEmpty(password)) {
                Toast.makeText(this, R.string.register_error_fill_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            btnCreateAccount.setEnabled(false);

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            btnCreateAccount.setEnabled(true);
                            Toast.makeText(this, R.string.register_error_create_account, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        FirebaseUser currentUser = auth.getCurrentUser();

                        if (currentUser == null) {
                            btnCreateAccount.setEnabled(true);
                            Toast.makeText(this, R.string.register_error_create_account, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String userId = currentUser.getUid();

                        Map<String, Object> userData = new HashMap<>();
                        userData.put("userId", userId);
                        userData.put("fullName", fullName);
                        userData.put("phoneNumber", phoneNumber);
                        userData.put("email", email);
                        userData.put("photoUrl", buildAvatarUrl(fullName, email));

                        UserProfileChangeRequest profileUpdates =
                                new UserProfileChangeRequest.Builder()
                                        .setDisplayName(fullName)
                                        .build();

                        currentUser.updateProfile(profileUpdates);

                        db.collection("users")
                                .document(userId)
                                .set(userData)
                                .addOnSuccessListener(unused -> {
                                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                })
                                .addOnFailureListener(e -> {
                                    btnCreateAccount.setEnabled(true);
                                    Toast.makeText(this,
                                            R.string.register_error_save_profile,
                                            Toast.LENGTH_SHORT).show();
                                });
                    });
        });
    }

    private String buildAvatarUrl(String fullName, String email) {
        String seed = !TextUtils.isEmpty(fullName) ? fullName : email;

        return Uri.parse(AVATAR_API_URL)
                .buildUpon()
                .appendQueryParameter("name", seed)
                .appendQueryParameter("background", "7E57C2")
                .appendQueryParameter("color", "FFFFFF")
                .appendQueryParameter("size", "256")
                .build()
                .toString();
    }
}
