package org.opendroneid.android.views;

import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;

public class OSMCustomColorFilter {
    public static ColorMatrixColorFilter createCustomFilter(int destinationColor) {
        ColorMatrix inverseMatrix = new ColorMatrix(new float[] {
                -1.0f, 0.0f, 0.0f, 0.0f, 255f,
                0.0f, -1.0f, 0.0f, 0.0f, 255f,
                0.0f, 0.0f, -1.0f, 0.0f, 255f,
                0.0f, 0.0f, 0.0f, 1.0f, 0.0f
        });

        float lr = (255.0f - Color.red(destinationColor))/255.0f;
        float lg = (255.0f - Color.green(destinationColor))/255.0f;
        float lb = (255.0f - Color.blue(destinationColor))/255.0f;
        ColorMatrix grayscaleMatrix = new ColorMatrix(new float[] {
                lr, lg, lb, 0, 0, //
                lr, lg, lb, 0, 0, //
                lr, lg, lb, 0, 0, //
                0, 0, 0, 0, 255, //
        });
        grayscaleMatrix.preConcat(inverseMatrix);

        // Darken the tiles by reducing brightness
        float brightnessScale = 0.7f;
        ColorMatrix brightnessMatrix = new ColorMatrix();
        brightnessMatrix.setScale(brightnessScale, brightnessScale, brightnessScale, 1);

        // Apply a slightly bluish tint for a dark mode effect
        float tintScale = 0.8f;
        ColorMatrix tintMatrix = new ColorMatrix(new float[] {
                tintScale, 0, 0, 0, 0, //
                0, tintScale, 0, 0, 0, //
                0, 0, tintScale, 0, 0, //
                0, 0, 0, 1, 0, //
        });

        ColorMatrix combinedMatrix = new ColorMatrix();
        combinedMatrix.setConcat(grayscaleMatrix, brightnessMatrix);
        combinedMatrix.postConcat(tintMatrix);

        return new ColorMatrixColorFilter(combinedMatrix);
    }
}
