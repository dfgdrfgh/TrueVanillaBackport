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
expected = ["ice_crystal","icicle","brittle_ice","packed_ice_bricks","packed_ice_brick_stairs","packed_ice_brick_slab","packed_ice_brick_wall","chiseled_packed_ice","ice_door","ice_ball","frostbite_spawn_egg"]
item_list = block.split("for (String name : List.of(", 1)[1].split("))", 1)[0]
found = re.findall(r'"(ice_crystal|icicle|brittle_ice|packed_ice_bricks|packed_ice_brick_stairs|packed_ice_brick_slab|packed_ice_brick_wall|chiseled_packed_ice|ice_door|ice_ball|frostbite_spawn_egg)"', item_list)
assert found == expected, (found, expected)
assert "Items.BLUE_ICE" in block and ".icon(() ->" in block
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
        assert "ice_caves" in metadata.get("provides", [])
        assert "org.zerw.net.icecaves.IceCavesFabric" in metadata["entrypoints"]["main"]
        assert "org.zerw.net.icecaves.client.IceCavesFabricClient" in metadata["entrypoints"]["client"]
        assert metadata["mixins"].count("ice_caves.mixins.json") == 1
        assert not any("ice-caves" in e["file"] for e in metadata.get("jars", []))
        assert not any("ice-caves-backport" in n for n in names)
        assert all(n in names for n in (
            "org/zerw/net/icecaves/IceCavesFabric.class",
            "org/zerw/net/icecaves/entity/FrostbiteZombie.class",
            "ice_caves.mixins.json",
            "ice_caves-fabric-1.21.1-refmap.json",
            "data/ice_caves/recipe/packed_ice_bricks.json",
            "assets/ice_caves/lang/en_us.json"
        ))
        assert "assets/minecraft/models/item/tipped_arrow.json" in names
        for path, item in (
            ("data/minecraft/tags/block/walls.json", "ice_caves:packed_ice_brick_wall"),
            ("data/minecraft/tags/block/mineable/pickaxe.json", "ice_caves:packed_ice_bricks")
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
