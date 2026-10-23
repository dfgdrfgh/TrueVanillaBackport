package com.blackgear.vanillabackport.core.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;

import java.util.List;

/** Insert new vanilla-namespaced Ice Caves items beside their natural peers. */
public final class IceCavesCreativeSorting implements ModInitializer {
    private static final String NS = "minecraft";

    private static Item item(String path) {
        return BuiltInRegistries.ITEM.getOptional(ResourceLocation.fromNamespaceAndPath(NS, path))
            .orElseThrow(() -> new IllegalStateException("Missing integrated item: " + NS + ":" + path));
    }

    private static net.minecraft.core.Holder<Potion> potion(String path) {
        return BuiltInRegistries.POTION.getHolder(ResourceKey.create(
                Registries.POTION, ResourceLocation.fromNamespaceAndPath(NS, path)))
            .orElseThrow(() -> new IllegalStateException("Missing integrated potion: " + NS + ":" + path));
    }

    @Override
    public void onInitialize() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS).register(entries ->
            entries.addAfter(Items.BLUE_ICE,
                item("ice_crystal"), item("icicle"), item("brittle_ice")));

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register(entries ->
            entries.addAfter(Items.PRISMARINE_BRICKS,
                item("packed_ice_bricks"), item("packed_ice_brick_stairs"),
                item("packed_ice_brick_slab"), item("packed_ice_brick_wall"),
                item("chiseled_packed_ice")));

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(entries -> {
            entries.addAfter(Items.SNOWBALL, item("ice_ball"));
            entries.addAfter(Items.TIPPED_ARROW, List.of(
                PotionContents.createItemStack(Items.TIPPED_ARROW, potion("freezing")),
                PotionContents.createItemStack(Items.TIPPED_ARROW, potion("long_freezing"))
            ));
        });

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(entries ->
            entries.addAfter(Items.FROG_SPAWN_EGG, item("frostbite_spawn_egg")));

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(entries -> {
            entries.addAfter(Items.POTION, List.of(
                PotionContents.createItemStack(Items.POTION, potion("freezing")),
                PotionContents.createItemStack(Items.POTION, potion("long_freezing"))
            ));
            entries.addAfter(Items.SPLASH_POTION, List.of(
                PotionContents.createItemStack(Items.SPLASH_POTION, potion("freezing")),
                PotionContents.createItemStack(Items.SPLASH_POTION, potion("long_freezing"))
            ));
            entries.addAfter(Items.LINGERING_POTION, List.of(
                PotionContents.createItemStack(Items.LINGERING_POTION, potion("freezing")),
                PotionContents.createItemStack(Items.LINGERING_POTION, potion("long_freezing"))
            ));
        });
    }
}
