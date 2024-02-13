package org.opendroneid.android.views;

import static androidx.core.graphics.drawable.DrawableCompat.getColorFilter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.TilesOverlay;

public class OSMCustomTilesOverlay extends TilesOverlay {
    private Paint paint;
    private ColorMatrixColorFilter colorFilter;

    public OSMCustomTilesOverlay(MapTileProviderBasic tileProvider, MapView mapView) {
        super(tileProvider, mapView.getContext());
        paint = new Paint();
    }

    public void setColorFilter(ColorMatrixColorFilter colorFilter) {
        this.colorFilter = colorFilter;
    }

    @Override
    public void draw(Canvas c, MapView osmv, boolean shadow) {
        if (colorFilter != null) {
            paint.setColorFilter(colorFilter);
        }
        super.draw(c, osmv, shadow);
    }
}
