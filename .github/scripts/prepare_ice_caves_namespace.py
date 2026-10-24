"""Normalize the compiled Ice Caves source for the built-in Minecraft namespace.

Runs only on the pinned CI checkout, before building the Fabric 1.21.1 module.
Original upstream source remains unchanged for standalone users.
"""
from pathlib import Path
import re

ROOT=Path(__file__).resolve().parents[2]
SOURCE=ROOT / "vendor/ice-caves/src/main/java/org/zerw/net/icecaves"
MAIN=SOURCE / "IceCavesFabric.java"
CLIENT=SOURCE / "client/IceCavesFabricClient.java"
TABS=SOURCE / "registry/ModCreativeTabs.java"

for path in sorted(SOURCE.rglob("*.java")):
    if path.name.endswith(("_forge.java", "_neoforge.java")) or "forgelike" in path.parts:
        continue
    content=path.read_text()
    changed=content.replace('method_60655("ice_caves",', 'method_60655("minecraft",')
    changed=changed.replace('"ice_caves:', '"minecraft:')
    if changed != content:
        path.write_text(changed)

java=MAIN.read_text()
# Stop registering the non-vanilla Ice Door. It is not a 26.4 Ice Caves feature.
door_lines=[
    '   public static class_2248 ICE_DOOR;\n',
    '   public static class_1747 ICE_DOOR_ITEM;\n',
    '      ICE_DOOR = (class_2248)class_2378.method_10230(class_7923.field_41175, ModBlocks.ICE_DOOR_ID, ModBlocks.createIceDoor());\n',
    '      ICE_DOOR_ITEM = (class_1747)class_2378.method_10230(class_7923.field_41178, ModItems.ICE_DOOR_ID, ModItems.createIceDoorItem(ICE_DOOR));\n',
    '      class_2378.method_10230(class_7923.field_41172, ModSounds.ICE_DOOR_OPEN_ID, ModSounds.ICE_DOOR_OPEN);\n',
    '      class_2378.method_10230(class_7923.field_41172, ModSounds.ICE_DOOR_CLOSE_ID, ModSounds.ICE_DOOR_CLOSE);\n',
    '                     ICE_DOOR_ITEM,\n',
]
for line in door_lines:
    if java.count(line)!=1:
        raise RuntimeError(f"Ice Door source changed: {line.strip()} count={java.count(line)}")
    java=java.replace(line,"")
# This content is integrated into TVB; normal vanilla categories are inserted
# by TVB with anchored addAfter rather than appended at the ends by the old mod.
hit=java.find("ItemGroupEvents.modifyEntriesEvent(")
start=java.rfind("\n", 0, hit) + 1 if hit >= 0 else -1
end=java.find("BiomeModifications.addFeature(",hit)
end=java.rfind("\n", 0, end) + 1 if end >= 0 else -1
if start < 0 or end < 0 or java[start:end].count("ItemGroupEvents.modifyEntriesEvent")!=4:
    raise RuntimeError("Expected exactly four old unsorted creative events")
java=java[:start]+java[end:]
MAIN.write_text(java)

client=CLIENT.read_text()
line='      BlockRenderLayerMap.INSTANCE.putBlock(IceCavesFabric.ICE_DOOR, class_1921.method_23581());\n'
if client.count(line)!=1:
    raise RuntimeError("Ice Door render hook changed")
CLIENT.write_text(client.replace(line,""))

tabs=TABS.read_text()
for line in ('      class_1935 iceDoor,\n','         new class_1799(iceDoor),\n'):
    if tabs.count(line)!=1:
        raise RuntimeError("Standalone creative method changed")
    tabs=tabs.replace(line,"")
TABS.write_text(tabs)
# Snapshot 5 parity corrections, applied only to the pinned Fabric build.
# These are deliberately source-checked, small changes; no biome source
# replacement or global terrain noise hooks are introduced.
def replace_exact(path: Path, previous: str, replacement: str) -> None:
    original = path.read_text()
    if original.count(previous) != 1:
        raise RuntimeError(
            f"Pinned Ice Caves source changed: {path.relative_to(ROOT)} "
            f"has {original.count(previous)} copies of the expected snippet"
        )
    path.write_text(original.replace(previous, replacement))


