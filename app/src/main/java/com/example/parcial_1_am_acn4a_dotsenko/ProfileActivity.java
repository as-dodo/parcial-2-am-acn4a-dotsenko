package com.example.parcial_1_am_acn4a_dotsenko;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_RACHAS = "rachas";
    private static final String FIELD_NOMBRE = "nombre";
    private static final String FIELD_DIAS = "dias";
    private static final String FIELD_LAST_COMPLETED_DATE = "lastCompletedDate";
    private static final String FIELD_FULL_NAME = "fullName";
    private static final String FIELD_PHOTO_URL = "photoUrl";
    private static final String AVATAR_API_URL = "https://ui-avatars.com/api/";

    private ImageView imgProfile;
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

        imgProfile = findViewById(R.id.imgProfile);
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
            cargarFotoPerfil(null, "Usuario");
            mostrarEstadisticas(0, 0, getString(R.string.profile_no_rachas), 0);
        } else {
            String email = currentUser.getEmail();

            txtUserEmail.setText(email != null ? email : "");
            txtUserEmail.setVisibility(email != null && !email.isEmpty() ? View.VISIBLE : View.GONE);

            cargarPerfilUsuario(currentUser.getUid(), email);
            cargarEstadisticas(currentUser.getUid());
        }

        btnLogout.setOnClickListener(v -> {
            auth.signOut();

            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        BottomNavigationHelper.setup(this, R.id.menuPerfil);
    }

    private void cargarEstadisticas(String userId) {
        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_RACHAS)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int activeRachas = queryDocumentSnapshots.size();
                    int completedToday = 0;
                    String bestRachaName = getString(R.string.profile_no_rachas);
                    int bestRachaDays = 0;

                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                        String lastCompletedDate = documentSnapshot.getString(FIELD_LAST_COMPLETED_DATE);
                        if (getTodayDateKey().equals(lastCompletedDate)) {
                            completedToday++;
                        }

                        Long diasValue = documentSnapshot.getLong(FIELD_DIAS);
                        int dias = diasValue != null ? diasValue.intValue() : 0;
                        if (dias > bestRachaDays) {
                            bestRachaDays = dias;

                            String nombre = documentSnapshot.getString(FIELD_NOMBRE);
                            if (nombre != null && !nombre.trim().isEmpty()) {
                                bestRachaName = nombre;
                            }
                        }
                    }

                    mostrarEstadisticas(activeRachas, completedToday, bestRachaName, bestRachaDays);
                })
                .addOnFailureListener(e ->
                        mostrarEstadisticas(0, 0, getString(R.string.profile_no_rachas), 0));
    }

    private void mostrarEstadisticas(
            int activeRachas,
            int completedToday,
            String bestRachaName,
            int bestRachaDays
    ) {
        txtActiveRachas.setText(getString(R.string.profile_active_rachas, activeRachas));
        txtCompletedToday.setText(getString(R.string.profile_completed_today, completedToday));
        txtBestRacha.setText(getString(R.string.profile_best_streak, bestRachaName, bestRachaDays));
    }

    private void cargarPerfilUsuario(String userId, String email) {
        db.collection(COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        mostrarFallbackUsuario(email);
                        return;
                    }

                    String fullName = documentSnapshot.getString(FIELD_FULL_NAME);
                    String photoUrl = documentSnapshot.getString(FIELD_PHOTO_URL);

                    if (fullName == null || fullName.trim().isEmpty()) {
                        mostrarFallbackUsuario(email);
                        cargarFotoPerfil(photoUrl, email);
                    } else {
                        txtUserName.setText(fullName);
                        cargarFotoPerfil(photoUrl, fullName);
                    }
                })
                .addOnFailureListener(e -> {
                    mostrarFallbackUsuario(email);
                    cargarFotoPerfil(null, email);
                });
    }

    private void mostrarFallbackUsuario(String email) {
        if (email != null && !email.isEmpty()) {
            txtUserName.setText(email);
        } else {
            txtUserName.setText("Usuario");
        }
    }

    private void cargarFotoPerfil(String photoUrl, String fallbackText) {
        String imageUrl = !TextUtils.isEmpty(photoUrl)
                ? photoUrl
                : buildAvatarUrl(fallbackText);

        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.profile)
                .error(R.drawable.profile)
                .circleCrop()
                .into(imgProfile);
    }

    private String buildAvatarUrl(String fallbackText) {
        String seed = !TextUtils.isEmpty(fallbackText) ? fallbackText : "Usuario";

        return Uri.parse(AVATAR_API_URL)
                .buildUpon()
                .appendQueryParameter("name", seed)
                .appendQueryParameter("background", "7E57C2")
                .appendQueryParameter("color", "FFFFFF")
                .appendQueryParameter("size", "256")
                .build()
                .toString();
    }

    private String getTodayDateKey() {
        SimpleDateFormat storageDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return storageDateFormat.format(Calendar.getInstance().getTime());
    }
}
