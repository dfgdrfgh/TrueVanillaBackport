"""Validate Ice Caves integrated directly into TrueVanillaBackport's Fabric JAR.

Also verify creative content order, merged block tags and unchanged NeoForge.
"""
import json
import re
import sys
import zipfile
from pathlib import Path

loader = sys.argv[1]
root = Path(__file__).resolve().parents[2]
releases = list((root / "dist").glob("*.jar"))
assert len(releases) == 1, releases
source = (root / "common/src/main/java/com/blackgear/vanillabackport/client/registries/ModBundledTabs.java").read_text()
block = source.split("public static final BundledTabs ICE_CAVES", 1)[1].split("public static BundledTabs register(", 1)[0]
expected = ["ice_crystal","icicle","brittle_ice","packed_ice_bricks","packed_ice_brick_stairs","packed_ice_brick_slab","packed_ice_brick_wall","chiseled_packed_ice","ice_ball","frostbite_spawn_egg"]
item_list = block.split("for (String name : List.of(", 1)[1].split("))", 1)[0]
found = re.findall(r'"(ice_crystal|icicle|brittle_ice|packed_ice_bricks|packed_ice_brick_stairs|packed_ice_brick_slab|packed_ice_brick_wall|chiseled_packed_ice|ice_ball|frostbite_spawn_egg)"', item_list)
assert found == expected, (found, expected)
assert "Items.BLUE_ICE" in block and ".icon(() ->" in block
assert "ice_door" not in block
sorting = (root / "fabric/src/main/java/com/blackgear/vanillabackport/core/fabric/IceCavesCreativeSorting.java").read_text()
for category in ("NATURAL_BLOCKS","BUILDING_BLOCKS","COMBAT","SPAWN_EGGS","FOOD_AND_DRINKS"):
    assert "CreativeModeTabs."+category in sorting
assert sorting.count("entries.addAfter(")>=7
# 1.21.1 vanilla spawn eggs are alphabetically sorted. The Frostbite egg
# belongs between Frog and Ghast, never after Stray.
assert 'entries.addAfter(Items.FROG_SPAWN_EGG, item("frostbite_spawn_egg"))' in sorting
assert 'entries.addAfter(Items.STRAY_SPAWN_EGG, item("frostbite_spawn_egg"))' not in sorting
assert sorting.count('item("frostbite_spawn_egg")') == 1

assert block.count("Items.TIPPED_ARROW") == 2, "Expected normal and extended arrow, each once"
assert block.count("Items.POTION, potion") == 2
assert block.count("Items.SPLASH_POTION, potion") == 2
assert block.count("Items.LINGERING_POTION, potion") == 2

