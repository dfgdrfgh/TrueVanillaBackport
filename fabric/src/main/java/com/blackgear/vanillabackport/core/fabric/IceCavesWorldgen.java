package com.blackgear.vanillabackport.core.fabric;

import com.blackgear.platform.common.worldgen.placement.BiomePlacement;
import com.blackgear.vanillabackport.common.registries.worldgen.IceCavesBiomes;
import com.blackgear.vanillabackport.core.VanillaBackport;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Climate;

/** Adds the snapshot cave climate point without changing existing surface slots. */
public final class IceCavesWorldgen implements ModInitializer {
    @Override
    public void onInitialize() {
        Registry.register(BuiltInRegistries.FEATURE,
            ResourceLocation.fromNamespaceAndPath("vanillabackport", "ice_cave_ore"),
            new IceCaveOreFeature());
        BiomePlacement.registerBiomePlacements(IceCavesWorldgen::bootstrap);
    }

    public static void bootstrap(BiomePlacement.Event event) {
        if (VanillaBackport.COMMON_CONFIG.hasIceCaves.get()) {
            event.add(Pair.of(Climate.parameters(
                Climate.Parameter.span(-1.0F, -0.8F),
                Climate.Parameter.span(-1.0F, 0.7F),
                Climate.Parameter.span(-1.05F, -0.19F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(0.2F, 0.9F),
                Climate.Parameter.span(0.5F, 1.1F),
                0.0F
            ), IceCavesBiomes.ICE_CAVES));
        }
    }
}
