package com.example.parcial_1_am_acn4a_dotsenko;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placeholder_page);

        TextView txtPageTitle = findViewById(R.id.txtPageTitle);
        TextView txtPageSubtitle = findViewById(R.id.txtPageSubtitle);

        txtPageTitle.setText(R.string.title_profile);
        txtPageSubtitle.setText(R.string.subtitle_profile);

        BottomNavigationHelper.setup(this, R.id.menuPerfil);
    }
}
