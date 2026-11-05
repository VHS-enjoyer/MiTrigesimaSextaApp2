package com.curso_simulaciones.mitrigesimasextaapp.actividades_secundarias;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.curso_simulaciones.mitrigesimasextaapp.R;
import com.curso_simulaciones.mitrigesimasextaapp.almacen_datos.AlmacenDatosRAM;
import com.curso_simulaciones.mitrigesimasextaapp.comunicaciones_bluetooth.ServidorBluetooth;
import com.curso_simulaciones.mitrigesimasextaapp.elementos_graficos.Acelerometro;
import com.curso_simulaciones.mitrigesimasextaapp.elementos_graficos.Boton;
import com.curso_simulaciones.mitrigesimasextaapp.elementos_graficos.Magnetometro;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressLint("WrongThread")
public class ActividadComoServidorBluetooth extends Activity implements Runnable {
    // botones de componentes: aceleración (ax, ay, az, a) y campo magnético (bx, by, bz, b)
    private Boton ax, ay, az, a;
    private Boton bx, by, bz, b;
    private Acelerometro acelerometro;
    private Magnetometro magnetometro;
    private int componente_aceleracion = 4; // por defecto, la magnitud
    private int componente_campo = 4;       // por defecto, la magnitud
    private TextView textviewAviso;
    private TextView textviewRol;
    private Button botonEmpezar;
    private int tamanoLetraResolucionIncluida;
    private final int COLOR_2 = Color.rgb(156, 220, 80);

    // Handler para actualizar la interfaz desde el hilo de comunicación
    private final Handler myHandler = new Handler(Looper.getMainLooper());
    private ServidorBluetooth servidor;

