/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Google satellite imagery tile source for osmdroid.
 *
 * Un-official use of the Google tile endpoints (the "lyrs=s" layer).
 * The tiles are plain Web Mercator / WGS-84, so NO coordinate conversion is
 * needed when this source is used (unlike the AMap sources).
 *
 * Note: these hosts are not reachable from mainland China without a proxy.
 */

package org.opendroneid.android.app;

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.MapTileIndex;

import java.util.Locale;

public class GoogleSatTileSource extends OnlineTileSourceBase {

    public GoogleSatTileSource() {
        super("GoogleSat", 2, 20, 256, ".jpg",
                new String[]{"https://mt0.google.com"});
    }

    @Override
    public String getTileURLString(long pMapTileIndex) {
        int zoom = MapTileIndex.getZoom(pMapTileIndex);
        int x = MapTileIndex.getX(pMapTileIndex);
        int y = MapTileIndex.getY(pMapTileIndex);
        // mt0..mt3
        int sub = (x + y) % 4;
        return String.format(Locale.US,
                "https://mt%d.google.com/vt/lyrs=s&x=%d&y=%d&z=%d",
                sub, x, y, zoom);
    }
}
