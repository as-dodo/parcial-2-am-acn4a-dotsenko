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

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private LinearLayout rachaContainer;
    private TextView txtCompletedToday;
    private ArrayList<Racha> rachas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        btnNuevaRacha.setOnClickListener(v -> {
            agregarRacha("⭐", "Nueva racha", 0, false);
        });
        BottomNavigationHelper.setup(this, R.id.menuInicio);

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