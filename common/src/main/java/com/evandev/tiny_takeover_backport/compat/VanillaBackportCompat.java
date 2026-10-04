package com.evandev.tiny_takeover_backport.compat;

import com.blackgear.vanillabackport.core.VanillaBackport;

public class VanillaBackportCompat {
    public static boolean hasFarmAnimalVariants() {
        try {
            return VanillaBackport.COMMON_CONFIG.hasFarmAnimalVariants.get();
        } catch (Throwable t) {
            return false;
        }
    }
}
