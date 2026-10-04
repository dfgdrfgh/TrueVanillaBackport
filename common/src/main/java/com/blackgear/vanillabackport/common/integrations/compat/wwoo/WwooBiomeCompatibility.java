package com.blackgear.vanillabackport.common.integrations.compat.wwoo;

import com.blackgear.vanillabackport.core.ModChecker;
import com.blackgear.vanillabackport.core.VanillaBackport;
import com.blackgear.vanillabackport.common.registries.worldgen.ModBiomes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;

public final class WwooBiomeCompatibility {
    private WwooBiomeCompatibility() {}

    public static boolean useNativePaleGardenPlacement() {
        return ModChecker.WWOO;
    }

    /** Vanilla 1.21.4's plateau variant [temperature 2][humidity 4]. */
    public static ResourceKey<Biome> selectPlateauBiome(ResourceKey<Biome> original, int temperature, int humidity, Climate.Parameter weirdness) {
        if (useNativePaleGardenPlacement() && VanillaBackport.COMMON_CONFIG.hasPaleGarden.get()
            && temperature == 2 && humidity == 4 && weirdness.max() >= 0L
            && original.equals(Biomes.DARK_FOREST)) {
            return ModBiomes.PALE_GARDEN;
        }
        return original;
    }
}
