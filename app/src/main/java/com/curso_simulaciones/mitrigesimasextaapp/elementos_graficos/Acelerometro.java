package com.curso_simulaciones.mitrigesimasextaapp.elementos_graficos;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class Acelerometro extends GaugeSimple implements SensorEventListener {
    private SensorManager sensorManager;
    private int componenteAceleracion = 4;

    public Acelerometro(Context context) {
        super(context);
        setComponenteAcelerometro(componenteAceleracion);
    }

    /** 1: ax, 2: ay, 3: az, 4: magnitud a */
    public void setComponenteAcelerometro(int componenteAceleracion) {
        this.componenteAceleracion = componenteAceleracion;
        if (componenteAceleracion == 1) {
            this.setUnidades("ax (m/s^2)");
            this.setRango(-20, 20);
        }
        if (componenteAceleracion == 2) {
            this.setUnidades("ay (m/s^2)");
            this.setRango(-20, 20);
        }
        if (componenteAceleracion == 3) {
            this.setUnidades("az (m/s^2)");
            this.setRango(-20, 20);
        }
        if (componenteAceleracion == 4) {
            this.setUnidades("a (m/s^2)");
            this.setRango(0, 20);
        }
    }

    public void captarSensor(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
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
        float a = (float) Math.sqrt(medida_x * medida_x + medida_y * medida_y + medida_z * medida_z);

        float medida = a;
        if (componenteAceleracion == 1) medida = medida_x;
        if (componenteAceleracion == 2) medida = medida_y;
        if (componenteAceleracion == 3) medida = medida_z;

        // un decimal
        medida = Math.round(medida * 10) / 10.0f;
        this.setMedida(medida);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}