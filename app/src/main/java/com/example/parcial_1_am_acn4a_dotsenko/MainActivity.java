package com.example.parcial_1_am_acn4a_dotsenko;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_RACHAS = "rachas";
    private static final String FIELD_ICONO = "icono";
    private static final String FIELD_NOMBRE = "nombre";
    private static final String FIELD_DIAS = "dias";
    private static final String FIELD_LAST_COMPLETED_DATE = "lastCompletedDate";
    private static final String FIELD_CREATED_AT = "createdAt";
    private static final String FIELD_UPDATED_AT = "updatedAt";

    private LinearLayout rachaContainer;
    private TextView txtCompletedToday;
    private TextView txtGreeting;
    private final ArrayList<Racha> rachas = new ArrayList<>();
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        currentUserId = currentUser.getUid();

        txtGreeting = findViewById(R.id.txtTitle);
        cargarPerfilUsuario(currentUserId);

        TextView txtToday = findViewById(R.id.txtToday);
        SimpleDateFormat readableDateFormat =
                new SimpleDateFormat("d 'de' MMMM", Locale.getDefault());
        String fecha = readableDateFormat.format(new Date());
        txtToday.setText(getString(R.string.today_format, fecha));

        rachaContainer = findViewById(R.id.rachaContainer);
        txtCompletedToday = findViewById(R.id.txtCompletedToday);
        Button btnNuevaRacha = findViewById(R.id.btnNuevaRacha);

        btnNuevaRacha.setOnClickListener(v -> mostrarDialogNuevaRacha());
        BottomNavigationHelper.setup(this, R.id.menuInicio);

        cargarRachas();
    }

    private void cargarPerfilUsuario(String userId) {
        db.collection(COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        txtGreeting.setText(getString(R.string.greeting_default));
                        return;
                    }

                    String fullName = documentSnapshot.getString("fullName");
                    if (fullName == null || fullName.trim().isEmpty()) {
                        txtGreeting.setText(getString(R.string.greeting_default));
                    } else {
                        txtGreeting.setText(getString(R.string.greeting_user_format, fullName));
                    }
                })
                .addOnFailureListener(e ->
                        txtGreeting.setText(getString(R.string.greeting_default)));
    }

    private void cargarRachas() {
        getRachasReference()
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        crearRachasIniciales();
                        return;
                    }

                    rachas.clear();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                        rachas.add(Racha.fromDocument(documentSnapshot));
                    }

                    ordenarRachas();
                    mostrarRachas();
                })
                .addOnFailureListener(e -> {
                    rachas.clear();
                    mostrarRachas();
                    Toast.makeText(this, R.string.racha_load_failed, Toast.LENGTH_SHORT).show();
                });
    }

    private void crearRachasIniciales() {
        String today = getTodayDateKey();
        String yesterday = getYesterdayDateKey();
        Random random = new Random();

        Racha[] initialRachas = new Racha[] {
                new Racha(null, getString(R.string.seed_racha_yoga), "🧘", randomDays(random), today),
                new Racha(null, getString(R.string.seed_racha_english), "🇬🇧", randomDays(random), yesterday),
                new Racha(null, getString(R.string.seed_racha_spanish), "🇪🇸", randomDays(random), today),
                new Racha(null, getString(R.string.seed_racha_running), "🏃", randomDays(random), yesterday),
                new Racha(null, getString(R.string.seed_racha_no_smoking), "🚭", randomDays(random), today)
        };

        WriteBatch batch = db.batch();
        CollectionReference rachasReference = getRachasReference();

        for (Racha racha : initialRachas) {
            DocumentReference rachaReference = rachasReference.document();
            racha.id = rachaReference.getId();
            batch.set(rachaReference, racha.toMap(true));
        }

        batch.commit()
                .addOnSuccessListener(unused -> cargarRachas())
                .addOnFailureListener(e ->
                        Toast.makeText(this, R.string.racha_seed_failed, Toast.LENGTH_SHORT).show());
    }

    private int randomDays(Random random) {
        return random.nextInt(14) + 1;
    }

    private void mostrarDialogNuevaRacha() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_racha, null);

        EditText inputNombre = dialogView.findViewById(R.id.inputRachaNombre);
        EditText inputIcono = dialogView.findViewById(R.id.inputRachaIcono);
        EditText inputDias = dialogView.findViewById(R.id.inputRachaDias);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_new_racha_title)
                .setView(dialogView)
                .setPositiveButton(R.string.dialog_new_racha_add, null)
                .setNegativeButton(R.string.dialog_new_racha_cancel, null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String nombre = inputNombre.getText().toString().trim();
                String icono = inputIcono.getText().toString().trim();
                String diasText = inputDias.getText().toString().trim();

                if (TextUtils.isEmpty(nombre)) {
                    inputNombre.setError(getString(R.string.dialog_new_racha_name_required));
                    return;
                }

                int dias = 0;

                if (!TextUtils.isEmpty(diasText)) {
                    try {
                        dias = Integer.parseInt(diasText);
                    } catch (NumberFormatException ex) {
                        inputDias.setError(getString(R.string.dialog_new_racha_invalid_days));
                        return;
                    }
                }

                if (dias < 0) {
                    inputDias.setError(getString(R.string.dialog_new_racha_invalid_days));
                    return;
                }

                if (TextUtils.isEmpty(icono)) {
                    icono = getString(R.string.default_racha_icon);
                }

                positiveButton.setEnabled(false);
                guardarNuevaRacha(icono, nombre, dias, dialog, positiveButton);
            });
        });

        dialog.show();
    }

    private void guardarNuevaRacha(
            String icono,
            String nombre,
            int dias,
            AlertDialog dialog,
            Button positiveButton
    ) {
        DocumentReference rachaReference = getRachasReference().document();
        String lastCompletedDate = dias > 0 ? getYesterdayDateKey() : null;
        Racha racha = new Racha(rachaReference.getId(), nombre, icono, dias, lastCompletedDate);

        rachaReference
                .set(racha.toMap(true))
                .addOnSuccessListener(unused -> {
                    rachas.add(racha);
                    ordenarRachas();
                    mostrarRachas();
                    Toast.makeText(this, R.string.dialog_new_racha_added, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    positiveButton.setEnabled(true);
                    Toast.makeText(this, R.string.racha_save_failed, Toast.LENGTH_SHORT).show();
                });
    }

    private void mostrarRachas() {
        rachaContainer.removeAllViews();

        for (Racha racha : rachas) {
            View card = getLayoutInflater().inflate(R.layout.item_racha, rachaContainer, false);

            TextView txtIcon = card.findViewById(R.id.txtRachaIcon);
            TextView txtName = card.findViewById(R.id.txtRachaName);
            TextView txtDays = card.findViewById(R.id.txtRachaDays);
            ImageView imgFire = card.findViewById(R.id.imgFire);

            boolean completedToday = isCompletedToday(racha);

            txtIcon.setText(racha.icono);
            txtName.setText(racha.nombre);
            txtDays.setText(getString(R.string.racha_days_format, racha.dias));
            imgFire.setImageResource(completedToday
                    ? R.drawable.ic_fire_active
                    : R.drawable.ic_fire_inactive);

            imgFire.setOnClickListener(v -> completarRachaHoy(racha, imgFire));

            card.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, RachaDetailActivity.class);
                intent.putExtra(RachaDetailActivity.EXTRA_NOMBRE, racha.nombre);
                intent.putExtra(RachaDetailActivity.EXTRA_ICONO, racha.icono);
                intent.putExtra(RachaDetailActivity.EXTRA_DIAS, racha.dias);
                intent.putExtra(RachaDetailActivity.EXTRA_COMPLETADA_HOY, completedToday);
                startActivity(intent);
            });

            rachaContainer.addView(card);
        }
        actualizarResumenDelDia();
    }

    private void completarRachaHoy(Racha racha, ImageView imgFire) {
        if (isCompletedToday(racha)) {
            Toast.makeText(this, R.string.racha_already_completed_today, Toast.LENGTH_SHORT).show();
            return;
        }

        int nextDias = isCompletedYesterday(racha) ? racha.dias + 1 : 1;
        String today = getTodayDateKey();

        imgFire.setEnabled(false);

        Map<String, Object> updates = new HashMap<>();
        updates.put(FIELD_DIAS, nextDias);
        updates.put(FIELD_LAST_COMPLETED_DATE, today);
        updates.put(FIELD_UPDATED_AT, FieldValue.serverTimestamp());

        getRachasReference()
                .document(racha.id)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    racha.dias = nextDias;
                    racha.lastCompletedDate = today;
                    ordenarRachas();
                    mostrarRachas();
                })
                .addOnFailureListener(e -> {
                    imgFire.setEnabled(true);
                    Toast.makeText(this, R.string.racha_update_failed, Toast.LENGTH_SHORT).show();
                });
    }

    private void ordenarRachas() {
        Collections.sort(rachas, new Comparator<Racha>() {
            @Override
            public int compare(Racha racha1, Racha racha2) {
                return Integer.compare(racha2.dias, racha1.dias);
            }
        });
    }

    private void actualizarResumenDelDia() {
        int completadas = 0;

        for (Racha racha : rachas) {
            if (isCompletedToday(racha)) {
                completadas++;
            }
        }

        txtCompletedToday.setText(getString(R.string.completed_today_format, completadas));
    }

    private CollectionReference getRachasReference() {
        return db.collection(COLLECTION_USERS)
                .document(currentUserId)
                .collection(COLLECTION_RACHAS);
    }

    private boolean isCompletedToday(Racha racha) {
        return getTodayDateKey().equals(racha.lastCompletedDate);
    }

    private boolean isCompletedYesterday(Racha racha) {
        return getYesterdayDateKey().equals(racha.lastCompletedDate);
    }

    private String getTodayDateKey() {
        return formatDateKey(Calendar.getInstance());
    }

    private String getYesterdayDateKey() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        return formatDateKey(calendar);
    }

    private String formatDateKey(Calendar calendar) {
        SimpleDateFormat storageDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return storageDateFormat.format(calendar.getTime());
    }

    private static class Racha {
        String id;
        String nombre;
        String icono;
        int dias;
        String lastCompletedDate;

        Racha(String id, String nombre, String icono, int dias, String lastCompletedDate) {
            this.id = id;
            this.nombre = nombre;
            this.icono = icono;
            this.dias = dias;
            this.lastCompletedDate = lastCompletedDate;
        }

        static Racha fromDocument(DocumentSnapshot documentSnapshot) {
            String nombre = documentSnapshot.getString(FIELD_NOMBRE);
            String icono = documentSnapshot.getString(FIELD_ICONO);
            String lastCompletedDate = documentSnapshot.getString(FIELD_LAST_COMPLETED_DATE);
            Long diasValue = documentSnapshot.getLong(FIELD_DIAS);

            return new Racha(
                    documentSnapshot.getId(),
                    nombre != null ? nombre : "",
                    icono != null ? icono : "",
                    diasValue != null ? diasValue.intValue() : 0,
                    lastCompletedDate
            );
        }

        Map<String, Object> toMap(boolean includeCreatedAt) {
            Map<String, Object> data = new HashMap<>();
            data.put(FIELD_NOMBRE, nombre);
            data.put(FIELD_ICONO, icono);
            data.put(FIELD_DIAS, dias);
            data.put(FIELD_LAST_COMPLETED_DATE, lastCompletedDate);
            data.put(FIELD_UPDATED_AT, FieldValue.serverTimestamp());

            if (includeCreatedAt) {
                data.put(FIELD_CREATED_AT, FieldValue.serverTimestamp());
            }

            return data;
        }
    }
}
