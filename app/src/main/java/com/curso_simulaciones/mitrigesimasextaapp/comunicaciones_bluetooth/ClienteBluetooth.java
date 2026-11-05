package com.curso_simulaciones.mitrigesimasextaapp.comunicaciones_bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.curso_simulaciones.mitrigesimasextaapp.almacen_datos.AlmacenDatosRAM;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

@SuppressLint("MissingPermission") // los permisos se piden en la actividad principal
public class ClienteBluetooth {
    public BluetoothAdapter adaptadorBluetooth;
    public BluetoothDevice dispositivo;
    public BluetoothSocket clienteSocket;
    // Los mensajes (JSON) terminan en '\n'; se leen por líneas
    private BufferedReader flujoEntrada;
    private BufferedOutputStream flujoSalida;

    // UUID estándar del servicio SPP (puerto serie virtual vía RFCOMM)
    public static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public ClienteBluetooth() {
    }

    // 1. Abrir un socket (Socket) para conectarse y comunicarse con el Servidor
    public boolean abrirSocketCliente(String direccion) {
        adaptadorBluetooth = BluetoothAdapter.getDefaultAdapter();
        if (adaptadorBluetooth == null || direccion == null
                || !BluetoothAdapter.checkBluetoothAddress(direccion)) {
            return false;
        }
        dispositivo = adaptadorBluetooth.getRemoteDevice(direccion);
        try {
            clienteSocket = dispositivo.createRfcommSocketToServiceRecord(uuid);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 2. Establecer conexión con el Servidor (BLOQUEANTE: llamar desde un hilo secundario)
    public boolean conectarSocketCliente() {
        try {
            adaptadorBluetooth.cancelDiscovery();
            clienteSocket.connect();
            AlmacenDatosRAM.conexion_bluetooth = " Conectado con "
                    + String.valueOf(clienteSocket.getRemoteDevice().getName());
            return true;
        } catch (IOException e) {
            AlmacenDatosRAM.conexion_bluetooth = " No se pudo conectar...";
            e.printStackTrace();
            cerrarSocketCliente();
            return false;
        }
    }

    // 3. Flujo de entrada
    public void abrirFlujoEntrada() {
        if (clienteSocket != null) {
            try {
                flujoEntrada = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 3. Flujo de salida
    public void abrirFlujoSalida() {
        if (clienteSocket != null) {
            try {
                flujoSalida = new BufferedOutputStream(clienteSocket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 4. Leer/Escribir a los flujos de acuerdo al protocolo establecido
    /**
     * Lee un mensaje (una línea). Si el servidor envió varios mientras tanto,
     * devuelve el más reciente. Devuelve null si la conexión se cerró o hubo error.
     */
    public String leerString() {
        if (flujoEntrada == null) return null;
        try {
            String linea = flujoEntrada.readLine(); // bloquea hasta recibir un mensaje
            if (linea == null) return null;         // el servidor cerró la conexión
            while (flujoEntrada.ready()) {          // descartar los atrasados
                String siguiente = flujoEntrada.readLine();
                if (siguiente == null) break;
                linea = siguiente;
            }
            return linea;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean escribirBytes(byte[] datoByteParaEnviar) {
        if (flujoSalida == null || datoByteParaEnviar == null) return false;
        try {
            flujoSalida.write(datoByteParaEnviar);
            flujoSalida.flush();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // 6. Cerrar flujos y sockets
    public void cerrarSocketCliente() {
        if (clienteSocket != null) {
            try {
                clienteSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void cerrarFlujoEntrada() {
        if (flujoEntrada != null) {
            try {
                flujoEntrada.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void cerrarFlujoSalida() {
        if (flujoSalida != null) {
            try {
                flujoSalida.flush();
                flujoSalida.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}