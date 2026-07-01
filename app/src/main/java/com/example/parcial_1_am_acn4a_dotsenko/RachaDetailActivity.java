package com.example.parcial_1_am_acn4a_dotsenko;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RachaDetailActivity extends AppCompatActivity {

    public static final String EXTRA_NOMBRE = "nombre";
    public static final String EXTRA_NOMBRE_KEY = "nombreKey";
    public static final String EXTRA_ICONO = "icono";
    public static final String EXTRA_DIAS = "dias";
    public static final String EXTRA_COMPLETADA_HOY = "completadaHoy";

    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_RACHAS = "rachas";
    private static final String COLLECTION_FRIENDS = "friends";
    private static final String FIELD_NOMBRE_KEY = "nombreKey";
    private static final String FIELD_DIAS = "dias";
    private static final String FIELD_FULL_NAME = "fullName";
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_PHOTO_URL = "photoUrl";
    private static final String FIELD_FRIEND_USER_ID = "friendUserId";
    private static final String FIELD_MATCHED_RACHA_NAME = "matchedRachaName";
    private static final String FIELD_MATCHED_RACHA_ICON = "matchedRachaIcon";
    private static final String FIELD_MATCHED_RACHA_DAYS = "matchedRachaDays";
    private static final String FIELD_SELECTED_RACHAS = "selectedRachas";
    private static final String FIELD_CREATED_AT = "createdAt";
    private static final String FIELD_UPDATED_AT = "updatedAt";
    private static final String AVATAR_API_URL = "https://ui-avatars.com/api/";

    private LinearLayout sameRachaUsersContainer;
    private FirebaseFirestore db;
    private String currentUserId;
    private String nombre;
    private String nombreKey;
    private String icono;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_racha_detail);

        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        }

        TextView txtIcono = findViewById(R.id.txtDetailIcono);
        TextView txtNombre = findViewById(R.id.txtDetailNombre);
        TextView txtDias = findViewById(R.id.txtDetailDias);
        TextView txtEstado = findViewById(R.id.txtDetailEstado);
        TextView txtDescripcion = findViewById(R.id.txtDetailDescripcion);
        Button btnVolver = findViewById(R.id.btnDetailVolver);
        sameRachaUsersContainer = findViewById(R.id.sameRachaUsersContainer);

        nombre = getIntent().getStringExtra(EXTRA_NOMBRE);
        nombreKey = getIntent().getStringExtra(EXTRA_NOMBRE_KEY);
        icono = getIntent().getStringExtra(EXTRA_ICONO);
        int dias = getIntent().getIntExtra(EXTRA_DIAS, 0);
        boolean completadaHoy = getIntent().getBooleanExtra(EXTRA_COMPLETADA_HOY, false);

        if (nombre == null || nombre.trim().isEmpty()) {
            nombre = getString(R.string.detail_unknown_racha);
        }

        if (nombreKey == null || nombreKey.trim().isEmpty()) {
            nombreKey = normalizeNombreKey(nombre);
        }

        if (icono == null || icono.trim().isEmpty()) {
            icono = getString(R.string.default_racha_icon);
        }

        String estado = completadaHoy
                ? getString(R.string.detail_completed_today)
                : getString(R.string.detail_pending_today);

        String descripcion = completadaHoy
                ? getString(R.string.detail_description_completed)
                : getString(R.string.detail_description_pending);

        txtIcono.setText(icono);
        txtNombre.setText(nombre);
        txtDias.setText(getString(R.string.detail_days_format, dias));
        txtEstado.setText(getString(R.string.detail_status_format, estado));
        txtDescripcion.setText(descripcion);

        btnVolver.setOnClickListener(v -> finish());

        cargarUsuariosConMismaRacha();
    }

    private void cargarUsuariosConMismaRacha() {
        sameRachaUsersContainer.removeAllViews();
        addInfoRow(getString(R.string.detail_same_racha_loading));

        if (currentUserId == null) {
            sameRachaUsersContainer.removeAllViews();
            addInfoRow(getString(R.string.detail_same_racha_login_required));
            return;
        }

        db.collection(COLLECTION_USERS)
                .get()
                .addOnSuccessListener(userSnapshots -> {
                    sameRachaUsersContainer.removeAllViews();

                    int[] pendingUsers = {0};
                    int[] matches = {0};

                    for (DocumentSnapshot userDocument : userSnapshots.getDocuments()) {
                        String ownerUserId = userDocument.getId();
                        if (ownerUserId.equals(currentUserId)) {
                            continue;
                        }

                        pendingUsers[0]++;
                        cargarRachaCandidata(userDocument, pendingUsers, matches);
                    }

                    if (pendingUsers[0] == 0) {
                        addInfoRow(getString(R.string.detail_same_racha_empty));
                    }
                })
                .addOnFailureListener(e -> {
                    sameRachaUsersContainer.removeAllViews();
                    addInfoRow(getString(R.string.detail_same_racha_error));
                });
    }

    private void cargarRachaCandidata(
            DocumentSnapshot userDocument,
            int[] pendingUsers,
            int[] matches
    ) {
        String ownerUserId = userDocument.getId();
        db.collection(COLLECTION_USERS)
                .document(ownerUserId)
                .collection(COLLECTION_RACHAS)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot rachaDocument : task.getResult().getDocuments()) {
                            String rachaNombreKey = rachaDocument.getString(FIELD_NOMBRE_KEY);
                            if (!nombreKey.equals(rachaNombreKey)) {
                                continue;
                            }

                            Long diasValue = rachaDocument.getLong(FIELD_DIAS);
                            int dias = diasValue != null ? diasValue.intValue() : 0;
                            addCandidateRow(
                                    ownerUserId,
                                    getUserDisplayName(userDocument),
                                    userDocument.getString(FIELD_EMAIL),
                                    userDocument.getString(FIELD_PHOTO_URL),
                                    dias
                            );
                            matches[0]++;
                            break;
                        }
                    }

                    pendingUsers[0]--;
                    if (pendingUsers[0] == 0 && matches[0] == 0) {
                        addInfoRow(getString(R.string.detail_same_racha_empty));
                    }
                });
    }

    private String getUserDisplayName(DocumentSnapshot userDocument) {
        String fullName = userDocument.getString(FIELD_FULL_NAME);
        String email = userDocument.getString(FIELD_EMAIL);

        if (fullName != null && !fullName.trim().isEmpty()) {
            return fullName;
        }

        if (email != null && !email.trim().isEmpty()) {
            return email;
        }

        return getString(R.string.detail_unknown_user);
    }

    private void addInfoRow(String text) {
        TextView textView = new TextView(this);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        textView.setBackgroundResource(R.drawable.light_card_bg);
        textView.setPadding(dp(16), dp(16), dp(16), dp(16));
        textView.setText(text);
        textView.setTextColor(ContextCompat.getColor(this, R.color.gray_dark));
        textView.setTextSize(14f);
        sameRachaUsersContainer.addView(textView);
    }

    private void addCandidateRow(
            String friendUserId,
            String fullName,
            String email,
            String photoUrl,
            int dias
    ) {
        LinearLayout row = new LinearLayout(this);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rowParams.bottomMargin = dp(12);
        row.setLayoutParams(rowParams);
        row.setBackgroundResource(R.drawable.light_card_bg);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dp(14), dp(14), dp(14), dp(14));

        ImageView avatar = new ImageView(this);
        avatar.setLayoutParams(new LinearLayout.LayoutParams(dp(48), dp(48)));
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        cargarAvatar(avatar, photoUrl, fullName, email);
        row.addView(avatar);

        LinearLayout texts = new LinearLayout(this);
        LinearLayout.LayoutParams textsParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        );
        textsParams.setMargins(dp(12), 0, dp(12), 0);
        texts.setLayoutParams(textsParams);
        texts.setOrientation(LinearLayout.VERTICAL);

        TextView txtName = new TextView(this);
        txtName.setText(fullName);
        txtName.setTextColor(ContextCompat.getColor(this, R.color.black));
        txtName.setTextSize(16f);
        txtName.setTypeface(null, android.graphics.Typeface.BOLD);
        texts.addView(txtName);

        TextView txtRacha = new TextView(this);
        txtRacha.setText(getString(R.string.detail_same_racha_days_format, icono, nombre, dias));
        txtRacha.setTextColor(ContextCompat.getColor(this, R.color.gray_dark));
        txtRacha.setTextSize(14f);
        texts.addView(txtRacha);

        row.addView(texts);

        Button button = new Button(this);
        button.setText(R.string.detail_add_friend);
        button.setEnabled(false);

        DocumentReference friendReference = getFriendReference(friendUserId);
        friendReference.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && hasSelectedRacha(documentSnapshot)) {
                        button.setText(R.string.detail_friend_added);
                    } else {
                        button.setEnabled(true);
                    }
                })
                .addOnFailureListener(e -> button.setEnabled(true));

        button.setOnClickListener(v -> guardarAmigo(friendUserId, fullName, email, photoUrl, dias, button));
        row.addView(button);

        sameRachaUsersContainer.addView(row);
    }

    private void guardarAmigo(
            String friendUserId,
            String fullName,
            String email,
            String photoUrl,
            int dias,
            Button button
    ) {
        button.setEnabled(false);

        Map<String, Object> friendData = new HashMap<>();
        friendData.put(FIELD_FRIEND_USER_ID, friendUserId);
        friendData.put(FIELD_FULL_NAME, fullName);
        friendData.put(FIELD_EMAIL, email);
        friendData.put(FIELD_PHOTO_URL, photoUrl);
        friendData.put(FIELD_MATCHED_RACHA_NAME, nombre);
        friendData.put(FIELD_MATCHED_RACHA_ICON, icono);
        friendData.put(FIELD_MATCHED_RACHA_DAYS, dias);
        friendData.put(FIELD_CREATED_AT, FieldValue.serverTimestamp());
        friendData.put(FIELD_UPDATED_AT, FieldValue.serverTimestamp());

        DocumentReference friendReference = getFriendReference(friendUserId);

        friendReference
                .set(friendData, SetOptions.merge())
                .addOnSuccessListener(unused -> guardarRachaSeleccionada(friendReference, dias, button))
                .addOnFailureListener(e -> {
                    button.setEnabled(true);
                    Toast.makeText(this, R.string.detail_friend_add_error, Toast.LENGTH_SHORT).show();
                });
    }

    private DocumentReference getFriendReference(String friendUserId) {
        return db.collection(COLLECTION_USERS)
                .document(currentUserId)
                .collection(COLLECTION_FRIENDS)
                .document(friendUserId);
    }

    private boolean hasSelectedRacha(DocumentSnapshot friendDocument) {
        Object selectedRachasValue = friendDocument.get(FIELD_SELECTED_RACHAS);
        if (selectedRachasValue instanceof Map) {
            Map<?, ?> selectedRachas = (Map<?, ?>) selectedRachasValue;
            return selectedRachas.containsKey(nombreKey);
        }

        String matchedRachaName = friendDocument.getString(FIELD_MATCHED_RACHA_NAME);
        return nombreKey.equals(normalizeNombreKey(matchedRachaName));
    }

    private void guardarRachaSeleccionada(DocumentReference friendReference, int dias, Button button) {
        friendReference
                .update(FieldPath.of(FIELD_SELECTED_RACHAS, nombreKey), getSelectedRachaData(dias))
                .addOnSuccessListener(unused -> {
                    button.setText(R.string.detail_friend_added);
                    Toast.makeText(this, R.string.detail_friend_added_toast, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    button.setEnabled(true);
                    Toast.makeText(this, R.string.detail_friend_add_error, Toast.LENGTH_SHORT).show();
                });
    }

    private Map<String, Object> getSelectedRachaData(int dias) {
        Map<String, Object> selectedRacha = new HashMap<>();
        selectedRacha.put(FIELD_NOMBRE_KEY, nombreKey);
        selectedRacha.put(FIELD_MATCHED_RACHA_NAME, nombre);
        selectedRacha.put(FIELD_MATCHED_RACHA_ICON, icono);
        selectedRacha.put(FIELD_MATCHED_RACHA_DAYS, dias);
        selectedRacha.put(FIELD_UPDATED_AT, FieldValue.serverTimestamp());
        return selectedRacha;
    }

    private String normalizeNombreKey(String value) {
        if (value == null) {
            return "";
        }

        return value.trim().toLowerCase(Locale.ROOT);
    }

    private void cargarAvatar(ImageView avatar, String photoUrl, String fullName, String email) {
        String imageUrl = !TextUtils.isEmpty(photoUrl)
                ? photoUrl
                : buildAvatarUrl(!TextUtils.isEmpty(fullName) ? fullName : email);

        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.profile)
                .error(R.drawable.profile)
                .circleCrop()
                .into(avatar);
    }

    private String buildAvatarUrl(String fallbackText) {
        String seed = !TextUtils.isEmpty(fallbackText) ? fallbackText : getString(R.string.detail_unknown_user);

        return Uri.parse(AVATAR_API_URL)
                .buildUpon()
                .appendQueryParameter("name", seed)
                .appendQueryParameter("background", "7E57C2")
                .appendQueryParameter("color", "FFFFFF")
                .appendQueryParameter("size", "256")
                .build()
                .toString();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