# Do not delete overworld hostile spawn entries from every snowy biome.
# The old module's REMOVALS phase strips them across all cold surface biomes,
# even at sites without generated ice caves. This broke ordinary world spawning
# regardless of the biome-local entity rule corrections below.
remove_vanilla_hostiles = (
    '      BiomeModifications.create(class_2960.method_60655("minecraft", "clear_vanilla_hostiles"))\n'
    '         .add(ModificationPhase.REMOVALS, BiomeSelectors.tag(IceCaveSpawnControl.HAS_ICE_CAVES), (selection, context) -> {\n'
    '            for (class_1299<?> type : ICE_CAVE_BLOCKED_HOSTILES) {\n'
    '               context.getSpawnSettings().removeSpawnsOfEntityType(type);\n'
    '            }\n'
    '         });\n'
)
replace_exact(MAIN, remove_vanilla_hostiles, '')
if 'removeSpawnsOfEntityType' in MAIN.read_text():
    raise RuntimeError("Global spawn-list removal survived the ice cave fix")

blocks = SOURCE / "registry/ModBlocks.java"
replace_exact(
    blocks,
    'return class_2251.method_9637().method_31710(class_3620.field_16016).method_9628(0.98F).method_9632(0.5F).method_9626(ModSounds.ICE);',
    # Packed Ice Bricks have normal block friction, unlike ordinary packed ice.
    'return class_2251.method_9637().method_31710(class_3620.field_16016).method_9632(0.5F).method_9626(ModSounds.ICE);',
)

spawns = SOURCE / "worldgen/IceCaveSpawnControl.java"
replace_exact(
    spawns,
    'return !isIceCaveInterior(level, pos) ? mob instanceof FrostbiteZombie : isMonster(mob) && !isAllowedCaveHostile(mob);',
    # Only prevent Frostbites spawning outside decorated ice-cave interiors.
    # Spiders, creepers, skeletons, etc. retain their normal cave rules.
    'return mob instanceof FrostbiteZombie && !isIceCaveInterior(level, pos);',
)
replace_exact(
    spawns,
    'return isIceCaveInterior(level, pos) ? !isAllowedCaveHostileType(type) : isFrostbiteType(type);',
    'return isFrostbiteType(type) && !isIceCaveInterior(level, pos);',
)

icicles = SOURCE / "block/IcicleBlock.java"
replace_exact(
    icicles,
    'private static final int MELT_LIGHT_LEVEL = 8;',
    'private static final int MELT_LIGHT_LEVEL = 5;',
)
replace_exact(
    icicles,
    'return level.method_8314(class_1944.field_9282, pos) >= 8;',
    'return level.method_8314(class_1944.field_9282, pos) >= MELT_LIGHT_LEVEL;',
)

# 1.21.1 thaws two ticks every game tick outside powder snow. Snapshot
# Freezing blocks that thaw path and adds one tick; we compensate locally
# without adding a global Entity/baseTick mixin that could affect other mods.
# In powder snow vanilla 1.21.1 already increments freezing, so add only
# the extra one tick that the new effect contributes.
freezing_effect = SOURCE / "effect/FreezingMobEffect.java"
replace_exact(
    freezing_effect,
    'mob.method_32317(Math.min(threshold + 3 + Math.max(0, amplification), current + 2));',
    'mob.method_32317(Math.min(threshold, current + (mob.field_27857 ? 1 : 3)));',
)

# In Snapshot 5 the Ice Ball is a kinetic projectile, not a source of
# the Freezing potion effect. The pinned module added an extra 20 tick effect.
ice_ball = SOURCE / "entity/IceBall.java"
replace_exact(
    ice_ball,
    '            ModEffects.applyFreezing(living, 20, this.method_24921());\n',
    '',
)
# Play the dedicated Ice Ball impact sound for block hits as well as entities.
replace_exact(
    ice_ball,
    '      if (!this.method_37908().method_8608()) {\n         this.method_37908().method_8421(this, (byte)3);',
    '      if (!this.method_37908().method_8608()) {\n'
    '         if (!(hitResult instanceof class_3966)) {\n'
    '            class_3414 impact = ModSounds.iceBallHit();\n'
    '            if (impact != null) {\n'
    '               this.method_37908().method_43128(null, this.method_23317(), this.method_23318(), this.method_23321(), impact, class_3419.field_15254, 1.0F, 1.0F);\n'
    '            }\n'
    '         }\n'
    '         this.method_37908().method_8421(this, (byte)3);',
)
# A creature standing *on* Brittle Ice occupies the block above it,
# not the brittle ice block itself. The old floor-position equality meant
# the scheduled crack tick never saw the creature's weight.
brittle_ice = SOURCE / "block/BrittleIceBlock.java"
replace_exact(
    brittle_ice,
    'if (isWeight(entity) && entity.method_23312().equals(pos)) {',
    'if (isWeight(entity) && entity.method_23312().equals(pos.method_10084())) {',
)

