/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Button overlay drawn next to the compass: centres the map on the current
 * position of the phone (the receiver's own location) when tapped.
 *
 * The icon is dimmed until a first location fix is available.
 */

package org.opendroneid.android.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;

import org.opendroneid.android.R;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class MyLocationCenterOverlay extends Overlay {

    /** Screen position, in dp. The compass sits at (35, 35), so this is just right of it. */
    private static final int CENTER_X_DP = 82;
    private static final int CENTER_Y_DP = 35;
    private static final int SIZE_DP = 32;

    /** Zoom level the map jumps to when centring on the phone - street level. */
    private static final double CENTRE_ZOOM_LEVEL = 15.0;

    private final float scale;
    private final Drawable icon;
    private final Context context;
    private final MyLocationNewOverlay locationOverlay;

    public MyLocationCenterOverlay(Context context, MyLocationNewOverlay locationOverlay) {
        super();
        this.context = context.getApplicationContext();
        this.locationOverlay = locationOverlay;
        scale = context.getResources().getDisplayMetrics().density;
        Drawable source = AppCompatResources.getDrawable(context, R.drawable.ic_my_location);
        if (source != null) {
            icon = DrawableCompat.wrap(source).mutate();
            DrawableCompat.setTint(icon, 0xFF2979FF); // Google "my location" blue
        } else {
            icon = null;
        }
    }

    private GeoPoint currentPosition() {
        return locationOverlay == null ? null : locationOverlay.getMyLocation();
    }

    private int[] screenBounds() {
        int size = (int) (SIZE_DP * scale);
        int left = (int) (CENTER_X_DP * scale) - size / 2;
        int top = (int) (CENTER_Y_DP * scale) - size / 2;
        return new int[]{left, top, left + size, top + size};
    }

    @Override
    public void draw(Canvas canvas, Projection projection) {
        if (icon == null) return;
        int[] b = screenBounds();
        icon.setBounds(b[0], b[1], b[2], b[3]);
        icon.setAlpha(currentPosition() == null ? 70 : 255);
        icon.draw(canvas);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event, MapView mapView) {
        int[] b = screenBounds();
        float x = event.getX();
        float y = event.getY();
        if (x < b[0] || x > b[2] || y < b[1] || y > b[3]) {
            return false;
        }
        GeoPoint position = currentPosition();
        if (position == null) {
            Toast.makeText(context, R.string.my_location_unavailable, Toast.LENGTH_SHORT).show();
            return true;
        }
        double zoom = Math.max(mapView.getMinZoomLevel(),
                Math.min(mapView.getMaxZoomLevel(), CENTRE_ZOOM_LEVEL));
        // Animate the pan and the zoom together, so the map lands on the phone's
        // position at street level instead of just sliding there at the old zoom.
        mapView.getController().animateTo(position, zoom, null, null);
        return true;
    }
}
