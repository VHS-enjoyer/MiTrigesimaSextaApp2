package com.curso_simulaciones.mitrigesimasextaapp.elementos_graficos;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;

import androidx.appcompat.widget.AppCompatImageView;

public class Boton extends AppCompatImageView {
    private Bitmap imagen;
    private Bitmap imagen_escalada = null;
    private int ladoEscalaActual = -1;
    private String cadena = "";
    private final Paint pincel = new Paint();

    public Boton(Context context) {
        super(context);
    }

    public void setText(String cadena) {
        this.cadena = cadena;
    }

    public String getText() {
        return cadena;
    }

    public void setImagen(int imagen_importada) {
        imagen = BitmapFactory.decodeResource(getResources(), imagen_importada);
        imagen_escalada = null;
        ladoEscalaActual = -1;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (imagen == null) return;

        pincel.setAntiAlias(true);
        pincel.setLinearText(true);

        float escala = Math.min(getWidth(), getHeight());
        int lado = (int) (0.9f * escala);
        if (lado <= 0) return;

        // Solo se reescala cuando cambia el tamaño de la vista
        if (imagen_escalada == null || lado != ladoEscalaActual) {
            imagen_escalada = escalarImagen(imagen, lado, lado);
            ladoEscalaActual = lado;
        }

        int ex = (getWidth() - imagen_escalada.getWidth()) / 2;
        int ey = (getHeight() - imagen_escalada.getHeight()) / 2;
        float tamanoLetra = 0.08f * getWidth();

        canvas.drawBitmap(imagen_escalada, ex, ey, null);

        pincel.setTextSize(tamanoLetra);
        float anchoCadena = pincel.measureText(cadena);
        float posicion_x_letra = (getWidth() - anchoCadena) / 2;
        float posicion_y_letra = ey + imagen_escalada.getHeight() + tamanoLetra;
        pincel.setColor(Color.BLACK);
        canvas.drawText(cadena, posicion_x_letra, posicion_y_letra, pincel);
    }

    /* Se escala con Matrix para no dañar la resolución */
    public Bitmap escalarImagen(Bitmap bitmap, int newWidth, int newHeight) {
        Bitmap.Config config = bitmap.getConfig() != null ? bitmap.getConfig() : Bitmap.Config.ARGB_8888;
        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, config);
        float scaleX = newWidth / (float) bitmap.getWidth();
        float scaleY = newHeight / (float) bitmap.getHeight();
        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scaleX, scaleY, 0, 0);
        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return scaledBitmap;
    }
}