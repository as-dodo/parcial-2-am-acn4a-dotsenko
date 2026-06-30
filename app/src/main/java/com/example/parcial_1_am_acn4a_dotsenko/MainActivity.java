package com.example.parcial_1_am_acn4a_dotsenko;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import android.content.Intent;
import android.app.AlertDialog;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private LinearLayout rachaContainer;
    private TextView txtCompletedToday;
    private TextView txtGreeting;
    private ArrayList<Racha> rachas = new ArrayList<>();
    private com.google.firebase.auth.FirebaseAuth auth;
    private com.google.firebase.firestore.FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth = com.google.firebase.auth.FirebaseAuth.getInstance();
        db = com.google.firebase.firestore.FirebaseFirestore.getInstance();

        com.google.firebase.auth.FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        txtGreeting = findViewById(R.id.txtTitle);
        cargarPerfilUsuario(currentUser.getUid());

        TextView txtToday = findViewById(R.id.txtToday);

        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("d 'de' MMMM", java.util.Locale.getDefault());

        String fecha = sdf.format(new java.util.Date());

        txtToday.setText(getString(R.string.today_format, fecha));

        rachaContainer = findViewById(R.id.rachaContainer);
        txtCompletedToday = findViewById(R.id.txtCompletedToday);
        Button btnNuevaRacha = findViewById(R.id.btnNuevaRacha);

        agregarRacha("🧘", "Yoga", 15, true);
        agregarRacha("🇬🇧", "Inglés", 42, false);
        agregarRacha("🇪🇸", "Español", 28, true);
        agregarRacha("🏃", "Correr", 10, false);
        agregarRacha("🚭", "No fumar", 60, true);

        btnNuevaRacha.setOnClickListener(v -> mostrarDialogNuevaRacha());
        BottomNavigationHelper.setup(this, R.id.menuInicio);

    }
    private void cargarPerfilUsuario(String userId) {
        db.collection("users")
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
                .addOnFailureListener(e -> {
                    txtGreeting.setText(getString(R.string.greeting_default));
                });
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
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
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

                agregarRacha(icono, nombre, dias, false);
                mostrarRachas();
                actualizarResumenDelDia();

                Toast.makeText(this, R.string.dialog_new_racha_added, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void agregarRacha(String icono, String nombre, int dias, boolean completadaHoy) {
        rachas.add(new Racha(icono, nombre, dias, completadaHoy));

        Collections.sort(rachas, new Comparator<Racha>() {
            @Override
            public int compare(Racha racha1, Racha racha2) {
                return Integer.compare(racha2.dias, racha1.dias);
            }
        });

        mostrarRachas();
    }

    private void mostrarRachas() {
        rachaContainer.removeAllViews();

        for (Racha racha : rachas) {
            View card = getLayoutInflater().inflate(R.layout.item_racha, rachaContainer, false);

            TextView txtIcon = card.findViewById(R.id.txtRachaIcon);
            TextView txtName = card.findViewById(R.id.txtRachaName);
            TextView txtDays = card.findViewById(R.id.txtRachaDays);
            ImageView imgFire = card.findViewById(R.id.imgFire);

            txtIcon.setText(racha.icono);
            txtName.setText(racha.nombre);
            txtDays.setText(racha.dias + " días");

            if (racha.completadaHoy) {
                imgFire.setImageResource(R.drawable.ic_fire_active);
            } else {
                imgFire.setImageResource(R.drawable.ic_fire_inactive);
            }

            imgFire.setOnClickListener(v -> {
                if (racha.completadaHoy) {
                    racha.completadaHoy = false;

                    if (racha.dias > 0) {
                        racha.dias = racha.dias - 1;
                    }

                    imgFire.setImageResource(R.drawable.ic_fire_inactive);
                } else {
                    racha.completadaHoy = true;
                    racha.dias = racha.dias + 1;
                    imgFire.setImageResource(R.drawable.ic_fire_active);
                }

                txtDays.setText(getString(R.string.racha_days_format, racha.dias));
                actualizarResumenDelDia();
            });

            card.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, RachaDetailActivity.class);
                intent.putExtra(RachaDetailActivity.EXTRA_NOMBRE, racha.nombre);
                intent.putExtra(RachaDetailActivity.EXTRA_ICONO, racha.icono);
                intent.putExtra(RachaDetailActivity.EXTRA_DIAS, racha.dias);
                intent.putExtra(RachaDetailActivity.EXTRA_COMPLETADA_HOY, racha.completadaHoy);
                startActivity(intent);
            });

            rachaContainer.addView(card);
        }
        actualizarResumenDelDia();
    }
    private void actualizarResumenDelDia() {
        int completadas = 0;

        for (Racha racha : rachas) {
            if (racha.completadaHoy) {
                completadas++;
            }
        }

        txtCompletedToday.setText(getString(R.string.completed_today_format, completadas));
    }
    private static class Racha {
        String icono;
        String nombre;
        int dias;
        boolean completadaHoy;

        Racha(String icono, String nombre, int dias, boolean completadaHoy) {
            this.icono = icono;
            this.nombre = nombre;
            this.dias = dias;
            this.completadaHoy = completadaHoy;
        }
    }
}