with zipfile.ZipFile(releases[0]) as jar:
    names = set(jar.namelist())
    assert "com/blackgear/vanillabackport/client/registries/ModBundledTabs.class" in names
    if loader == "fabric":
        metadata = json.loads(jar.read("fabric.mod.json"))
        assert metadata["id"] == "vanillabackport"
        assert "ice_caves" not in metadata.get("provides", [])
        assert "org.zerw.net.icecaves.IceCavesFabric" in metadata["entrypoints"]["main"]
        assert "org.zerw.net.icecaves.client.IceCavesFabricClient" in metadata["entrypoints"]["client"]
        assert metadata["mixins"].count("ice_caves.mixins.json") == 1
        assert not any("ice-caves" in e["file"] for e in metadata.get("jars", []))
        assert not any(n.startswith("META-INF/jars/ice-caves-backport") for n in names)
        # The original named refmap is legitimately shipped at the JAR root.
        assert "ice-caves-backport-fabric-refmap.json" in names
        assert all(n in names for n in (
            "org/zerw/net/icecaves/IceCavesFabric.class",
            "org/zerw/net/icecaves/entity/FrostbiteZombie.class",
            "ice_caves.mixins.json",
            "ice_caves-fabric-1.21.1-refmap.json",
            "data/minecraft/recipe/packed_ice_bricks.json",
            "assets/minecraft/lang/en_us.json"
        ))
        assert "assets/minecraft/models/item/tipped_arrow.json" in names
        assert "com/blackgear/vanillabackport/core/fabric/IceCavesCreativeSorting.class" in names
        assert all("ice_door" not in path for path in names)
        # No override of packed ice's vanilla Silk Touch-only drop table.
        assert "data/minecraft/loot_table/blocks/packed_ice.json" not in names
        crystal = "data/minecraft/loot_table/blocks/ice_crystal.json"
        assert crystal in names, "Ice Crystal must drop itself when mined"
        crystal_loot = json.loads(jar.read(crystal))
        assert crystal_loot["pools"][0]["entries"][0]["name"] == "minecraft:ice_crystal"
        frostbite_loot = json.loads(jar.read("data/minecraft/loot_table/entities/frostbite.json"))
        ice_ball_pool = frostbite_loot["pools"][1]
        assert not ice_ball_pool.get("conditions"), "Ice Balls should not require a fixed 25% chance"
        assert ice_ball_pool["entries"][0]["name"] == "minecraft:ice_ball"
        loot_functions = ice_ball_pool["entries"][0]["functions"]
        assert loot_functions[0]["function"] == "minecraft:set_count"
        assert loot_functions[0]["count"]["min"] == 0
        assert loot_functions[0]["count"]["max"] == 3
        assert loot_functions[1]["function"] == "minecraft:enchanted_count_increase"
        assert loot_functions[1]["enchantment"] == "minecraft:looting"
        ice_source = root / "vendor/ice-caves/src/main/java/org/zerw/net/icecaves"
        blocks_source = (ice_source / "registry/ModBlocks.java").read_text()
        friction_method = blocks_source.split("private static class_2251 iceBrickProperties()", 1)[1].split("public static class_2248 createPackedIceBricks()", 1)[0]
        assert ".method_9628(0.98F)" not in friction_method, "Packed ice bricks must not be slippery"
        startup_source = (ice_source / "IceCavesFabric.java").read_text()
        assert "removeSpawnsOfEntityType" not in startup_source, (
            "Ice Caves must not erase vanilla hostile entries across entire cold biomes"
        )
        spawn_source = (ice_source / "worldgen/IceCaveSpawnControl.java").read_text()
        assert "return mob instanceof FrostbiteZombie && isBelowSurface(level, pos) && !isIceCaveInterior(level, pos);" in spawn_source
        assert "return isFrostbiteType(type) && isBelowSurface(level, pos) && !isIceCaveInterior(level, pos);" in spawn_source
        playlist_source = (ice_source / "block/JukeboxChestPlaylist.java").read_text()
        assert "container != null && container.method_5439() == 27 ? container : null;" in playlist_source, (
            "Jukebox playlists must not consume music discs from double chests"
        )
        freezing_source = (ice_source / "effect/FreezingMobEffect.java").read_text()
        assert "current + (mob.field_27857 ? 1 : 3)" in freezing_source
        iceball_source = (ice_source / "entity/IceBall.java").read_text()
        assert "ModEffects.applyFreezing(living" not in iceball_source
        icicle_source = (ice_source / "block/IcicleBlock.java").read_text()
        assert "entity.method_6965(1.0F * size, 40)" in icicle_source
        assert "random.method_43057() < 0.05F" not in icicle_source
        assert "applyFreezingNearby(level, entity);" not in icicle_source
        assert "Math.floorMod(start, size)" in playlist_source, (
            "Negative playlist positions must wrap safely"
        )
        brittle_source = (ice_source / "block/BrittleIceBlock.java").read_text()
        assert "entity.method_23312().equals(pos.method_10084())" in brittle_source, (
            "Brittle Ice must detect a creature standing in the block above it"
        )
        icicle_source = (ice_source / "block/IcicleBlock.java").read_text()
        assert "private static final int MELT_LIGHT_LEVEL = 5;" in icicle_source
        assert "pos) >= MELT_LIGHT_LEVEL;" in icicle_source
        lang = json.loads(jar.read("assets/minecraft/lang/en_us.json"))
        assert lang["block.minecraft.ice_crystal"] == "Ice Crystal"
        assert "block.minecraft.ice_door" not in lang
        assert json.loads(jar.read("data/minecraft/recipe/packed_ice_bricks.json"))["result"]["id"] == "minecraft:packed_ice_bricks"
        for path, item in (
            ("data/minecraft/tags/block/walls.json", "minecraft:packed_ice_brick_wall"),
            ("data/minecraft/tags/block/mineable/pickaxe.json", "minecraft:packed_ice_bricks")
        ):
            values = json.loads(jar.read(path))["values"]
            assert item in values and "minecraft:cinnabar_wall" in values if item.endswith("wall") else item in values and "minecraft:cinnabar" in values, path
        mixins = json.loads(jar.read("ice_caves.mixins.json"))
        for name in mixins["mixins"] + mixins["client"]:
            assert f'org/zerw/net/icecaves/mixin/{name}.class' in names
        assert not any(n.startswith("META-INF/jars/ice-caves") for n in names)
    else:
        assert not any(n.startswith("org/zerw/net/icecaves/") for n in names)
        assert "ice_caves.mixins.json" not in names
print(f"PASS: {loader} Ice Caves packaging and creative sorting")
