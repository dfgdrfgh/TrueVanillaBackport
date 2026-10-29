"""Verify the 26.3 explorer resources and run migration against real 1.21.1 components.

Pass a file containing the common compile classpath to also run the Java harness.
Only the mod registration boundary is replaced; production conversion and vanilla
ItemStack, map decorations, text components and registries execute unchanged.
"""
import hashlib
import json
import os
import re
import subprocess
import sys
import tempfile
from pathlib import Path

root = Path(__file__).resolve().parents[2]
source = root / "common/src/main/java/com/blackgear/vanillabackport"
# SHA-256 of the unmodified textures in Mojang's official 26.3 client JAR.
textures = {'ocean_monument_map': '946e97c6f590eea1108bafb485eddf5165de9d04c739710de784f92cf84c8d9b', 'woodland_mansion_map': 'c8df872f0397ce803a1efec05e509aed98ee2bced2bd3e51436afe6d8fe2aca0', 'buried_trial_chambers_map': '47c5866edff73625a8ae1a47145a4a4ed678bf8d55d085d410130394ca802cbd', 'jungle_pyramid_map': '4b798b8d5ce96e0e013947dda1cff874e2930486dbeff8196e7a2b5fd7c3f064', 'swamp_hut_map': '22e9677eff901911bd67444610eeaf5b23137c16a0befe2925989a4d6fe09708', 'desert_village_map': 'a2d9211194bc6c803aa842c385f8b48fd026c461a47358eb9f2530440b4e9324', 'plains_village_map': 'dd210e0c15a635feb48281e018da402558b79dd4e405815739b1c67fb7f03c45', 'savanna_village_map': '0da470ef4dec00249874738b873ff5c5b613f5e1bd33e9c17070ba5baeb7b58b', 'snowy_village_map': '00262a964489a06db90eab455717a30c71bc2b7fb1585dc7f682c19fe10d8edb', 'taiga_village_map': 'b02c61345fdc72482c0e3a273e28827d1feeb9fcff34a2ccc0443978c3c174e3', 'buried_treasure_map': 'd732fc827a285cb84065332b32000cf53a5dd0d279f74947c4bf243d6908f2eb', 'buried_ancient_city_map': 'b3b0c921ce5f9e956bfe3d9c81d525d182b1b7d749402ed832fbed10b5cc3a61', 'buried_mineshaft_map': 'f202cf37c8237804bd04a3abe965302c61a40fe60cbfc2ba7add83ba92e6b833', 'desert_pyramid_map': '67a16df4ee39568647adf79ab3247b8d2f1cc7e48e0fdd6d8301de929d642bba', 'abandoned_camp_map': 'f1fdebb2a83fa2868db187bb1782dbd0349b51e6a1e498955e30be88036df1ed', 'warm_ocean_ruins_map': 'dbd4e4dcccd5d4949a70121ae42167c1ee0747b79f3f5b1430ad82cedfefbce2'}
registrations = (source / "common/registries/items/ModItems.java").read_text()
for name, digest in textures.items():
    assert re.search(r'REGISTRIES.register\("' + name + r'",\s*MapItem::new, new Properties\(\).component\(DataComponents.MAP_DECORATIONS, MapDecorations.EMPTY\)\)', registrations), name
    assets = root / "common/src/main/resources/assets/minecraft"
    assert hashlib.sha256((assets / f"textures/item/{name}.png").read_bytes()).hexdigest() == digest, name
    assert json.loads((assets / f"models/item/{name}.json").read_text()) == {
        "parent": "minecraft:item/generated", "textures": {"layer0": f"minecraft:item/{name}"}}, name
    for creative_file in (source / "client").rglob("*.java"):
        if "registries" in str(creative_file) or "integrations" in str(creative_file):
            assert "ModItems." + name.upper() not in creative_file.read_text(), creative_file
    language = json.loads((root / "common/src/main/generated/assets/vanillabackport/lang/en_us.json").read_text())
    assert language.get("item.minecraft." + name), name
assert json.loads((root / "common/src/main/resources/data/minecraft/tags/item/extendable_maps.json").read_text()) == {"values": ["minecraft:filled_map"]}
print("PASS: 16 official explorer textures, generated models, registrations, names, Creative exclusion and extension tag")
if len(sys.argv) == 1:
    sys.exit(0)

