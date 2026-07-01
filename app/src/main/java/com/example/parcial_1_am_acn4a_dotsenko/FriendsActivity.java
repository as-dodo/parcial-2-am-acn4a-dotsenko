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

public class FriendsActivity extends AppCompatActivity {

    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_FRIENDS = "friends";
    private static final String FIELD_FULL_NAME = "fullName";
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_MATCHED_RACHA_NAME = "matchedRachaName";
    private static final String FIELD_MATCHED_RACHA_ICON = "matchedRachaIcon";
    private static final String FIELD_MATCHED_RACHA_DAYS = "matchedRachaDays";

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
        String rachaName = friendDocument.getString(FIELD_MATCHED_RACHA_NAME);
        String rachaIcon = friendDocument.getString(FIELD_MATCHED_RACHA_ICON);
        Long daysValue = friendDocument.getLong(FIELD_MATCHED_RACHA_DAYS);
        int days = daysValue != null ? daysValue.intValue() : 0;

        if (fullName == null || fullName.trim().isEmpty()) {
            fullName = getString(R.string.detail_unknown_user);
        }

        if (email == null) {
            email = "";
        }

        if (rachaName == null || rachaName.trim().isEmpty()) {
            rachaName = getString(R.string.detail_unknown_racha);
        }

        if (rachaIcon == null || rachaIcon.trim().isEmpty()) {
            rachaIcon = getString(R.string.default_racha_icon);
        }

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

        TextView txtRacha = new TextView(this);
        txtRacha.setText(getString(R.string.friends_matched_racha_format, rachaIcon, rachaName));
        txtRacha.setTextColor(ContextCompat.getColor(this, R.color.gray_dark));
        txtRacha.setTextSize(14f);
        texts.addView(txtRacha);

        row.addView(texts);

        TextView txtDays = new TextView(this);
        txtDays.setText(getString(R.string.racha_days_format, days));
        txtDays.setTextColor(ContextCompat.getColor(this, R.color.purple_main));
        txtDays.setTextSize(16f);
        txtDays.setTypeface(null, Typeface.BOLD);
        row.addView(txtDays);

        friendsContainer.addView(row);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
