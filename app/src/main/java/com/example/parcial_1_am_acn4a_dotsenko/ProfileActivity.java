package com.example.parcial_1_am_acn4a_dotsenko;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private TextView txtUserEmail;
    private TextView txtUserName;
    private TextView txtActiveRachas;
    private TextView txtCompletedToday;
    private TextView txtBestRacha;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        txtUserEmail = findViewById(R.id.txtUserEmail);
        txtUserName = findViewById(R.id.txtUserName);
        txtActiveRachas = findViewById(R.id.txtActiveRachas);
        txtCompletedToday = findViewById(R.id.txtCompletedToday);
        txtBestRacha = findViewById(R.id.txtBestRacha);
        Button btnLogout = findViewById(R.id.btnLogout);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            txtUserName.setText("Usuario");
            txtUserEmail.setVisibility(View.GONE);
        } else {
            String email = currentUser.getEmail();

            txtUserEmail.setText(email != null ? email : "");
            txtUserEmail.setVisibility(email != null && !email.isEmpty() ? View.VISIBLE : View.GONE);

            cargarPerfilUsuario(currentUser.getUid(), email);
        }

        btnLogout.setOnClickListener(v -> {
            auth.signOut();

            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        mostrarEstadisticasMock();
        BottomNavigationHelper.setup(this, R.id.menuPerfil);
    }

    private void mostrarEstadisticasMock() {
        int activeRachas = 5;
        int completedToday = 3;
        String bestRachaName = "No fumar";
        int bestRachaDays = 60;

        txtActiveRachas.setText(getString(R.string.profile_active_rachas, activeRachas));
        txtCompletedToday.setText(getString(R.string.profile_completed_today, completedToday));
        txtBestRacha.setText(getString(R.string.profile_best_streak, bestRachaName, bestRachaDays));
    }

    private void cargarPerfilUsuario(String userId, String email) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        mostrarFallbackUsuario(email);
                        return;
                    }

                    String fullName = documentSnapshot.getString("fullName");

                    if (fullName == null || fullName.trim().isEmpty()) {
                        mostrarFallbackUsuario(email);
                    } else {
                        txtUserName.setText(fullName);
                    }
                })
                .addOnFailureListener(e -> mostrarFallbackUsuario(email));
    }

    private void mostrarFallbackUsuario(String email) {
        if (email != null && !email.isEmpty()) {
            txtUserName.setText(email);
        } else {
            txtUserName.setText("Usuario");
        }
    }
}