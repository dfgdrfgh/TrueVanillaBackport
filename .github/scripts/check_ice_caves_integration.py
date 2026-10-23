"""Check installable TVB releases for Ice Caves jar-in-jar and correct section sorting."""
import json
import re
import sys
import zipfile
from pathlib import Path
from io import BytesIO

loader = sys.argv[1]
root = Path(__file__).resolve().parents[2]
release = list((root / "dist").glob("*.jar"))
assert len(release) == 1, release
bundled = (root / "common/src/main/java/com/blackgear/vanillabackport/client/registries/ModBundledTabs.java").read_text()
block = bundled.split("public static final BundledTabs ICE_CAVES", 1)[1].split("public static BundledTabs register(", 1)[0]
items = re.findall(r'"(ice_crystal|icicle|brittle_ice|packed_ice_bricks|packed_ice_brick_stairs|packed_ice_brick_slab|packed_ice_brick_wall|chiseled_packed_ice|ice_door|ice_ball|frostbite_spawn_egg)"', block)
expected = ["ice_crystal", "icicle", "brittle_ice", "packed_ice_bricks", "packed_ice_brick_stairs", "packed_ice_brick_slab", "packed_ice_brick_wall", "chiseled_packed_ice", "ice_door", "ice_ball", "frostbite_spawn_egg"]
assert items == expected, (items, expected)
assert 'List.of("freezing", "long_freezing")' in block
assert block.count('Items.TIPPED_ARROW') == 1
with zipfile.ZipFile(release[0]) as jar:
    names = set(jar.namelist())
    assert "com/blackgear/vanillabackport/client/registries/ModBundledTabs.class" in names
    if loader == "fabric":
        meta = json.loads(jar.read("fabric.mod.json"))
        paths = [x["file"] for x in meta.get("jars", [])]
        ice = [path for path in paths if path.startswith("META-INF/jars/ice-caves-backport-fabric-")]
        assert len(ice) == 1, paths
        with zipfile.ZipFile(BytesIO(jar.read(ice[0]))) as child:
            child_meta = json.loads(child.read("fabric.mod.json"))
            assert child_meta["id"] == "ice_caves"
            for name in ("org/zerw/net/icecaves/IceCavesFabric.class",
                         "org/zerw/net/icecaves/entity/FrostbiteZombie.class",
                         "data/ice_caves/recipe/packed_ice_bricks.json"):
                assert name in child.namelist(), name
            assert child_meta.get("jars", []) == []
    else:
        assert not any("ice-caves" in name for name in names)
print("PASS: Ice Caves release packaging and section ordering", loader)