    /* hilo que espera al cliente y le envía los datos */
    private volatile boolean corriendo;
    private final long periodo_muestreo = 100; // pausas de 100 ms
    private Thread hilo;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        gestionarResolucion();
        creacionElementosGUI();
        setContentView(crearGUI());
        eventos();
        hilo = new Thread(this);
    }

    private void gestionarResolucion() {
        tamanoLetraResolucionIncluida = (int) (0.8 * AlmacenDatosRAM.tamanoLetraResolucionIncluida);
    }

    private void creacionElementosGUI() {
        // botones de aceleración
        ax = new Boton(this);
        ax.setImagen(R.drawable.ax);
        ay = new Boton(this);
        ay.setImagen(R.drawable.ay);
        az = new Boton(this);
        az.setImagen(R.drawable.az);
        a = new Boton(this);
        a.setImagen(R.drawable.a);

        // botones de campo magnético
        bx = new Boton(this);
        bx.setImagen(R.drawable.bx);
        by = new Boton(this);
        by.setImagen(R.drawable.by);
        bz = new Boton(this);
        bz.setImagen(R.drawable.bz);
        b = new Boton(this);
        b.setImagen(R.drawable.b);

        acelerometro = new Acelerometro(this);
        acelerometro.setAngulosSectores(50, 100, 100);
        acelerometro.setColorFranjaDinamica(Color.rgb(0, 255, 0));
        acelerometro.captarSensor(this);

        magnetometro = new Magnetometro(this);
        magnetometro.setColorFranjaDinamica(Color.rgb(255, 0, 255));
        magnetometro.captarSensor(this);

        textviewRol = new TextView(this);
        textviewRol.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (0.8 * tamanoLetraResolucionIncluida));
        textviewRol.setBackgroundColor(Color.YELLOW);
        textviewRol.setText("SERVIDOR");
        textviewRol.setTextColor(Color.RED);
        textviewRol.setGravity(Gravity.CENTER);
        textviewRol.setEnabled(false);

        textviewAviso = new TextView(this);
        textviewAviso.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (0.8 * tamanoLetraResolucionIncluida));
        textviewAviso.setBackgroundColor(Color.YELLOW);
        textviewAviso.setTextColor(Color.RED);
        textviewAviso.setGravity(Gravity.CENTER);
        textviewAviso.setText(AlmacenDatosRAM.conexion_bluetooth);

        botonEmpezar = new Button(this);
        botonEmpezar.setTextSize(TypedValue.COMPLEX_UNIT_SP, tamanoLetraResolucionIncluida);
        botonEmpezar.setText("EMPEZAR");
        botonEmpezar.getBackground().setColorFilter(COLOR_2, PorterDuff.Mode.MULTIPLY);

        // crear el Servidor (abrir el ServerSocket no bloquea)
        crearServidor();
    }

    private LinearLayout.LayoutParams filaParams(float peso) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        p.weight = peso;
        return p;
    }

    /* Fila con un tacómetro a la izquierda (80%) y cuatro botones a la derecha (20%) */
    private LinearLayout filaConBotones(View tacometro, Boton b1, Boton b2, Boton b3, Boton b4) {
        LinearLayout fila = new LinearLayout(this);
        fila.setBackgroundColor(Color.WHITE);
        fila.setWeightSum(10f);
        fila.setLayoutParams(filaParams(3.75f));

        LinearLayout columnaIzquierda = new LinearLayout(this);
        LinearLayout columnaDerecha = new LinearLayout(this);
        columnaDerecha.setOrientation(LinearLayout.VERTICAL);
        columnaDerecha.setWeightSum(4f);

        LinearLayout.LayoutParams parametrosIzquierda = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT);
        parametrosIzquierda.weight = 8.0f;
        fila.addView(columnaIzquierda, parametrosIzquierda);

        LinearLayout.LayoutParams parametrosDerecha = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT);
        parametrosDerecha.weight = 2.0f;
        fila.addView(columnaDerecha, parametrosDerecha);

        columnaIzquierda.addView(tacometro, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        Boton[] botones = {b1, b2, b3, b4};
        for (Boton boton : botones) {
            LinearLayout.LayoutParams pb = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 0);
            pb.weight = 1.0f;
            columnaDerecha.addView(boton, pb);
        }
        return fila;
    }

    private LinearLayout crearGUI() {
        LinearLayout linearLayoutPrincipal = new LinearLayout(this);
        linearLayoutPrincipal.setOrientation(LinearLayout.VERTICAL);
        linearLayoutPrincipal.setBackgroundColor(Color.BLACK);
        linearLayoutPrincipal.setWeightSum(10f);

        // fila 1: rol
        LinearLayout linearLayoutFilaUno = new LinearLayout(this);
        linearLayoutFilaUno.setBackgroundColor(Color.WHITE);
        linearLayoutFilaUno.setLayoutParams(filaParams(1.0f));
        LinearLayout.LayoutParams parametrosPegadoRol = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        linearLayoutFilaUno.setGravity(Gravity.CENTER);
        textviewRol.setPadding(20, 20, 20, 20);
        parametrosPegadoRol.setMargins(20, 20, 20, 20);
        textviewRol.setLayoutParams(parametrosPegadoRol);
        linearLayoutFilaUno.addView(textviewRol);

        // fila 2: acelerómetro + botones ax, ay, az, a
        LinearLayout linearLayoutFilaDos = filaConBotones(acelerometro, ax, ay, az, a);

        // fila 3: magnetómetro + botones bx, by, bz, b
        LinearLayout linearLayoutFilaTres = filaConBotones(magnetometro, bx, by, bz, b);

        // fila 4: aviso
        LinearLayout linearLayoutFilaCuatro = new LinearLayout(this);
        linearLayoutFilaCuatro.setOrientation(LinearLayout.HORIZONTAL);
        linearLayoutFilaCuatro.setBackgroundColor(Color.WHITE);
        linearLayoutFilaCuatro.setLayoutParams(filaParams(0.5f));
        linearLayoutFilaCuatro.addView(textviewAviso, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // fila 5: botón EMPEZAR
        LinearLayout linearLayoutFilaCinco = new LinearLayout(this);
        linearLayoutFilaCinco.setOrientation(LinearLayout.HORIZONTAL);
        linearLayoutFilaCinco.setBackgroundColor(Color.WHITE);
        linearLayoutFilaCinco.setLayoutParams(filaParams(1.0f));
        LinearLayout.LayoutParams parametrosPegadoBotones = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT);
        parametrosPegadoBotones.weight = 1.0f;
        linearLayoutFilaCinco.addView(botonEmpezar, parametrosPegadoBotones);

        linearLayoutPrincipal.addView(linearLayoutFilaUno);
        linearLayoutPrincipal.addView(linearLayoutFilaDos);
        linearLayoutPrincipal.addView(linearLayoutFilaTres);
        linearLayoutPrincipal.addView(linearLayoutFilaCuatro);
        linearLayoutPrincipal.addView(linearLayoutFilaCinco);

        return linearLayoutPrincipal;
    }

    private void eventos() {
        botonEmpezar.setOnClickListener(v -> {
            botonEmpezar.setEnabled(false);
            textviewAviso.setText(" Esperando al cliente...");
            // el accept() bloquea: se hace dentro del hilo
            hilo.start();
        });

        // componentes de la aceleración
        ax.setOnClickListener(v -> seleccionarAceleracion(1));
        ay.setOnClickListener(v -> seleccionarAceleracion(2));
        az.setOnClickListener(v -> seleccionarAceleracion(3));
        a.setOnClickListener(v -> seleccionarAceleracion(4));

        // componentes del campo magnético
        bx.setOnClickListener(v -> seleccionarCampo(1));
        by.setOnClickListener(v -> seleccionarCampo(2));
        bz.setOnClickListener(v -> seleccionarCampo(3));
        b.setOnClickListener(v -> seleccionarCampo(4));
    }

    private void seleccionarAceleracion(int componente) {
        acelerometro.setComponenteAcelerometro(componente);
        componente_aceleracion = componente;
    }

    private void seleccionarCampo(int componente) {
        magnetometro.setComponenteMagnetometro(componente);
        componente_campo = componente;
    }

    private void crearServidor() {
        servidor = new ServidorBluetooth();
        // el servidor abre el BluetoothServerSocket y lo pone a escuchar
        servidor.abrirSocketServidor();
    }

    /** BLOQUEANTE: se llama desde el hilo. Devuelve true si el cliente se conectó. */
    private boolean empezarComunicacionConCliente() {
        // accept() bloquea el hilo hasta que llegue una conexión entrante
        servidor.abrirSocketCliente();
        if (servidor.clienteSocket == null) {
            return false;
        }
        servidor.abrirFlujoSalida();
        servidor.abrirFlujoEntrada();
        return true;
    }

    private void terminarComunicacionConCliente() {
        if (servidor == null) return;
        servidor.cerrarFlujoSalida();
        servidor.cerrarFlujoEntrada();
        servidor.cerrarSocketCliente();
        servidor.cerrarSocketServidor();
    }

    public void detener() {
        corriendo = false;
        terminarComunicacionConCliente();
    }

    /* Este hilo espera la conexión del cliente y luego le envía los datos (JSON en bytes) */
    @Override
    public void run() {
        if (!empezarComunicacionConCliente()) {
            hacerTrabajoDuro();
            return;
        }
        corriendo = true;
        while (corriendo) {
            try {
                Thread.sleep(periodo_muestreo);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!escribir()) {
                AlmacenDatosRAM.conexion_bluetooth = " Cliente desconectado";
                corriendo = false;
            } else {
                AlmacenDatosRAM.conexion_bluetooth = " Enviando datos al cliente ...";
            }
            hacerTrabajoDuro();
        }
    }

    // enviar datos al cliente; cada mensaje termina en '\n'
    private boolean escribir() {
        String dato = getStringJSON() + "\n";
        return servidor.escribirBytes(dato.getBytes());
    }

    private String getStringJSON() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("componente_aceleracion", componente_aceleracion);
            obj.put("valor_aceleracion", acelerometro.getMedida());
            obj.put("componente_campo", componente_campo);
            obj.put("valor_campo", magnetometro.getMedida());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj.toString();
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
        acelerometro.detenerSensor();
        magnetometro.detenerSensor();
        // cerrar los sockets también desbloquea el accept() si aún esperaba
        terminarComunicacionConCliente();
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