classpath = Path(sys.argv[1]).read_text()
java = Path(os.environ["JAVA_HOME"]) / "bin/java"
fixture = """
package com.blackgear.vanillabackport.common.registries.items;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.core.component.DataComponents;
public class ModItems {
    public record Entry(Item value) { public Item get() { return value; } }
    private static Entry register(String id) {
        return new Entry(Registry.register(BuiltInRegistries.ITEM, ResourceLocation.withDefaultNamespace(id),
            new MapItem(new Item.Properties().component(DataComponents.MAP_DECORATIONS, MapDecorations.EMPTY))));
    }
    %s
}
""" % "\n".join(f'public static final Entry {name.upper()} = register("{name}");' for name in textures)
harness = r"""
import com.blackgear.vanillabackport.common.level.items.maps.ExplorerMaps;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.level.saveddata.maps.*;
import java.util.*;

public class ExplorerMapRegression {
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    private static ResourceLocation id(String path) { return ResourceLocation.withDefaultNamespace(path); }
    private static void reopen(Registry<?> registry) throws Exception {
        var frozen = MappedRegistry.class.getDeclaredField("frozen");
        frozen.setAccessible(true); frozen.set(registry, false);
        var intrusive = MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");
        intrusive.setAccessible(true);
        if (registry == BuiltInRegistries.ITEM) intrusive.set(registry, new IdentityHashMap<>());
    }
    private static Holder<MapDecorationType> decoration(String path) {
        return BuiltInRegistries.MAP_DECORATION_TYPE.getHolder(ResourceKey.create(Registries.MAP_DECORATION_TYPE, id(path))).orElseThrow();
    }
    private static ItemStack map(String type, String marker) {
        ItemStack stack = new ItemStack(Items.FILLED_MAP, 3);
        stack.set(DataComponents.MAP_ID, new MapId(77));
        stack.set(DataComponents.MAP_DECORATIONS, new MapDecorations(Map.of(marker,
            new MapDecorations.Entry(decoration(type), 123, -456, 180))));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("My expedition"));
        stack.set(DataComponents.LORE, new ItemLore(List.of(Component.literal("Keep this note"))));
        return stack;
    }
    public static void main(String[] args) throws Exception {
        SharedConstants.tryDetectVersion(); Bootstrap.bootStrap();
        reopen(BuiltInRegistries.ITEM);
        Class.forName("com.blackgear.vanillabackport.common.registries.items.ModItems");
        BuiltInRegistries.ITEM.freeze();
        reopen(BuiltInRegistries.MAP_DECORATION_TYPE);
        for (String path : List.of("ancient_city", "mineshaft", "desert_pyramid", "abandoned_camp", "ocean_ruin_warm")) {
            Registry.register(BuiltInRegistries.MAP_DECORATION_TYPE, id(path), new MapDecorationType(id(path), true, -1, true, false));
        }
        BuiltInRegistries.MAP_DECORATION_TYPE.freeze();
        Map<String, String> pairs = Map.ofEntries(
            Map.entry("mansion", "woodland_mansion_map"), Map.entry("monument", "ocean_monument_map"),
            Map.entry("trial_chambers", "buried_trial_chambers_map"), Map.entry("jungle_temple", "jungle_pyramid_map"),
            Map.entry("swamp_hut", "swamp_hut_map"), Map.entry("village_desert", "desert_village_map"),
            Map.entry("village_plains", "plains_village_map"), Map.entry("village_savanna", "savanna_village_map"),
            Map.entry("village_snowy", "snowy_village_map"), Map.entry("village_taiga", "taiga_village_map"),
            Map.entry("red_x", "buried_treasure_map"), Map.entry("ancient_city", "buried_ancient_city_map"),
            Map.entry("mineshaft", "buried_mineshaft_map"), Map.entry("desert_pyramid", "desert_pyramid_map"),
            Map.entry("abandoned_camp", "abandoned_camp_map"), Map.entry("ocean_ruin_warm", "warm_ocean_ruins_map"));
        for (var pair : pairs.entrySet()) {
            ItemStack original = map(pair.getKey(), "+");
            ItemStack converted = ExplorerMaps.convert(original);
            require(converted.is(BuiltInRegistries.ITEM.get(id(pair.getValue()))), "Wrong item for " + pair);
            require(converted.getItem() instanceof MapItem, "Lost map behavior");
            require(converted.getCount() == 3 && new MapId(77).equals(converted.get(DataComponents.MAP_ID)), "Lost count/map ID");
            require(original.getComponents().equals(converted.getComponents()), "Lost component data");
            require(original.is(Items.FILLED_MAP), "Mutated source stack");
            require(ExplorerMaps.convert(converted) == converted, "Migration is not idempotent");
        }
        ItemStack old = map("mansion", "+");
        old.set(DataComponents.ITEM_NAME, Component.translatable("filled_map.mansion"));
        require(!ExplorerMaps.convert(old).has(DataComponents.ITEM_NAME), "Default legacy name was not removed");
        old.set(DataComponents.ITEM_NAME, Component.translatable("filled_map.mansion").withStyle(net.minecraft.ChatFormatting.GOLD));
        require(ExplorerMaps.convert(old).has(DataComponents.ITEM_NAME), "Styled name was removed");
        old.set(DataComponents.ITEM_NAME, Component.literal("Custom item name"));
        require(ExplorerMaps.convert(old).get(DataComponents.ITEM_NAME).equals(old.get(DataComponents.ITEM_NAME)), "Custom name was removed");
        ItemStack camp = map("abandoned_camp", "+");
        camp.set(DataComponents.ITEM_NAME, Component.translatable("filled_map.bamboo_camp_map"));
        require(ExplorerMaps.convert(camp).get(DataComponents.ITEM_NAME).equals(camp.get(DataComponents.ITEM_NAME)), "Camp biome name lost");
        ItemStack ordinary = new ItemStack(Items.FILLED_MAP);
        require(ExplorerMaps.convert(ordinary) == ordinary, "Converted ordinary filled map");
        require(ExplorerMaps.convert(ItemStack.EMPTY) == ItemStack.EMPTY, "Converted empty stack");
        ItemStack marker = map("mansion", "custom_marker");
        require(ExplorerMaps.convert(marker) == marker, "Converted non-explorer marker");
        ItemStack player = map("player", "+");
        require(ExplorerMaps.convert(player) == player, "Converted unknown marker");
        System.out.println("PASS: all 16 map types, component/count/ID preservation, idempotence, safe names and marker filtering");
    }
}
"""
with tempfile.TemporaryDirectory() as directory:
    folder = Path(directory)
    registration = folder / "com/blackgear/vanillabackport/common/registries/items/ModItems.java"
    registration.parent.mkdir(parents=True)
    registration.write_text(fixture)
    test = folder / "ExplorerMapRegression.java"
    test.write_text(harness)
    subprocess.run([str(java.with_name("javac")), "-proc:none", "--release", "21", "-cp", classpath,
                    "-d", str(folder), str(registration), str(source / "common/level/items/maps/ExplorerMaps.java"), str(test)], check=True)
    subprocess.run([str(java), "-cp", str(folder) + os.pathsep + classpath, "ExplorerMapRegression"], check=True)
