package com.example.parcial_1_am_acn4a_dotsenko;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FriendsActivity extends AppCompatActivity {

    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_FRIENDS = "friends";
    private static final String FIELD_FULL_NAME = "fullName";
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_PHOTO_URL = "photoUrl";
    private static final String FIELD_NOMBRE_KEY = "nombreKey";
    private static final String FIELD_MATCHED_RACHA_NAME = "matchedRachaName";
    private static final String FIELD_MATCHED_RACHA_ICON = "matchedRachaIcon";
    private static final String FIELD_MATCHED_RACHA_DAYS = "matchedRachaDays";
    private static final String FIELD_SELECTED_RACHAS = "selectedRachas";
    private static final String AVATAR_API_URL = "https://ui-avatars.com/api/";

    private LinearLayout friendsContainer;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        friendsContainer = findViewById(R.id.friendsContainer);
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        BottomNavigationHelper.setup(this, R.id.menuAmigos);
        cargarAmigos(currentUser.getUid());
    }

    private void cargarAmigos(String userId) {
        friendsContainer.removeAllViews();
        addInfoRow(getString(R.string.friends_loading));

        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_FRIENDS)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    friendsContainer.removeAllViews();

                    if (queryDocumentSnapshots.isEmpty()) {
                        addInfoRow(getString(R.string.friends_empty));
                        return;
                    }

                    for (DocumentSnapshot friendDocument : queryDocumentSnapshots.getDocuments()) {
                        addFriendRow(friendDocument);
                    }
                })
                .addOnFailureListener(e -> {
                    friendsContainer.removeAllViews();
                    addInfoRow(getString(R.string.friends_load_error));
                    Toast.makeText(this, R.string.friends_load_error, Toast.LENGTH_SHORT).show();
                });
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
        friendsContainer.addView(textView);
    }

    private void addFriendRow(DocumentSnapshot friendDocument) {
        String fullName = friendDocument.getString(FIELD_FULL_NAME);
        String email = friendDocument.getString(FIELD_EMAIL);
        String photoUrl = friendDocument.getString(FIELD_PHOTO_URL);
        Map<String, SharedRacha> selectedRachas = getSelectedRachas(friendDocument);

        if (fullName == null || fullName.trim().isEmpty()) {
            fullName = getString(R.string.detail_unknown_user);
        }

        if (email == null) {
            email = "";
        }

        LinearLayout card = new LinearLayout(this);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rowParams.bottomMargin = dp(12);
        card.setLayoutParams(rowParams);
        card.setBackgroundResource(R.drawable.light_card_bg);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));

        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);

        ImageView avatar = new ImageView(this);
        avatar.setLayoutParams(new LinearLayout.LayoutParams(dp(48), dp(48)));
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        cargarAvatar(avatar, photoUrl, fullName, email);
        row.addView(avatar);

        LinearLayout texts = new LinearLayout(this);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        );
        textParams.setMargins(dp(12), 0, dp(12), 0);
        texts.setLayoutParams(textParams);
        texts.setOrientation(LinearLayout.VERTICAL);

        TextView txtName = new TextView(this);
        txtName.setText(fullName);
        txtName.setTextColor(ContextCompat.getColor(this, R.color.black));
        txtName.setTextSize(16f);
        txtName.setTypeface(null, Typeface.BOLD);
        texts.addView(txtName);

        TextView txtEmail = new TextView(this);
        txtEmail.setText(email);
        txtEmail.setTextColor(ContextCompat.getColor(this, R.color.gray_dark));
        txtEmail.setTextSize(13f);
        texts.addView(txtEmail);

        row.addView(texts);
        card.addView(row);

        TextView txtCommonTitle = new TextView(this);
        txtCommonTitle.setText(R.string.friends_common_rachas_title);
        txtCommonTitle.setTextColor(ContextCompat.getColor(this, R.color.purple_main));
        txtCommonTitle.setTextSize(15f);
        txtCommonTitle.setTypeface(null, Typeface.BOLD);
        txtCommonTitle.setPadding(0, dp(12), 0, dp(4));
        card.addView(txtCommonTitle);

        LinearLayout commonContainer = new LinearLayout(this);
        commonContainer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        commonContainer.setOrientation(LinearLayout.VERTICAL);
        card.addView(commonContainer);

        if (selectedRachas.isEmpty()) {
            addCommonInfoRow(commonContainer, getString(R.string.friends_common_rachas_empty));
        } else {
            for (SharedRacha racha : selectedRachas.values()) {
                addCommonRachaRow(commonContainer, racha.nombre, racha.icono, racha.days);
            }
        }

        friendsContainer.addView(card);
    }

    private void addCommonInfoRow(LinearLayout container, String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextColor(ContextCompat.getColor(this, R.color.gray_dark));
        textView.setTextSize(13f);
        container.addView(textView);
    }

    private void addCommonRachaRow(LinearLayout container, String nombre, String icono, int days) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(4), 0, dp(4));

        TextView txtName = new TextView(this);
        txtName.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        ));
        txtName.setText(getString(R.string.friends_common_racha_format, icono, nombre));
        txtName.setTextColor(ContextCompat.getColor(this, R.color.gray_dark));
        txtName.setTextSize(14f);
        row.addView(txtName);

        TextView txtDays = new TextView(this);
        txtDays.setText(getString(R.string.racha_days_format, days));
        txtDays.setTextColor(ContextCompat.getColor(this, R.color.purple_main));
        txtDays.setTextSize(14f);
        txtDays.setTypeface(null, Typeface.BOLD);
        row.addView(txtDays);

        container.addView(row);
    }

    private Map<String, SharedRacha> getSelectedRachas(DocumentSnapshot friendDocument) {
        Map<String, SharedRacha> selectedRachas = new HashMap<>();
        Object selectedRachasValue = friendDocument.get(FIELD_SELECTED_RACHAS);

        if (selectedRachasValue instanceof Map) {
            Map<?, ?> selectedRachasMap = (Map<?, ?>) selectedRachasValue;

            for (Map.Entry<?, ?> entry : selectedRachasMap.entrySet()) {
                if (!(entry.getValue() instanceof Map)) {
                    continue;
                }

                SharedRacha racha = getSharedRachaFromMap((Map<?, ?>) entry.getValue());
                String nombreKey = getStringValue(entry.getKey());
                if (nombreKey.trim().isEmpty()) {
                    nombreKey = getStringValue(((Map<?, ?>) entry.getValue()).get(FIELD_NOMBRE_KEY));
                }
                if (nombreKey.trim().isEmpty()) {
                    nombreKey = normalizeNombreKey(racha.nombre);
                }

                if (!nombreKey.trim().isEmpty()) {
                    selectedRachas.put(nombreKey, racha);
                }
            }
        }

        if (selectedRachas.isEmpty()) {
            SharedRacha legacyRacha = getLegacySharedRacha(friendDocument);
            if (legacyRacha != null) {
                String legacyKey = normalizeNombreKey(legacyRacha.nombre);
                if (!legacyKey.trim().isEmpty()) {
                    selectedRachas.put(legacyKey, legacyRacha);
                }
            }
        }

        return selectedRachas;
    }

    private SharedRacha getSharedRachaFromMap(Map<?, ?> rachaData) {
        String nombre = getStringValue(rachaData.get(FIELD_MATCHED_RACHA_NAME));
        String icono = getStringValue(rachaData.get(FIELD_MATCHED_RACHA_ICON));
        int days = getIntValue(rachaData.get(FIELD_MATCHED_RACHA_DAYS));

        if (nombre.trim().isEmpty()) {
            nombre = getString(R.string.detail_unknown_racha);
        }

        if (icono.trim().isEmpty()) {
            icono = getString(R.string.default_racha_icon);
        }

        return new SharedRacha(nombre, icono, days);
    }

    private SharedRacha getLegacySharedRacha(DocumentSnapshot friendDocument) {
        String nombre = friendDocument.getString(FIELD_MATCHED_RACHA_NAME);
        String icono = friendDocument.getString(FIELD_MATCHED_RACHA_ICON);
        Long daysValue = friendDocument.getLong(FIELD_MATCHED_RACHA_DAYS);

        if (nombre == null || nombre.trim().isEmpty()) {
            return null;
        }

        if (icono == null || icono.trim().isEmpty()) {
            icono = getString(R.string.default_racha_icon);
        }

        return new SharedRacha(nombre, icono, daysValue != null ? daysValue.intValue() : 0);
    }

    private String getStringValue(Object value) {
        return value instanceof String ? (String) value : "";
    }

    private int getIntValue(Object value) {
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }

        if (value instanceof Integer) {
            return (Integer) value;
        }

        return 0;
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

    private static class SharedRacha {
        final String nombre;
        final String icono;
        final int days;

        SharedRacha(String nombre, String icono, int days) {
            this.nombre = nombre;
            this.icono = icono;
            this.days = days;
        }
    }
}
