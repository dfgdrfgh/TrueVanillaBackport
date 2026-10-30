package com.blackgear.vanillabackport.common.level.items.maps;

/** Vanilla 26.3 uses the same direction for on-map and off-map player markers. */
public final class MapMarkerRotation {
    private MapMarkerRotation() { }

    public static byte calculate(double yaw, boolean nether, long gameTime) {
        if (nether) {
            int time = (int)(gameTime / 10L);
            return (byte)((time * time * 34187121 + time * 121 >> 15) & 15);
        }
        double adjusted = yaw < 0.0 ? yaw - 8.0 : yaw + 8.0;
        return (byte)(adjusted * 16.0 / 360.0);
    }
}
