package com.example.parcial_1_am_acn4a_dotsenko;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
    private static final String COLLECTION_RACHAS = "rachas";
    private static final String FIELD_FULL_NAME = "fullName";
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_NOMBRE = "nombre";
    private static final String FIELD_NOMBRE_KEY = "nombreKey";
    private static final String FIELD_ICONO = "icono";
    private static final String FIELD_DIAS = "dias";

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
                .collection(COLLECTION_RACHAS)
                .get()
                .addOnSuccessListener(myRachaSnapshots ->
                        cargarDocumentosDeAmigos(userId, getRachasByKey(myRachaSnapshots.getDocuments())))
                .addOnFailureListener(e -> {
                    friendsContainer.removeAllViews();
                    addInfoRow(getString(R.string.friends_load_error));
                    Toast.makeText(this, R.string.friends_load_error, Toast.LENGTH_SHORT).show();
                });
    }

    private void cargarDocumentosDeAmigos(String userId, Map<String, SharedRacha> myRachasByKey) {
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
                        addFriendRow(friendDocument, myRachasByKey);
                    }
                })
                .addOnFailureListener(e -> {
                    friendsContainer.removeAllViews();
                    addInfoRow(getString(R.string.friends_load_error));
                    Toast.makeText(this, R.string.friends_load_error, Toast.LENGTH_SHORT).show();
                });
    }

    private Map<String, SharedRacha> getRachasByKey(Iterable<DocumentSnapshot> rachaDocuments) {
        Map<String, SharedRacha> rachasByKey = new HashMap<>();

        for (DocumentSnapshot rachaDocument : rachaDocuments) {
            String nombre = rachaDocument.getString(FIELD_NOMBRE);
            String nombreKey = rachaDocument.getString(FIELD_NOMBRE_KEY);
            String icono = rachaDocument.getString(FIELD_ICONO);

            if (nombre == null || nombre.trim().isEmpty()) {
                nombre = getString(R.string.detail_unknown_racha);
            }

            if (nombreKey == null || nombreKey.trim().isEmpty()) {
                nombreKey = normalizeNombreKey(nombre);
            }

            if (icono == null || icono.trim().isEmpty()) {
                icono = getString(R.string.default_racha_icon);
            }

            if (!nombreKey.trim().isEmpty()) {
                rachasByKey.put(nombreKey, new SharedRacha(nombre, icono));
            }
        }

        return rachasByKey;
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

    private void addFriendRow(DocumentSnapshot friendDocument, Map<String, SharedRacha> myRachasByKey) {
        String fullName = friendDocument.getString(FIELD_FULL_NAME);
        String email = friendDocument.getString(FIELD_EMAIL);

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
        avatar.setImageResource(R.drawable.profile);
        avatar.setColorFilter(ContextCompat.getColor(this, R.color.purple_main));
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

        addCommonLoadingRow(commonContainer);
        friendsContainer.addView(card);

        db.collection(COLLECTION_USERS)
                .document(friendDocument.getId())
                .collection(COLLECTION_RACHAS)
                .get()
                .addOnSuccessListener(friendRachaSnapshots -> {
                    commonContainer.removeAllViews();
                    int matches = 0;

                    for (DocumentSnapshot rachaDocument : friendRachaSnapshots.getDocuments()) {
                        String nombre = rachaDocument.getString(FIELD_NOMBRE);
                        String nombreKey = rachaDocument.getString(FIELD_NOMBRE_KEY);

                        if (nombreKey == null || nombreKey.trim().isEmpty()) {
                            nombreKey = normalizeNombreKey(nombre);
                        }

                        SharedRacha myRacha = myRachasByKey.get(nombreKey);
                        if (myRacha == null) {
                            continue;
                        }

                        String icono = rachaDocument.getString(FIELD_ICONO);
                        Long daysValue = rachaDocument.getLong(FIELD_DIAS);
                        int days = daysValue != null ? daysValue.intValue() : 0;

                        if (icono == null || icono.trim().isEmpty()) {
                            icono = myRacha.icono;
                        }

                        addCommonRachaRow(commonContainer, myRacha.nombre, icono, days);
                        matches++;
                    }

                    if (matches == 0) {
                        addCommonInfoRow(commonContainer, getString(R.string.friends_common_rachas_empty));
                    }
                })
                .addOnFailureListener(e -> {
                    commonContainer.removeAllViews();
                    addCommonInfoRow(commonContainer, getString(R.string.friends_common_rachas_error));
                });
    }

    private void addCommonLoadingRow(LinearLayout container) {
        addCommonInfoRow(container, getString(R.string.friends_common_rachas_loading));
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

    private String normalizeNombreKey(String value) {
        if (value == null) {
            return "";
        }

        return value.trim().toLowerCase(Locale.ROOT);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private static class SharedRacha {
        final String nombre;
        final String icono;

        SharedRacha(String nombre, String icono) {
            this.nombre = nombre;
            this.icono = icono;
        }
    }
}
