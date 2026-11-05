package com.curso_simulaciones.mitrigesimasextaapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.curso_simulaciones.mitrigesimasextaapp.actividades_secundarias.ActividadComunicacion;
import com.curso_simulaciones.mitrigesimasextaapp.almacen_datos.AlmacenDatosRAM;
import com.curso_simulaciones.mitrigesimasextaapp.elementos_graficos.Boton;

public class ActividadPrincipalMiTrigesimaSextaApp extends Activity {
    private static final int CODIGO_PERMISOS = 100;

    private Boton entrar, salir;
    private BluetoothAdapter BA;
    LinearLayout linear_layout_segunda_fila;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gestionarResolucion();
        crearElementosGUI();

        ViewGroup.LayoutParams parametro_layout_principal = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        this.setContentView(crearGUI(), parametro_layout_principal);
        eventos();

        // permisos
        verificacionPermisos();
    }

    /* Método auxiliar para asuntos de resolución */
    private void gestionarResolucion() {
        DisplayMetrics displayMetrics = this.getApplicationContext().getResources().getDisplayMetrics();
        int alto = displayMetrics.heightPixels;
        int ancho = displayMetrics.widthPixels;
        AlmacenDatosRAM.ancho = ancho;
        AlmacenDatosRAM.alto = alto;

        // tomar el menor valor entre alto y ancho de pantalla
        int dimensionReferencia = Math.min(alto, ancho);
        AlmacenDatosRAM.dimensionReferencia = dimensionReferencia;

        int tamanoLetra = dimensionReferencia / 20;
        int tamanoLetraResolucionIncluida = (int) (tamanoLetra / displayMetrics.scaledDensity);
        AlmacenDatosRAM.tamanoLetraResolucionIncluida = tamanoLetraResolucionIncluida;
    }

    private void crearElementosGUI() {
        entrar = new Boton(this);
        entrar.setImagen(R.drawable.entrar);
        salir = new Boton(this);
        salir.setImagen(R.drawable.salir);
    }

    private LinearLayout crearGUI() {
        LinearLayout linear_layout_principal = new LinearLayout(this);
        linear_layout_principal.setOrientation(LinearLayout.VERTICAL);
        linear_layout_principal.setGravity(Gravity.FILL);
        linear_layout_principal.setBackgroundColor(Color.WHITE);
        linear_layout_principal.setWeightSum(10);

        // primera fila
        LinearLayout linear_layout_primera_fila = new LinearLayout(this);
        linear_layout_primera_fila.setOrientation(LinearLayout.HORIZONTAL);
        linear_layout_primera_fila.setGravity(Gravity.FILL);
        linear_layout_primera_fila.setBackgroundColor(Color.WHITE);

        LinearLayout.LayoutParams parametros_primera_fila = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0);
        parametros_primera_fila.weight = 8.0f;
        linear_layout_primera_fila.setLayoutParams(parametros_primera_fila);

        Drawable fondo = ContextCompat.getDrawable(this, R.drawable.comunicacion_cliente_servidor);
        linear_layout_primera_fila.setBackground(fondo);

        // segunda fila
        linear_layout_segunda_fila = new LinearLayout(this);
        linear_layout_segunda_fila.setOrientation(LinearLayout.HORIZONTAL);
        linear_layout_segunda_fila.setGravity(Gravity.FILL);
        LinearLayout.LayoutParams parametros_segunda_fila = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0);
        parametros_segunda_fila.weight = 2.0f;
        linear_layout_segunda_fila.setWeightSum(1.0f);
        linear_layout_segunda_fila.setLayoutParams(parametros_segunda_fila);

        LinearLayout.LayoutParams parametros_pegado_boton = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT);
        parametros_pegado_boton.weight = 1.0f;
        entrar.setLayoutParams(parametros_pegado_boton);
        salir.setLayoutParams(parametros_pegado_boton);

        linear_layout_segunda_fila.addView(entrar);

        linear_layout_principal.addView(linear_layout_primera_fila);
        linear_layout_principal.addView(linear_layout_segunda_fila);
        return linear_layout_principal;
    }

    private void eventos() {
        entrar.setOnClickListener(v -> {
            if (!tienePermisos()) {
                Toast.makeText(this, "Faltan permisos de Bluetooth", Toast.LENGTH_SHORT).show();
                verificacionPermisos();
                return;
            }
            activarBluetooth();
            startActivity(new Intent(this, ActividadComunicacion.class));
            linear_layout_segunda_fila.removeAllViews();
            linear_layout_segunda_fila.addView(salir);
        });

        salir.setOnClickListener(v -> finish());
    }

    /* Desde Android 13 una app ya no puede encender el Bluetooth directamente:
       se le pide al usuario que lo active. */
    @SuppressLint("MissingPermission")
    private void activarBluetooth() {
        BA = BluetoothAdapter.getDefaultAdapter();
        if (BA == null) {
            Toast.makeText(this, "Este dispositivo no tiene Bluetooth", Toast.LENGTH_LONG).show();
            return;
        }
        if (!BA.isEnabled()) {
            startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AlmacenDatosRAM.conexion_bluetooth = " ";
    }

    private boolean tienePermisos() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
    }

    /*
    A partir de Android 12 se piden en tiempo de ejecución BLUETOOTH_SCAN,
    BLUETOOTH_ADVERTISE y BLUETOOTH_CONNECT. En Android 11 o inferior el escaneo
    exige el permiso de ubicación.
    */
    private void verificacionPermisos() {
        if (tienePermisos()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT
            }, CODIGO_PERMISOS);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, CODIGO_PERMISOS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CODIGO_PERMISOS) {
            boolean todos = grantResults.length > 0;
            for (int r : grantResults) {
                if (r != PackageManager.PERMISSION_GRANTED) todos = false;
            }
            if (!todos) {
                Toast.makeText(this, "Permiso denegado.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}