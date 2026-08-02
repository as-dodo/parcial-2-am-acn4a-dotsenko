package com.example.parcial_1_am_acn4a_dotsenko;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarActivity extends AppCompatActivity {

    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_RACHAS = "rachas";
    private static final String COLLECTION_COMPLETIONS = "completions";
    private static final String FIELD_NOMBRE = "nombre";
    private static final String FIELD_ICONO = "icono";
    private static final String FIELD_DIAS = "dias";
    private static final String FIELD_DATE = "date";

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String currentUserId;

    private TextView monthTitle;
    private TextView txtDaysCompleted;
    private TextView txtBestStreak;
    private TextView txtHabitsSectionTitle;
    private Button btnPreviousMonth;
    private Button btnNextMonth;
    private GridLayout calendarGrid;
    private LinearLayout habitsList;

    private final List<RachaInfo> rachas = new ArrayList<>();
    /** dateKey (yyyy-MM-dd) -> list of completions that day */
    private final Map<String, List<CompletionInfo>> completionsByDate = new HashMap<>();

    private Calendar visibleMonth;
    private String selectedDateKey;
    private int monthLoadGeneration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        currentUserId = currentUser.getUid();

        TextView txtPageTitle = findViewById(R.id.txtPageTitle);
        TextView txtPageSubtitle = findViewById(R.id.txtPageSubtitle);
        txtPageTitle.setText(R.string.title_calendar);
        txtPageSubtitle.setText(R.string.subtitle_calendar);

        monthTitle = findViewById(R.id.monthTitle);
        txtDaysCompleted = findViewById(R.id.txtDaysCompleted);
        txtBestStreak = findViewById(R.id.txtBestStreak);
        txtHabitsSectionTitle = findViewById(R.id.txtHabitsSectionTitle);
        btnPreviousMonth = findViewById(R.id.btnPreviousMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        calendarGrid = findViewById(R.id.calendarGrid);
        habitsList = findViewById(R.id.habitsList);

        visibleMonth = Calendar.getInstance();
        visibleMonth.set(Calendar.DAY_OF_MONTH, 1);
        selectedDateKey = formatDateKey(Calendar.getInstance());

        btnPreviousMonth.setOnClickListener(v -> cambiarMes(-1));
        btnNextMonth.setOnClickListener(v -> cambiarMes(1));

        BottomNavigationHelper.setup(this, R.id.menuCalendario);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentUserId != null) {
            cargarDatosDelMes();
        }
    }

    private void cargarDatosDelMes() {
        int loadGeneration = ++monthLoadGeneration;
        String monthStart = getMonthStartKey(visibleMonth);
        String monthEnd = getMonthEndKey(visibleMonth);

        db.collection(COLLECTION_USERS)
                .document(currentUserId)
                .collection(COLLECTION_RACHAS)
                .get()
                .addOnSuccessListener(rachaSnapshots -> {
                    if (loadGeneration != monthLoadGeneration) {
                        return;
                    }

                    rachas.clear();
                    completionsByDate.clear();

                    List<Task<QuerySnapshot>> completionTasks = new ArrayList<>();
                    List<RachaInfo> loadedRachas = new ArrayList<>();

                    for (DocumentSnapshot documentSnapshot : rachaSnapshots.getDocuments()) {
                        RachaInfo racha = RachaInfo.fromDocument(documentSnapshot);
                        loadedRachas.add(racha);

                        Task<QuerySnapshot> task = documentSnapshot.getReference()
                                .collection(COLLECTION_COMPLETIONS)
                                .whereGreaterThanOrEqualTo(FIELD_DATE, monthStart)
                                .whereLessThanOrEqualTo(FIELD_DATE, monthEnd)
                                .get();
                        completionTasks.add(task);
                    }

                    rachas.addAll(loadedRachas);

                    if (completionTasks.isEmpty()) {
                        renderUi();
                        return;
                    }

                    Tasks.whenAllSuccess(completionTasks)
                            .addOnSuccessListener(results -> {
                                if (loadGeneration != monthLoadGeneration) {
                                    return;
                                }

                                for (int i = 0; i < results.size(); i++) {
                                    QuerySnapshot snapshot = (QuerySnapshot) results.get(i);
                                    RachaInfo racha = loadedRachas.get(i);

                                    for (DocumentSnapshot completionDoc : snapshot.getDocuments()) {
                                        String dateKey = completionDoc.getString(FIELD_DATE);
                                        if (dateKey == null || dateKey.trim().isEmpty()) {
                                            dateKey = completionDoc.getId();
                                        }

                                        String nombre = completionDoc.getString(FIELD_NOMBRE);
                                        String icono = completionDoc.getString(FIELD_ICONO);
                                        if (nombre == null || nombre.trim().isEmpty()) {
                                            nombre = racha.nombre;
                                        }
                                        if (icono == null || icono.trim().isEmpty()) {
                                            icono = racha.icono;
                                        }

                                        CompletionInfo completion = new CompletionInfo(
                                                racha.id,
                                                nombre,
                                                icono
                                        );
                                        List<CompletionInfo> dayList = completionsByDate.get(dateKey);
                                        if (dayList == null) {
                                            dayList = new ArrayList<>();
                                            completionsByDate.put(dateKey, dayList);
                                        }
                                        dayList.add(completion);
                                    }
                                }

                                renderUi();
                            })
                            .addOnFailureListener(e -> {
                                if (loadGeneration != monthLoadGeneration) {
                                    return;
                                }

                                Toast.makeText(this, R.string.calendar_load_error, Toast.LENGTH_SHORT).show();
                                renderUi();
                            });
                })
                .addOnFailureListener(e -> {
                    if (loadGeneration != monthLoadGeneration) {
                        return;
                    }

                    Toast.makeText(this, R.string.calendar_load_error, Toast.LENGTH_SHORT).show();
                    rachas.clear();
                    completionsByDate.clear();
                    renderUi();
                });
    }

    private void renderUi() {
        actualizarTituloMes();
        dibujarCalendario();
        actualizarEstadisticas();
        mostrarHabitosDelDiaSeleccionado();
    }

    private void actualizarTituloMes() {
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        String title = monthFormat.format(visibleMonth.getTime());
        if (!title.isEmpty()) {
            title = Character.toUpperCase(title.charAt(0)) + title.substring(1);
        }
        monthTitle.setText(title);
        btnNextMonth.setEnabled(!isCurrentMonth());
        btnNextMonth.setAlpha(btnNextMonth.isEnabled() ? 1f : 0.4f);
    }

    private void cambiarMes(int monthDelta) {
        Calendar targetMonth = (Calendar) visibleMonth.clone();
        targetMonth.add(Calendar.MONTH, monthDelta);
        targetMonth.set(Calendar.DAY_OF_MONTH, 1);

        if (isAfterCurrentMonth(targetMonth)) {
            return;
        }

        visibleMonth = targetMonth;
        selectedDateKey = isCurrentMonth()
                ? formatDateKey(Calendar.getInstance())
                : getMonthStartKey(visibleMonth);

        renderUi();
        cargarDatosDelMes();
    }

    private boolean isCurrentMonth() {
        Calendar today = Calendar.getInstance();
        return visibleMonth.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                && visibleMonth.get(Calendar.MONTH) == today.get(Calendar.MONTH);
    }

    private boolean isAfterCurrentMonth(Calendar month) {
        Calendar currentMonth = Calendar.getInstance();
        currentMonth.set(Calendar.DAY_OF_MONTH, 1);
        currentMonth.set(Calendar.HOUR_OF_DAY, 0);
        currentMonth.set(Calendar.MINUTE, 0);
        currentMonth.set(Calendar.SECOND, 0);
        currentMonth.set(Calendar.MILLISECOND, 0);

        Calendar normalizedMonth = (Calendar) month.clone();
        normalizedMonth.set(Calendar.DAY_OF_MONTH, 1);
        normalizedMonth.set(Calendar.HOUR_OF_DAY, 0);
        normalizedMonth.set(Calendar.MINUTE, 0);
        normalizedMonth.set(Calendar.SECOND, 0);
        normalizedMonth.set(Calendar.MILLISECOND, 0);
        return normalizedMonth.after(currentMonth);
    }

    private void dibujarCalendario() {
        calendarGrid.removeAllViews();

        Calendar gridCursor = (Calendar) visibleMonth.clone();
        int firstDayOfWeek = gridCursor.get(Calendar.DAY_OF_WEEK);
        // Calendar: Sunday=1 ... Saturday=7. Convert to Monday-first offset 0..6
        int leadingEmpty = (firstDayOfWeek + 5) % 7;
        int daysInMonth = gridCursor.getActualMaximum(Calendar.DAY_OF_MONTH);
        int totalCells = 42;

        String todayKey = formatDateKey(Calendar.getInstance());

        for (int cellIndex = 0; cellIndex < totalCells; cellIndex++) {
            LinearLayout cell = new LinearLayout(this);
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = 0;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            lp.setMargins(4, 4, 4, 4);
            cell.setLayoutParams(lp);
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(Gravity.CENTER);
            cell.setPadding(4, 8, 4, 8);

            int dayNumber = cellIndex - leadingEmpty + 1;
            boolean inMonth = dayNumber >= 1 && dayNumber <= daysInMonth;

            TextView tvDay = new TextView(this);
            tvDay.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            tvDay.setTextSize(16f);
            tvDay.setTypeface(null, Typeface.NORMAL);

            if (!inMonth) {
                tvDay.setText("");
                cell.addView(tvDay);
                calendarGrid.addView(cell);
                continue;
            }

            String dateKey = buildDateKey(visibleMonth.get(Calendar.YEAR),
                    visibleMonth.get(Calendar.MONTH), dayNumber);
            List<CompletionInfo> dayCompletions = completionsByDate.get(dateKey);
            int completedCount = countUniqueRachas(dayCompletions);

            tvDay.setText(String.valueOf(dayNumber));

            if (completedCount >= rachas.size() && !rachas.isEmpty()) {
                tvDay.setTextColor(ContextCompat.getColor(this, R.color.green));
            } else if (completedCount > 0) {
                tvDay.setTextColor(ContextCompat.getColor(this, R.color.orange));
            } else if (dateKey.equals(todayKey) && !rachas.isEmpty()) {
                tvDay.setTextColor(ContextCompat.getColor(this, R.color.pink));
            } else {
                tvDay.setTextColor(ContextCompat.getColor(this, R.color.gray_dark));
            }

            if (dateKey.equals(selectedDateKey)) {
                tvDay.setTypeface(null, Typeface.BOLD);
                cell.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
            }

            cell.addView(tvDay);

            if (completedCount > 0) {
                TextView tvFlame = new TextView(this);
                tvFlame.setText("🔥");
                tvFlame.setTextSize(12f);
                LinearLayout.LayoutParams flameLp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                flameLp.topMargin = 4;
                tvFlame.setLayoutParams(flameLp);
                cell.addView(tvFlame);
            }

            cell.setOnClickListener(v -> {
                selectedDateKey = dateKey;
                dibujarCalendario();
                mostrarHabitosDelDiaSeleccionado();
            });

            calendarGrid.addView(cell);
        }
    }

    private void actualizarEstadisticas() {
        int daysWithActivity = 0;
        Calendar dayCursor = (Calendar) visibleMonth.clone();
        int daysInMonth = dayCursor.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int day = 1; day <= daysInMonth; day++) {
            String dateKey = buildDateKey(
                    visibleMonth.get(Calendar.YEAR),
                    visibleMonth.get(Calendar.MONTH),
                    day
            );
            List<CompletionInfo> dayCompletions = completionsByDate.get(dateKey);
            if (dayCompletions != null && !dayCompletions.isEmpty()) {
                daysWithActivity++;
            }
        }

        txtDaysCompleted.setText(String.valueOf(daysWithActivity));

        String bestName = getString(R.string.calendar_no_rachas);
        int bestDays = 0;
        for (RachaInfo racha : rachas) {
            if (racha.dias > bestDays) {
                bestDays = racha.dias;
                bestName = racha.nombre;
            }
        }

        if (bestDays > 0) {
            txtBestStreak.setText(getString(R.string.calendar_best_streak_value, bestName, bestDays));
        } else {
            txtBestStreak.setText(getString(R.string.calendar_best_streak_empty));
        }
    }

    private void mostrarHabitosDelDiaSeleccionado() {
        habitsList.removeAllViews();

        boolean isToday = selectedDateKey.equals(formatDateKey(Calendar.getInstance()));
        if (isToday) {
            txtHabitsSectionTitle.setText(R.string.calendar_habits_today_title);
        } else {
            txtHabitsSectionTitle.setText(
                    getString(R.string.calendar_habits_day_title, selectedDateKey)
            );
        }

        if (rachas.isEmpty()) {
            addEmptyHabitsMessage(getString(R.string.calendar_habits_empty_no_rachas));
            return;
        }

        List<CompletionInfo> dayCompletions = completionsByDate.get(selectedDateKey);
        Map<String, CompletionInfo> completedByRachaId = new HashMap<>();
        if (dayCompletions != null) {
            for (CompletionInfo completion : dayCompletions) {
                completedByRachaId.put(completion.rachaId, completion);
            }
        }

        int shown = 0;
        for (RachaInfo racha : rachas) {
            boolean completed = completedByRachaId.containsKey(racha.id);
            String title = (racha.icono != null ? racha.icono + " " : "") + racha.nombre;
            addHabitRow(habitsList, title, completed);
            shown++;
        }

        if (shown == 0) {
            addEmptyHabitsMessage(getString(R.string.calendar_habits_empty_day));
        }
    }

    private void addEmptyHabitsMessage(String message) {
        TextView tv = new TextView(this);
        tv.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        tv.setText(message);
        tv.setTextColor(ContextCompat.getColor(this, R.color.gray_dark));
        tv.setPadding(0, 8, 0, 8);
        habitsList.addView(tv);
    }

    private void addHabitRow(LinearLayout parent, String title, boolean completed) {
        LinearLayout row = new LinearLayout(this);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 8, 0, 8);

        TextView tv = new TextView(this);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        tv.setText(title);
        tv.setTextColor(ContextCompat.getColor(this, R.color.black));

        TextView check = new TextView(this);
        check.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        if (completed) {
            check.setText("✓");
            check.setTextColor(ContextCompat.getColor(this, R.color.green));
        } else {
            check.setText("○");
            check.setTextColor(ContextCompat.getColor(this, R.color.gray_light));
        }

        row.addView(tv);
        row.addView(check);
        parent.addView(row);
    }

    private int countUniqueRachas(List<CompletionInfo> dayCompletions) {
        if (dayCompletions == null || dayCompletions.isEmpty()) {
            return 0;
        }

        Map<String, Boolean> unique = new HashMap<>();
        for (CompletionInfo completion : dayCompletions) {
            unique.put(completion.rachaId, true);
        }
        return unique.size();
    }

    private String getMonthStartKey(Calendar month) {
        return buildDateKey(month.get(Calendar.YEAR), month.get(Calendar.MONTH), 1);
    }

    private String getMonthEndKey(Calendar month) {
        int lastDay = month.getActualMaximum(Calendar.DAY_OF_MONTH);
        return buildDateKey(month.get(Calendar.YEAR), month.get(Calendar.MONTH), lastDay);
    }

    private String buildDateKey(int year, int monthZeroBased, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthZeroBased);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        return formatDateKey(calendar);
    }

    private String formatDateKey(Calendar calendar) {
        SimpleDateFormat storageDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return storageDateFormat.format(calendar.getTime());
    }

    private static class RachaInfo {
        final String id;
        final String nombre;
        final String icono;
        final int dias;

        RachaInfo(String id, String nombre, String icono, int dias) {
            this.id = id;
            this.nombre = nombre;
            this.icono = icono;
            this.dias = dias;
        }

        static RachaInfo fromDocument(DocumentSnapshot documentSnapshot) {
            String nombre = documentSnapshot.getString(FIELD_NOMBRE);
            String icono = documentSnapshot.getString(FIELD_ICONO);
            Long diasValue = documentSnapshot.getLong(FIELD_DIAS);

            return new RachaInfo(
                    documentSnapshot.getId(),
                    nombre != null ? nombre : "",
                    icono != null ? icono : "",
                    diasValue != null ? diasValue.intValue() : 0
            );
        }
    }

    private static class CompletionInfo {
        final String rachaId;
        final String nombre;
        final String icono;

        CompletionInfo(String rachaId, String nombre, String icono) {
            this.rachaId = rachaId;
            this.nombre = nombre;
            this.icono = icono;
        }
    }
}