# Java 26.4 playlists accept a *single* chest (including trapped/copper).
# ChestBlock.getContainer returns 54 slots for a connected double chest;
# never treat those as a playlist, including after joining two single chests.
playlist = SOURCE / "block/JukeboxChestPlaylist.java"
replace_exact(
    playlist,
    'return state.method_26204() instanceof class_2281 chest ? class_2281.method_17458(chest, state, level, above, true) : null;',
    'if (!(state.method_26204() instanceof class_2281 chest)) return null;\n'
    '      class_1263 container = class_2281.method_17458(chest, state, level, above, true);\n'
    '      return container != null && container.method_5439() == 27 ? container : null;',
)

# The bundled version used to replace vanilla packed ice's drop table,
# adding a non-vanilla 25% Ice Ball drop. Keep vanilla's own loot untouched.
import json
packed_ice_loot = ROOT / "vendor/ice-caves/src/main/resources/data/minecraft/loot_table/blocks/packed_ice.json"
if not packed_ice_loot.is_file():
    raise RuntimeError("Expected pinned packed ice loot override was not found")
loot = json.loads(packed_ice_loot.read_text())
if len(loot.get("pools", [])) != 2 or "ice_caves:ice_ball" not in packed_ice_loot.read_text():
    raise RuntimeError("Pinned packed ice loot override changed; refusing to remove it blindly")
packed_ice_loot.unlink()

# Snapshot 5 crystals self-drop; pinned source did not ship any
# blocks/ice_crystal loot table (so survival mining yields nothing).
crystal_loot = ROOT / "vendor/ice-caves/src/main/resources/data/ice_caves/loot_table/blocks/ice_crystal.json"
if crystal_loot.exists():
    raise RuntimeError("Pinned Ice Crystal loot table unexpectedly exists; audit it first")
crystal_loot.write_text(json.dumps({
    "type": "minecraft:block",
    "pools": [{
        "rolls": 1,
        "entries": [{"type": "minecraft:item", "name": "minecraft:ice_crystal"}],
        "conditions": [{"condition": "minecraft:survives_explosion"}]
    }]
}, indent=2) + "\n")

# In vanilla Java 26.4 a Frostbite drops 0-3 Ice Balls, with Looting
# increasing the count. The pinned backport incorrectly drops 1 at 25%.
frostbite_loot = ROOT / "vendor/ice-caves/src/main/resources/data/ice_caves/loot_table/entities/frostbite.json"
frostbite_table = json.loads(frostbite_loot.read_text())
if len(frostbite_table.get("pools", [])) != 2:
    raise RuntimeError("Unexpected Frostbite loot table structure")
ice_ball_pool = frostbite_table["pools"][1]
if (ice_ball_pool.get("conditions") != [{"condition": "minecraft:random_chance", "chance": 0.25}]
        or len(ice_ball_pool.get("entries", [])) != 1
        or ice_ball_pool["entries"][0].get("name") != "ice_caves:ice_ball"):
    raise RuntimeError("Unexpected pinned Frostbite Ice Ball loot source")
ice_ball_pool.pop("conditions")
ice_ball_pool["entries"][0]["functions"] = [
    {
        "function": "minecraft:set_count",
        "count": {"type": "minecraft:uniform", "min": 0.0, "max": 3.0},
    },
    {
        "function": "minecraft:enchanted_count_increase",
        "enchantment": "minecraft:looting",
        "count": {"type": "minecraft:uniform", "min": 0.0, "max": 1.0},
    },
]
frostbite_loot.write_text(json.dumps(frostbite_table, indent=2) + "\n")

assert "ICE_DOOR_ITEM" not in MAIN.read_text()
assert "ICE_DOOR" not in CLIENT.read_text()
assert "iceDoor" not in TABS.read_text()
print("PASS: minecraft: Ice Caves registry IDs; Ice Door and old creative insertion removed")
