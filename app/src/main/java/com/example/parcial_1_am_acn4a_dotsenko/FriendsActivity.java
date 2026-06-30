package com.example.parcial_1_am_acn4a_dotsenko;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class FriendsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        BottomNavigationHelper.setup(this, R.id.menuAmigos);
    }
}
