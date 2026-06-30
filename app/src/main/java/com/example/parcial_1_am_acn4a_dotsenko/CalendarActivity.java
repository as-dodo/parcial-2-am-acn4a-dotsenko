package com.example.parcial_1_am_acn4a_dotsenko;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.HashSet;
import java.util.Set;

public class CalendarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        TextView txtPageTitle = findViewById(R.id.txtPageTitle);
        TextView txtPageSubtitle = findViewById(R.id.txtPageSubtitle);

        txtPageTitle.setText(R.string.title_calendar);
        txtPageSubtitle.setText(R.string.subtitle_calendar);

        BottomNavigationHelper.setup(this, R.id.menuCalendario);

        // Populate simple calendar grid (6 rows x 7 cols)
        GridLayout calendarGrid = findViewById(R.id.calendarGrid);
        calendarGrid.removeAllViews();

        int totalCells = 6 * 7;
        int day = 1;

        // sample days that had activity (show flame)
        Set<Integer> flameDays = new HashSet<>();
        flameDays.add(4);
        flameDays.add(5);
        flameDays.add(6);
        flameDays.add(9);
        flameDays.add(11);
        flameDays.add(13);
        flameDays.add(14);
        flameDays.add(16);
        flameDays.add(20);
        flameDays.add(24);

        // sample days with different status to color
        Set<Integer> greenDays = new HashSet<>();
        greenDays.add(9);
        greenDays.add(13);
        greenDays.add(14);
        greenDays.add(16);
        greenDays.add(20);

        Set<Integer> orangeDays = new HashSet<>();
        orangeDays.add(6);
        orangeDays.add(24);

        Set<Integer> pinkDays = new HashSet<>();
        pinkDays.add(11);

        for (int i = 0; i < totalCells; i++) {
            LinearLayout cell = new LinearLayout(this);
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = 0;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            lp.setMargins(4, 4, 4, 4);
            cell.setLayoutParams(lp);
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(Gravity.CENTER);

            TextView tvDay = new TextView(this);
            tvDay.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            tvDay.setTextSize(16f);
            tvDay.setTypeface(null, Typeface.NORMAL);

            if (day <= 31) {
                tvDay.setText(String.valueOf(day));

                // color backgrounds for sample statuses
                if (greenDays.contains(day)) {
                    tvDay.setBackgroundResource(android.R.color.transparent);
                    tvDay.setTextColor(ContextCompat.getColor(this, R.color.green));
                } else if (orangeDays.contains(day)) {
                    tvDay.setTextColor(ContextCompat.getColor(this, R.color.orange));
                } else if (pinkDays.contains(day)) {
                    tvDay.setTextColor(ContextCompat.getColor(this, R.color.pink));
                } else {
                    tvDay.setTextColor(ContextCompat.getColor(this, R.color.gray_dark));
                }

                // flame indicator as small emoji below number
                if (flameDays.contains(day)) {
                    TextView tvFlame = new TextView(this);
                    tvFlame.setText("🔥");
                    tvFlame.setTextSize(12f);
                    LinearLayout.LayoutParams flameLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    flameLp.topMargin = 4;
                    tvFlame.setLayoutParams(flameLp);
                    cell.addView(tvDay);
                    cell.addView(tvFlame);
                } else {
                    cell.addView(tvDay);
                }

                day++;
            } else {
                // empty cell
                tvDay.setText("");
                cell.addView(tvDay);
            }

            calendarGrid.addView(cell);
        }

        // Fill habits list mock
        LinearLayout habitsList = findViewById(R.id.habitsList);
        addHabitRow(habitsList, "Yoga");
        addHabitRow(habitsList, "Inglés");
        addHabitRow(habitsList, "Beber agua");
    }

    private void addHabitRow(LinearLayout parent, String title) {
        LinearLayout row = new LinearLayout(this);
        row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 8, 0, 8);

        TextView tv = new TextView(this);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        tv.setText(title);
        tv.setTextColor(ContextCompat.getColor(this, R.color.black));

        TextView check = new TextView(this);
        check.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        check.setText("✓");
        check.setTextColor(ContextCompat.getColor(this, R.color.green));

        row.addView(tv);
        row.addView(check);
        parent.addView(row);
    }

}
