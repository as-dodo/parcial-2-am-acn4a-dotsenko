package com.example.parcial_1_am_acn4a_dotsenko;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CalendarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placeholder_page);

        TextView txtPageTitle = findViewById(R.id.txtPageTitle);
        TextView txtPageSubtitle = findViewById(R.id.txtPageSubtitle);

        txtPageTitle.setText(R.string.title_calendar);
        txtPageSubtitle.setText(R.string.subtitle_calendar);

        BottomNavigationHelper.setup(this, R.id.menuCalendario);
    }
}
