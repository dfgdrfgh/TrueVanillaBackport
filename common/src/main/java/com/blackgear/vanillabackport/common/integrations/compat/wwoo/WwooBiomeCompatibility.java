package com.blackgear.vanillabackport.common.integrations.compat.wwoo;

import com.blackgear.vanillabackport.core.VanillaBackport;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WwooBiomeCompatibility {
    private static final List<ResourceLocation> BACKPORTED_BIOMES = List.of(
        ResourceLocation.withDefaultNamespace("worldgen/biome/pale_garden.json"),
        ResourceLocation.withDefaultNamespace("worldgen/biome/dappled_forest.json"),
        ResourceLocation.withDefaultNamespace("worldgen/biome/sulfur_caves.json")
    );

    private WwooBiomeCompatibility() {}

    /** Keep WWOO's overhaul of older biomes without replacing backported biome content. */
    public static Map<ResourceLocation, Resource> resolveBiomes(Map<ResourceLocation, Resource> resources, ResourceManager manager) {
        Map<ResourceLocation, Resource> resolved = resources;
        for (ResourceLocation location : BACKPORTED_BIOMES) {
            Resource selected = resources.get(location);
            if (selected == null || !isWwooPack(selected.sourcePackId())) {
                continue;
            }

            // Resource stacks run from lowest to highest priority. Preserve the highest
            // non-WWOO definition, including explicit world datapacks supplied by the user.
            List<Resource> stack = manager.getResourceStack(location);
            for (int index = stack.size() - 1; index >= 0; index--) {
                Resource candidate = stack.get(index);
                if (!isWwooPack(candidate.sourcePackId())) {
                    if (resolved == resources) {
                        resolved = new HashMap<>(resources);
                    }
                    resolved.put(location, candidate);
                    VanillaBackport.LOGGER.info("WWOO compatibility: preserving {} from {} instead of {}",
                        location, candidate.sourcePackId(), selected.sourcePackId());
                    break;
                }
            }
        }
        return resolved;
    }

    private static boolean isWwooPack(String packId) {
        return packId.startsWith("wwoo:") || packId.startsWith("wwoo/")
            || packId.equals("mod/wwoo") || packId.equals("mod:wwoo");
    }
}
