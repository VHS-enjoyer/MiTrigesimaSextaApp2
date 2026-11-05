package com.curso_simulaciones.mitrigesimasextaapp.actividades_secundarias;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.curso_simulaciones.mitrigesimasextaapp.almacen_datos.AlmacenDatosRAM;
import com.curso_simulaciones.mitrigesimasextaapp.comunicaciones_bluetooth.ClienteBluetooth;
import com.curso_simulaciones.mitrigesimasextaapp.elementos_graficos.Acelerometro;
import com.curso_simulaciones.mitrigesimasextaapp.elementos_graficos.Magnetometro;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressLint("WrongThread")
public class ActividadComoClienteBluetooth extends Activity implements Runnable {
    private Acelerometro acelerometro;
    private Magnetometro magnetometro;
    private Button botonConectar, botonBuscar;
    private TextView textviewRol;
    private TextView textviewAviso;
    private int tamanoLetraResolucionIncluida;
    private final int COLOR_1 = Color.rgb(220, 156, 80);

    // Handler para actualizar la interfaz desde el hilo de comunicación
    private final Handler myHandler = new Handler(Looper.getMainLooper());
    private ClienteBluetooth cliente;
    private volatile boolean corriendo;

    // 0: sin conectar, 1: conectando, 2: conectado (listo para EMPEZAR)
    private int estado = 0;

    /* hilo pendiente del intercambio de datos con el servidor */
    private Thread hilo;

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
        acelerometro = new Acelerometro(this);
        acelerometro.setAngulosSectores(50, 100, 100);
        acelerometro.setColorFranjaDinamica(Color.rgb(0, 255, 0));

        magnetometro = new Magnetometro(this);
        magnetometro.setColorFranjaDinamica(Color.rgb(255, 0, 255));

