package com.curso_simulaciones.mitrigesimasextaapp.actividades_secundarias;

import android.app.Activity;
import android.os.Bundle;

import com.curso_simulaciones.mitrigesimasextaapp.almacen_datos.AlmacenDatosRAM;

// IMPORTANTE: use exactamente el mismo import de ScannerBluetooth que le funcione
// en MiTrigesimaQuintaApp (el paquete real está dentro de comunicaciones.aar).
import com.example.comunicaciones.ScannerBluetooth;

public class ActividadEscaneoDispositivos extends Activity {
    ScannerBluetooth scanear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scanear = new ScannerBluetooth(this);
        scanear.descubriendoDispositivos();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (scanear != null && scanear.direccion != null) {
            AlmacenDatosRAM.direccion = scanear.direccion;
        }
    }
}