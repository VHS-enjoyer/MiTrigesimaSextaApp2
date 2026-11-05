package com.curso_simulaciones.mitrigesimasextaapp.comunicaciones_bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import com.curso_simulaciones.mitrigesimasextaapp.almacen_datos.AlmacenDatosRAM;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.UUID;

@SuppressLint("MissingPermission") // los permisos se piden en la actividad principal
public class ServidorBluetooth {

    public static final String NOMBRE_SEGURO = "BluetoothServiceSecure";
    public BluetoothAdapter adaptadorBluetooth;
    public BluetoothServerSocket serverSocket;
    public BluetoothSocket clienteSocket;
    private BufferedInputStream flujoEntrada;
    private BufferedOutputStream flujoSalida;

    // UUID estándar del servicio SPP
    public static final UUID UUID_SEGURO = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public ServidorBluetooth() {
        adaptadorBluetooth = BluetoothAdapter.getDefaultAdapter();
    }

    // 1. Abrir un socket (ServerSocket) para esperar por peticiones de conexión
    public void abrirSocketServidor() {
        serverSocket = null;
        try {
            serverSocket = adaptadorBluetooth.listenUsingRfcommWithServiceRecord(NOMBRE_SEGURO, UUID_SEGURO);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 2. Al llegar una petición, crear otro socket (BLOQUEANTE: llamar desde un hilo secundario)
    public void abrirSocketCliente() {
        if (serverSocket == null) {
            AlmacenDatosRAM.conexion_bluetooth = " Falló la conexión";
            return;
        }
        try {
            clienteSocket = serverSocket.accept();
            AlmacenDatosRAM.conexion_bluetooth = " Conectado con "
                    + String.valueOf(clienteSocket.getRemoteDevice().getName());
            // ya no se aceptarán más conexiones: se libera el ServerSocket
            cerrarSocketServidor();
        } catch (IOException e) {
            AlmacenDatosRAM.conexion_bluetooth = " Falló la conexión";
            e.printStackTrace();
        }
    }

    // 3. Flujo de entrada
    public void abrirFlujoEntrada() {
        if (clienteSocket == null) return;
        try {
            flujoEntrada = new BufferedInputStream(clienteSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 3. Flujo de salida
    public void abrirFlujoSalida() {
        if (clienteSocket == null) return;
        try {
            flujoSalida = new BufferedOutputStream(clienteSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 4. Leer/Escribir a los flujos de acuerdo al protocolo establecido
    public byte[] leerBytes() {
        byte[] buffer = new byte[1024];
        if (flujoEntrada != null) {
            try {
                int nuevoDato = flujoEntrada.read(buffer);
                if (nuevoDato > 0) {
                    byte[] recibido = new byte[nuevoDato];
                    System.arraycopy(buffer, 0, recibido, 0, nuevoDato);
                    return recibido;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /** Devuelve false si falló (conexión caída) */
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
                // el cliente puede haberse ido ya
            }
        }
    }

    public void cerrarSocketCliente() {
        if (clienteSocket != null) {
            try {
                clienteSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void cerrarSocketServidor() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}