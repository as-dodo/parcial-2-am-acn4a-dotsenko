package com.example.parcial_1_am_acn4a_dotsenko;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

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

            if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(phoneNumber)
                    || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, R.string.register_error_fill_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String userId = auth.getCurrentUser().getUid();
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("userId", userId);
                            userData.put("fullName", fullName);
                            userData.put("phoneNumber", phoneNumber);
                            userData.put("email", email);

                            db.collection("users")
                                    .add(userData)
                                    .addOnSuccessListener(documentReference -> {
                                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(this,
                                            R.string.register_error_save_profile,
                                            Toast.LENGTH_SHORT).show());
                        } else {
                            Toast.makeText(this, R.string.register_error_create_account, Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
