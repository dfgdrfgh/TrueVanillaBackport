package com.blackgear.vanillabackport.core.fabric;

import com.blackgear.vanillabackport.common.registries.items.ModItems;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Creative-inventory ordering adjustments introduced through Java 26.3.
 * Only changes the order of entries that are already present; it never adds a
 * disabled item or removes another mod's entries. Unknown entries keep their
 * order relative to each other.
 */
public final class ModernCreativeSorting {
    private ModernCreativeSorting() {}

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(entries -> {
            reorderPresent(entries.getDisplayStacks(), ingredientOrder());
            reorderPresent(entries.getSearchTabStacks(), ingredientOrder());
        });

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register(entries -> {
            // 26.3 groups mineral blocks before the extensive copper family.
            ItemLike[] minerals = {
                Items.COAL_BLOCK, Items.REDSTONE_BLOCK, Items.LAPIS_BLOCK,
                Items.EMERALD_BLOCK, Items.DIAMOND_BLOCK,
                Items.RAW_COPPER_BLOCK, Items.RAW_IRON_BLOCK, Items.RAW_GOLD_BLOCK,
                Items.IRON_BLOCK, Items.GOLD_BLOCK, Items.NETHERITE_BLOCK,
                Items.COPPER_BLOCK, Items.EXPOSED_COPPER, Items.WEATHERED_COPPER,
                Items.OXIDIZED_COPPER
            };
            reorderPresent(entries.getDisplayStacks(), minerals);
            reorderPresent(entries.getSearchTabStacks(), minerals);
        });

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS).register(entries -> {
            // Preserve the ordering *within* each dimension while grouping
            // Overworld -> Nether -> End, as in Java 26.3.
            reorderDimensions(entries.getDisplayStacks());
            reorderDimensions(entries.getSearchTabStacks());
        });

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(entries -> {
            ItemLike[] themed = {
                // Overworld: farm animals, mounts, pets and wildlife.
                Items.CHICKEN_SPAWN_EGG, Items.COW_SPAWN_EGG,
                Items.PIG_SPAWN_EGG, Items.SHEEP_SPAWN_EGG,
                Items.CAMEL_SPAWN_EGG, ModItems.CAMEL_HUSK_SPAWN_EGG.get(),
                Items.DONKEY_SPAWN_EGG, Items.HORSE_SPAWN_EGG,
                Items.LLAMA_SPAWN_EGG, Items.MULE_SPAWN_EGG,
                ModItems.NAUTILUS_SPAWN_EGG.get(), Items.SKELETON_HORSE_SPAWN_EGG,
                Items.TRADER_LLAMA_SPAWN_EGG, Items.ZOMBIE_HORSE_SPAWN_EGG,
                ModItems.ZOMBIE_NAUTILUS_SPAWN_EGG.get(),
                Items.CAT_SPAWN_EGG, Items.PARROT_SPAWN_EGG, Items.WOLF_SPAWN_EGG,
                Items.ARMADILLO_SPAWN_EGG, Items.AXOLOTL_SPAWN_EGG,
                Items.BAT_SPAWN_EGG, Items.BEE_SPAWN_EGG, Items.FOX_SPAWN_EGG,
                Items.FROG_SPAWN_EGG, Items.GOAT_SPAWN_EGG,
                Items.OCELOT_SPAWN_EGG, Items.PANDA_SPAWN_EGG,
                Items.POLAR_BEAR_SPAWN_EGG, Items.RABBIT_SPAWN_EGG,
                Items.SNIFFER_SPAWN_EGG, Items.TURTLE_SPAWN_EGG,
                // Overworld: other mobs.
                Items.ALLAY_SPAWN_EGG, Items.IRON_GOLEM_SPAWN_EGG,
                ModItems.COPPER_GOLEM_SPAWN_EGG.get(),
                Items.SNOW_GOLEM_SPAWN_EGG, Items.VILLAGER_SPAWN_EGG,
                Items.WANDERING_TRADER_SPAWN_EGG,
                Items.BOGGED_SPAWN_EGG, Items.BREEZE_SPAWN_EGG,
                Items.CAVE_SPIDER_SPAWN_EGG, Items.CREEPER_SPAWN_EGG,
                ModItems.CREAKING_SPAWN_EGG.get(), Items.DROWNED_SPAWN_EGG,
                Items.HUSK_SPAWN_EGG, Items.PHANTOM_SPAWN_EGG,
                Items.PILLAGER_SPAWN_EGG, Items.RAVAGER_SPAWN_EGG,
                Items.SKELETON_SPAWN_EGG, ModItems.PARCHED_SPAWN_EGG.get(),
                Items.SLIME_SPAWN_EGG, Items.SPIDER_SPAWN_EGG,
                Items.STRAY_SPAWN_EGG, Items.VEX_SPAWN_EGG,
                Items.VINDICATOR_SPAWN_EGG, Items.WITCH_SPAWN_EGG,
                Items.ZOMBIE_SPAWN_EGG, Items.ZOMBIE_VILLAGER_SPAWN_EGG,
                // Nether, then End.
                Items.BLAZE_SPAWN_EGG, Items.GHAST_SPAWN_EGG,
                Items.HOGLIN_SPAWN_EGG, Items.MAGMA_CUBE_SPAWN_EGG,
                Items.PIGLIN_SPAWN_EGG, Items.PIGLIN_BRUTE_SPAWN_EGG,
                Items.STRIDER_SPAWN_EGG, Items.WITHER_SKELETON_SPAWN_EGG,
                Items.ZOGLIN_SPAWN_EGG, Items.ZOMBIFIED_PIGLIN_SPAWN_EGG,
                Items.ENDERMAN_SPAWN_EGG, Items.ENDERMITE_SPAWN_EGG,
                Items.SHULKER_SPAWN_EGG
            };
            reorderPresent(entries.getDisplayStacks(), themed);
            reorderPresent(entries.getSearchTabStacks(), themed);
        });
    }

    private static ItemLike[] ingredientOrder() {
        return new ItemLike[] {
            // Non-tiered ingredients.
            Items.COAL, Items.CHARCOAL, Items.FLINT, Items.CLAY_BALL,
            Items.BRICK, Items.NETHER_BRICK,
            Items.PRISMARINE_SHARD, Items.PRISMARINE_CRYSTALS,
            Items.QUARTZ, Items.AMETHYST_SHARD,
            Items.REDSTONE, Items.LAPIS_LAZULI, Items.EMERALD, Items.DIAMOND,
            // Unrefined tier materials, followed by refined nuggets and ingots.
            Items.RAW_COPPER, Items.RAW_IRON, Items.RAW_GOLD,
            ModItems.COPPER_NUGGET.get(), Items.IRON_NUGGET, Items.GOLD_NUGGET,
            Items.COPPER_INGOT, Items.IRON_INGOT, Items.GOLD_INGOT,
            Items.NETHERITE_SCRAP, Items.NETHERITE_INGOT
        };
    }

    /**
     * Sorts only matching slots. This leaves other mods' entries in place,
     * including special-component item stacks and undisclosed items.
     */
    private static void reorderPresent(List<ItemStack> stacks, ItemLike[] order) {
        Map<Item, Integer> rank = new IdentityHashMap<>();
        for (int i = 0; i < order.length; i++) rank.put(order[i].asItem(), i);
        List<Integer> positions = new ArrayList<>();
        List<ItemStack> matching = new ArrayList<>();
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            if (rank.containsKey(stack.getItem())) {
                positions.add(i);
                matching.add(stack);
            }
        }
        matching.sort(Comparator.comparingInt(stack -> rank.get(stack.getItem())));
        for (int i = 0; i < positions.size(); i++) stacks.set(positions.get(i), matching.get(i));
    }

    private static void reorderDimensions(List<ItemStack> stacks) {
        // Stable ordering; never derive the dimension from an arbitrary mod ID.
        // Known Nether and End block families are grouped after Overworld blocks.
        stacks.sort(Comparator.comparingInt(ModernCreativeSorting::dimensionOrder));
    }

    private static int dimensionOrder(ItemStack stack) {
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        if (!id.startsWith("minecraft:")) return 0;
        String name = id.substring("minecraft:".length());
        if (name.startsWith("end_stone") || name.startsWith("chorus") ||
            name.startsWith("purpur") || name.equals("end_rod") ||
            name.equals("dragon_egg")) return 2;
        if (name.contains("netherrack") || name.contains("nether_gold") ||
            name.contains("nether_quartz") || name.startsWith("crimson") ||
            name.startsWith("warped") || name.startsWith("soul_") ||
            name.contains("basalt") || name.contains("blackstone") ||
            name.contains("nylium") || name.contains("nether_wart") ||
            name.contains("shroomlight") || name.contains("weeping_vines") ||
            name.contains("twisting_vines") || name.contains("glowstone") ||
            name.contains("magma") || name.contains("ancient_debris")) return 1;
        return 0;
    }
}
