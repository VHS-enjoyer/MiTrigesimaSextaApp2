package com.curso_simulaciones.mitrigesimasextaapp.actividades_secundarias;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;

import com.curso_simulaciones.mitrigesimasextaapp.R;
import com.curso_simulaciones.mitrigesimasextaapp.almacen_datos.AlmacenDatosRAM;

public class ActividadComunicacion extends Activity {
    private Button botonCliente, botonServidor;
    private int tamanoLetraResolucionIncluida;
    private final int COLOR_1 = Color.rgb(220, 156, 80);

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        gestionarResolucion();
        creacionElementosGUI();
        setContentView(crearGUI());
        eventos();
    }

    private void gestionarResolucion() {
        tamanoLetraResolucionIncluida = (int) (0.8 * AlmacenDatosRAM.tamanoLetraResolucionIncluida);
    }

    private void creacionElementosGUI() {
        botonCliente = new Button(this);
        botonCliente.setTextSize(TypedValue.COMPLEX_UNIT_SP, tamanoLetraResolucionIncluida);
        botonCliente.setText("CLIENTE");
        botonCliente.getBackground().setColorFilter(COLOR_1, PorterDuff.Mode.MULTIPLY);
        botonCliente.setEnabled(true);

        botonServidor = new Button(this);
        botonServidor.setTextSize(TypedValue.COMPLEX_UNIT_SP, tamanoLetraResolucionIncluida);
        botonServidor.setText("SERVIDOR");
        botonServidor.getBackground().setColorFilter(COLOR_1, PorterDuff.Mode.MULTIPLY);
        botonServidor.setEnabled(true);
    }

    private LinearLayout crearGUI() {
        LinearLayout linearLayoutPrincipal = new LinearLayout(this);
        linearLayoutPrincipal.setOrientation(LinearLayout.VERTICAL);
        linearLayoutPrincipal.setBackgroundColor(Color.WHITE);
        linearLayoutPrincipal.setWeightSum(10f);

        LinearLayout linearLayoutPrimeraFila = new LinearLayout(this);
        linearLayoutPrimeraFila.setOrientation(LinearLayout.VERTICAL);

        // fondo primera fila
        Drawable fondo = ContextCompat.getDrawable(this, R.drawable.modelo);
        linearLayoutPrimeraFila.setBackground(fondo);

        LinearLayout linearLayoutSegundaFila = new LinearLayout(this);
        linearLayoutSegundaFila.setOrientation(LinearLayout.HORIZONTAL);
        linearLayoutSegundaFila.setBackgroundColor(Color.WHITE);
        linearLayoutSegundaFila.setWeightSum(2f);

        LinearLayout.LayoutParams parametrosPrimeraFila = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0);
        parametrosPrimeraFila.weight = 9.0f;
        linearLayoutPrimeraFila.setLayoutParams(parametrosPrimeraFila);

        LinearLayout.LayoutParams parametrosSegundaFila = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0);
        parametrosSegundaFila.weight = 1.0f;
        linearLayoutSegundaFila.setLayoutParams(parametrosSegundaFila);

        linearLayoutPrincipal.addView(linearLayoutPrimeraFila);
        linearLayoutPrincipal.addView(linearLayoutSegundaFila);

        LinearLayout.LayoutParams parametrosPegadoBotones = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT);
        parametrosPegadoBotones.weight = 1.0f;
        linearLayoutSegundaFila.addView(botonCliente, parametrosPegadoBotones);
        linearLayoutSegundaFila.addView(botonServidor, parametrosPegadoBotones);

        return linearLayoutPrincipal;
    }

    private void eventos() {
        botonCliente.setOnClickListener(v -> {
            AlmacenDatosRAM.rol = "SOY EL CLIENTE";
            botonServidor.setEnabled(false);
            botonCliente.setEnabled(false);
            startActivity(new Intent(this, ActividadComoClienteBluetooth.class));
        });

        botonServidor.setOnClickListener(v -> {
            AlmacenDatosRAM.rol = "SOY EL SERVIDOR";
            botonCliente.setEnabled(false);
            botonServidor.setEnabled(false);
            startActivity(new Intent(this, ActividadComoServidorBluetooth.class));
        });
    }
}