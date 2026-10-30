package com.blackgear.vanillabackport.tests;

import com.blackgear.vanillabackport.common.level.entities.mob.monster.sulfur_cube.SulfurCube;
import com.blackgear.vanillabackport.common.registries.entities.ModEntityTypes;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.item.component.MapId;
import net.minecraft.world.item.component.MapItemColor;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.MapCloningRecipe;
import net.minecraft.world.item.crafting.MapExtendingRecipe;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.List;
import java.util.Map;

/** Separate test mod: never included in the installable release JAR. */
public final class ParityTestMod implements ModInitializer {
    private static ResourceLocation id(String path) { return ResourceLocation.withDefaultNamespace(path); }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    @Override public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            checkMaps(server);
            checkIce(server);
            System.out.println("TVB_PARITY_RUNTIME_OK: transformed map save/network/cloning/zoom and ice mechanics");
        });
    }

    private static void checkMaps(MinecraftServer server) {
        ServerLevel level = server.overworld();
        var ops = RegistryOps.create(JsonOps.INSTANCE, server.registryAccess());
        var pairs = Map.ofEntries(
            Map.entry("mansion", "woodland_mansion_map"), Map.entry("monument", "ocean_monument_map"),
            Map.entry("trial_chambers", "buried_trial_chambers_map"), Map.entry("jungle_temple", "jungle_pyramid_map"),
            Map.entry("swamp_hut", "swamp_hut_map"), Map.entry("village_desert", "desert_village_map"),
            Map.entry("village_plains", "plains_village_map"), Map.entry("village_savanna", "savanna_village_map"),
            Map.entry("village_snowy", "snowy_village_map"), Map.entry("village_taiga", "taiga_village_map"),
            Map.entry("red_x", "buried_treasure_map"), Map.entry("ancient_city", "buried_ancient_city_map"),
            Map.entry("mineshaft", "buried_mineshaft_map"), Map.entry("desert_pyramid", "desert_pyramid_map"),
            Map.entry("abandoned_camp", "abandoned_camp_map"), Map.entry("ocean_ruin_warm", "warm_ocean_ruins_map"));
        for (var pair : pairs.entrySet()) {
            ItemStack legacy = MapItem.create(level, 0, 0, (byte)0, true, true);
            legacy.setCount(3);
            var mapId = legacy.get(DataComponents.MAP_ID);
            var type = BuiltInRegistries.MAP_DECORATION_TYPE.getHolder(ResourceKey.create(Registries.MAP_DECORATION_TYPE, id(pair.getKey()))).orElseThrow();
            legacy.set(DataComponents.MAP_DECORATIONS, new MapDecorations(Map.of("+", new MapDecorations.Entry(type, 40, 80, 90))));
            legacy.set(DataComponents.CUSTOM_NAME, Component.literal("Saved expedition"));
            legacy.set(DataComponents.MAP_COLOR, new MapItemColor(0x123456));
            ItemStack loaded = ItemStack.CODEC.parse(ops, ItemStack.CODEC.encodeStart(ops, legacy).getOrThrow()).getOrThrow();
            require(loaded.is(BuiltInRegistries.ITEM.get(id(pair.getValue()))), "Saved map migration: " + pair);
            require(loaded.getCount() == 3 && mapId.equals(loaded.get(DataComponents.MAP_ID)), "Lost map ID/count");
            require(legacy.get(DataComponents.CUSTOM_NAME).equals(loaded.get(DataComponents.CUSTOM_NAME)), "Lost custom name");
            require(legacy.get(DataComponents.MAP_COLOR).equals(loaded.get(DataComponents.MAP_COLOR)), "Lost map color");
            RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), server.registryAccess());
            try {
                ItemStack.STREAM_CODEC.encode(buffer, legacy);
                ItemStack network = ItemStack.STREAM_CODEC.decode(buffer);
                require(network.is(loaded.getItem()) && mapId.equals(network.get(DataComponents.MAP_ID)), "Network map migration");
            } finally { buffer.release(); }
            var input = CraftingInput.of(2, 1, List.of(loaded, new ItemStack(Items.MAP)));
            var clone = new MapCloningRecipe(CraftingBookCategory.MISC);
            require(clone.matches(input, level), "Map cannot be cloned: " + pair);
            ItemStack copy = clone.assemble(input, server.registryAccess());
            require(copy.is(loaded.getItem()) && copy.getCount() == 2 && mapId.equals(copy.get(DataComponents.MAP_ID)), "Clone lost item/data");
            var grid = new java.util.ArrayList<ItemStack>();
            for (int i = 0; i < 9; i++) grid.add(i == 4 ? loaded : new ItemStack(Items.PAPER));
            require(!new MapExtendingRecipe(CraftingBookCategory.MISC).matches(CraftingInput.of(3, 3, grid), level), "Explorer map can be zoomed");
        }
        ItemStack ordinary = MapItem.create(level, 0, 0, (byte)0, true, false);
        require(ItemStack.CODEC.parse(ops, ItemStack.CODEC.encodeStart(ops, ordinary).getOrThrow()).getOrThrow().is(Items.FILLED_MAP), "Ordinary map converted");
    }

    private static void checkIce(MinecraftServer server) {
        ServerLevel level = server.overworld();
        BlockPos pos = new BlockPos(32, 80, 32);
        level.getChunkAt(pos);
        Block brittle = BuiltInRegistries.BLOCK.getOptional(id("brittle_ice")).orElseThrow();
        IntegerProperty age = (IntegerProperty)brittle.getStateDefinition().getProperty("age");
        level.setBlockAndUpdate(pos, brittle.defaultBlockState());
        var stray = EntityType.STRAY.create(level);
        brittle.stepOn(level, pos, level.getBlockState(pos), stray);
        require(level.getBlockState(pos).getValue(age) == 0, "Stray cracked brittle ice");
        boolean griefing = level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
        level.getGameRules().getRule(GameRules.RULE_MOBGRIEFING).set(false, server);
        brittle.stepOn(level, pos, level.getBlockState(pos), EntityType.ZOMBIE.create(level));
        require(level.getBlockState(pos).getValue(age) == 0, "Brittle ice ignored mobGriefing");
        level.getGameRules().getRule(GameRules.RULE_MOBGRIEFING).set(true, server);
        brittle.stepOn(level, pos, level.getBlockState(pos), EntityType.ZOMBIE.create(level));
        require(level.getBlockState(pos).getValue(age) == 1, "Brittle ice did not crack immediately");
        level.getGameRules().getRule(GameRules.RULE_MOBGRIEFING).set(griefing, server);
        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());

        BlockPos center = new BlockPos(40, 80, 40);
        for (int dx = -4; dx <= 4; dx++) for (int dz = -4; dz <= 4; dz++) {
            level.setBlockAndUpdate(center.offset(dx, -1, dz), dx == 0 && dz == 0 ? Blocks.STONE.defaultBlockState() : Blocks.WATER.defaultBlockState());
            level.setBlockAndUpdate(center.offset(dx, 0, dz), Blocks.AIR.defaultBlockState());
        }
        SulfurCube cube = new SulfurCube(ModEntityTypes.SULFUR_CUBE.get(), level);
        cube.setPos(center.getX() + 0.5, center.getY(), center.getZ() + 0.5);
        cube.setOnGround(true);
        cube.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.BLUE_ICE));
        cube.collectEquipmentChanges();
        require(level.getBlockState(center.offset(1, -1, 0)).is(Blocks.FROSTED_ICE), "Grounded cold cube did not freeze water");
    }
}
