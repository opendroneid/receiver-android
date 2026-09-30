/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * AMap (Gaode) online raster tile source for osmdroid.
 *
 * This gives the receiver app a detailed Chinese road/satellite base map
 * without requiring any AMap developer key. It is an un-official use of the
 * AMap tile endpoints: if they stop working, register a free key at
 * https://lbs.amap.com and switch to the official AMap Android SDK.
 *
 * AMap tiles are rendered in the GCJ-02 reference frame - pair this tile
 * source with org.opendroneid.android.data.CoordTransform, otherwise every
 * marker will be offset by a few hundred meters.
 */

package org.opendroneid.android.app;

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.MapTileIndex;

import java.util.Locale;

public class AMapTileSource extends OnlineTileSourceBase {

    /** Vector road map with Chinese labels (default). */
    public static final String TYPE_VECTOR = "7";
    /** Satellite imagery, no labels - combine with TYPE_ROAD_NET for overlays. */
    public static final String TYPE_SATELLITE = "6";
    /** Transparent road network overlay, meant to be stacked on top of TYPE_SATELLITE. */
    public static final String TYPE_ROAD_NET = "8";

    private final String type;

    public AMapTileSource(String type) {
        super("Amap-style" + type, 3, 19, 256, ".png",
                new String[]{"https://wprd01.is.autonavi.com"});
        this.type = type;
    }

    @Override
    public String getTileURLString(long pMapTileIndex) {
        int zoom = MapTileIndex.getZoom(pMapTileIndex);
        int x = MapTileIndex.getX(pMapTileIndex);
        int y = MapTileIndex.getY(pMapTileIndex);
        // The vector map (style=7) is served from wprd01..wprd04 with full query
        // parameters; satellite imagery (style=6) and the road/label overlay
        // (style=8) live on the separate webst01..webst04 hosts.
        if (TYPE_VECTOR.equals(type)) {
            int sub = (x + y) % 4 + 1;
            return String.format(Locale.US,
                    "https://wprd0%d.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scl=1&style=%s&x=%d&y=%d&z=%d",
                    sub, type, x, y, zoom);
        }
        int sub = (x + y) % 4 + 1;
        return String.format(Locale.US,
                "https://webst0%d.is.autonavi.com/appmaptile?style=%s&x=%d&y=%d&z=%d",
                sub, type, x, y, zoom);
    }
}
