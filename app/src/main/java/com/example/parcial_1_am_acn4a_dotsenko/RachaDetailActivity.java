package com.example.parcial_1_am_acn4a_dotsenko;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RachaDetailActivity extends AppCompatActivity {

    public static final String EXTRA_NOMBRE = "nombre";
    public static final String EXTRA_ICONO = "icono";
    public static final String EXTRA_DIAS = "dias";
    public static final String EXTRA_COMPLETADA_HOY = "completadaHoy";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_racha_detail);

        TextView txtIcono = findViewById(R.id.txtDetailIcono);
        TextView txtNombre = findViewById(R.id.txtDetailNombre);
        TextView txtDias = findViewById(R.id.txtDetailDias);
        TextView txtEstado = findViewById(R.id.txtDetailEstado);
        TextView txtDescripcion = findViewById(R.id.txtDetailDescripcion);
        Button btnVolver = findViewById(R.id.btnDetailVolver);

        String nombre = getIntent().getStringExtra(EXTRA_NOMBRE);
        String icono = getIntent().getStringExtra(EXTRA_ICONO);
        int dias = getIntent().getIntExtra(EXTRA_DIAS, 0);
        boolean completadaHoy = getIntent().getBooleanExtra(EXTRA_COMPLETADA_HOY, false);

        if (nombre == null || nombre.trim().isEmpty()) {
            nombre = getString(R.string.detail_unknown_racha);
        }

        if (icono == null || icono.trim().isEmpty()) {
            icono = getString(R.string.default_racha_icon);
        }

        String estado = completadaHoy
                ? getString(R.string.detail_completed_today)
                : getString(R.string.detail_pending_today);

        String descripcion = completadaHoy
                ? getString(R.string.detail_description_completed)
                : getString(R.string.detail_description_pending);

        txtIcono.setText(icono);
        txtNombre.setText(nombre);
        txtDias.setText(getString(R.string.detail_days_format, dias));
        txtEstado.setText(getString(R.string.detail_status_format, estado));
        txtDescripcion.setText(descripcion);

        btnVolver.setOnClickListener(v -> finish());
    }
}
