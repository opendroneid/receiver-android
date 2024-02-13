package org.opendroneid.android.views;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;

public class OSMCustomColorFilter {
    public static ColorMatrixColorFilter createDarkModeFilter() {
        ColorMatrix colorMatrix = new ColorMatrix(new float[]{
                0.2f, 0.0f, 0.0f, 0.0f, 0.0f,
                0.2f, 0.0f, 0.0f, 0.0f, 0.0f,
                0.2f, 0.0f, 0.0f, 0.0f, 0.0f,
                0.8f, 0.8f, 0.8f, 1.0f, 0.0f,
                0, 0, 0, 1, 0
        });
        return new ColorMatrixColorFilter(colorMatrix);
    }
}