package com.curso_simulaciones.mitrigesimasextaapp.elementos_graficos;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class Magnetometro extends GaugeSimple implements SensorEventListener {
    private SensorManager sensorManager;
    private int componenteCampo = 4;

    public Magnetometro(Context context) {
        super(context);
        setComponenteMagnetometro(componenteCampo);
    }

    /** 1: Bx, 2: By, 3: Bz, 4: magnitud B (en microteslas) */
    public void setComponenteMagnetometro(int componenteCampo) {
        this.componenteCampo = componenteCampo;
        if (componenteCampo == 1) {
            this.setUnidades("Bx (µT)");
            this.setRango(-100, 100);
        }
        if (componenteCampo == 2) {
            this.setUnidades("By (µT)");
            this.setRango(-100, 100);
        }
        if (componenteCampo == 3) {
            this.setUnidades("Bz (µT)");
            this.setRango(-100, 100);
        }
        if (componenteCampo == 4) {
            this.setUnidades("B (µT)");
            this.setRango(0, 100);
        }
    }

    public void captarSensor(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    /** Libera el sensor (llamar en onDestroy) */
    public void detenerSensor() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float medida_x = event.values[SensorManager.DATA_X];
        float medida_y = event.values[SensorManager.DATA_Y];
        float medida_z = event.values[SensorManager.DATA_Z];
        float b = (float) Math.sqrt(medida_x * medida_x + medida_y * medida_y + medida_z * medida_z);

        float medida = b;
        if (componenteCampo == 1) medida = medida_x;
        if (componenteCampo == 2) medida = medida_y;
        if (componenteCampo == 3) medida = medida_z;

        // un decimal
        medida = Math.round(medida * 10) / 10.0f;
        this.setMedida(medida);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}