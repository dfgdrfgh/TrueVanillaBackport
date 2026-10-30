package com.blackgear.vanillabackport.common.level.items.maps;

import com.blackgear.vanillabackport.common.registries.items.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.MapDecorations;

import java.util.Map;
import java.util.function.Supplier;

/** The 26.3 explorer-map item migration, using only the vanilla "+" target marker. */
public final class ExplorerMaps {
    public static final TagKey<Item> EXTENDABLE_MAPS = TagKey.create(Registries.ITEM,
        ResourceLocation.withDefaultNamespace("extendable_maps"));

    private record MapType(Supplier<Item> item, String legacyName) { }

    private static final Map<String, MapType> TYPES = Map.ofEntries(
        Map.entry("mansion", new MapType(ModItems.WOODLAND_MANSION_MAP, "filled_map.mansion")),
        Map.entry("monument", new MapType(ModItems.OCEAN_MONUMENT_MAP, "filled_map.monument")),
        Map.entry("trial_chambers", new MapType(ModItems.BURIED_TRIAL_CHAMBERS_MAP, "filled_map.trial_chambers")),
        Map.entry("jungle_temple", new MapType(ModItems.JUNGLE_PYRAMID_MAP, "filled_map.explorer_jungle")),
        Map.entry("swamp_hut", new MapType(ModItems.SWAMP_HUT_MAP, "filled_map.explorer_swamp")),
        Map.entry("village_desert", new MapType(ModItems.DESERT_VILLAGE_MAP, "filled_map.village_desert")),
        Map.entry("village_plains", new MapType(ModItems.PLAINS_VILLAGE_MAP, "filled_map.village_plains")),
        Map.entry("village_savanna", new MapType(ModItems.SAVANNA_VILLAGE_MAP, "filled_map.village_savanna")),
        Map.entry("village_snowy", new MapType(ModItems.SNOWY_VILLAGE_MAP, "filled_map.village_snowy")),
        Map.entry("village_taiga", new MapType(ModItems.TAIGA_VILLAGE_MAP, "filled_map.village_taiga")),
        Map.entry("red_x", new MapType(ModItems.BURIED_TREASURE_MAP, "filled_map.buried_treasure")),
        Map.entry("ancient_city", new MapType(ModItems.BURIED_ANCIENT_CITY_MAP, null)),
        Map.entry("mineshaft", new MapType(ModItems.BURIED_MINESHAFT_MAP, null)),
        Map.entry("desert_pyramid", new MapType(ModItems.DESERT_PYRAMID_MAP, null)),
        Map.entry("abandoned_camp", new MapType(ModItems.ABANDONED_CAMP_MAP, null)),
        Map.entry("ocean_ruin_warm", new MapType(ModItems.WARM_OCEAN_RUINS_MAP, null))
    );

    private ExplorerMaps() { }

    public static ItemStack convert(ItemStack stack) {
        if (!stack.is(Items.FILLED_MAP)) return stack;
        MapDecorations.Entry target = stack.getOrDefault(DataComponents.MAP_DECORATIONS, MapDecorations.EMPTY)
            .decorations().get("+");
        if (target == null) return stack;
        ResourceLocation id = target.type().unwrapKey().map(key -> key.location()).orElse(null);
        if (id == null || !id.getNamespace().equals("minecraft")) return stack;
        MapType type = TYPES.get(id.getPath());
        if (type == null) return stack;
        ItemStack result = stack.transmuteCopy(type.item.get());
        // Preserve player names, styled names, camp-specific names and every other component.
        if (type.legacyName != null && Component.translatable(type.legacyName).equals(result.get(DataComponents.ITEM_NAME))) {
            result.remove(DataComponents.ITEM_NAME);
        }
        return result;
    }
}
