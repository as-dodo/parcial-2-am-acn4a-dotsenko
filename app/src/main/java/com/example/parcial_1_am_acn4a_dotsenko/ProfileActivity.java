package com.example.parcial_1_am_acn4a_dotsenko;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_RACHAS = "rachas";
    private static final String FIELD_NOMBRE = "nombre";
    private static final String FIELD_DIAS = "dias";
    private static final String FIELD_LAST_COMPLETED_DATE = "lastCompletedDate";
    private static final String FIELD_FULL_NAME = "fullName";
    private static final String FIELD_PHONE_NUMBER = "phoneNumber";
    private static final String FIELD_PHOTO_URL = "photoUrl";
    private static final String FIELD_UPDATED_AT = "updatedAt";
    private static final String AVATAR_API_URL = "https://ui-avatars.com/api/";

    private ImageView imgProfile;
    private TextView txtUserEmail;
    private TextView txtUserName;
    private TextView txtUserPhone;
    private TextView txtActiveRachas;
    private TextView txtCompletedToday;
    private TextView txtBestRacha;
    private Button btnEditProfile;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String currentUserId;
    private String currentFullName = "";
    private String currentPhoneNumber = "";
    private String currentEmail = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        currentUserId = currentUser.getUid();
        currentEmail = currentUser.getEmail() != null ? currentUser.getEmail() : "";

        imgProfile = findViewById(R.id.imgProfile);
        txtUserEmail = findViewById(R.id.txtUserEmail);
        txtUserName = findViewById(R.id.txtUserName);
        txtUserPhone = findViewById(R.id.txtUserPhone);
        txtActiveRachas = findViewById(R.id.txtActiveRachas);
        txtCompletedToday = findViewById(R.id.txtCompletedToday);
        txtBestRacha = findViewById(R.id.txtBestRacha);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        Button btnLogout = findViewById(R.id.btnLogout);

        txtUserEmail.setText(currentEmail);
        txtUserEmail.setVisibility(!TextUtils.isEmpty(currentEmail) ? View.VISIBLE : View.GONE);

        cargarPerfilUsuario();
        cargarEstadisticas();

        btnEditProfile.setOnClickListener(v -> mostrarDialogEditarPerfil());
        btnLogout.setOnClickListener(v -> {
            auth.signOut();

            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        BottomNavigationHelper.setup(this, R.id.menuPerfil);
    }

    private void cargarEstadisticas() {
        db.collection(COLLECTION_USERS)
                .document(currentUserId)
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

    private void cargarPerfilUsuario() {
        db.collection(COLLECTION_USERS)
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        mostrarFallbackUsuario();
                        return;
                    }

                    String fullName = documentSnapshot.getString(FIELD_FULL_NAME);
                    String phoneNumber = documentSnapshot.getString(FIELD_PHONE_NUMBER);
                    String photoUrl = documentSnapshot.getString(FIELD_PHOTO_URL);

                    currentFullName = fullName != null ? fullName.trim() : "";
                    currentPhoneNumber = phoneNumber != null ? phoneNumber.trim() : "";

                    if (TextUtils.isEmpty(currentFullName)) {
                        mostrarFallbackUsuario();
                    } else {
                        txtUserName.setText(currentFullName);
                    }

                    mostrarTelefono(currentPhoneNumber);
                    cargarFotoPerfil(
                            photoUrl,
                            !TextUtils.isEmpty(currentFullName) ? currentFullName : currentEmail
                    );
                })
                .addOnFailureListener(e -> {
                    mostrarFallbackUsuario();
                    cargarFotoPerfil(null, currentEmail);
                });
    }

    private void mostrarTelefono(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            txtUserPhone.setVisibility(View.GONE);
            txtUserPhone.setText("");
            return;
        }

        txtUserPhone.setText(getString(R.string.profile_phone_format, phoneNumber));
        txtUserPhone.setVisibility(View.VISIBLE);
    }

    private void mostrarFallbackUsuario() {
        if (!TextUtils.isEmpty(currentEmail)) {
            txtUserName.setText(currentEmail);
        } else {
            txtUserName.setText(R.string.profile_user_fallback);
        }
    }

    private void mostrarDialogEditarPerfil() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);

        EditText inputFullName = dialogView.findViewById(R.id.inputProfileFullName);
        EditText inputPhone = dialogView.findViewById(R.id.inputProfilePhone);

        inputFullName.setText(currentFullName);
        inputPhone.setText(currentPhoneNumber);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_edit_profile_title)
                .setView(dialogView)
                .setPositiveButton(R.string.dialog_edit_profile_save, null)
                .setNegativeButton(R.string.dialog_edit_profile_cancel, null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String fullName = inputFullName.getText().toString().trim();
                String phoneNumber = inputPhone.getText().toString().trim();

                if (TextUtils.isEmpty(fullName)) {
                    inputFullName.setError(getString(R.string.dialog_edit_profile_name_required));
                    return;
                }

                if (TextUtils.isEmpty(phoneNumber)) {
                    inputPhone.setError(getString(R.string.dialog_edit_profile_phone_required));
                    return;
                }

                positiveButton.setEnabled(false);
                guardarPerfil(fullName, phoneNumber, dialog, positiveButton);
            });
        });

        dialog.show();
    }

    private void guardarPerfil(
            String fullName,
            String phoneNumber,
            AlertDialog dialog,
            Button positiveButton
    ) {
        String photoUrl = buildAvatarUrl(fullName);

        Map<String, Object> updates = new HashMap<>();
        updates.put(FIELD_FULL_NAME, fullName);
        updates.put(FIELD_PHONE_NUMBER, phoneNumber);
        updates.put(FIELD_PHOTO_URL, photoUrl);
        updates.put(FIELD_UPDATED_AT, FieldValue.serverTimestamp());

        db.collection(COLLECTION_USERS)
                .document(currentUserId)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    currentFullName = fullName;
                    currentPhoneNumber = phoneNumber;

                    txtUserName.setText(fullName);
                    mostrarTelefono(phoneNumber);
                    cargarFotoPerfil(photoUrl, fullName);

                    FirebaseUser currentUser = auth.getCurrentUser();
                    if (currentUser != null) {
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(fullName)
                                .build();
                        currentUser.updateProfile(profileUpdates);
                    }

                    Toast.makeText(this, R.string.profile_update_success, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    positiveButton.setEnabled(true);
                    Toast.makeText(this, R.string.profile_update_error, Toast.LENGTH_SHORT).show();
                });
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
        String seed = !TextUtils.isEmpty(fallbackText) ? fallbackText : getString(R.string.profile_user_fallback);

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