        textviewRol = new TextView(this);
        textviewRol.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (0.8 * tamanoLetraResolucionIncluida));
        textviewRol.setBackgroundColor(Color.YELLOW);
        textviewRol.setText("CLIENTE");
        textviewRol.setTextColor(Color.RED);
        textviewRol.setGravity(Gravity.CENTER);
        textviewRol.setEnabled(false);

        textviewAviso = new TextView(this);
        textviewAviso.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (0.8 * tamanoLetraResolucionIncluida));
        textviewAviso.setBackgroundColor(Color.YELLOW);
        textviewAviso.setTextColor(Color.RED);
        textviewAviso.setGravity(Gravity.CENTER);
        textviewAviso.setText(AlmacenDatosRAM.conexion_bluetooth);

        botonBuscar = new Button(this);
        botonBuscar.setTextSize(TypedValue.COMPLEX_UNIT_SP, tamanoLetraResolucionIncluida);
        botonBuscar.setText("BUSCAR");
        botonBuscar.getBackground().setColorFilter(COLOR_1, PorterDuff.Mode.MULTIPLY);
        botonBuscar.setEnabled(true);

        botonConectar = new Button(this);
        botonConectar.setTextSize(TypedValue.COMPLEX_UNIT_SP, tamanoLetraResolucionIncluida);
        botonConectar.setText("CONECTAR");
        botonConectar.getBackground().setColorFilter(COLOR_1, PorterDuff.Mode.MULTIPLY);
        botonConectar.setEnabled(false);
    }

    private LinearLayout.LayoutParams filaParams(float peso) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        p.weight = peso;
        return p;
    }

    private LinearLayout crearGUI() {
        LinearLayout linearLayoutPrincipal = new LinearLayout(this);
        linearLayoutPrincipal.setOrientation(LinearLayout.VERTICAL);
        linearLayoutPrincipal.setBackgroundColor(Color.WHITE);
        linearLayoutPrincipal.setWeightSum(10f);

        LinearLayout linearLayoutFilaUno = new LinearLayout(this);
        linearLayoutFilaUno.setBackgroundColor(Color.WHITE);
        LinearLayout linearLayoutFilaDos = new LinearLayout(this);
        linearLayoutFilaDos.setBackgroundColor(Color.WHITE);
        LinearLayout linearLayoutFilaTres = new LinearLayout(this);
        linearLayoutFilaTres.setBackgroundColor(Color.WHITE);
        LinearLayout linearLayoutFilaCuatro = new LinearLayout(this);
        linearLayoutFilaCuatro.setOrientation(LinearLayout.HORIZONTAL);
        linearLayoutFilaCuatro.setBackgroundColor(Color.WHITE);
        LinearLayout linearLayoutFilaCinco = new LinearLayout(this);
        linearLayoutFilaCinco.setOrientation(LinearLayout.HORIZONTAL);
        linearLayoutFilaCinco.setBackgroundColor(Color.WHITE);
        linearLayoutFilaCinco.setWeightSum(2f);

        linearLayoutFilaUno.setLayoutParams(filaParams(1.0f));
        linearLayoutFilaDos.setLayoutParams(filaParams(3.75f));
        linearLayoutFilaTres.setLayoutParams(filaParams(3.75f));
        linearLayoutFilaCuatro.setLayoutParams(filaParams(0.5f));
        linearLayoutFilaCinco.setLayoutParams(filaParams(1.0f));

        linearLayoutPrincipal.addView(linearLayoutFilaUno);
        linearLayoutPrincipal.addView(linearLayoutFilaDos);
        linearLayoutPrincipal.addView(linearLayoutFilaTres);
        linearLayoutPrincipal.addView(linearLayoutFilaCuatro);
        linearLayoutPrincipal.addView(linearLayoutFilaCinco);

        // fila 1: rol
        LinearLayout.LayoutParams parametrosPegadoRol = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        linearLayoutFilaUno.setGravity(Gravity.CENTER);
        textviewRol.setPadding(20, 20, 20, 20);
        parametrosPegadoRol.setMargins(20, 20, 20, 20);
        textviewRol.setLayoutParams(parametrosPegadoRol);
        linearLayoutFilaUno.addView(textviewRol);

        // fila 2: acelerómetro
        linearLayoutFilaDos.setGravity(Gravity.CENTER);
        linearLayoutFilaDos.addView(acelerometro, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // fila 3: magnetómetro
        linearLayoutFilaTres.setGravity(Gravity.CENTER);
        linearLayoutFilaTres.addView(magnetometro, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // fila 4: aviso
        linearLayoutFilaCuatro.addView(textviewAviso, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // fila 5: botones
        LinearLayout.LayoutParams parametrosPegadoBotones = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT);
        parametrosPegadoBotones.weight = 1.0f;
        linearLayoutFilaCinco.addView(botonBuscar, parametrosPegadoBotones);
        linearLayoutFilaCinco.addView(botonConectar, parametrosPegadoBotones);

        return linearLayoutPrincipal;
    }

    private void eventos() {
        botonBuscar.setOnClickListener(v -> {
            lanzarBuscandoDispositivos();
            botonConectar.setEnabled(true);
            botonBuscar.setEnabled(false);
        });

        botonConectar.setOnClickListener(v -> {
            if (estado == 0) {
                // conectar en un hilo aparte: connect() bloquea
                estado = 1;
                botonConectar.setEnabled(false);
                botonBuscar.setEnabled(false);
                textviewAviso.setText(" Conectando...");
                new Thread(() -> {
                    final boolean ok = crearCliente();
                    runOnUiThread(() -> {
                        textviewAviso.setText(AlmacenDatosRAM.conexion_bluetooth);
                        botonConectar.setEnabled(true);
                        if (ok) {
                            estado = 2;
                            botonConectar.setText("EMPEZAR");
                        } else {
                            estado = 0;
                            botonBuscar.setEnabled(true);
                        }
                    });
                }).start();
            } else if (estado == 2) {
                botonConectar.setEnabled(false);
                empezarHilo();
            }
        });
    }

    private void lanzarBuscandoDispositivos() {
        Intent intent = new Intent(this, ActividadEscaneoDispositivos.class);
        startActivity(intent);
    }

    public void empezarHilo() {
        hilo = new Thread(this);
        hilo.start();
    }

    /** Devuelve true si quedó conectado con el servidor y con los flujos abiertos */
    private boolean crearCliente() {
        String direccion = AlmacenDatosRAM.direccion;
        cliente = new ClienteBluetooth();
        if (!cliente.abrirSocketCliente(direccion)) {
            AlmacenDatosRAM.conexion_bluetooth = " Dirección inválida: use BUSCAR";
            return false;
        }
        if (!cliente.conectarSocketCliente()) {
            return false;
        }
        cliente.abrirFlujoEntrada();
        cliente.abrirFlujoSalida();
        return true;
    }

    private void terminarComunicacionConServidor() {
        if (cliente == null) return;
        cliente.cerrarFlujoEntrada();
        cliente.cerrarFlujoSalida();
        cliente.cerrarSocketCliente();
    }

    @Override
    public void run() {
        corriendo = true;
        while (corriendo) {
            // leerString() bloquea hasta que llegue un mensaje (marca el ritmo del bucle)
            String nuevo_dato_string = cliente.leerString();
            if (nuevo_dato_string == null) {
                AlmacenDatosRAM.conexion_bluetooth = " Conexión cerrada";
                corriendo = false;
            } else {
                convertirStringJson(nuevo_dato_string);
                AlmacenDatosRAM.conexion_bluetooth = " Recibiendo datos del servidor ...";
            }
            hacerTrabajoDuro();
        }
    }

    // obtener la información del JSON
    public void convertirStringJson(String datoString) {
        try {
            JSONObject obj = new JSONObject(datoString);

            acelerometro.setComponenteAcelerometro(obj.getInt("componente_aceleracion"));
            acelerometro.setMedida((float) obj.getDouble("valor_aceleracion"));

            magnetometro.setComponenteMagnetometro(obj.getInt("componente_campo"));
            magnetometro.setMedida((float) obj.getDouble("valor_campo"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        corriendo = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        corriendo = false;
        // cerrar el socket también desbloquea la lectura pendiente del hilo
        terminarComunicacionConServidor();
    }

    /* Se usa un Handler para actualizar la UI desde el hilo */
    public void hacerTrabajoDuro() {
        myHandler.post(updateRunnable);
    }

    final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            avisoEstadoComunicacion();
        }
    };

    private void avisoEstadoComunicacion() {
        textviewAviso.setText(AlmacenDatosRAM.conexion_bluetooth);
    }
}