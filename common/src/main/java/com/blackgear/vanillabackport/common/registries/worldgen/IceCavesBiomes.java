package com.blackgear.vanillabackport.common.registries.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

/** A key only: the Frostbite-dependent biome is supplied by Fabric resources. */
public final class IceCavesBiomes {
    public static final ResourceKey<Biome> ICE_CAVES = ResourceKey.create(
        Registries.BIOME, ResourceLocation.withDefaultNamespace("ice_caves"));

    private IceCavesBiomes() {}
}
